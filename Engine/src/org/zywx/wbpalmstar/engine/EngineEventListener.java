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

import android.content.Context;

import org.apache.http.NameValuePair;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.util.List;

public interface EngineEventListener {

    /**
     * 主Widget标识
     */
    public static final int WGT_TYPE_MAIN = 0;

    /**
     * 子Widget标识
     */
    public static final int WGT_TYPE_SUB = 1;

    /**
     * 对应拆分之前WidgetOneApplication下的:<br>
     * widgetRegist函数: wgtType = 0, 主widget;<br>
     * widgetReport函数: wgtType = 1, 子widget.<br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onWidgetStart(int wgtType, WWidgetData wgtData, Context context);

    /**
     * 对应拆分之前EBrowser下的windowOpenAnalytics函数 <br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onWindowOpen(String beEndUrl, String beShowUrl,
                             String[] beEndPopupUrls);

    /**
     * 对应拆分之前EBrowser下的windowCloseAnalytics函数 <br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onWindowClose(String beEndUrl, String beShowUrl,
                              String[] beEndPopupUrls, String[] beShowPopupUrls);

    /**
     * 对应拆分之前EBrowser下的windowBackAnalytics函数 <br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onWindowBack(String beEndUrl, String beShowUrl,
                             String[] beEndPopupUrls, String[] beShowPopupUrls);

    /**
     * 对应拆分之前EBrowser下的windowForwardAnalytics函数 <br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onWindowForward(String beEndUrl, String beShowUrl,
                                String[] beEndPopupUrls, String[] beShowPopupUrls);

    /**
     * 对应拆分之前EBrowser下的popOpenAnalytics函数 <br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onPopupOpen(String curWindowUrl, String beShowPopupUrl);

    /**
     * 对应拆分之前EBrowser下的popCloseAnalytics函数 <br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onPopupClose(String beEndPopupUrl);

    /**
     * 对应拆分之前EBrowser下的onAppResumeAnalytics和setAppBecomeActive函数 <br>
     * 两个函数合并，MAM在执行onAppResumeAnalytics逻辑后需立即调用setAppBecomeActive.<br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onAppResume(String beEndUrl, String beShowUrl,
                            String[] beShowPopupUrls);

    /**
     * 对应拆分之前EBrowser下的onAppPauseAnalytics和setAppBecomeBackground函数 <br>
     * 两个函数合并，MAM在执行onAppPauseAnalytics逻辑后需立即调用setAppBecomeBackground.<br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onAppPause(String beEndUrl, String beShowUrl,
                           String[] beEndPopupUrls);

    /**
     * 对应拆分之前EBrowser下的startAnalytics函数 <br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onAppStart(String startUrl);

    /**
     * 对应拆分之前WidgetOneApplication下的stopAnalyticsAgent函数 <br>
     * <br>
     * <strong>拆分后需把该函数下所有逻辑拷入MAM插件对应函数中.</strong><br>
     **/
    public void onAppStop();

    /**
     * 保留接口，留作扩展备用.<br>
     * 可能因拆分并且稳定后，该接口类又新增函数，但之前的插件包中因未实现该新增函数而导致编译错误<br>
     * 提供接口，以后的新增事件消息时可通过该接口分发.
     **/
    public void onOther(int type, Object any);

    /**
     * widget插件中的setPushInfo接口
     */
    public void setPushInfo(Context context, List<NameValuePair> nameValuePairs);

    /**
     * widget插件中的delPushInfo接口
     */
    public void delPushInfo(Context context, List<NameValuePair> nameValuePairs);

    /**
     * widget插件中的setPushState接口
     */
    public void setPushState(Context context, int state);

    public void getPushInfo(Context context, String userInfo, String occuredAt);
}
