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

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.Log;

import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.platform.push.report.PushReportUtility;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private static ExecutorService mExecutorService;


    public static void init() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            if (file.exists()) {
                DEBUG = true;
            }
        }
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
        if (BUtility.sdCardIsWork()){
            String developPath = BUtility.getSdCardRootPath()
                    + "widgetone/log/";
            File dir = new File(developPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File log = new File(developPath +plugin +"_log"+ ".txt");
            try {
                if (!log.exists()) {
                    log.createNewFile();
                }
                FileInputStream inputStream=new FileInputStream(log);
                BufferedWriter m_fout = new BufferedWriter(new FileWriter(log,
                        inputStream.available()<102400));
                m_fout.write(PushReportUtility.getNowTime() + "\n"
                        + content+"\n");
                m_fout.flush();
                m_fout.close();
                m_fout = null;
            } catch (Exception e) {
                if (DEBUG){
                    e.printStackTrace();
                }
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


}
