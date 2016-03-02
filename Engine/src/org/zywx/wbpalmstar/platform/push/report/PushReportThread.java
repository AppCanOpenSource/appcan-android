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

package org.zywx.wbpalmstar.platform.push.report;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.platform.push.PushService;

import java.util.List;

@SuppressWarnings("rawtypes")
public class PushReportThread extends Thread implements PushReportConstants {
    public Context m_activity = null;
    private int mThreadType;
    private String host_pushReport = null;
    private String host_pushBindUser = null;
    private String mTaskId = null;
    private String mTenantId = null;
    private String mSoftToken = null;
    // private PushReportAgent mPushAgent = null;
    private List mNameValuePairs = null;
    private boolean mIsRun;

    public PushReportThread(Context inActivity, int threadType) {
        m_activity = inActivity;
        // mPushAgent = pushAgent;
        mThreadType = threadType;
        host_pushReport = ResoureFinder.getInstance().getString(inActivity,
                KEY_PUSH_REPORT_HOST);
        host_pushBindUser = ResoureFinder.getInstance().getString(inActivity,
                KEY_PUSH_BINDUSER_HOST);
        setName("Appcan-Push");
    }

    public static PushReportThread getPushThread(Context inActivity,
                                                 PushReportAgent pushAgent, int threadType) {
        PushReportThread pushReportThread = new PushReportThread(inActivity,
                threadType);
        return pushReportThread;
    }

    public static PushReportThread getPushBindUserThread(Context inActivity,
                                                         PushReportAgent pushAgent, int threadType, List nameValuePairs) {
        PushReportThread pushReportThread = new PushReportThread(inActivity,
                threadType);
        pushReportThread.mNameValuePairs = nameValuePairs;
        return pushReportThread;
    }

    public static PushReportThread getPushReportThread(Context inActivity,
                                                       PushReportAgent pushAgent, int threadType, List nameValuePairs) {
        PushReportThread pushReportThread = new PushReportThread(inActivity,
                threadType);
        pushReportThread.mNameValuePairs = nameValuePairs;
        return pushReportThread;
    }

    public static PushReportThread getNewPushReportOpen(Context inActivity,
            int threadType, String taskId, String tenantId, String softToken) {
        PushReportThread pushReportThread = new PushReportThread(inActivity, threadType);
        pushReportThread.mTaskId = taskId;
        pushReportThread.mTenantId = tenantId;
        pushReportThread.mSoftToken = softToken;
        return pushReportThread;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mIsRun = true;
        try {
            while (mIsRun) {
                switch (mThreadType) {
                    case TYPE_INIT_PUSH:
                        initPush();
                        break;
                    case TYPE_PUSH_BINDUSER:
                        if (TextUtils.isEmpty(host_pushBindUser)) {
                            Log.w("PushReportThread", "host_pushBindUser is empty");
                            break;
                        }
                        bindUserInfo();
                        break;
                    case TYPE_PUSH_UNBINDUSER:
                        if (TextUtils.isEmpty(host_pushBindUser)) {
                            Log.w("PushReportThread", "host_pushBindUser is empty");
                            break;
                        }
                        unBindUserInfo();
                        break;
                    case TYPE_PUSH_REPORT_OPEN:
                        if (TextUtils.isEmpty(host_pushReport)) {
                            Log.w("PushReportThread", "host_pushReport is empty");
                            break;
                        }
                        pushReportOpen();
                        break;
                    case TYPE_PUSH_REPORT_ARRIVED:
                        if (TextUtils.isEmpty(host_pushReport)) {
                            Log.w("PushReportThread", "host_pushReport is empty");
                            break;
                        }
                        pushReportArrive();
                        break;
                    case TYPE_NEW_PUSH_REPORT_OPEN:
                        if (TextUtils.isEmpty(host_pushBindUser)) {
                            Log.w("PushReportThread", "host_pushBindUser is empty");
                            break;
                        }
                        newPushReportOpen();
                        break;
                }
                mIsRun = false;
            }
        } catch (Exception e) {
            Log.e("PushReportThread", e.getMessage());
        }
    }

    private void unBindUserInfo() {
        String softToken = PushReportUtility.getSoftToken((Activity) m_activity,
                PushReportAgent.mCurWgt.m_appkey);
        String bu = PushReportHttpClient.sendPostDataByNameValuePair(
                (host_pushBindUser + "msg/" + softToken + "/unBindUser"), mNameValuePairs,
                m_activity);
        Log.i("PushReportThread", "unBindUserInfo======" + bu);
    }

    private void initPush() {
        String localPushMes = "0";// setPushState 可以改变
        String pushMes = "1";
        SharedPreferences sp = m_activity.getSharedPreferences("saveData",
                Context.MODE_MULTI_PROCESS);
        Editor editor = sp.edit();
        if (!PushReportAgent.widgetPush) {
            pushMes = "0";
        }
        editor.putString("pushMes", pushMes);
        editor.commit();
        localPushMes = sp.getString("localPushMes", pushMes);
        if ("1".equals(localPushMes) && "1".equals(pushMes)) {
            Intent myIntent = new Intent(m_activity, PushService.class);
            myIntent.putExtra("type", 1);
            m_activity.startService(myIntent);
        } else {
            Intent myIntent = new Intent(m_activity, PushService.class);
            myIntent.putExtra("type", 0);
            m_activity.startService(myIntent);
        }
    }

    private void bindUserInfo() {
        String bu = PushReportHttpClient.sendPostDataByNameValuePair(
                (host_pushBindUser + url_push_bindUser), mNameValuePairs,
                m_activity);
        Log.i("PushReportThread", "bindUserInfo======" + bu);
    }

    private void pushReportOpen() {
        String result = PushReportHttpClient.sendPostDataByNameValuePair(
                (host_pushReport + url_push_report), mNameValuePairs,
                m_activity);
        Log.i("PushReportThread", "pushReportOpen result======" + result);
    }

    private void newPushReportOpen() {
        if (!host_pushBindUser.endsWith("/")) {
            host_pushBindUser = host_pushBindUser + "/";
        }
        host_pushBindUser = host_pushBindUser + "4.0/count/" + mTaskId;
        String result = PushReportHttpClient.newPushOpenByPostData(
                host_pushBindUser, m_activity, mTenantId, mSoftToken);
        Log.i("PushReportThread", "newPushReportOpen result======" + result);
    }

    private void pushReportArrive() {
        String result = PushReportHttpClient.sendPostDataByNameValuePair(
                (host_pushReport + url_push_report), mNameValuePairs,
                m_activity);
        Log.i("PushReportThread", "pushReportArrive result======" + result);
    }

}
