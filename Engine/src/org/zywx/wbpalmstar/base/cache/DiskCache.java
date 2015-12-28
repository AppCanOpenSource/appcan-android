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

package org.zywx.wbpalmstar.base.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DiskCache {

    public static final String TAG = "DiskCache";
    public static File cacheFolder;

    public static void initDiskCache(Context context) {

        if (Build.VERSION.SDK_INT <= 7) {
            if (Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())) {
                String appendPath = "Android/data/" + context.getPackageName()
                        + "/cache";
                try {
                    cacheFolder = new File(
                            Environment.getExternalStorageDirectory(),
                            appendPath);
                    if (!cacheFolder.exists()) {
                        cacheFolder.mkdirs();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            cacheFolder = context.getExternalCacheDir(); // 返回外部高速缓存路径，当没有SDCard时，返回null，当应用程序卸载的时候，此路径也会被卸载，
        }
    }

    public static boolean writeDiskCache(String fileName, Bitmap bitmap) {
        if (cacheFolder == null || fileName == null || bitmap == null
                || (!cacheFolder.exists())) {
            return false;
        }
        if (bitmap.isRecycled()) {
            return false;
        }
        boolean isWrited = false; // 判断是否写入成功
        FileOutputStream fos = null;
        File cacheFile = null;
        try {
            cacheFile = new File(cacheFolder, fileName);
            if (cacheFile.exists()) { // 判断文件是否存在，存在则删除
                cacheFile.delete();
            }
            fos = new FileOutputStream(cacheFile);
            /**
             * bitmap.hasAlpha()判断图片是否有apha通道，如果有就压缩成PNG，没有则压缩成JPG
             */
            if (bitmap.compress(bitmap.hasAlpha() ? CompressFormat.PNG
                    : CompressFormat.JPEG, 100, fos)) {
                isWrited = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isWrited;
    }

    public static Bitmap readCache(String key) {
        if (cacheFolder == null || key == null) {
            return null;
        }
        Bitmap bitmap = null;
        File cacheFile = new File(cacheFolder, key);
        if (!cacheFile.exists() || cacheFile.isFile() == false) { // 文件存在或者cacheFile不是一个文件时（如传递进来的是drawable下的id值），返回null
            return null;
        }
        FileInputStream input = null;
        try {
            input = new FileInputStream(cacheFile);
            bitmap = BitmapFactory.decodeFileDescriptor(input.getFD());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }
}
