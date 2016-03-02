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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class PushBroadCastReceiver extends BroadcastReceiver {
    public static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            SharedPreferences sp = context.getSharedPreferences("saveData",
                    Context.MODE_MULTI_PROCESS);
            String pushMes = sp.getString("pushMes", "0");
            String localPushMes = sp.getString("localPushMes", pushMes);
            if (!TextUtils.isEmpty(pushMes) && "1".equals(pushMes)
                    && "1".equals(localPushMes)) {
                Intent myIntent = new Intent(context, PushService.class);
                myIntent.putExtra("type", 1);
                context.startService(myIntent);
            }
        }
    }

}