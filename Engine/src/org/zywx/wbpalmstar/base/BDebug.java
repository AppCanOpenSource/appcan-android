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

import android.os.Environment;
import android.util.Log;


import java.io.File;

/**
 * SD卡根目录新建文件“appcandebug.txt”，即打开debug开关，删除文件即关闭
 * <p/>
 * Log输出用逗号隔开
 * 如Log.i("key","value","end");
 */
public class BDebug {

    public static final String FILE_NAME = "appcandebug.txt";
    public static boolean DEBUG = false;

    public static final String TAG = "appcan";

    public static void init() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            if (file.exists()) {
                DEBUG = true;
            }
        }
    }

    public static void log(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    public static void e(Object... msg) {
        if (DEBUG) {
            Log.e(TAG, getMsg(msg));
        }
    }

    public static void d(Object... msg) {
        if (DEBUG) {
            Log.d(TAG, getMsg(msg));
        }
    }

    public static void v(Object... msg) {
        if (DEBUG) {
            Log.v(TAG, getMsg(msg));
        }
    }

    public static void w(Object... msg) {
        if (DEBUG) {
            Log.w(TAG, getMsg(msg));
        }
    }

    public static void i(Object... msg) {
        if (DEBUG) {
            Log.i(TAG, getMsg(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            e(tag, msg, "");
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            d(tag, msg, "");
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            v(tag, msg, "");
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            w(tag, msg, "");
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            i(tag, msg, "");
        }
    }

    public static String getMsg(Object... msg) {
        StringBuilder str = new StringBuilder();
        if (msg != null) {
            for (Object obj : msg) {
                if (obj == null) {
                    continue;
                }
                str.append(obj).append(" ");
            }
        } else {
            str.append("null");
        }
        try {
            StackTraceElement[] sts = Thread.currentThread().getStackTrace();
            StackTraceElement st = null;
            String tag = null;
            if (sts != null && sts.length > 4) {
                st = sts[4];
                if (st != null) {
                    String fileName = st.getFileName();
                    tag = (fileName == null) ? "Unkown" : fileName.replace(".java", "");
                    str.insert(0, "[ " + tag + "." + st.getMethodName() + "()" + " ] \n");
                }
            }
        } catch (Exception e) {

        }
        return str.toString();
    }

}
