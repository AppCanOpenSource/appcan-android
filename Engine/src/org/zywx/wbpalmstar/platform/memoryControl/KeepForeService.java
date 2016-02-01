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

package org.zywx.wbpalmstar.platform.memoryControl;


import org.zywx.wbpalmstar.base.BDebug;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeepForeService extends Service {

    static final int nid = 100000;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        BDebug.d("ldx", "KeepForeService: onCreate");
        startForeground(nid, makeNotify());
    }

    @Override
    public void onStart(Intent intent, int startId) {
        BDebug.d("ldx", "KeepForeService: onStart");
        startForeground(nid, makeNotify());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BDebug.d("ldx", "KeepForeService: onStartCommand");
        onStart(intent, startId);
        return Service.START_STICKY_COMPATIBILITY;
    }

    private Notification makeNotify() {
        Intent notyIntent = new Intent(this, KeepForeService.class);
        PendingIntent contentIntent = PendingIntent.getService(this, nid, notyIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = new Notification();
        notification.defaults = 0;
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        notification.setLatestEventInfo(this, "", "", contentIntent);
        return notification;
    }

    @Override
    public void onDestroy() {
        BDebug.d("ldx", "KeepForeService: onDestroy");
    }

    public void onLowMemory() {
        BDebug.d("ldx", "KeepForeService: onDestroy");
    }

    public void onTrimMemory(int level) {
        String levelStr = "none";
        switch (level) {
            case 80://ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                levelStr = "complete";
                break;
            case 60://ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                levelStr = "moderate";
                break;
            case 40://ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                levelStr = "background";
                break;
            case 20://ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                levelStr = "ui_hidden";
                break;
            case 15://ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                levelStr = "running_critical";
                break;
            case 10://ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                levelStr = "running_low";
                break;
        }
        BDebug.d("ldx", "KeepForeService: onTrimMemory " + level + " , " + levelStr);
    }
}
