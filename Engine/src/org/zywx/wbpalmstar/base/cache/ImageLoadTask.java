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


import android.graphics.Bitmap;

import org.zywx.wbpalmstar.base.cache.BytesArrayFactory.BytesArray;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class ImageLoadTask {

    /**
     * 封装图片加载操作的抽象类，用来封装图片加载和处理的信息和操作，以及回调
     */
    public String filePath;
    private ImageLoadTaskCallback callback;
    public static final int STATUS_READY = 0;
    public static final int STATUS_STARTED = 1;
    public static final int STATUS_FINISHED = 2;
    private int currentState = STATUS_READY;
    private String key = null;

    public ImageLoadTask(String filePath) {
        this.filePath = filePath;
        this.key = getDigestCode(filePath);
    }

    public ImageLoadTask addCallback(ImageLoadTaskCallback callback) {
        this.callback = callback;
        return this;
    }

    public ImageLoadTaskCallback getCallBack() {
        return this.callback;
    }

    public Bitmap startExecute() {
        currentState = STATUS_STARTED;
        return doInBackground();
    }

    public String getKey() {
        return this.key;
    }

    /**
     * 子类实现此方法，定义具体的图片加载操作
     *
     * @return
     */
    protected abstract Bitmap doInBackground();

    /**
     * 子类实现此方法，定义具体的bitmap转字节数组方式(png或jpg,是否压缩等)
     *
     * @param bitmap
     * @return
     */
    protected BytesArray transBitmapToBytesArray(Bitmap bitmap) {
        return null;
    }

    ;

    public void removeCallback() {
        this.callback = null;
    }

    public void performCallback(Bitmap bitmap) {
        if (this.callback != null) {
            this.callback.onImageLoaded(this, bitmap);
        }
        currentState = STATUS_FINISHED;
    }

    public void setStatus(int status) {
        currentState = status;
    }

    public int getStatus() {
        return currentState;
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    // equals相等 hashcode必须相等
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImageLoadTask)) {
            return false;
        }
        ImageLoadTask task = (ImageLoadTask) o;
        return this.filePath.equals(task.filePath);
    }

    public static interface ImageLoadTaskCallback {
        void onImageLoaded(ImageLoadTask task, Bitmap bitmap);
    }

    /**
     * 获得字符串的16位MD5码
     *
     * @param msg
     * @return
     */
    public static String getDigestCode(String msg) {
        String digest = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(msg.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            digest = buf.toString().substring(8, 24);
            // System.out.println("result: " + buf.toString());// 32位的加密
            // System.out.println("result: " + );// 16位的加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest;
    }
}
