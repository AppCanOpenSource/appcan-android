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

package org.zywx.wbpalmstar.base;

import android.app.Application;
import android.os.AsyncTask;

import java.text.DecimalFormat;

public class BConstant {

    public static final String ENGINE_VERSION="4.3.01";
    public static final int ENGINE_VERSION_CODE=43001;

    public static final String F_URL = "url";
    public static final String F_WIDGET = "widget";
    public static final String F_MULTIPLEWINDOW = "MultipleWindow";
    public static final String F_WIDGETONE = "widgetone";
    public static final String F_USER_AGENT = "userAgent";
    public static final String F_CONTENT_DISPOSITION = "contentDisposition";
    public static final String F_MIMETYPE = "mimeType";
    public static final String F_CONTENTLENGTH = "contentLength";
    public static final String F_DIALOG_TYPE = "dialogType";
    public static final String F_ASSETS_ROOT = "android_asset/";
    public static final String F_SDCARD_ROOT = "file:///sdcard/";
    public static final String F_PUSH_APPID = "appId";
    public static final String F_PUSH_WIN_NAME = "winName";
    public static final String F_PUSH_NOTI_FUN_NAME = "funName";

    public static String USERAGENT_NEW;
    public static final String USERAGENT_APPCAN = " Appcan/3.1";

    public static Application app = null;

    public static String byteChange(int size) {
        DecimalFormat df = new DecimalFormat("0.00");
        float f;
        if (size < 1024 * 1024) {
            f = (float) ((float) size / (float) 1024);
            return (df.format(new Float(f).doubleValue()) + " KB");
        } else {
            f = (float) ((float) size / (float) (1024 * 1024));
            return (df.format(new Float(f).doubleValue()) + " MB");
        }

    }

    public static String getSizeText(int downLoadSize, int fileSize) {
        return byteChange(downLoadSize) + "/" + byteChange(fileSize);
    }

    public static enum downLoadStatus {
        /**
         * Indicates that the task has not been executed yet.
         */
        WAIT,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        PAUSED,
        /**
         * Indicates that {@link AsyncTask#onPostExecute} has finished.
         */
        FINISHED,
    }


}
