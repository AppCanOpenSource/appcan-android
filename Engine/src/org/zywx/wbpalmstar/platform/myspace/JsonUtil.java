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

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayInstallInfo;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayStartInfo;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayUninstallInfo;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DownloadData;
import org.zywx.wbpalmstar.platform.myspace.UserInfo.LoginInfo;

import java.util.ArrayList;

public class JsonUtil {

    private static final String TAG = "JsonParser";

    /**
     * 解析推荐的应用列表的下载信息
     *
     * @param data
     * @return
     */
    public ArrayList<DownloadData> parseRecommendAppsList(String data) {
        BDebug.i(TAG, "parseRecommendAppsList: " + data);
        if (data == null) {
            return null;
        }
        ArrayList<DownloadData> arrayList = null;
        try {
            JSONObject dataJson = new JSONObject(data);
            JSONArray arrayJson = dataJson.getJSONArray("recommendAppList");
            arrayList = new ArrayList<DownloadData>();
            for (int i = 0, length = arrayJson.length(); i < length; i++) {
                JSONObject jsonItem = arrayJson.optJSONObject(i);
                DownloadData downloadData = new DownloadData();
                downloadData.softwareId = jsonItem.getString("id");
                downloadData.appId = jsonItem.getString("appId");
                downloadData.downloadUrl = jsonItem.getString("downloadUrl");
                downloadData.appName = jsonItem.getString("name");
                downloadData.appSize = jsonItem.getString("size");
                downloadData.iconLoc = jsonItem.getString("iconLoc");
                downloadData.mode = CommonUtility.ParseInt(jsonItem.getString("createMethod"));
                BDebug.d(TAG, "parseRecommendAppsList: " + downloadData.toString());
                arrayList.add(downloadData);
            }
        } catch (JSONException e) {
            BDebug.d(TAG, "parseRecommendAppsList(): " + e.getMessage());
            e.printStackTrace();
        }
        return arrayList;
    }

    /**
     * 解析我的已下载的应用列表
     *
     * @param data
     * @return
     */
    public ArrayList<DownloadData> parseMyAppsList(String data) {
        BDebug.i(TAG, "parseMyAppsList: " + data);
        if (data == null || data.length() == 0) {
            return null;
        }
        ArrayList<DownloadData> arrayList = new ArrayList<AppInfo.DownloadData>();
        try {
            JSONObject dataJson = new JSONObject(data);
            JSONArray arrayJson = dataJson.getJSONArray("myAppList");
            if (arrayJson == null || arrayJson.length() == 0) {
                return arrayList;
            }
            for (int i = 0, length = arrayJson.length(); i < length; i++) {
                JSONObject jsonItem = arrayJson.optJSONObject(i);
                DownloadData downloadInfo = new DownloadData();
                downloadInfo.softwareId = jsonItem.getString("id");
                downloadInfo.appId = jsonItem.getString("appId");
                downloadInfo.appName = jsonItem.getString("name");
                downloadInfo.appSize = jsonItem.getString("size");
                downloadInfo.downloadUrl = jsonItem.getString("downloadUrl");
                downloadInfo.iconLoc = jsonItem.getString("iconLoc");
                downloadInfo.mode = CommonUtility.ParseInt(jsonItem.getString("createMethod"));
                arrayList.add(downloadInfo);
                Log.i(TAG, "parseMyAppsList: " + downloadInfo.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public DownloadData parseWebAppDownloadInfo(String json) {
        BDebug.i(TAG, "parseWebAppDownloadInfo: " + json);
        DownloadData downloadInfo = null;
        try {
            JSONObject dataJson = new JSONObject(json);
            downloadInfo = new DownloadData();
            downloadInfo.softwareId = dataJson.getString("id");
            downloadInfo.appId = dataJson.getString("appId");
            downloadInfo.appName = dataJson.getString("name");
            downloadInfo.appSize = dataJson.getString("size");
            downloadInfo.downloadUrl = dataJson.getString("downloadUrl");
            downloadInfo.iconLoc = dataJson.getString("iconLoc");
            downloadInfo.mode = CommonUtility.ParseInt(dataJson.getString("createMethod"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return downloadInfo;
    }

    public String parseSessionKey(String data) {
        BDebug.i(TAG, "parseSessionKey:" + data);
        String sessionKey = null;
        try {
            JSONObject dataJson = new JSONObject(data);
            sessionKey = dataJson.getString("txSessionKey");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sessionKey;
    }

    public LoginInfo parseLoginInfo(String json) {
        BDebug.i(TAG, json);
        if (json == null) {
            return null;
        }
        LoginInfo loginInfo = null;
        try {
            JSONObject dataJson = new JSONObject(json);
            loginInfo = new LoginInfo();
            loginInfo.userId = dataJson.getString("uid");
            loginInfo.fromDomain = dataJson.getString("fromDomain");
        } catch (JSONException e) {
            BDebug.e(TAG, e.getMessage());
        } finally {
            if (loginInfo == null || loginInfo.userId == null || loginInfo.fromDomain == null) {
                loginInfo = null;
            }
        }
        return loginInfo;
    }

    /**
     * 组合启动上报数据
     *
     * @param infos
     * @return
     */
    public String combineDelayStartReportInfo(ArrayList<DelayStartInfo> infos) {
        JSONArray array = new JSONArray();
        for (DelayStartInfo info : infos) {
            JSONObject object = new JSONObject();
            try {
                object.put("txSessionKey", info.sessionKey);
                object.put("startId", info.softwareId);
                object.put("reportTime", info.reportTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(object);
        }
        return array.toString();
    }

    /**
     * 组合安装延迟上报数据
     *
     * @param infos
     * @return
     */
    public String combineDelayInstallReportInfo(ArrayList<DelayInstallInfo> infos) {
        JSONArray array = new JSONArray();
        for (DelayInstallInfo info : infos) {
            JSONObject object = new JSONObject();
            try {
                object.put("txSessionKey", info.sessionKey);
                object.put("portalAppId", info.mainAppId);
                object.put("intallAppId", info.softwareId);
                object.put("platFormId", info.platformId);
                object.put("reportTime", info.reportTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(object);
        }
        return array.toString();
    }

    /**
     * 组合卸载延迟上报数据
     *
     * @param infos
     * @return
     */
    public String combineDelayUninstallReportInfo(ArrayList<DelayUninstallInfo> infos) {
        JSONArray array = new JSONArray();
        for (DelayUninstallInfo info : infos) {
            JSONObject object = new JSONObject();
            try {
                object.put("txSessionKey", info.sessionKey);
                object.put("portalAppId", info.mainAppId);
                object.put("intallAppId", info.softwareId);
                object.put("platFormId", info.platformId);
                object.put("reportTime", info.reportTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(object);
        }
        return array.toString();
    }

}
