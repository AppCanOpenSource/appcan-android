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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.*;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.base.cache.BytesArrayFactory;
import org.zywx.wbpalmstar.base.cache.BytesArrayFactory.BytesArray;
import org.zywx.wbpalmstar.base.cache.ImageLoadTask;
import org.zywx.wbpalmstar.base.cache.ImageLoadTask.ImageLoadTaskCallback;
import org.zywx.wbpalmstar.base.cache.ImageLoaderManager;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.InstallInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MyAppsAdapter extends BaseAdapter {

    public static final String TAG = "MyAppsAdapter";
    private ArrayList<InstallInfo> appList;
    private LayoutInflater inflater;
    private ImageLoaderManager loaderManager;
    private GridView gridView;
    private Drawable grayCover;
    private Drawable defaultItemBg;
    private ArrayList<View> viewList = new ArrayList<View>();

    private ResoureFinder finder;

    public MyAppsAdapter(ArrayList<InstallInfo> apps, Context context, GridView gv) {
        finder = ResoureFinder.getInstance(context);
        this.appList = apps;
        this.inflater = LayoutInflater.from(context);
        this.gridView = gv;
        grayCover = finder.getDrawable("platform_myspace_grid_item_cover");
        defaultItemBg = finder.getDrawable("platform_myspace_grid_item_default_bg");
        loaderManager = ImageLoaderManager.initImageLoaderManager(context);
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public InstallInfo getItem(int position) {
        if (position < 0 || position > getCount() - 1) {
            return null;
        }
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int addItem(InstallInfo installInfo) {
        appList.add(installInfo);
        notifyDataSetChanged();
        return appList.indexOf(installInfo);
    }

    public void reload(ArrayList<InstallInfo> infos) {
        this.appList.clear();
        this.viewList.clear();
        this.appList = infos;
        notifyDataSetChanged();
    }

    public boolean checkDownloaded(String appId) {
        for (int i = 0, size = getCount(); i < size; i++) {
            final InstallInfo installInfo = this.appList.get(i);
            if (installInfo.isDownload && installInfo.getAppId().equals(appId)) {
                return true;
            }
        }
        return false;
    }

    public boolean addItemAtPostion(InstallInfo installInfo, int postion) {
        if (postion < 0 || postion > getCount() - 1) {
            return false;
        }
        appList.add(postion, installInfo);
        notifyDataSetChanged();
        return true;
    }

    public InstallInfo getInstallInfoByAppId(String appId) {
        if (appId == null) {
            return null;
        }
        for (InstallInfo installInfo : appList) {
            if (installInfo.getAppId().equals(appId)) {
                return installInfo;
            }
        }
        return null;
    }

    public void removeItemByAppId(String appId) {
        if (appId == null || appId.length() == 0) {
            return;
        }
        for (int i = 0, size = appList.size(); i < size; i++) {
            InstallInfo current = appList.get(i);
            if (current.getAppId().equals(appId)) {
                appList.remove(current);
                if (i < viewList.size()) {
                    viewList.remove(i);
                }
                notifyDataSetChanged();
                break;
            }
        }
    }

    public View getBindViewAtPostion(int postion) {
        int max = Math.min(getCount(), viewList.size());
        if (postion >= 0 && postion < max) {
            return viewList.get(postion);
        }
        return null;
    }

    public void removeItemAtPostion(int postion) {
        if (postion < 0 || postion > getCount() - 1) {
            return;
        }
        appList.remove(postion);
        notifyDataSetChanged();
    }

    public int indexOfBindView(View view) {
        return appList.indexOf(view);
    }

    public View getBindViewByAppId(String appId) {
        if (appId == null) {
            return null;
        }
        for (int i = 0, size = appList.size(); i < size; i++) {
            final InstallInfo installInfo = appList.get(i);
            if (appId.equals(installInfo.getAppId())) {
                return getBindViewAtPostion(i);
            }
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewCache viewCache = null;
        View itemView = null;
        if (position >= viewList.size()) {
            itemView = inflater.inflate(finder.getLayoutId("platform_myspace_myapp_item"), null);
            viewCache = new ViewCache();
            viewCache.iconImageView = (ImageView) itemView.findViewById(finder.getId("platform_myspace_app_icon"));
            viewCache.downloadProgressBar = (ProgressBar) itemView.findViewById(finder
                    .getId("platform_myspace_app_download_indicator"));
            viewCache.nameTextView = (TextView) itemView.findViewById(finder.getId("platform_myspace_app_name"));
            itemView.setTag(viewCache);
            viewList.add(itemView);
        }
        itemView = viewList.get(position);
        viewCache = (ViewCache) itemView.getTag();
        final InstallInfo installInfo = appList.get(position);
        // if (installInfo.isDownload) {
        // viewCache.downloadProgressBar.setVisibility(View.GONE);
        // } else {
        // viewCache.downloadProgressBar.setVisibility(View.VISIBLE);
        // }
        viewCache.nameTextView.setText(installInfo.getDownloadInfo().appName);
        String imgUrl = installInfo.getDownloadInfo().iconLoc;
        if (installInfo.isDownload) {
            imgUrl = installInfo.installPath + "icon.png";
        }
        // Log.d(TAG, "imageUrl:"+imgUrl);
        viewCache.iconImageView.setTag(imgUrl);
        final Bitmap bitmap = loaderManager.getCacheBitmap(imgUrl);
        if (bitmap == null) {
            // 设置默认
            viewCache.iconImageView.setBackgroundDrawable(defaultItemBg);
            loaderManager.asyncLoad(new IconLoadTask(imgUrl, inflater.getContext())
                    .addCallback(new ImageLoadTaskCallback() {

                        @Override
                        public void onImageLoaded(ImageLoadTask task, Bitmap bitmap) {
                            if (bitmap == null) {
                                return;
                            }
                            View tagedView = gridView.findViewWithTag(task.filePath);
                            if (tagedView != null) {
                                ((ImageView) tagedView).setBackgroundDrawable(new BitmapDrawable(bitmap));
                            }
                        }

                    }));
        } else {
            viewCache.iconImageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
        }
        if (installInfo.isDownload) {
            viewCache.iconImageView.setImageDrawable(null);
        } else {
            viewCache.iconImageView.setImageDrawable(grayCover);
        }
        return itemView;
    }

    private class IconLoadTask extends ImageLoadTask {

        private float destSize;
        DisplayMetrics dm = null;

        public IconLoadTask(String filePath, Context context) {
            super(filePath);
            dm = context.getResources().getDisplayMetrics();
            destSize = 48 * dm.density;
        }

        @Override
        protected Bitmap doInBackground() {
            Bitmap bitmap = null;
            byte[] data = null;
            if (URLUtil.isNetworkUrl(filePath)) {
                for (int i = 0; i < 3; i++) {
                    data = CommonUtility.requestData(filePath);
                    if (data != null && data.length > 0) {
                        break;
                    }
                }
                if (data != null) {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            } else {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(filePath);
                    bitmap = BitmapFactory.decodeFileDescriptor(fis.getFD());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            Bitmap dest = null;
            if (bitmap != null) {
                Bitmap scaleBitmap = BUtility.imageScale(bitmap, (int) destSize, (int) destSize);
                dest = CommonUtility.GetRoundedCornerBitmap(scaleBitmap, 8 * dm.density);
                if (!scaleBitmap.isRecycled()) {
                    scaleBitmap.recycle();
                }
                bitmap.recycle();
            }
            return dest;
        }

        @Override
        public BytesArray transBitmapToBytesArray(Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            BytesArray array = BytesArrayFactory.getDefaultInstance().requestBytesArray(8192);
            bitmap.compress(CompressFormat.PNG, 100, array);
            return array;
        }

    }

}
