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

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.vo.NameValuePairVO;
import org.zywx.wbpalmstar.base.vo.PushDeviceBindVO;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.certificates.Http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class PushReportHttpClient {

    public static String newPushOpenByPostData(String url, Context mCtx, String tenantId, String softToken) {
        PushReportUtility.log(url);
        PushReportUtility.log("softToken ==" + softToken);

        HttpURLConnection conn = null;
        String response = null;
        try {
            if (url.startsWith("https://")){
                conn=Http.getHttpsURLConnection(url);
            }else{
                conn = (HttpURLConnection) new URL(url).openConnection();
            }
            conn.setRequestMethod("POST");
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(60 * 1000);
            conn.setRequestProperty("Accept", "*/*");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            SharedPreferences preferences = mCtx.getSharedPreferences(
                    "app", Context.MODE_PRIVATE);
            String appid = preferences.getString("appid", null);
            if (!TextUtils.isEmpty(tenantId)) {
                appid = tenantId + ":" + appid;
            }
            String appkey = EUExUtil.getString("appkey");
            appkey = BUtility.decodeStr(appkey);
            PushReportUtility.log("appid ==" + appid + " appkey ==" + appkey);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-mas-app-id", appid);
            conn.setRequestProperty(PushReportUtility.KEY_APPVERIFY,
                    BUtility.getAppVerifyValue(appid, appkey,
                            System.currentTimeMillis()));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("count", 1);
            jsonObject.put("tenantMark", BUtility.getTenantAccount(mCtx));
            jsonObject.put("softToken", softToken);

            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();
            outputStream.close();
            conn.connect();
            int responseCode = conn.getResponseCode();
            PushReportUtility.log("responseCode ==" + responseCode);

            if (responseCode == 200) {

                InputStream is = null;
                try {
                    is = conn.getInputStream();
                    int ch;
                    StringBuilder sb = new StringBuilder();
                    while ((ch = is.read()) != -1) {
                        sb.append((char) ch);
                    }
                    response = sb.toString();
                    is.close();
                } catch (Exception e) {
                    if (BDebug.DEBUG) {
                        e.printStackTrace();
                    }
                }
                return response;
            }

        } catch (Exception e) {
            PushReportUtility.oe("newPushOpenByNameValuePair: " + url, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public static String bindOrUnbindDeviceInfo(String url, PushDeviceBindVO pushDeviceBind, Context mCtx) {
        PushReportUtility.log(url);
        HttpURLConnection conn = null;
        String response = null;
        try {
            if (url.startsWith("https://")) {
                conn = Http.getHttpsURLConnection(url);
            } else {
                conn = (HttpURLConnection) new URL(url).openConnection();
            }
            conn.setRequestMethod("POST");
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(60 * 1000);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
            conn.setRequestProperty("x-push-verify-key", "push");
            conn.setRequestProperty("x-push-verify-id", "push");
            SharedPreferences preferences = mCtx.getSharedPreferences(
                    "app", Context.MODE_PRIVATE);
            String appid = preferences.getString("appid", null);
            String appkey = EUExUtil.getString("appkey");
            appkey = BUtility.decodeStr(appkey);
            BDebug.d("appid ==" + appid + " appkey ==" + appkey);
            conn.setRequestProperty("x-mas-app-id", appid);
            conn.setRequestProperty("x-mas-verify",
                    BUtility.getAppVerifyValue(appid, appkey,
                            System.currentTimeMillis()));
            conn.setDoInput(true);
            conn.setDoOutput(true);

            String param = DataHelper.gson.toJson(pushDeviceBind);
            BDebug.d(param);
            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(param);
            writer.flush();
            writer.close();
            outputStream.close();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {

                InputStream is = null;
                try {
                    is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    response = sb.toString();
                    is.close();
                } catch (Exception e) {
                    if (BDebug.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
            PushReportUtility.log("responseCode ==" + responseCode);
            BDebug.d("response ==" + response);
            return response;

        } catch (Exception e) {
            if (BDebug.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public static String sendPostDataByNameValuePair(String url,
                                                     List<NameValuePairVO> nameValuePairs, Context mCtx) {
        PushReportUtility.log(url);
        HttpURLConnection conn = null;
        String response = null;
        try {
            if (url.startsWith("https://")){
                conn=Http.getHttpsURLConnection(url);
            }else{
                conn = (HttpURLConnection) new URL(url).openConnection();
            }
            conn.setRequestMethod("POST");
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(60 * 1000);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("charset","utf-8");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            for (NameValuePairVO nameValuePair : nameValuePairs) {
                PushReportUtility.log(nameValuePair.getName() + "="
                        + nameValuePair.getValue());
            }
            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(getQuery(nameValuePairs));
            writer.flush();
            writer.close();
            outputStream.close();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {

                InputStream is = null;
                try {
                    is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    int ch;
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    response = sb.toString();
                    is.close();
                } catch (Exception e) {
                    if (BDebug.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
            PushReportUtility.log("responseCode ==" + responseCode);
            PushReportUtility.log("response ==" + response);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            PushReportUtility.oe("sendPostDataByNameValuePair: " + url, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public static String getGetData(String url, Context mCtx) {
        PushReportUtility.log(url);
        HttpURLConnection urlConnection = null;
        try {
            if (url.startsWith("https://")){
                urlConnection=Http.getHttpsURLConnection(url);
            }else{
                urlConnection = (HttpURLConnection) new URL(url).openConnection();
            }
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(60 * 1000);
            urlConnection.setConnectTimeout(60 * 1000);
            urlConnection.setRequestProperty("Accept", "*/*");

            Map<String, List<String>> headers = urlConnection.getRequestProperties();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                PushReportUtility.log(header.getKey() + "="
                        + header.getValue());
            }

            int responseCode = urlConnection.getResponseCode();
            BDebug.d("debug", "responseCode == ", responseCode);
            PushReportUtility.log("responseCode = " + responseCode);
            if (urlConnection.getResponseCode() == 200) {
                byte[] resultBytes = read(urlConnection.getInputStream());
                String resultStr = new String(resultBytes, "UTF-8");
                PushReportUtility.log("res = " + resultStr);
                return resultStr;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    //从流中读取数据
    public static byte[] read(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8 * 1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }


    private static String getQuery(List<NameValuePairVO> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePairVO pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
