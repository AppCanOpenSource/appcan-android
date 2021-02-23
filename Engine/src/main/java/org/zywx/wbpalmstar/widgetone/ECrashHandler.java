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

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import org.zywx.wbpalmstar.base.BUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ECrashHandler implements UncaughtExceptionHandler {

    private static ECrashHandler eCrashHandler;
    public static String m_ECrashHandler_SharedPre = "crash";
    public static String m_ECrashHandler_Key = "saveCrashInfo2File";

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private Context mContext;

    private ECrashHandler(Context context) {
        mContext = context.getApplicationContext();
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
            String fileName = time + "_" + mContext.getPackageName() + ".log";
            String path = null;
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // SD卡读写权限已经获取并且已经挂载可使用状态，则写入。
                String ePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                path = ePath + "/widgetone/log/crash/";
            }else{
                path = BUtility.getExterBoxPath(mContext) + "appcanlog/log/crash/";
            }
            SharedPreferences sp = mContext.getSharedPreferences(
                    m_ECrashHandler_SharedPre, Context.MODE_PRIVATE);
            sp.edit().putString(m_ECrashHandler_Key, path + fileName).apply();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(sb.toString().getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
