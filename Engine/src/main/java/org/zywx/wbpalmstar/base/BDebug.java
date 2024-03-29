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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.Keep;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

/**
 * SD卡根目录新建文件“appcandebug.txt”，即打开debug开关，删除文件即关闭
 * <p/>
 * Log输出用逗号隔开
 * 如Log.i("key","value","end");
 */
public class BDebug {

    public static final String FILE_NAME_LOG_ENGINE="engine";
    public static final String FILE_NAME = "appcandebug.txt";
    public static boolean DEBUG = false;

    public static final String TAG = "appcan";
    public static final String SDCARD_LOG_DIR = "widgetone/log/";
    public static final String LOG_DIR = "appcanlog/";

    private static String outputLogPath = "";

    public static void init() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            // 20210312 note：经过在Android11的谷歌版Android系统下测试，判断文件是否存在的代码在没有获取存储权限的情况下依然可以生效。不知未来是否会有变化。
            File file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            if (file.exists()) {
                DEBUG = true;
                Log.i(TAG, "BDebug init: DEBUG = true");
            }
        }
    }

    public synchronized static void init(Context applicationContext){
        init();
        if (TextUtils.isEmpty(outputLogPath)){
            outputLogPath = getOutputLogBasePath(applicationContext);
        }
    }

    public static String getOutputLogBasePath(Context applicationContext){
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return BUtility.getSdCardRootPath() + SDCARD_LOG_DIR;
        }else{
            return BUtility.getExterBoxPath(applicationContext) + LOG_DIR;
        }
    }

    public static boolean isDebugMode(){
        return DEBUG;
    }

    /**
     * 不想显示log 行号等信息，调用该接口
     * @param msg
     */
    public static void log(String msg) {
        if (DEBUG) {
            Log.i(TAG,msg);
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

    public static void logToFileJson(String plugin,String json){
        logToFile(plugin, DataHelper.toPrettyJson(json));
    }

    /**
     * 不判断是否调试模式，直接输出日志到文件,每个插件使用单独的文件保存,文件超过100k时会清空所有内容
     * @param plugin 插件名作为文件名 引擎使用BDebug.FILE_NAME_LOG_ENGINE
     * @param content 日志内容,添加到文件最后
     */
    public static void logToFile(String plugin,String content){
        if(TextUtils.isEmpty(plugin)||TextUtils.isEmpty(content)){
            BDebug.e("params error.");
            return;
        }
        final long logFileMaxSize = 102400;// log文件上限100KB
        String logDirPath = null;
        if (TextUtils.isEmpty(outputLogPath)){
            if (BUtility.sdCardIsWork()){
                // 如果输出路径还未生成（逻辑问题），则尝试使用旧逻辑输出日志
                logDirPath = BUtility.getWidgetOneRootPath() + "log/";
            }else{
                BDebug.e("outputLogPath is null.");
                return;
            }
        }else{
            logDirPath = outputLogPath + "log/";
        }
        File dir = new File(logDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File log = new File(logDirPath + plugin +"_log" + ".txt");
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            FileInputStream inputStream=new FileInputStream(log);
            BufferedWriter m_fout = new BufferedWriter(new FileWriter(log,
                    inputStream.available() < logFileMaxSize));
            m_fout.write(getNowTime() + "\n"
                    + content + "\n");
            m_fout.flush();
            m_fout.close();
            m_fout = null;
        } catch (Exception e) {
            if (DEBUG){
                e.printStackTrace();
            }
        }
    }





    /**
     * 打印json
     */
    public static void json(String json) {
        if (DEBUG) {
            if (TextUtils.isEmpty(json)) {
                return;
            }
            i(DataHelper.toPrettyJson(json));
        }
    }

    /**
     * 兼容插件处理
     */
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
            if (sts==null){
                return str.toString();
            }
            StackTraceElement st = null;
            String tag = null;
            if (sts.length > 4&&!(BDebug.class.getSimpleName()+".java").equals(sts[4].getFileName())) {
                st = sts[4];
            }else if (sts.length > 5){
                st = sts[5];
            }
            if (st != null) {
                String fileName = st.getFileName();
                tag = (fileName == null) ? "Unkown" : fileName.replace(".java", "");
                str.insert(0, "[ " + tag + "." + st.getMethodName() + "()" +"  ("+st.getFileName()+":"+st
                        .getLineNumber()+
                        ") ] \n");
            }
        } catch (Exception e) {

        }
        return str.toString();
    }

    @Keep
    public static void sendUDPLog(String log){
        if (WDataManager.sRootWgt==null||WDataManager.sRootWgt.m_appdebug==0|| TextUtils.isEmpty(WDataManager.sRootWgt.m_logServerIp)){
            return;
        }
        Intent intent=new Intent(BConstant.app,DebugService.class);
        intent.putExtra(DebugService.KEY_TYPE_DEBUG,DebugService.TYPE_LOG);
        intent.putExtra(DebugService.KEY_LOG_DATA,log);
        BConstant.app.startService(intent);
    }

    private static String getNowTime() {
        Time time = new Time();
        time.setToNow();
        int year = time.year;
        int month = time.month + 1;
        int day = time.monthDay;
        int minute = time.minute;
        int hour = time.hour;
        int sec = time.second;
        return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":"
                + sec;
    }

    private static String getCurYearAndMonth() {
        Time time = new Time();
        time.setToNow();
        int year = time.year;
        int month = time.month + 1;
        return year + "_" + month;
    }

}
