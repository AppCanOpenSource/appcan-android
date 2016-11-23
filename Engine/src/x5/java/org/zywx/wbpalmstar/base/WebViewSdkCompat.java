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

        final CountDownLatch countDownLatch = new CountDownLatch(1);

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

        int mTbsVersion = QbSdk.getTbsVersion(context);
        if (noTencentX5 || (mTbsVersion > 0 && mTbsVersion < 30000)) {
            BDebug.i("AppCanTBS", "QbSdk.forceSysWebView()");
            QbSdk.forceSysWebView();
        }

        //preinit只需要调用一次，如果已经完成了初始化，那么就直接构造view
        if (!QbSdk.isTbsCoreInited() && (mTbsVersion == 0 || mTbsVersion >= 30000) && !noTencentX5) {
            final long timerCounter = System.currentTimeMillis();
            QbSdk.preInit(context, new QbSdk.PreInitCallback() {

                @Override
                public void onViewInitFinished() {
                    float deltaTime = (System.currentTimeMillis() - timerCounter);
                    BDebug.i("AppCanTBS", "x5初始化使用了" + deltaTime + "毫秒");
                    countDownLatch.countDown();
                }

                @Override
                public void onCoreInitFinished() {
                    BDebug.i("AppCanTBS", "onX5CoreInitFinished!!!!");
                }
            });//设置X5初始化完成的回调接口  第三个参数为true：如果首次加载失败则继续尝试加载；

            try {
                countDownLatch.await(2, TimeUnit.SECONDS);//最多等待两秒
            } catch (InterruptedException e) {
                if (BDebug.DEBUG) {
                    e.printStackTrace();
                }
            }
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
