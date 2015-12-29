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

package org.zywx.wbpalmstar.engine;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class EAdaptJniTask extends Thread {

    private ECallback mCallback;
    private Context mContext;
    private static String DeviceDirName = File.separator + "sdcard" + File.separator;
    private static final String RootDirName = "widget";

    public EAdaptJniTask(Context ctx) {
        mContext = ctx;
        DeviceDirName = ctx.getDir(RootDirName, Context.MODE_PRIVATE) + File.separator;
    }

    public void setCallback(ECallback callback) {
        mCallback = callback;
    }

    public void asynExecute() {

        start();
    }

    public boolean synExecute() {

        return doInBackground();
    }

    private boolean doInBackground() {
        boolean ok = true;
        AssetManager asset = mContext.getAssets();
        try {
            File rootDir = new File(DeviceDirName + RootDirName);
            if (!rootDir.exists()) {
                rootDir.mkdir();
            }
            String[] fileList = asset.list(RootDirName);
            if (null != fileList) {
                for (String level : fileList) {
                    String realPath = RootDirName + File.separator + level;
                    if (isFile(realPath)) {
                        handleFile(asset, realPath);
                    } else {
                        handleDirectory(asset, realPath);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ok = false;
        }
        return ok;
    }

    @Override
    public void run() {
        boolean ok = doInBackground();
        if (null != mCallback) {
            mCallback.callback(ok);
        }
    }

    private void handleDirectory(AssetManager asset, String dir) throws Exception {
        File fDir = new File(DeviceDirName + dir);
        if (!fDir.exists()) {
            fDir.mkdir();
        }
        String[] fileList = asset.list(dir);
        for (String level : fileList) {
            String realPath = dir + File.separator + level;
            if (isFile(realPath)) {
                handleFile(asset, realPath);
            } else {
                handleDirectory(asset, realPath);
            }
        }
    }

    private void handleFile(AssetManager asset, String fileName) throws Exception {
        FileOutputStream outStream = new FileOutputStream(DeviceDirName + fileName);
        InputStream inStream = asset.open(fileName);
        byte[] temp = new byte[1024 * 8];
        int i = 0;
        while ((i = inStream.read(temp)) > 0) {
            outStream.write(temp, 0, i);
        }
        outStream.flush();
        inStream.close();
        outStream.close();
    }

    private boolean isFile(String name) {

        return name.contains(".");
    }

}
