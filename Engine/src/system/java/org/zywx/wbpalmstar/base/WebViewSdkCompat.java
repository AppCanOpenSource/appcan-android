package org.zywx.wbpalmstar.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;

/**
 * Created by ylt on 16/5/20.
 */
public class WebViewSdkCompat {

    private static final String TAG = "WebViewSdkCompat";
    
    public static final String type="system";

    public static void initInActivity(Activity activity){

    }

    public static void initInApplication(Context context){
        CookieSyncManager.createInstance(context);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().removeSessionCookie();
        CookieManager.getInstance().removeExpiredCookie();
    }


    public static void setCookie(String inUrl, String cookie){
        CookieManager.getInstance().setCookie(inUrl, cookie);
        if (Build.VERSION.SDK_INT < 21) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }
    }

    public static  String getCookie(String inUrl){
        return CookieManager.getInstance().getCookie(inUrl);
    }

    public static void stopSync(){
        CookieSyncManager.getInstance().stopSync();
    }

    public static void clearCookie() {
        CookieManager.getInstance().removeAllCookies(value -> {
            BDebug.d(TAG, "clearCookie finish result: " + value);
        });
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


    public interface CustomViewCallback extends WebChromeClient.CustomViewCallback{

    }


    public interface ValueCallback<T> extends android.webkit.ValueCallback <T>{

    }

    /**
     * 兼容多内核的parseResult方法
     *
     * @param resultCode
     * @param data
     * @return
     */
    public static Uri[] fileChooserParamsParseResult(int resultCode, Intent data){
        return WebChromeClient.FileChooserParams.parseResult(resultCode, data);
    }

}
