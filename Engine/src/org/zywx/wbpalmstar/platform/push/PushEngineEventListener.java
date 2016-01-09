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

import android.content.Context;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.zywx.wbpalmstar.engine.EngineEventListener;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.push.report.PushReportAgent;
import org.zywx.wbpalmstar.platform.push.report.PushReportConstants;
import org.zywx.wbpalmstar.platform.push.report.PushReportUtility;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.util.List;

public class PushEngineEventListener implements EngineEventListener {
    PushReportAgent pushReportAgent = null;

    public PushEngineEventListener() {
        pushReportAgent = PushReportAgent.getInstance();
    }

    @Override
    public void onWidgetStart(int wgtType, WWidgetData wgtData, Context context) {
        Log.i("push", "wgtType==" + wgtType);
        if (wgtType == WGT_TYPE_MAIN) {
            wgtData.m_appkey = EUExUtil.getString("appkey");
            wgtData.m_appkey = PushReportUtility.decodeStr(wgtData.m_appkey);
            // PushReportAgent.m_appId = wgtData.m_appId;
            // PushReportAgent.m_appKey = wgtData.m_appkey;
            PushReportAgent.mCurWgt = wgtData;
            pushReportAgent.initPush(wgtData, context);
            Log.i("zyp", "Push onMainWidgetStart");
        }
    }

    @Override
    public void onWindowOpen(String beEndUrl, String beShowUrl,
                             String[] beEndPopupUrls) {
    }

    @Override
    public void onWindowClose(String beEndUrl, String beShowUrl,
                              String[] beEndPopupUrls, String[] beShowPopupUrls) {
    }

    @Override
    public void onWindowBack(String beEndUrl, String beShowUrl,
                             String[] beEndPopupUrls, String[] beShowPopupUrls) {

    }

    @Override
    public void onWindowForward(String beEndUrl, String beShowUrl,
                                String[] beEndPopupUrls, String[] beShowPopupUrls) {
    }

    @Override
    public void onPopupOpen(String curWindowUrl, String beShowPopupUrl) {
    }

    @Override
    public void onPopupClose(String beEndPopupUrl) {
    }

    @Override
    public void onAppResume(String beEndUrl, String beShowUrl,
                            String[] beShowPopupUrls) {
    }

    @Override
    public void onAppPause(String beEndUrl, String beShowUrl,
                           String[] beEndPopupUrls) {
    }

    @Override
    public void onAppStart(String startUrl) {
    }

    @Override
    public void onAppStop() {
    }

    @Override
    public void onOther(int type, Object any) {

    }

    public void widgetReport(final WWidgetData data, final Context ctx) {

    }

    @Override
    public void setPushInfo(final Context context, final List nameValuePairs) {
        Log.i("push", "setPushInfo");
        PushReportAgent.setPushInfo(context, nameValuePairs);
    }

    @Override
    public void setPushState(Context context, int state) {
        Log.i("push", "setPushState");
        PushReportAgent.setPushState(context, state);
    }

    @Override
    public void getPushInfo(Context context, String pushInfo, String occuredAt) {
        Log.i("push", "getPushInfo");
        String appKey = EUExUtil.getString("appkey");
        appKey = PushReportUtility.decodeStr(appKey);
        String softToken = PushReportUtility.getSoftToken(context, appKey);
        PushReportAgent.reportPush(pushInfo, occuredAt,
                PushReportConstants.EVENT_TYPE_OPEN, softToken, context);
    }

    @Override
    public void delPushInfo(Context context, List<NameValuePair> nameValuePairs) {
        Log.i("push", "delPushInfo");
        PushReportAgent.delPushInfo(context, nameValuePairs);
    }

}
