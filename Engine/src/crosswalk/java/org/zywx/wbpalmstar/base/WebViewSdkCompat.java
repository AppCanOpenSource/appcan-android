package org.zywx.wbpalmstar.base;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;

import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

/**
 * Created by ylt on 16/5/20.
 */
public class WebViewSdkCompat {

    private static XWalkCookieManager sXWalkCookieManager;

    public static XWalkCookieManager getCookieInstance(){
        if (sXWalkCookieManager==null){
            sXWalkCookieManager=new XWalkCookieManager();
        }
        sXWalkCookieManager.setAcceptCookie(true);
        sXWalkCookieManager.setAcceptFileSchemeCookies(true);
        return sXWalkCookieManager;
    }

    public static final String type="crosswalk";

    public static void initInActivity(Activity activity) {
        XWalkView xWalkView=new XWalkView(activity);
        getCookieInstance().removeExpiredCookie();
        getCookieInstance().removeSessionCookie();
    }

    public static void initInApplication(Context context) {
        XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);
        XWalkPreferences.setValue(XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true);
        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW,true);//设置TextureView为默认的渲染方式,
    }

    public static void setCookie(String inUrl, String cookie){
        getCookieInstance().setCookie(inUrl, cookie);
    }

    public static  String getCookie(String inUrl){
        return getCookieInstance().getCookie(inUrl);
    }

    public static void stopSync(){

    }

    public static void clearCookie() {
        getCookieInstance().removeAllCookie();
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


    public interface ValueCallback<Uri> extends android.webkit.ValueCallback <Uri>{

    }

}
