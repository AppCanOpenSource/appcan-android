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

package org.zywx.wbpalmstar.widgetone.dataservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.ESystemInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Process;
import android.util.Xml;

public class WHttpManager {

    private static final String F_WIDGETONE_REGIST_URL = "http://wgb.tx100.com/mobile/wg-reg.wg";
    // http://wgb.3g2win.com/mobile/wg-reg.wg?ver=4030.1.01.439.00.00.9999.000&screenSize=320*240

    private static final String F_WIDGET_REGIST_URL = "http://wgb.tx100.com/mobile/soft-reg.wg";

    private static final String F_WIDGET_REPORT_URL = "http://wgb.tx100.com/mobile/soft-startup-report.wg";

    private static final String F_WIDGETONE_RE_XML_TAGNAME_WIDGETONEID = "widgetOneId";

    private static final String F_WIDGET_RE_XML_TAGNAME_WIDGETID = "widgetId";

    private static final String F_WIDGET_RE_XML_TAGNAME_FILENAME = "updateFileName";

    private static final String F_WIDGET_RE_XML_TAGNAME_FILEURL = "updateFileUrl";

    private static final String F_WIDGET_RE_XML_TAGNAME_FILESIZE = "fileSize";

    private static final String F_WIDGET_RE_XML_TAGNAME_VERSION = "version";

    private static final String F_WIDGET_RE_XML_TAGNAME_MYSPACESTATUS = "mySpaceStatus";

    private static final String F_WIDGET_RE_XML_TAGNAME_MYSPACEMOREAPP = "mySpaceMoreApp";

    private static final String F_WIDGET_RE_XML_TAGNAME_WIDGETSTATUS = "widgetStatus";

    private static final String F_WIDGET_RE_XML_TAGNAME_WIDGETADSTATUS = "widgetAdStatus";

    private static final String F_WIDGET_RE_XML_TAGNAME_ERRORCODE = "errorCode";

    private static final String F_WIDGET_ERRORCODE_APPID = "9998";

    // private static final String ERROR_RES_XML_TAGNAME = "errorCode";

    private static final int F_PARSE_XML_TYPE_WIDGETONEID = 0;

    private static final int F_PARSE_XML_TYPE_WIDGETID = 1;

    private static final int F_PARSE_XML_TYPE_WIDGET_UPDATE = 2;

    private static final int F_PARSE_XML_TYPE_WIDGET_REPORT = 3;

    private static final int F_PARSE_XML_TYPE_SERVER_ERROR = 4;

    // private String widgetOneVer;
    // private String screenSize;
    // private String widgetOneId;
    // private String appId;
    // private String widgetVer;
    // private String channelCode;
    // private String imei;
    // private String md5Code;
    // private String widgetId;

    /**
     * WidgetOne 注册
     *
     * @param widgetOneVer widgetOne 版本号
     * @param screenSize   手机屏幕大小（320×480）
     */
    public static String widgetOneRegist(Context context, String widgetOneVer,
                                         String screenSize, String imei) {
        String url = F_WIDGETONE_REGIST_URL + "?ver=" + widgetOneVer
                + "&screenSize=" + screenSize + "&imei=" + imei;

        ReData reData = getHttpReData(context, url,
                F_PARSE_XML_TYPE_WIDGETONEID);
        if (reData != null) {
            return reData.widgetOneId;
        }
        return null;

    }

    /**
     * Widget 注册
     *
     * @param widgetOneId 手机端WidgetOne系统的唯一标识
     * @param appId       应用程序标识
     * @param ver         Widget版本号（String类型）
     * @param channelCode 渠道号
     * @param imei        手机IMEI号码
     * @param md5Code     上传参数校验码
     */
    public static String widgetRegist(Context context, String widgetOneId,
                                      String appId, String ver, String channelCode, String imei,
                                      String md5Code) {
        String url = F_WIDGET_REGIST_URL + "?widgetOneId=" + widgetOneId
                + "&appId=" + appId + "&ver=" + ver + "&channelCode="
                + channelCode + "&imei=" + imei + "&md5Code=" + md5Code;

        ReData reData = getHttpReData(context, url, F_PARSE_XML_TYPE_WIDGETID);
        if (reData != null) {
            return reData.widgetId;
        }
        return null;
    }

