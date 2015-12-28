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

package org.zywx.wbpalmstar.platform.certificates;

import android.content.Context;
import android.content.res.AssetManager;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;

public class Http {

    public static HashMap<String, KeyStore> KEY_STORE = new HashMap<String, KeyStore>();
    public static String algorithm = "X509";
    public static String keyType = "pkcs12";

    public static HttpClient getHttpsClient(int mTimeOut) {
        try {
            KeyStore trustStore = KeyStore.getInstance(keyType);
            trustStore.load(null, null);
            SSLSocketFactory socketFact = new HSSLSocketFactory(trustStore, null);
            socketFact.setHostnameVerifier(new HX509HostnameVerifier());
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", socketFact, 443));

            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, mTimeOut);
            HttpConnectionParams.setSoTimeout(params, mTimeOut);
            HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
            HttpClientParams.setRedirecting(params, true);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HttpClient getHttpsClientWithCert(String cPassWord, String cPath, int mTimeOut, Context ctx) {
        InputStream inStream = null;
        try {
//			cPath = "file:///android_asset/widget/wgtRes/client.p12"; 
//			cPassWord ="q1w2e3r4";
            int index = cPath.lastIndexOf('/');
            String keyName = cPath.substring(index);
            KeyStore ksP12 = KEY_STORE.get(keyName);
            if (null == ksP12) {
                String assertFile = "file:///android_asset/";
                String sdcardFile = "file:///sdcard";
                if (cPath.startsWith(assertFile)) {
                    cPath = cPath.substring(assertFile.length());
                    AssetManager asset = ctx.getAssets();
                    inStream = asset.open(cPath);
                } else if (cPath.startsWith(sdcardFile)) {
                    cPath = cPath.substring("file://".length());
                    inStream = new FileInputStream(cPath);
                } else {
                    inStream = new FileInputStream(cPath);
                }
                ksP12 = KeyStore.getInstance(keyType);
                ksP12.load(inStream, cPassWord.toCharArray());
                KEY_STORE.put(keyName, ksP12);
            }
            SSLSocketFactory socketFact = new HSSLSocketFactory(ksP12, cPassWord);
            socketFact.setHostnameVerifier(new HX509HostnameVerifier());
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", socketFact, 443));

            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, mTimeOut);
            HttpConnectionParams.setSoTimeout(params, mTimeOut);
            HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
            HttpClientParams.setRedirecting(params, true);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != inStream) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static HttpClient getHttpClient(int mTimeOut) {
        BasicHttpParams bparams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(bparams, mTimeOut);
        HttpConnectionParams.setSoTimeout(bparams, mTimeOut);
        HttpConnectionParams.setSocketBufferSize(bparams, 8 * 1024);
        HttpClientParams.setRedirecting(bparams, true);
        return new DefaultHttpClient(bparams);
    }

    public static HNetSSLSocketFactory getSSLSocketFactoryWithCert(String cPassWord, String cPath, Context ctx) {
        InputStream inStream = null;
        HNetSSLSocketFactory ssSocketFactory = null;
        try {
            int index = cPath.lastIndexOf('/');
            String keyName = cPath.substring(index);
            KeyStore ksP12 = KEY_STORE.get(keyName);
            if (null == ksP12) {
                String assertFile = "file:///android_asset/";
                String sdcardFile = "/sdcard/";
                String wgtFile = "widget/";
                String file = "file://";
                if (cPath.contains(assertFile)) {
                    cPath = cPath.substring(assertFile.length());
                    AssetManager asset = ctx.getAssets();
                    inStream = asset.open(cPath);
                } else if (cPath.contains(sdcardFile)) {
                    if (cPath.contains(file)) {
                        cPath = cPath.substring("file://".length());
                    }
                    inStream = new FileInputStream(cPath);
                } else if (cPath.startsWith(wgtFile)) {
                    AssetManager asset = ctx.getAssets();
                    inStream = asset.open(cPath);
                } else {
                    inStream = new FileInputStream(cPath);
                }
                ksP12 = KeyStore.getInstance("pkcs12");
                ksP12.load(inStream, cPassWord.toCharArray());
                KEY_STORE.put(keyName, ksP12);
            }
            ssSocketFactory = new HNetSSLSocketFactory(ksP12, cPassWord);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssSocketFactory;
    }
}
