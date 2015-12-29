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

package org.zywx.wbpalmstar.engine;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ENotification {

    public static final int F_TYPE_PUSH = 10;
    public static final int F_TYPE_USER = 11;
    public static final int F_TYPE_SYS = 12;

    private NotificationManager mNotifyMgr;
    private Context mContext;
    private int mId;


    public ENotification(Context context) {
        mContext = context;
        mId = 10;
        mNotifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static boolean isPush(int type) {

        return F_TYPE_PUSH == type;
    }

    public static boolean isUser(int type) {

        return F_TYPE_USER == type;
    }

    public void notification(String title, String msg) {
        if (null == title || title.length() == 0) {
            title = "";
        }
        if (null == msg || msg.length() == 0) {
            msg = "";
        }
        Intent notyIntent = new Intent(mContext, EBrowserActivity.class);
        notyIntent.putExtra("nid", mId);
        notyIntent.putExtra("ntype", F_TYPE_USER);
        notyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, mId, notyIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = new Notification(EResources.icon, title, System.currentTimeMillis());
        notification.defaults = Notification.DEFAULT_SOUND;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(mContext, title, msg, contentIntent);
        mNotifyMgr.notify(mId, notification);
        mId++;
    }

    public void cancelOne(int nid) {
        mNotifyMgr.cancel(nid);
    }

}