    /**
     * Widget 更新
     *
     * @param widgetId 应用程序标识
     * @param ver      版本号（String类型）
     */
    public static ReData getUpdate(Context context, String updateurl,
                                   String appId, String ver) {
        String url = null;

        if (updateurl.indexOf("?") == -1) {
            url = updateurl + "?appId=" + appId + "&ver=" + ver + "&platform=1";
        } else {
            url = updateurl + "&appId=" + appId + "&ver=" + ver + "&platform=1";
        }

        ReData reData = getHttpReData(context, url,
                F_PARSE_XML_TYPE_WIDGET_UPDATE);

        return reData;
    }

    /**
     * Widget 上报
     *
     * @param widgetId 应用程序标识
     */
    public static ReData widgetReport(Context context, String widgetId) {
        String url = F_WIDGET_REPORT_URL + "?widgetId=" + widgetId;

        return getHttpReData(context, url, F_PARSE_XML_TYPE_WIDGET_REPORT);
    }

    private static ReData getHttpReData(Context context, String httpUrl,
                                        int type) {
        URL url = null;
        HttpURLConnection httpconn = null;
        InputStream is = null;
        try {
            url = new URL(httpUrl);
            httpconn = (HttpURLConnection) url.openConnection();
            httpconn.setConnectTimeout(90000);
            httpconn.setReadTimeout(100000);
            int responseCode = httpconn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = httpconn.getInputStream();
                if (type == -1) {
                    return null;
                }
                // int size = httpconn.getContentLength();
                // if(size == 0 || size == -1){
                // return new ReData();
                // }
                return getHttpDataOfXML(context, is, type);
            } else if (responseCode == 400) {
                is = httpconn.getErrorStream();
                return getHttpDataOfXML(context, is,
                        F_PARSE_XML_TYPE_SERVER_ERROR);
            }

        } catch (Exception e) {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
            e.printStackTrace();
        } finally {
            if (httpconn != null) {
                httpconn.disconnect();
                httpconn = null;
            }
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        }
        return null;
    }

    private static ReData getHttpDataOfXML(Context context, InputStream is,
                                           int type) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(is, "utf-8");
            ReData reData = null;
            int tokenType = 0;
            while (true) {
                tokenType = parser.next();
                switch (tokenType) {
                    case XmlPullParser.START_TAG:
                        switch (type) {
                            case F_PARSE_XML_TYPE_WIDGETONEID:
                                if (F_WIDGETONE_RE_XML_TAGNAME_WIDGETONEID
                                        .equals(parser.getName())) {
                                    reData = new ReData();
                                    reData.widgetOneId = parser.nextText();
                                    // return reData;
                                }
                                break;
                            case F_PARSE_XML_TYPE_WIDGETID:
                                if (F_WIDGET_RE_XML_TAGNAME_WIDGETID.equals(parser
                                        .getName())) {
                                    reData = new ReData();
                                    reData.widgetId = parser.nextText();
                                    // return reData;
                                }
                                break;
                            case F_PARSE_XML_TYPE_WIDGET_UPDATE:

                                if (F_WIDGET_RE_XML_TAGNAME_FILENAME.equals(parser
                                        .getName())) {
                                    reData = new ReData();
                                    reData.fileName = parser.nextText();
                                } else if (F_WIDGET_RE_XML_TAGNAME_FILEURL
                                        .equals(parser.getName())) {
                                    reData.fileUrl = parser.nextText();
                                } else if (F_WIDGET_RE_XML_TAGNAME_FILESIZE
                                        .equals(parser.getName())) {
                                    String text = parser.nextText();
                                    if (text != null && text.length() > 0) {
                                        reData.fileSize = Integer.parseInt(text);
                                    }
                                } else if (F_WIDGET_RE_XML_TAGNAME_VERSION
                                        .equals(parser.getName())) {
                                    reData.version = parser.nextText();
                                    return reData;
                                }
                                break;
                            case F_PARSE_XML_TYPE_WIDGET_REPORT:
                                if (reData == null) {
                                    reData = new ReData();
                                }
                                if (F_WIDGET_RE_XML_TAGNAME_MYSPACESTATUS.equals(parser
                                        .getName())) {
                                    String value = parser.nextText();
                                    if ("000".equals(value)) {
                                        reData.mySpaceStatus = WWidgetData.F_SPACESTATUS_CLOSE;
                                    } else {
                                        reData.mySpaceStatus = WWidgetData.F_SPACESTATUS_OPEN;
                                    }

                                } else if (F_WIDGET_RE_XML_TAGNAME_WIDGETSTATUS
                                        .equals(parser.getName())) {
                                    String value = parser.nextText();
                                    if ("000".equals(value)) {
                                        showDialog(
                                                context,
                                                ResoureFinder.getInstance().getString(
                                                        context, "exit_message_server"));

                                    }
                                } else if (F_WIDGET_RE_XML_TAGNAME_MYSPACEMOREAPP
                                        .equals(parser.getName())) {
                                    String value = parser.nextText();
                                    if ("000".equals(value)) {
                                        reData.mySpaceMoreApp = WWidgetData.F_MYSPACEMOREAPP_CLOSE;
                                    } else {
                                        reData.mySpaceMoreApp = WWidgetData.F_MYSPACEMOREAPP_OPEN;
                                    }

                                } else if (F_WIDGET_RE_XML_TAGNAME_WIDGETADSTATUS
                                        .equals(parser.getName())) {
                                    String value = parser.nextText();
                                    if ("000".equals(value)) {
                                        reData.widgetAdStatus = WWidgetData.F_WIDGETADSTATUS_CLOSE;
                                    } else {
                                        reData.widgetAdStatus = WWidgetData.F_WIDGETADSTATUS_OPEN;
                                    }
                                }

                                break;
                            case F_PARSE_XML_TYPE_SERVER_ERROR:
                                if (F_WIDGET_RE_XML_TAGNAME_ERRORCODE.equals(parser
                                        .getName())) {
                                    String value = parser.nextText();
                                    if (F_WIDGET_ERRORCODE_APPID.equals(value)) {
                                        showDialog(
                                                context,
                                                ResoureFinder.getInstance().getString(
                                                        context, "exit_message_appid"));
                                        if (reData == null) {
                                            reData = new ReData();
                                        }
                                        reData.widgetId = "-1";
                                    }
                                }
                                break;
                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        return reData;

                }

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            if (F_PARSE_XML_TYPE_WIDGET_UPDATE == type) {
                return new ReData();
            }
            e.printStackTrace();

        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        }
        return null;
    }

    private static void showDialog(final Context context, final String message) {
        if (ESystemInfo.getIntence().mIsDevelop) {
            return;
        }
        if (context instanceof Activity) {
            Activity uiThread = (Activity) context;
            Runnable showDialog = new Runnable() {
                @Override
                public void run() {
                    Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(
                                    ResoureFinder.getInstance().getString(
                                            context, "confirm"),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            Process.killProcess(Process.myUid());
                                        }
                                    }).show();
                }
            };
            uiThread.runOnUiThread(showDialog);
        }
    }

    // public static class ReData {
    // public String widgetOneId;
    // public String widgetId;
    // public String fileName;
    // public String fileUrl;
    // public int fileSize;
    // public String version;
    // public int mySpaceStatus;
    // public int mySpaceMoreApp;
    // public int widgetAdStatus;
    // // public String widgetStatus;
    // // public int errorCode;
    // }
}
