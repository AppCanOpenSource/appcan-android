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

import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

public class PushRecieveMsgReceiver extends BroadcastReceiver {

    public static final String ACTION_PUSH = "org.zywx.push.receive";
    private static Context mContext;
    public static final int F_TYPE_PUSH = 10;
    private static int notificationNB = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("PushRecieveMsgReceiver", "onReceive->isForground = " + EBrowserActivity.isForground);
        String packg = intent.getStringExtra("packg");
        if(TextUtils.isEmpty(packg) ||
               !packg.equals(context.getPackageName().toString())){
            return;
        }
        if(intent.getAction().equals(ACTION_PUSH)){
            if(EBrowserActivity.isForground){
                if(mContext != null){
                    intent.putExtra("ntype", F_TYPE_PUSH);
                    ((EBrowserActivity) mContext).handleIntent(intent);
                }
            }else{
                CharSequence tickerText = intent.getStringExtra("title"); // 状态栏显示的通知文本提示
                Resources res = context.getResources();
                int icon = res.getIdentifier("icon", "drawable", packg);
                String ns = Context.NOTIFICATION_SERVICE;
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
                long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示
                // 用上面的属性初始化Nofification
                Notification notification = new Notification(icon, tickerText, when);
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notification.defaults |= Notification.DEFAULT_SOUND;

                String notifyTitle = null;
                String value = intent.getStringExtra("data"); // 推送消息内容json
                try {
                    JSONObject bodyJson = new JSONObject(value);
                    notifyTitle = bodyJson.getString("msgName");// 自定义标题解析
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(notifyTitle)) {
                    notifyTitle = intent.getStringExtra("widgetName");// 若msgName为空，则使用widgetName作为消息标题
                }
                if (TextUtils.isEmpty(notifyTitle)) {
                    notifyTitle = "APPCAN";// 若widgetName为空，则使用APPCAN作为消息标题
                }
                CharSequence contentTitle = notifyTitle; // 通知栏标题
                Intent notificationIntent = new Intent(context, EBrowserActivity.class); // 点击该通知后要跳转的Activity
                notificationIntent.putExtra("data", value);
                notificationIntent.putExtra("ntype", F_TYPE_PUSH);
                PendingIntent contentIntent = PendingIntent.getActivity(context,
                        notificationNB, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setLatestEventInfo(context, contentTitle, tickerText,
                        contentIntent);
                // 把Notification传递给NotificationManager
                mNotificationManager.notify(notificationNB, notification);
                notificationNB++;
            }
        }        
    }
    
    public static void setContext(Context context){
        mContext = context;
    }
}
