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

package org.zywx.wbpalmstar.platform.push.mqttpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.platform.push.report.PushReportHttpClient;

import java.util.Timer;
import java.util.TimerTask;

public class PushGetData {
    private Timer timer = null;
    private long sleepTime = 0;
    private MyTimerTask myTimerTask = null;
    private boolean isTemporary = true;
    private String mSoftToken = null;
    private String getPushURL = null;
    private Context mCtx = null;
    private PushDataCallback pushDataCallback = null;
    private boolean isNotify = false;
    private BroadcastReceiver myBroadcastReceiver = null;

    public PushGetData(String softToken, String url, Context context,
                       PushDataCallback callback, String[] parm) {
        mSoftToken = softToken;
        getPushURL = url + "/msg/";
        mCtx = context;
        pushDataCallback = callback;
    }

    public void start() {
        onReceive();
    }

    private void init() {
        if (isTemporary) {
            sleepTime = 1000 * 30;
        } else {
            sleepTime = 1000 * 60 * 2;
        }

        if (timer == null) {
            timer = new Timer();
        }

        if (myTimerTask == null) {
            myTimerTask = new MyTimerTask();
        }

        timer.schedule(myTimerTask, 0, sleepTime);

    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            getPushInfo();
            if (isTemporary && sleepTime == 1000 * 60 * 2) {
                sleepTime = 1000 * 60 * 15;
                notifiTimer();
                return;
            }
            if (sleepTime == 1000 * 60 * 15) {
                sleepTime = 1000 * 60 * 30;
                notifiTimer();
            } else if (sleepTime == 1000 * 60 * 30) {
                sleepTime = 1000 * 60 * 60;
                notifiTimer();
            } else if (sleepTime == 1000 * 60 * 60) {
                sleepTime = 1000 * 60 * 120;
                notifiTimer();
            }
            // startTime = System.currentTimeMillis();
        }

    }

    private void notifiTimer() {

        if (myTimerTask != null) {
            myTimerTask.cancel();
            myTimerTask = null;
        }
        myTimerTask = new MyTimerTask();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(myTimerTask, sleepTime, sleepTime);
    }

    private void getPushInfo() {
        // String softToken = preferences.getString("softToken", null);
        // appName = preferences.getString("appName", null);
        String reData = PushReportHttpClient.getGetData(getPushURL + mSoftToken
                + "/listMsg", mCtx);
        if (reData != null && reData.length() > 0) {
            try {
                JSONObject json = new JSONObject(reData);
                String status = json.getString("status");
                if ("ok".equals(status)) {
                    String messageList = json.getString("messageList");
                    JSONArray jsonArray = new JSONArray(messageList);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        // runningNotification(jsonArray.getJSONObject(i));
                        if (pushDataCallback != null) {
                            pushDataCallback.pushData(jsonArray
                                    .getJSONObject(i));
                        }

                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void stop(boolean isRealStop) {
        if (myTimerTask != null) {
            myTimerTask.cancel();
            myTimerTask = null;
        }
        // if (udpTimerTask != null) {
        // udpTimerTask.cancel();
        // udpTimerTask = null;
        // }
        // if (udpTimer != null) {
        // udpTimer.cancel();
        // udpTimer = null;
        // }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        // runStatus = Status.PENDING;
        // sleepTime = 0;
        // if (mUDPSocket != null) {
        // mUDPSocket.disconnect();
        // mUDPSocket.close();
        // mUDPSocket = null;
        // }
        // isUDPRunning = false;
        if (isRealStop && myBroadcastReceiver != null && mCtx != null) {

            mCtx.unregisterReceiver(myBroadcastReceiver);
        }
    }

    private void onReceive() {
        final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        if (myBroadcastReceiver == null) {
            myBroadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.SCREEN_OFF".equals(intent
                            .getAction())) {
                        if (isTemporary) {
                            sleepTime = 1000 * 60 * 2;
                        } else {
                            sleepTime = 1000 * 60 * 15;
                        }

                        notifiTimer();
                    } else if ("android.intent.action.SCREEN_ON".equals(intent
                            .getAction())) {
                        if (isTemporary) {
                            sleepTime = 1000 * 30;
                        } else {
                            sleepTime = 1000 * 60 * 2;
                        }

                        // notifiTimer();
                    }
                    if (TextUtils.equals(intent.getAction(),
                            CONNECTIVITY_CHANGE_ACTION)) {

                        ConnectivityManager mConnMgr = (ConnectivityManager) context
                                .getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (mConnMgr != null) {

                            NetworkInfo aActiveInfo = mConnMgr
                                    .getActiveNetworkInfo(); // 获取活动网络连接信息
                            if (!isNotify && aActiveInfo != null
                                    && aActiveInfo.isConnectedOrConnecting()) {
                                init();
                                isNotify = true;
                            } else {
                                isNotify = false;
                                stop(false);
                            }

                        } else {
                            isNotify = false;
                            stop(false);
                        }

                    }
                }
            };
        }
        mCtx.registerReceiver(myBroadcastReceiver, filter);
    }
}
