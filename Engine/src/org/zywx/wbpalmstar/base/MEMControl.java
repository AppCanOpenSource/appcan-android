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

import org.zywx.wbpalmstar.platform.memoryControl.KeepForeService;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MEMControl {

    static final String largeHeap_action = "appcan.intent.action.largeHeap";
    static boolean inLargeHeap = false;
    static int SDK_INT = Build.VERSION.SDK_INT;

    public static void setProcessInLargeHeap(Context context) {
        if (inLargeHeap || SDK_INT < 14) {
            return;
        }
        inLargeHeap = true;
        Intent remoteIntent = new Intent(context, KeepForeService.class);
        context.startService(remoteIntent);
    }

    public static void setProcessInLowHeap(Context context) {
        if (!inLargeHeap || SDK_INT < 14) {
            return;
        }
        inLargeHeap = false;
        Intent remoteIntent = new Intent(context, KeepForeService.class);
        context.stopService(remoteIntent);
    }
}
