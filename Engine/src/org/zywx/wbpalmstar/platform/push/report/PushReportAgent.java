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
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.encryption.PEncryption;
import org.zywx.wbpalmstar.platform.push.PushService;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.util.ArrayList;
import java.util.List;

public class PushReportAgent implements PushReportConstants {

    // public static String m_appKey;
    // public static String m_appId;
    // public static Context mContext;
    // public static String mSoftToken;

    public int m_status = -1;
    private static PushReportAgent sAgent = null;
    private static String hexStr = "0123456789ABCDEF";
    public static boolean startReport = true;
    public static boolean widgetStatus = true;
    public static boolean widgetPush = true;
    public static boolean widgetParam = true;
    public static boolean widgetUpdate = true;
    public static boolean widgetAnalytics = true;
    public static boolean mam = false;
    public static boolean checkRoot = false;
    public static boolean isCertificate = false;
    public static boolean isUpdateWidget = false;
    public String certificatePsw = null;
    public static WWidgetData mCurWgt;

    public static PushReportAgent getInstance() {
        if (sAgent == null) {
            sAgent = new PushReportAgent();
        }
        return sAgent;
    }

    /**
     * 主应用启动时初始化推送
     */

    public void initPush(WWidgetData wData, Context context) {
        // mContext = inActivity;
        String appkey = EUExUtil.getString("appkey");
        appkey = PushReportUtility.decodeStr(appkey);
        checkAppStatus(context, wData.m_appId);
        PushReportUtility.getSoftToken(context, appkey);// 初始化将softToken保存在sp中
        SharedPreferences sp = context.getSharedPreferences(SP_APP,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString("appid", wData.m_appId);
        editor.commit();
        PushReportThread.getPushThread(context, this, TYPE_INIT_PUSH).start();
    }

    public static void checkAppStatus(Context inActivity, String appId) {
        try {
            String appstatus = ResoureFinder.getInstance().getString(
                    inActivity, "appstatus");
            byte[] appstatusToByte = HexStringToBinary(appstatus);
            String appstatusDecrypt = new String(PEncryption.os_decrypt(
                    appstatusToByte, appstatusToByte.length, appId));
            String[] appstatuss = appstatusDecrypt.split(",");
            if (appstatuss == null || appstatuss.length == 0) {
                return;
            }
            if ("0".equals(appstatuss[0])) {
                startReport = false;
                // return;
            }
            if ("0".equals(appstatuss[1])) {
                widgetStatus = false;
            }
            if ("0".equals(appstatuss[3])) {
                widgetParam = false;
            }
            if ("0".equals(appstatuss[4])) {
                widgetPush = false;
            }
            if ("0".equals(appstatuss[5])) {
                widgetAnalytics = false;
            }
            if ("1".equals(appstatuss[6])) {
                mam = true;
            }
            if ("1".equals(appstatuss[8])) {
                isCertificate = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param hexString
     * @return 将十六进制转换为字节数组
     */
    public static byte[] HexStringToBinary(String hexString) {
        // hexString的长度对2取整，作为bytes的长度
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        byte high = 0;// 字节高四位
        byte low = 0;// 字节低四位

        for (int i = 0; i < len; i++) {
            // 右移四位得到高位
            high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
            low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
            bytes[i] = (byte) (high | low);// 高地位做或运算
        }
        return bytes;
    }

    /**
     * 推送消息相关上报
     *
     * @param pushInfo  消息内容
     * @param occuredAt 事件发生时间
     * @param eventType 时间类型，open 和 arrived
     */
    public static void reportPush(String pushInfo, String occuredAt,
                                  String eventType, String softToken, Context context) {
        PushReportUtility.log("reportPush===" + pushInfo + " eventType==="
                + eventType);
        SharedPreferences sp = context.getSharedPreferences(
                PushReportConstants.PUSH_DATA_SHAREPRE, Context.MODE_PRIVATE);
        String taskId = sp.getString(
                PushReportConstants.PUSH_DATA_SHAREPRE_TASKID, "");
        PushReportUtility.log("reportPush===taskId " + taskId);
        if (TextUtils.isEmpty(taskId)) {
            String msgId = parsePushInfo2MsgId(pushInfo);
            if (msgId != null) {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs
                        .add(new BasicNameValuePair(KEY_PUSH_REPORT_MSGID, msgId));
                nameValuePairs.add(new BasicNameValuePair(KEY_PUSH_REPORT_SOFTTOKEN,
                        softToken));
                nameValuePairs.add(new BasicNameValuePair(KEY_PUSH_REPORT_EVENTTYPE,
                        eventType));
                nameValuePairs.add(new BasicNameValuePair(KEY_PUSH_REPORT_OCCUREDAT,
                        occuredAt));
                if (eventType.equals(PushReportConstants.EVENT_TYPE_OPEN)) {
                    PushReportThread.getPushReportThread(context, sAgent,
                            TYPE_PUSH_REPORT_OPEN, nameValuePairs).start();
                    Log.i("push", "EVENT_TYPE_OPEN");
                } else if (eventType.equals(PushReportConstants.EVENT_TYPE_ARRIVED)) {
                    PushReportThread.getPushReportThread(context, sAgent,
                            TYPE_PUSH_REPORT_ARRIVED, nameValuePairs).start();
                    Log.i("push", "EVENT_TYPE_ARRIVED");
                }
            }
        } else {
            String tenantId = sp.getString(
                    PushReportConstants.PUSH_DATA_SHAREPRE_TENANTID, "");
            PushReportUtility.log("reportPush===tenantId " + tenantId);
            Editor editor = sp.edit();
            editor.putString(PushReportConstants.PUSH_DATA_SHAREPRE_DATA, "");
            editor.putString(PushReportConstants.PUSH_DATA_SHAREPRE_MESSAGE, "");
            editor.putString(PushReportConstants.PUSH_DATA_SHAREPRE_TASKID, "");
            editor.putString(PushReportConstants.PUSH_DATA_SHAREPRE_TENANTID, "");
            editor.commit();
            PushReportThread.getNewPushReportOpen(context,
                    TYPE_NEW_PUSH_REPORT_OPEN, taskId, tenantId, softToken).start();
            Log.i("push", "TYPE_NEW_PUSH_REPORT_OPEN");
        }
    }

    /**
     * 解析推送消息中的msgId
     *
     * @param pushInfo
     * @return
     */
    private static String parsePushInfo2MsgId(String pushInfo) {
        String msgId = null;
        try {
            JSONObject json = new JSONObject(pushInfo);
            msgId = json.getString(KEY_PUSH_REPORT_MSGID);
        } catch (Exception e) {
            PushReportUtility.oe("parsePushInfo2MsgId", e);
        }
        return msgId;
    }

    /**
     * 推送开关
     *
     * @param type 1 开 0 关
     */
    public static void setPushState(Context context, int type) {
        PushReportUtility.log("setPushState--" + type);
        SharedPreferences sp = context.getSharedPreferences("saveData",
                Context.MODE_MULTI_PROCESS);
        Editor editor = sp.edit();
        editor.putString("localPushMes", String.valueOf(type));
        editor.commit();
        String pushMes = sp.getString("pushMes", String.valueOf(type));
        if (type == 1 && "1".equals(pushMes)) {
            Intent myIntent = new Intent(context, PushService.class);
            myIntent.putExtra("type", type);
            context.startService(myIntent);
        } else {
            Intent myIntent = new Intent(context, PushService.class);
            myIntent.putExtra("type", type);
            context.startService(myIntent);
        }
    }

    /**
     * 推送绑定用户接口
     */
    @SuppressWarnings("rawtypes")
    public static void setPushInfo(Context context, List nameValuePairs) {
        String softToken = PushReportUtility.getSoftToken((Activity) context,
                mCurWgt.m_appkey);
        nameValuePairs.add(new BasicNameValuePair("softToken", softToken));
        nameValuePairs.add(new BasicNameValuePair("deviceToken", softToken));
        PushReportThread.getPushBindUserThread(context, sAgent,
                TYPE_PUSH_BINDUSER, nameValuePairs).start();
    }

    public static void delPushInfo(Context context,
                                   List<NameValuePair> nameValuePairs) {
        PushReportThread.getPushBindUserThread(context, sAgent,
                TYPE_PUSH_UNBINDUSER, nameValuePairs).start();
    }

    // public static void insertPush(String appId, String title, String body) {
    // PushDBAdapter pushDB = new PushDBAdapter(mContext);
    // pushDB.open();
    // ContentValues cv = new ContentValues();
    // cv.put(PushDBAdapter.F_COLUMN_APPID, appId);
    // cv.put(PushDBAdapter.F_COLUMN_BODY, body);
    // cv.put(PushDBAdapter.F_COLUMN_TITLE, title);
    // pushDB.insert(cv, PushDBAdapter.F_WIDGET_TABLE_NAME);
    // pushDB.close();
    // }

    // public static Cursor getCursor(String appId, String sql) {
    // PushDBAdapter pushDB = new PushDBAdapter(mContext);
    // pushDB.open();
    // Cursor cursor = pushDB.select(sql);
    // return cursor;
    // }
    //
    // public static List selectPush(String appId) {
    // List<Map> list = new ArrayList<Map>();
    // PushDBAdapter pushDB = new PushDBAdapter(mContext);
    // pushDB.open();
    // String sql = "select * from " + PushDBAdapter.F_WIDGET_TABLE_NAME
    // + " where " + PushDBAdapter.F_COLUMN_APPID + " = '" + appId
    // + "'";
    // Cursor cursor = pushDB.select(sql);
    // if (cursor != null) {
    // while (cursor.moveToNext()) {
    // Map<String, String> map = new HashMap<String, String>();
    // map.put(PushDBAdapter.F_COLUMN_ID,
    // String.valueOf(cursor.getInt(0)));
    // map.put(PushDBAdapter.F_COLUMN_TITLE, cursor.getString(1));
    // map.put(PushDBAdapter.F_COLUMN_BODY, cursor.getString(2));
    // map.put(PushDBAdapter.F_COLUMN_APPID, cursor.getString(3));
    // list.add(map);
    // }
    // cursor.close();
    // cursor = null;
    // }
    // pushDB.close();
    // return list;
    // }

    // public static void delPush(String appId) {
    // PushDBAdapter pushDB = new PushDBAdapter(mContext);
    // pushDB.open();
    // pushDB.deleteByAppID(PushDBAdapter.F_WIDGET_TABLE_NAME, appId);
    // pushDB.close();
    // }

    // // Check if we are online
    // public boolean isNetworkAvailable(Context context) {
    // ConnectivityManager mConnMan = (ConnectivityManager) context
    // .getApplicationContext().getSystemService(
    // Service.CONNECTIVITY_SERVICE);
    // NetworkInfo info = mConnMan.getActiveNetworkInfo();
    // if (info == null) {
    //
    // return PushReportHttpClient.isNetWork();
    // }
    // return info.isConnected();
    // }
}
