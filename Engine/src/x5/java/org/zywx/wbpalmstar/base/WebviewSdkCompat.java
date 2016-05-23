package org.zywx.wbpalmstar.base;

import android.content.Context;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebSettings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Created by ylt on 16/5/20.
 */

public class WebViewSdkCompat {

    public static final String type = "x5";

    public static void initInLoadingActivity(Context context) {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        if (!QbSdk.isTbsCoreInited()) {//preinit只需要调用一次，如果已经完成了初始化，那么就直接构造view
            final long timerCounter = System.currentTimeMillis();
            QbSdk.preInit(context, new QbSdk.PreInitCallback() {

                @Override
                public void onViewInitFinished() {
                    float deltaTime = (System.currentTimeMillis() - timerCounter) / 1000;
                    BDebug.i("AppCanTBS", "x5初始化使用了" + deltaTime + "秒， 但是可能还没加载完~");
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

    public static void initInApplication(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().removeSessionCookie();
        CookieManager.getInstance().removeExpiredCookie();
    }

    public static void setCookie(String inUrl, String cookie) {
        CookieManager.getInstance().setCookie(inUrl, cookie);
        CookieSyncManager.getInstance().sync();
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
