package org.zywx.wbpalmstar.base;

import android.app.Activity;
import android.content.Context;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebSettings;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Created by ylt on 16/5/20.
 */

public class WebViewSdkCompat {

    public static final String type = "x5";

    public static void initInActivity(Activity activity) {
    }

    public static void initInApplication(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().removeSessionCookie();
        CookieManager.getInstance().removeExpiredCookie();
        initTencentX5(context);
    }

    private static void initTencentX5(Context context) {
        int tbsVersion = 0;
        boolean noTencentX5 = false;
        try {
            String[] lists  = context.getAssets().list("widget");
            for (int i = 0; i < lists.length; i++) {
                if (lists[i].equalsIgnoreCase("notencentx5")) {
                    noTencentX5 = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化X5引擎SDK
        tbsVersion = QbSdk.getTbsVersion(context);
        if (noTencentX5 || (tbsVersion > 0 && tbsVersion < 30000)) {
            BDebug.i("AppCanTBS", "QbSdk.forceSysWebView()");
            QbSdk.forceSysWebView();
        }

        if(!QbSdk.isTbsCoreInited() && (tbsVersion == 0 || tbsVersion >= 30000) && !noTencentX5){
            final long timerCounter = System.currentTimeMillis();
            // 如果手机没有可以共享的X5内核，会先下载并安装，首次启动不会使用X5，再次启动才会使用X5；
            // 如果手机有可以共享的X5内核，但未安装，会先安装，首次启动不会使用X5，再次启动才会使用X5；
            // 如果手机有可以共享的X5内核，已经安装，首次启动会使用X5；
            QbSdk.initX5Environment(context, new QbSdk.PreInitCallback(){
                @Override
                public void onViewInitFinished(boolean success) {
                    float deltaTime = (System.currentTimeMillis() - timerCounter);
                    BDebug.i("AppCanTBS", "success " + success + " x5初始化使用了" + deltaTime + "毫秒");
                }

                @Override
                public void onCoreInitFinished() {
                    BDebug.i("AppCanTBS", "onX5CoreInitFinished!!!!");
                }
            });
        }
    }

    public static void setCookie(String inUrl, String cookie) {
        CookieManager.getInstance().setCookie(inUrl, cookie);
        CookieSyncManager.getInstance().sync();
    }
    public static void clearCookie() {
        CookieManager.getInstance().removeAllCookie();
    }
    public static String getCookie(String inUrl) {
        return CookieManager.getInstance().getCookie(inUrl);
    }


    public static void stopSync(){
        CookieSyncManager.getInstance().stopSync();
    }


    public enum ZoomDensityCompat{
        FAR(WebSettings.ZoomDensity.FAR),      // 240dpi
        MEDIUM(WebSettings.ZoomDensity.MEDIUM),    // 160dpi
        CLOSE(WebSettings.ZoomDensity.CLOSE);     // 120dpi
        ZoomDensityCompat(WebSettings.ZoomDensity size) {
            value = size;
        }

        public WebSettings.ZoomDensity getValue() {
            return value;
        }

        WebSettings.ZoomDensity value;
    }


    public interface CustomViewCallback extends IX5WebChromeClient.CustomViewCallback{

    }


    public interface ValueCallback<Uri> extends com.tencent.smtt.sdk.ValueCallback<Uri>{

    }

}
