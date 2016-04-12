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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.apache.http.cookie.SM;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.push.report.PushReportConstants;
import org.zywx.wbpalmstar.platform.push.report.PushReportUtility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.widget.RemoteViews;

public class PushRecieveMsgReceiver extends BroadcastReceiver {

    public static final String ACTION_PUSH = "org.zywx.push.receive";
    private static Context mContext;
    public static final int F_TYPE_PUSH = 10;
    private static int notificationNB = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String packg = intent.getPackage();
        if (TextUtils.isEmpty(packg) ||
                !packg.equals(context.getPackageName().toString())) {
            return;
        }
        if (ACTION_PUSH.equals(intent.getAction())) {
            if (intent.hasExtra(PushReportConstants.PUSH_DATA_INFO_KEY)) {
                newPushNotification(context, intent);
            } else {
                oldPushNotification(context, intent);
            }
        }
    }

    private void newPushNotification(Context context, Intent intent) {
        PushReportUtility.log("newPushNotification->isForground = "
                + EBrowserActivity.isForground);
        Bundle bundle = intent.getExtras();
        PushDataInfo dataInfo = (PushDataInfo) bundle
                .get(PushReportConstants.PUSH_DATA_INFO_KEY);
        int contentAvailable = dataInfo.getContentAvailable();
        if (contentAvailable == 0 && !EBrowserActivity.isForground) {
            buildPushNotification(context, intent, dataInfo);
        } else {
            if (mContext != null) {
                intent.putExtra("ntype", F_TYPE_PUSH);
                intent.putExtra("data", dataInfo.getAlert());
                intent.putExtra("message", dataInfo.getPushDataString());
                ((EBrowserActivity) mContext).handleIntent(intent);
            }
        }
    }

    private void buildPushNotification(Context context, Intent intent,
            PushDataInfo dataInfo) {
        String title = dataInfo.getTitle();
        String body = dataInfo.getAlert();
        String message = dataInfo.getPushDataString();
        Builder builder = new Builder(context);
        builder.setAutoCancel(true);
        builder.setContentTitle(title); // 通知标题
        builder.setContentText(body); // 通知内容
        builder.setTicker(body); // 通知栏信息

        String[] remindType = dataInfo.getRemindType();
        if (remindType != null) {
            if (remindType.length == 3) {
                builder.setDefaults(Notification.DEFAULT_ALL);
            } else {
                int defaults = 0;
                for (int i = 0; i < remindType.length; i++) {
                    if ("sound".equalsIgnoreCase(remindType[i])) {
                        defaults = Notification.DEFAULT_SOUND;
                        continue;
                    }
                    if ("shake".equalsIgnoreCase(remindType[i])) {
                        defaults = defaults
                                | Notification.DEFAULT_VIBRATE;
                        continue;
                    }
                    if ("breathe".equalsIgnoreCase(remindType[i])) {
                        defaults = defaults
                                | Notification.DEFAULT_LIGHTS;
                        continue;
                    }
                }
                builder.setDefaults(defaults);
            }
        }

        Resources res = context.getResources();
        int icon = res.getIdentifier("icon", "drawable",
                intent.getPackage());
        builder.setSmallIcon(icon);
        builder.setWhen(System.currentTimeMillis()); // 通知时间

        String iconUrl = dataInfo.getIconUrl();
        boolean isDefaultIcon = !TextUtils.isEmpty(iconUrl)
                && "default".equalsIgnoreCase(iconUrl);
        Bitmap bitmap = null;
        if (!isDefaultIcon) {
            bitmap = getIconBitmap(context, iconUrl);
        }
        String fontColor = dataInfo.getFontColor();
        RemoteViews remoteViews = null;
        if (!TextUtils.isEmpty(fontColor)) {
            int color = BUtility.parseColor(fontColor);
            int alphaColor = parseAlphaColor(fontColor);
            remoteViews = new RemoteViews(intent.getPackage(),
                    EUExUtil.getResLayoutID("push_notification_view"));
            // Title
            remoteViews.setTextViewText(
                    EUExUtil.getResIdID("notification_title"), title);
            remoteViews.setTextColor(
                    EUExUtil.getResIdID("notification_title"), color);
            // Body
            remoteViews.setTextViewText(
                    EUExUtil.getResIdID("notification_body"), body);
            remoteViews.setTextColor(
                    EUExUtil.getResIdID("notification_body"),
                    alphaColor);
            // LargeIcon
            if (bitmap != null) {
                remoteViews.setImageViewBitmap(
                        EUExUtil.getResIdID("notification_largeIcon"),
                        bitmap);
            } else {
                remoteViews.setImageViewResource(
                        EUExUtil.getResIdID("notification_largeIcon"),
                        EUExUtil.getResDrawableID("icon"));
            }
            // Time
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            remoteViews.setTextViewText(
                    EUExUtil.getResIdID("notification_time"),
                    format.format(System.currentTimeMillis()));
            remoteViews.setTextColor(
                    EUExUtil.getResIdID("notification_time"),
                    alphaColor);
            builder.setContent(remoteViews);
        }

        Intent notiIntent = new Intent(context, EBrowserActivity.class);
        notiIntent.putExtra("ntype", F_TYPE_PUSH);
        notiIntent.putExtra("data", body);
        notiIntent.putExtra("message", message);
        Bundle bundle = new Bundle();
        bundle.putSerializable(PushReportConstants.PUSH_DATA_INFO_KEY, dataInfo);
        notiIntent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationNB, notiIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        // 由于Android v4包的bug，在2.3及以下系统，Builder创建出来的Notification，并没有设置RemoteView，故需要添加此代码
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                && remoteViews != null) {
            notification.contentView = remoteViews;
        }
        manager.notify(notificationNB, notification);
        notificationNB++;
    }

    private int parseAlphaColor(String fontColor) {
        if (4 == fontColor.length()) {
            String tmpColor = fontColor.substring(1);
            char[] t = new char[6];
            t[0] = tmpColor.charAt(0);
            t[1] = tmpColor.charAt(0);
            t[2] = tmpColor.charAt(1);
            t[3] = tmpColor.charAt(1);
            t[4] = tmpColor.charAt(2);
            t[5] = tmpColor.charAt(2);
            fontColor = "#" + String.valueOf(t);
        }
        return BUtility.parseColor(fontColor.replaceFirst("#", "#AA"));
    }

    private Bitmap getIconBitmap(Context context, String iconUrl) {
        try {
            URL uRL = new URL(iconUrl);
            HttpURLConnection connection = (HttpURLConnection) uRL
                    .openConnection();
            String cookie = CookieManager.getInstance().getCookie(iconUrl);
            if (null != cookie) {
                connection.setRequestProperty(SM.COOKIE, cookie);
            }
            connection.connect();
            if (200 == connection.getResponseCode()) {
                InputStream input = connection.getInputStream();
                if (input != null) {
                    Environment.getDownloadCacheDirectory();
                    File ecd = context.getExternalCacheDir();
                    File file = new File(ecd, "pushIcon.png");
                    OutputStream outStream = new FileOutputStream(file);
                    byte buf[] = new byte[8 * 1024];
                    while (true) {
                        int numread = input.read(buf);
                        if (numread == -1) {
                            break;
                        }
                        outStream.write(buf, 0, numread);
                    }
                    Bitmap bitmap = BitmapFactory.decodeFile(file
                            .getAbsolutePath());
                    return bitmap;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void oldPushNotification(Context context, Intent intent) {
        PushReportUtility.log("oldPushNotification->isForground = "
                + EBrowserActivity.isForground);
        if(EBrowserActivity.isForground){
            if(mContext != null){
                intent.putExtra("ntype", F_TYPE_PUSH);
                ((EBrowserActivity) mContext).handleIntent(intent);
            }
        }else{
            CharSequence tickerText = intent.getStringExtra("title"); // 状态栏显示的通知文本提示
            Resources res = context.getResources();
            int icon = res.getIdentifier("icon", "drawable", intent.getPackage());
            long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示
            // 用上面的属性初始化Nofification

            String notifyTitle = null;
            String pushMessage = intent.getStringExtra("message");
            String value = intent.getStringExtra("data"); // 推送消息内容json
            try {
                JSONObject bodyJson = new JSONObject(value);
                notifyTitle = bodyJson.getString("msgName");// 自定义标题解析
            } catch (Exception e) {
                PushReportUtility.oe("onReceive", e);
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
            notificationIntent.putExtra("message", pushMessage);
            notificationIntent.putExtra("ntype", F_TYPE_PUSH);
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) context
                    .getSystemService(ns);
            Notification notification = new Notification(icon, tickerText,
                    when);
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            if (Build.VERSION.SDK_INT >= 16) {
                try {
                    Field priorityField = Notification.class
                            .getField("priority");
                    priorityField.setAccessible(true);
                    priorityField.set(notification, 1);
                } catch (Exception e) {
                    PushReportUtility.oe("onReceive", e);
                }
            }
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context,
                    notificationNB, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setLatestEventInfo(context, contentTitle, tickerText,
                    contentIntent);
            // 把Notification传递给NotificationManager
            mNotificationManager.notify(notificationNB, notification);
            notificationNB++;
        }
    }

    public static void setContext(Context context) {
        mContext = context;
    }
}
