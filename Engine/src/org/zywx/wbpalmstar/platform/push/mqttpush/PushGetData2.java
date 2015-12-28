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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.platform.push.PushService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttSimpleCallback;

public class PushGetData2 {

    private PushDataCallback pushDataCallback = null;
    private BroadcastReceiver myBroadcastReceiver = null;
    private boolean isNotify = false;
    private Context mCtx = null;
    private String ip = null;
    // private String clientId = null;
    private MqttClient mqttClient = null;
    private SimpleCallbackHandler simpleCallbackHandler = null;
    // private final static String CLIENT_ID = "aaaa";
    private final static boolean CLEAN_START = false;
    private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
    // 消息级别(0,1,2)
    private int[] QOS_VALUES = {2, 2};
    private String[] TOPICS = null;
    private String mAppId = null;
    String CLIENT_ID = null;
    private static final long KEEP_ALIVE_INTERVAL = 1000 * 60 * 10;

    public PushGetData2(String softToken, String url, Context context,
                        PushDataCallback callback, String[] parm) {
        pushDataCallback = callback;
        mCtx = context;
        ip = "tcp://" + url;
        SharedPreferences sp = mCtx.getSharedPreferences("app",
                Context.MODE_PRIVATE);
        mAppId = sp.getString("appid", null);
        TOPICS = new String[]{"push/" + mAppId, "push/" + softToken};
        // clientId = softToken;
    }

    public void start() {
        onReceive();
    }

    public void init() {
        try {
            // 创建MqttClient
            CLIENT_ID = getMacAddress() + mAppId;
            if (mqttClient == null) {
                mqttClient = new MqttClient(ip);
                // mqttClient = MqttClient.createMqttClient(ip,
                // MQTT_PERSISTENCE)
            }
            if (simpleCallbackHandler == null) {
                simpleCallbackHandler = new SimpleCallbackHandler();
            }
            mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
            mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
            mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接主题

            String conn = mqttClient.getConnection();
            startKeepAlives();
            System.out.println("客户机和broker已连接");
            // client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("mqtt  Exception  " + e.getMessage());
            if (isNotify) {
                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                init();
            }
        }

    }

    private void startKeepAlives() {
        Intent i = new Intent();
        i.setClass(mCtx, PushService.class);
        i.setAction(CLIENT_ID + ".KEEP_ALIVE");
        PendingIntent pi = PendingIntent.getService(mCtx, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager) mCtx
                .getSystemService(Service.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + KEEP_ALIVE_INTERVAL,
                KEEP_ALIVE_INTERVAL, pi);
    }

    private void stopKeepAlives() {
        Intent i = new Intent();
        i.setClass(mCtx, PushService.class);
        i.setAction(CLIENT_ID + ".KEEP_ALIVE");
        PendingIntent pi = PendingIntent.getService(mCtx, 0, i, 0);
        AlarmManager alarmMgr = (AlarmManager) mCtx
                .getSystemService(Service.ALARM_SERVICE);
        alarmMgr.cancel(pi);
    }

    public void stop(boolean isRealStop) {
        System.out.println("客户机和broker已经断开  stop " + isRealStop);
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mqttClient = null;
        }
        if (isRealStop && myBroadcastReceiver != null && mCtx != null) {

            mCtx.unregisterReceiver(myBroadcastReceiver);
            isNotify = false;
            stopKeepAlives();
        }
    }

    /**
     * 简单回调函数，处理client接收到的主题消息
     */
    class SimpleCallbackHandler implements MqttSimpleCallback {
        /**
         * 当客户机和broker意外断开时触发 可以再此处理重新订阅
         */
        @Override
        public void connectionLost() {
            System.out.println("客户机和broker已经断开  isNotify " + isNotify);
            if (isNotify) {
                try {

                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                init();
            }

        }

        /**
         * 客户端订阅消息后，该方法负责回调接收处理消息
         */
        @Override
        public void publishArrived(String topicName, byte[] payload, int Qos,
                                   boolean retained) throws Exception {
            String reData = new String(payload);
            if (reData != null && reData.length() > 0) {
                try {
                    JSONObject json = new JSONObject(Rc4Encrypt.decry_RC4(
                            reData, mAppId));
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
            // System.out.println("订阅主题: " + topicName);
            // System.out.println("消息数据: " + new String(payload));
            // System.out.println("消息级别(0,1,2): " + Qos);
            // System.out.println("是否是实时发送的消息(false=实时，true=服务器上保留的最后消息): "
            // + retained);
        }
    }

    private void onReceive() {
        final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        if (myBroadcastReceiver == null) {
            myBroadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

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
                            } else if (aActiveInfo == null) {
                                isNotify = false;
                                System.out.println("客户机和broker已经断开  stop 1");
                                stop(false);
                            }

                        } else {
                            isNotify = false;
                            stop(false);
                            System.out.println("客户机和broker已经断开  stop 2");
                        }

                    }
                }
            };
        }

        mCtx.registerReceiver(myBroadcastReceiver, filter);
    }

    private String getMacAddress() {
        WifiManager wifi = (WifiManager) mCtx
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress().replaceAll(":", "");
    }
}
