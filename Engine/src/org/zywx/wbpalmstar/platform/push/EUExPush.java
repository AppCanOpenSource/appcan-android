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

import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.widgetone.WidgetOneApplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class EUExPush extends EUExBase {
    public static final String function_getPushInfo = "uexPush.cbGetPushInfo";
    public static final String function_getPushState = "uexPush.cbGetPushState";

    public EUExPush(Context context, EBrowserView view) {
        super(context, view);
    }

    public void setPushInfo(String[] parm) {
        if (parm.length != 2) {
            parm = new String[]{"", ""};
        }
        final String userId = parm[0];
        final String userNick = parm[1];
        WidgetOneApplication app = (WidgetOneApplication) ((Activity) mContext)
                .getApplication();
        app.setPushInfo(userId, userNick, mContext, mBrwView);
    }

    public void setPushState(String[] parm) {
        if (parm.length != 1) {
            return;
        }
        WidgetOneApplication app = (WidgetOneApplication) ((Activity) mContext)
                .getApplication();
        app.setPushState(Integer.parseInt(parm[0]));
    }

    /**
     * 推送服务状态
     * <p/>
     * 0：关闭
     * <p/>
     * 1：开启
     *
     * @param parm
     */
    public void getPushState(String[] parm) {
        SharedPreferences sp = mContext.getSharedPreferences("saveData",
                Context.MODE_MULTI_PROCESS);
        String pushMes = sp.getString("pushMes", "0");
        String localPushMes = sp.getString("localPushMes", pushMes);
        jsCallback(function_getPushState, 0, EUExCallback.F_C_INT,
                Integer.parseInt(localPushMes));
    }

    public void getPushInfo(String[] parm) {
        String userInfo = ((EBrowserActivity) mContext).getIntent()
                .getStringExtra("data");
        String occuredAt = System.currentTimeMillis() + "";
        WidgetOneApplication app = (WidgetOneApplication) ((Activity) mContext)
                .getApplication();
        app.getPushInfo(userInfo, occuredAt);
        // reportGetPushInfo(userInfo, occuredAt);
        jsCallback(function_getPushInfo, 0, EUExCallback.F_C_TEXT, userInfo);
    }

    // /**
    // * 推送上报
    // *
    // * @param pushInfo
    // * @param occuredAt
    // */
    // private void reportGetPushInfo(String pushInfo, String occuredAt) {
    // PushReportAgent.reportGetPushInfo(pushInfo, occuredAt,
    // PushReportConstants.EVENT_TYPE_OPEN);
    // }

    @Override
    protected boolean clean() {
        return false;
    }

}
