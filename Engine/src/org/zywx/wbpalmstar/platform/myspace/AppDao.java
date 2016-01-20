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

package org.zywx.wbpalmstar.platform.myspace;

import android.content.Context;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DownloadData;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.InstallInfo;
import org.zywx.wbpalmstar.platform.myspace.UserInfo.LoginInfo;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AppDao {

    private JsonUtil jsonParser;

    public AppDao(Context context) {
        jsonParser = new JsonUtil();
    }

    /**
     * 获取推荐的应用列表
     *
     * @param appId
     * @param platformId
     * @param reTryTimes
     * @return
     */
    public ArrayList<DownloadData> requestRecommendAppsList(String appId, String platformId, int reTryTimes) {
        if (appId == null || platformId == null) {
            return null;
        }
        final StringBuffer sb = new StringBuffer(CommonUtility.URL_RECOMMEND_APP_LIST);
        sb.append("portalAppId=").append(appId);
        sb.append("&platFormId=").append(platformId);
        byte[] data = null;
        for (int i = 0; i < reTryTimes; i++) {
            data = CommonUtility.requestData(sb.toString());
            if (data != null) {
                break;
            }
        }
        if (data == null) {
            return null;
        }
        return jsonParser.parseRecommendAppsList(new String(data));
    }

    /**
     * 获取服务器上我的应用的列表
     *
     * @param platformId
     * @param appId
     * @param sessionKey
     * @param reTryTimes
     * @return
     */
    public ArrayList<DownloadData> requestMyAppsList(String platformId, String appId, String sessionKey, int reTryTimes) {
        if (appId == null || platformId == null && sessionKey == null) {
            return null;
        }
        final StringBuffer sb = new StringBuffer(CommonUtility.URL_GET_MYAPPS_LIST);
        sb.append("platFormId=").append(platformId);
        sb.append("&portalAppId=").append(appId);
        sb.append("&txSessionKey=").append(sessionKey);
        byte[] data = null;
        for (int i = 0; i < reTryTimes; i++) {
            data = CommonUtility.requestData(sb.toString());
            if (data != null) {
                break;
            }
        }
        if (data == null) {
            return null;
        }
        return jsonParser.parseMyAppsList(new String(data));
    }

    public String getSessionKey(String appId, int reTryTimes) {
        if (appId == null) {
            return null;
        }
        final StringBuffer sb = new StringBuffer(CommonUtility.URL_GET_SESSION_KEY);
        sb.append("appId=").append(appId);
        byte[] data = null;
        for (int i = 0; i < reTryTimes; i++) {
            data = CommonUtility.requestData(sb.toString());
            if (data != null) {
                break;
            }
        }
        if (data == null) {
            return null;
        }
        return jsonParser.parseSessionKey(new String(data));
    }

    public LoginInfo getLoginInfo(String json) {
        return jsonParser.parseLoginInfo(json);
    }

    /**
     * 上报Widget安装事件
     *
     * @param sessionKey
     * @param appId      主Widget ID
     * @param softwareId 被安装的widget softwareId
     * @param platformId 平台ID
     * @return
     */
    public boolean reportInstallWidget(String sessionKey, String appId, String softwareId, String platformId) {
        if (sessionKey == null || appId == null || softwareId == null || platformId == null) {
            return false;
        }
        final StringBuffer sb = new StringBuffer(CommonUtility.URL_REPORT_INSTALL_WIDGET);
        sb.append("txSessionKey=").append(sessionKey);
        sb.append("&portalAppId=").append(appId);
        sb.append("&intallAppId=").append(softwareId);
        sb.append("&platFormId=").append(platformId);
        boolean isReportSuc = false;
        for (int i = 0; i < 3; i++) {
            if (CommonUtility.getRequestResult(sb.toString())) {
                isReportSuc = true;
                break;
            }
        }
        return isReportSuc;
    }

    /**
     * 上报widget删除事件
     *
     * @param sessionKey
     * @param appId      主Widget ID
     * @param softwareId 被删除的widget softwareId
     * @return
     */
    public boolean reportUnistallWidget(String sessionKey, String appId, String softwareId, String platFormId) {
        if (sessionKey == null || appId == null || softwareId == null) {
            return false;
        }
        final StringBuffer sb = new StringBuffer(CommonUtility.URL_REPORT_UNISTALL_WIDGET);
        sb.append("txSessionKey=").append(sessionKey);
        sb.append("&portalAppId=").append(appId);
        sb.append("&intallAppId=").append(softwareId);
        sb.append("&platFormId=").append(platFormId);
        boolean isReportSuc = false;
        for (int i = 0; i < 3; i++) {
            if (CommonUtility.getRequestResult(sb.toString())) {
                isReportSuc = true;
                break;
            }
        }
        return isReportSuc;
    }

    /**
     * widget启动上报
     *
     * @param sessionKey
     * @param softwareId 应用的softwareId
     * @param beFirst
     * @return
     */
    public boolean reportStartWidget(String sessionKey, String softwareId) {
        if (sessionKey == null || softwareId == null) {
            return false;
        }
        final StringBuffer sb = new StringBuffer(CommonUtility.URL_REPORT_START_WIDGET);
        sb.append("txSessionKey=").append(sessionKey);
        sb.append("&startId=").append(softwareId);
        boolean isReportSuc = false;
        for (int i = 0; i < 3; i++) {
            if (CommonUtility.getRequestResult(sb.toString())) {
                isReportSuc = true;
                break;
            }
        }
        return isReportSuc;
    }

    /**
     * 同步用户本地与服务器上的应用列表 本地列表与服务器列表进行appId比对，
     *
     * @param serverList
     * @param localList
     * @return
     */
    public ArrayList<InstallInfo> syncUserAppsList(ArrayList<DownloadData> serverList, ArrayList<InstallInfo> localList) {
        if (serverList == null) {
            return localList;
        }
        ArrayList<InstallInfo> syncList = new ArrayList<InstallInfo>();
        for (int i = 0, size = serverList.size(); i < size; i++) {
            InstallInfo syncInfo = new InstallInfo();
            syncInfo.setDownloadInfo(serverList.get(i));
            if (localList != null) {
                inner:
                for (int m = 0, length = localList.size(); m < length; m++) {
                    final InstallInfo localInfo = localList.get(m);
                    if (localInfo.getDownloadInfo().appId.equals(syncInfo.getDownloadInfo().appId)) {
                        syncInfo.isDownload = true;
                        syncInfo.installPath = localInfo.installPath;
                        break inner;
                    }
                }
            }
            syncList.add(syncInfo);
        }
        return syncList;
    }

    public WWidgetData getWidgetDataByInstallPath(String installPath) {
        WWidgetData widgetData = null;
        FileInputStream fis = null;
        try {
            File configFile = new File(new File(installPath), "config.xml");
            fis = new FileInputStream(configFile);
            widgetData = WDataManager.getWidgetDataOfXML(fis);
            if (!BUtility.uriHasSchema(widgetData.m_indexUrl)) {
                if (widgetData.m_indexUrl.equals("#") || widgetData.m_indexUrl.equals("")) {
                    widgetData.m_indexUrl = "index.html";
                }
                widgetData.m_indexUrl = "file://" + installPath + widgetData.m_indexUrl;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return widgetData;
    }

    public DownloadData getWebAppDownloadInfo(String downloadInfo) {
        return jsonParser.parseWebAppDownloadInfo(downloadInfo);
    }


}
