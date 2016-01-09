/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.platform.myspace;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.cache.MyAsyncTask;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.InstallInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class AppTaskList {

    private LinkedList<AppDownTask> taskList = new LinkedList<AppDownTask>();

    public boolean isExistTask(String taskId) {
        synchronized (this) {
            for (int i = 0, size = taskList.size(); i < size; i++) {
                if (taskList.get(i).getTaskId().equals(taskId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addTask(AppDownTask downTask) {
        taskList.add(downTask);
    }

    public void removeTask(AppDownTask task) {
        taskList.remove(task);
    }

    public InstallInfo getTaskInfoByAppId(String appId) {
        for (AppDownTask task : taskList) {
            if (task.installInfo.getDownloadInfo().appId.equals(appId)) {
                return task.installInfo;
            }
        }
        return null;
    }

    public static class AppDownTask extends MyAsyncTask {

        public static final String TAG = "AppDownTask";
        public InstallInfo installInfo;
        private GridView gridView;
        private MyAppsAdapter adapter;
        private Timer timer;
        private int length;
        private int count;
        private boolean countable = false;
        private boolean completed = false;
        private TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                if (!completed) {
                    if (countable) {
                        int downloadPercent = (int) (((double) count / (double) length) * 100);
                        publishProgress(downloadPercent);
                    }
                }

            }
        };

        public AppDownTask(InstallInfo installInfo, GridView gridView) {
            if (installInfo == null) {
                throw new NullPointerException("new AppDownTask params can not be null...");
            }
            this.installInfo = installInfo;
            this.gridView = gridView;
            Log.d(TAG,
                    "new AppDownTask:" + installInfo.getDownloadInfo().appId + " name:"
                            + installInfo.getDownloadInfo().appName + " URL:"
                            + installInfo.getDownloadInfo().downloadUrl);
            timer = new Timer();
        }

        public String getTaskId() {
            return installInfo.getDownloadInfo().appId;
        }

        @Override
        protected Object doInBackground(Object... params) {
            HttpURLConnection conn = null;
            InputStream is = null;
            File tmpFile = null;
            String installPath = null;
            timer.schedule(timerTask, 0, 1000);
            try {
                Activity activity = (Activity) gridView.getContext();
                tmpFile = CommonUtility.createCacheFile(activity);
                BDebug.i(TAG, "download tmpFile:" + tmpFile.getAbsolutePath());
                tmpFile.createNewFile();
                conn = (HttpURLConnection) new URL(installInfo.getDownloadInfo().downloadUrl).openConnection();
                conn.setConnectTimeout(5000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();
                length = conn.getContentLength();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = conn.getInputStream();
                    if (length == -1) {
                        length = is.available();
                    }
                    countable = length == -1 ? false : true;
                    byte[] buffer = new byte[8096];
                    int actualSize = -1;
                    FileOutputStream fos = new FileOutputStream(tmpFile);
                    while (!isCancelled() && (actualSize = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, actualSize);
                        count = count + actualSize;
                    }
                    is.close();
                    fos.close();
                    if (!isCancelled()) {
                        FileInputStream fis = new FileInputStream(tmpFile);
                        installPath = CommonUtility.unzip(fis, CommonUtility.WIDGET_SAVE_PATH, null);
                        fis.close();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
                if (tmpFile != null) {
                    tmpFile.delete();
                }
                completed = true;
            }
            return installPath;
        }

        @Override
        public void handleOnPreLoad(MyAsyncTask task) {
            adapter = (MyAppsAdapter) gridView.getAdapter();
            View bindView = adapter.getBindViewByAppId(installInfo.getAppId());
            if (bindView != null) {
                ViewCache viewCache = (ViewCache) bindView.getTag();
                viewCache.downloadProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void handleOnUpdateProgress(MyAsyncTask task, int percent) {
            View bindView = adapter.getBindViewByAppId(installInfo.getAppId());
            if (bindView != null) {
                ViewCache viewCache = (ViewCache) bindView.getTag();
                viewCache.downloadProgressBar.setVisibility(View.VISIBLE);
                viewCache.downloadProgressBar.setProgress(percent);
                if (percent >= 100) {
                    viewCache.nameTextView.setText(EUExUtil.getString("install"));
                }
                gridView.invalidate();
            }
            BDebug.i(TAG, installInfo.getDownloadInfo().appName + "-->update:" + percent);
        }

        @Override
        public void handleOnCanceled(MyAsyncTask task) {
            timerTask.cancel();
            timer.cancel();
            timerTask = null;
            timer = null;
        }

        @Override
        public void handleOnCompleted(MyAsyncTask task, Object result) {
            timerTask.cancel();
            timer.cancel();
            timerTask = null;
            timer = null;
            View bindView = adapter.getBindViewByAppId(installInfo.getAppId());
            ViewCache viewCache = null;
            if (bindView != null) {
                viewCache = (ViewCache) bindView.getTag();
                viewCache.downloadProgressBar.setVisibility(View.GONE);
                viewCache.downloadProgressBar.setProgress(0);
                viewCache.nameTextView.setText(installInfo.getDownloadInfo().appName);
            }
            String installPath = (String) result;
            if (installPath != null) {
                installInfo.isDownload = true;
                installInfo.installPath = installPath;
                if (viewCache != null) {
                    viewCache.iconImageView.setImageDrawable(null);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof AppDownTask) {
                AppDownTask task = (AppDownTask) o;
                if (task.getTaskId().equals(this.getTaskId())) {
                    return true;
                }
            }
            return super.equals(o);
        }
    }
}
