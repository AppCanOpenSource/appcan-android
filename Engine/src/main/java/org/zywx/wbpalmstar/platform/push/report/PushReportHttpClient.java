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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.certificates.Http;

import java.util.ArrayList;
import java.util.List;

public class PushReportHttpClient {
    // static HttpClient httpClient = null;
    // static{
    // httpClient = getSSLHttpClient();
    // }
    private static boolean mIsCertificate = false;
    private static String mPassWord = null;
    private static String mPath = null;
    // private static HttpClient httpClient = null;
    private static List<HttpClient> httpClients = new ArrayList<HttpClient>();

    public static String newPushOpenByPostData(String url, Context mCtx, String tenantId, String softToken) {
        PushReportUtility.log(url);
        PushReportUtility.log("softToken ==" + softToken);
        HttpPost post = new HttpPost(url);
        HttpClient httpClient = getSSLHttpClient(mCtx);
        HttpResponse httpResponse = null;
        try {
            SharedPreferences preferences = mCtx.getSharedPreferences(
                    "app", Context.MODE_PRIVATE);
            String appid = preferences.getString("appid", null);
            if (!TextUtils.isEmpty(tenantId)) {
                appid = tenantId + ":" + appid;
            }
            String appkey = EUExUtil.getString("appkey");
            appkey = PushReportUtility.decodeStr(appkey);
            PushReportUtility.log("appid ==" + appid + " appkey ==" + appkey);
            post.addHeader("Accept", "*/*");
            post.addHeader("Content-Type", "application/json");
            post.addHeader("x-mas-app-id", appid);
            post.addHeader(PushReportUtility.KEY_APPVERIFY,
                    PushReportUtility.getAppVerifyValue(appid, appkey,
                            System.currentTimeMillis()));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("count", 1);
            jsonObject.put("softToken", softToken);
            StringEntity stringEntity = new StringEntity(jsonObject.toString());
            post.setEntity(stringEntity);

            // 取得HTTP response
            httpResponse = httpClient.execute(post);
            // 若状态码为200 ok
            int responesCode = httpResponse.getStatusLine().getStatusCode();
            PushReportUtility.log("responesCode ==" + responesCode);
            if (responesCode == 200) {
                // 取出回应字串
                String res = EntityUtils.toString(httpResponse.getEntity());
                return res;
            }
        } catch (Exception e) {
            PushReportUtility.oe("newPushOpenByNameValuePair: " + url, e);
        } finally {
            if (post != null) {
                post.abort();
                post = null;
            }
            if (httpResponse != null) {
                httpResponse = null;
            }
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
                httpClient = null;
            }
        }
        return null;
    }

    public static String sendPostDataByNameValuePair(String url,
                                                     List<NameValuePair> nameValuePairs, Context mCtx) {
        PushReportUtility.log(url);
        HttpPost post = new HttpPost(url);
        HttpClient httpClient = getSSLHttpClient(mCtx);
        // Post运作传送变数必须用NameValuePair[]阵列储存
        // 传参数服务端获取的方法为request.getParameter("name")
        // List<NameValuePair> params = new ArrayList<NameValuePair>();
        // params.add(new BasicNameValuePair("name", data));
        // post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        HttpResponse httpResponse = null;
        // HttpClient httpClient = null;
        post.setHeader("Accept", "*/*");
        try {

            for (NameValuePair nameValuePair : nameValuePairs) {
                PushReportUtility.log(nameValuePair.getName() + "="
                        + nameValuePair.getValue());
            }
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            // 取得HTTP response
            // httpClient = getSSLHttpClient();
            Header[] Headers = post.getAllHeaders();
            for (Header header : Headers) {
                PushReportUtility.log(header.getName() + "="
                        + header.getValue());
            }
            httpResponse = httpClient.execute(post);
            // 若状态码为200 ok
            int responesCode = httpResponse.getStatusLine().getStatusCode();
            BDebug.d("debug", "responesCode == " + responesCode);
            PushReportUtility.log("responesCode ==" + responesCode);
            if (responesCode == 200) {
                // 取出回应字串
                String res = EntityUtils.toString(httpResponse.getEntity());
                PushReportUtility.log("res ==" + res);
                return res;
            }

        } catch (Exception e) {
            PushReportUtility.oe("sendPostDataByNameValuePair: " + url, e);
        } finally {
            if (post != null) {
                post.abort();
                post = null;
            }
            if (httpResponse != null) {
                httpResponse = null;
            }
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
                httpClient = null;
            }
        }
        return null;
    }

    public static String getGetData(String url, Context mCtx) {
        PushReportUtility.log(url);
        HttpGet get = new HttpGet(url);
        HttpResponse httpResponse = null;
        HttpClient httpClient = getSSLHttpClient(mCtx);
        // HttpClient httpClient = null;
        get.setHeader("Accept", "*/*");
        try {
            // httpClient = new DefaultHttpClient(setRedirecting());
            Header[] Headers = get.getAllHeaders();
            for (Header header : Headers) {
                PushReportUtility.log(header.getName() + "="
                        + header.getValue());
            }
            httpResponse = httpClient.execute(get);
            int responesCode = httpResponse.getStatusLine().getStatusCode();
            BDebug.d("debug", "responesCode == " + responesCode);
            PushReportUtility.log("responesCode = " + responesCode);
            if (responesCode == 200) {
                // 取出回应字串
                String res = EntityUtils.toString(httpResponse.getEntity(),
                        HTTP.UTF_8);
                PushReportUtility.log("res = " + res);
                return res;
            }
        } catch (Exception e) {
            PushReportUtility.log("Exception ==" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (get != null) {
                get.abort();
                get = null;
            }
            if (httpResponse != null) {
                httpResponse = null;
            }
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
                httpClient = null;
            }
        }
        return null;
    }

    public static void setCertificate(boolean isCertificate, String cPassWord,
                                      String cPath, Context ctx) {
        mIsCertificate = isCertificate;
        mPassWord = cPassWord;
        mPath = cPath;
    }

    public static void close() {

        for (HttpClient httpClient : httpClients) {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
                httpClient = null;
            }
        }
        httpClients.clear();
    }

    public static HttpClient getSSLHttpClient(Context mCtx) {
        // try {
        // KeyStore trustStore = KeyStore.getInstance(KeyStore
        // .getDefaultType());
        // trustStore.load(null, null);
        // SSLSocketFactory sf = new ESSLSocketFactory(trustStore);
        // sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        // HttpParams params = new BasicHttpParams();
        // HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
        // HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
        // HttpClientParams.setRedirecting(params, true);
        // HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        //
        // SchemeRegistry registry = new SchemeRegistry();
        // registry.register(new Scheme("http", PlainSocketFactory
        // .getSocketFactory(), 80));
        // registry.register(new Scheme("https", sf, 443));
        // ClientConnectionManager ccm = new ThreadSafeClientConnManager(
        // params, registry);
        // return new DefaultHttpClient(ccm, params);
        // } catch (Exception e) {
        // e.printStackTrace();
        // return new DefaultHttpClient();
        // }
        HttpClient httpClient = null;
        if (mIsCertificate) {
            httpClient = Http.getHttpsClientWithCert(mPassWord, mPath,
                    60 * 1000, mCtx);
        } else {
            httpClient = Http.getHttpsClient(60 * 1000);
        }
        httpClients.add(httpClient);
        return httpClient;
    }

}
