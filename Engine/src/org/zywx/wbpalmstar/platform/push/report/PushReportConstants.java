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

public interface PushReportConstants {
    public static final String url_push_report = "report";
    public static final String url_push_bindUser = "msg/bindUser";

    public static final String KEY_PUSH_BINDUSER_HOST = "bindUser_host";
    public static final String KEY_PUSH_REPORT_HOST = "bindUser_host";

    public static final String SP_PUSH_BINDUSER_HOST = "bindUser_host";
    public static final String SP_PUSH_REPORT_HOST = "bindUser_host";
    public static String SP_APP = "app";

    public static final String EVENT_TYPE_OPEN = "open";
    public static final String EVENT_TYPE_ARRIVED = "arrived";

    public static final String KEY_PUSH_REPORT_MSGID = "msgId";
    public static final String KEY_PUSH_REPORT_SOFTTOKEN = "softToken";
    public static final String KEY_PUSH_REPORT_EVENTTYPE = "eventType";
    public static final String KEY_PUSH_REPORT_OCCUREDAT = "occuredAt";
    public static final String PUSH_DATA_SHAREPRE = "pushData_sharePre";
    public static final String PUSH_DATA_SHAREPRE_DATA = "pushData_data";
    public static final String PUSH_DATA_SHAREPRE_MESSAGE = "pushData_message";
    public static final String PUSH_DATA_SHAREPRE_TASKID = "pushData_taskId";
    public static final String PUSH_DATA_SHAREPRE_TENANTID = "pushData_tenantId";

    public static final String PUSH_DATA_INFO_KEY = "pushDataInfo";
    public static final String PUSH_DATA_JSON_KEY_APPID = "appId";
    public static final String PUSH_DATA_JSON_KEY_TASKID = "taskId";
    public static final String PUSH_DATA_JSON_KEY_TITLE = "title";
    public static final String PUSH_DATA_JSON_KEY_ALERT = "alert";
    public static final String PUSH_DATA_JSON_KEY_BADGE = "badge";
    public static final String PUSH_DATA_JSON_KEY_CONTENT_AVAILABLE = "content-available";
    public static final String PUSH_DATA_JSON_KEY_REMINDTYPE = "remindType";
    public static final String PUSH_DATA_JSON_KEY_STYLE = "style";
    public static final String PUSH_DATA_JSON_KEY_ICON = "icon";
    public static final String PUSH_DATA_JSON_KEY_RGB = "rgb";
    public static final String PUSH_DATA_JSON_KEY_BEHAVIOR = "behavior";
    public static final String PUSH_DATA_JSON_KEY_TENANTID = "tenantId";

    public final static int TYPE_INIT_PUSH = 0;
    public final static int TYPE_PUSH_BINDUSER = 1;
    public final static int TYPE_PUSH_REPORT_OPEN = 2;
    public final static int TYPE_PUSH_REPORT_ARRIVED = 3;
    public final static int TYPE_PUSH_UNBINDUSER = 4;
    public final static int TYPE_NEW_PUSH_REPORT_OPEN = 5;

    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /**
     * Current network is GPRS
     */
    public static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    public static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    public static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    public static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    public static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    public static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    public static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    public static final int NETWORK_TYPE_HSPAP = 15;
    // 实时上报
    public static final int REPORT_STRATEGY_REALTIME = 1;
    // 启动上报
    public static final int REPORT_STRATEGY_START = 0;
    // wifi上报
    public static final int REPORT_STRATEGY_WIFI = 3;
    // 每日上报
    public static final int REPORT_STRATEGY_DAILY = 2;
    public static final int REPORT_STRATEGY_NEW = -1;
    public static final int REPORT_STRATEGY_DEFAULT = -2;
    // 第一次的opener
    public static final String FIRST_OPENER = "application";

    // public static final String url_start9 =
    // "https://192.168.1.42:8443/dc/widgetStartup/postData/";
    //
    // public static final String url_push9="https://push.appcan.cn/push/msg/";
    //
    // public static final String url_error9 =
    // "https://dc.appcan.cn:8443/anError/byFile/";
    // public static final String url_report9 =
    // "https://dc.appcan.cn:8443/anEvent/byFile/";
    //
    // public static final String url_bindUser9 =
    // "https://192.168.1.42/push/msg/softToken/bindUser";

}
