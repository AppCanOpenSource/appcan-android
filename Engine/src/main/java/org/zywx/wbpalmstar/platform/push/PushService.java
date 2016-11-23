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

package org.zywx.wbpalmstar.platform.push;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.push.mqttpush.MQTTService;
import org.zywx.wbpalmstar.platform.push.mqttpush.PushDataCallback;
import org.zywx.wbpalmstar.platform.push.report.PushReportAgent;
import org.zywx.wbpalmstar.platform.push.report.PushReportConstants;
import org.zywx.wbpalmstar.platform.push.report.PushReportUtility;

import java.util.Timer;

/**
 * 为确保推送及时性，PushService 运行在单独进程中（在Manifest文件中配置），而非应用的进程，应注意数据的传递方式。
 */
public class PushService extends Service implements PushDataCallback {

    private String softToken;
    private Timer timer = null;
    SharedPreferences preferences = null;
    private String url_push = null;
    private int type = 0;
    private Object pushGetData = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // restart Service when the Service is stopped by user.
        Intent localIntent = new Intent();
        localIntent.setClass(this, PushService.class);
        localIntent.putExtra("type", type);
        this.startService(localIntent);
        super.onDestroy();
    }

    private void start() {
        EUExUtil.init(this.getApplicationContext());
        String appKey = EUExUtil.getString("appkey");
        appKey = BUtility.decodeStr(appKey);
        softToken = BUtility.getSoftToken(this, appKey);
        preferences = this.getSharedPreferences(PushReportConstants.SP_APP,
                Context.MODE_PRIVATE);
        url_push = ResoureFinder.getInstance().getString(this, "push_host");
        if (TextUtils.isEmpty(url_push)) {
            Log.w("PushService", "push_host is empty");
            return;
        }
        SharedPreferences sp = this.getSharedPreferences("saveData",
                Context.MODE_MULTI_PROCESS);
        String pushMes = sp.getString("pushMes", "0");
        String localPushMes = sp.getString("localPushMes", pushMes);
        if ("1".equals(localPushMes) && "1".equals(pushMes)) {
            type = 1;
        } else {
            type = 0;
        }
        PushReportUtility.log("start--" + type);
        try {
            if (type == 0) {
                if (pushGetData != null) {
                    ((MQTTService) pushGetData).onDestroy();
                    pushGetData = null;
                }
                return;
            }
            if (pushGetData == null) {
                String softToken = preferences.getString("softToken", null);
                pushGetData = new MQTTService(this, url_push, this, softToken);
                ((MQTTService) pushGetData).init();
            } else {
                Context ctx = getApplicationContext();
                Intent mQttPingIntent = new Intent(MQTTService.MQTT_PING_ACTION);
                mQttPingIntent.setPackage(ctx.getPackageName());
                ctx.sendBroadcast(mQttPingIntent);
            }

        } catch (Exception e) {
            if (BDebug.DEBUG) {
                e.printStackTrace();
            }
        }

    }

    private void runningNotification(JSONObject text) throws JSONException {
        String pushMessage = text.toString();// 推送消息全部内容
        // 设置通知的事件消息
        String tickerText = text.getString("title");
        String value = text.getString("body");
        String packg = getPackageName();
        String widgetName = null;
        PackageManager pm = getPackageManager();
        PackageInfo pinfo = null;
        try {
            pinfo = pm.getPackageInfo(packg, PackageManager.GET_CONFIGURATIONS);
            String appName = pinfo.applicationInfo.loadLabel(
                    getPackageManager()).toString();
            widgetName = appName;
            Editor editor = preferences.edit();
            editor.putString("widgetName", widgetName);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(widgetName)) {
            widgetName = preferences.getString("widgetName", "");
        }

        Intent intent = new Intent(PushRecieveMsgReceiver.ACTION_PUSH);
        intent.putExtra("data", value);
        intent.putExtra("title", tickerText);
        intent.putExtra("widgetName", widgetName);
        intent.setPackage(packg);
        intent.putExtra("message", pushMessage);
        sendBroadcast(intent);//传递过去

        try {
            PushReportAgent.reportPush(value, System.currentTimeMillis() + "",
                    PushReportConstants.EVENT_TYPE_ARRIVED, softToken, this);// 推送消息到达上报
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
     }

    @Override
    public void pushData(JSONObject text) {
        try {
            runningNotification(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pushDataInfo(JSONObject data) {
        PushDataInfo dataInfo = new PushDataInfo();
        dataInfo.setPushDataString(data.toString());
        dataInfo.setContentAvailable(data
                .optInt(PushReportConstants.PUSH_DATA_JSON_KEY_CONTENT_AVAILABLE));
        dataInfo.setAppId(data
                .optString(PushReportConstants.PUSH_DATA_JSON_KEY_APPID));
        dataInfo.setTaskId(data
                .optString(PushReportConstants.PUSH_DATA_JSON_KEY_TASKID));

        String title = data.optString(PushReportConstants.PUSH_DATA_JSON_KEY_TITLE);
        if (TextUtils.isEmpty(title)) {
            try {
                PackageManager pm = getPackageManager();
                PackageInfo pinfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
                title = pinfo.applicationInfo.loadLabel(pm).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dataInfo.setTitle(title);

        dataInfo.setAlert(data
                .optString(PushReportConstants.PUSH_DATA_JSON_KEY_ALERT));
        dataInfo.setBadge(data
                .optInt(PushReportConstants.PUSH_DATA_JSON_KEY_BADGE));
        dataInfo.setTenantId(data
                .optString(PushReportConstants.PUSH_DATA_JSON_KEY_TENANTID));
        String remindStrs = data
                .optString(PushReportConstants.PUSH_DATA_JSON_KEY_REMINDTYPE);
        if (!TextUtils.isEmpty(remindStrs)) {
            dataInfo.setRemindType(remindStrs.split(","));
        }

        JSONObject styleJsonObject = data
                .optJSONObject(PushReportConstants.PUSH_DATA_JSON_KEY_STYLE);
        if (null != styleJsonObject) {
            dataInfo.setIconUrl(styleJsonObject
                    .optString(PushReportConstants.PUSH_DATA_JSON_KEY_ICON));
            dataInfo.setFontColor(styleJsonObject
                    .optString(PushReportConstants.PUSH_DATA_JSON_KEY_RGB));
        }

        JSONObject behaviorJsonObject = data
                .optJSONObject(PushReportConstants.PUSH_DATA_JSON_KEY_BEHAVIOR);
        if (null != behaviorJsonObject) {
            dataInfo.setBehavior(behaviorJsonObject
                    .optString(PushReportConstants.PUSH_DATA_JSON_KEY_BEHAVIOR));
        }

        Intent intent = new Intent(PushRecieveMsgReceiver.ACTION_PUSH);
        Bundle bundle = new Bundle();
        bundle.putSerializable(PushReportConstants.PUSH_DATA_INFO_KEY, dataInfo);
        intent.putExtras(bundle);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }
}
