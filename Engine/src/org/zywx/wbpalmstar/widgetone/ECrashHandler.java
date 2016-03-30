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

package org.zywx.wbpalmstar.widgetone;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ECrashHandler implements UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static Context mContext;
    private static ECrashHandler eCrashHandler;
    public static String m_ECrashHandler_SharedPre = "crash";
    public static String m_ECrashHandler_Key = "saveCrashInfo2File";

    private ECrashHandler(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static ECrashHandler getInstance(Context context) {
        if (null == eCrashHandler) {
            eCrashHandler = new ECrashHandler(context);
        }
        return eCrashHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex)) {
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    public void destroy() {
        mDefaultHandler = null;
    }

    private boolean handleException(Throwable ex) {
        saveCrashInfo2File(ex);
        return false;
    }

    public void saveCrashInfo2File(Throwable ex) {
        if (ex == null) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = formatter.format(new Date());
            String fileName = time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String ePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String path = ePath + "/widgetone/log/crash/";
                SharedPreferences sp = mContext.getSharedPreferences(
                        m_ECrashHandler_SharedPre, Context.MODE_PRIVATE);
                sp.edit().putString(m_ECrashHandler_Key, path + fileName).commit();
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
