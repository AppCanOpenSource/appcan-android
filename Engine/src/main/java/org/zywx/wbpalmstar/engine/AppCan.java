package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import org.xmlpull.v1.XmlPullParser;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.base.BConstant;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.base.listener.OnAppCanFinishListener;
import org.zywx.wbpalmstar.base.listener.OnAppCanInitListener;
import org.zywx.wbpalmstar.base.util.SpManager;
import org.zywx.wbpalmstar.base.vo.NameValuePairVO;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginMgr;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginObject;
import org.zywx.wbpalmstar.platform.push.PushEngineEventListener;
import org.zywx.wbpalmstar.widgetone.ECrashHandler;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zywx.wbpalmstar.engine.LoadingActivity.KEY_INTENT_WIDGET_DATA;

/**
 * Created by ylt on 16/9/1.
 */
public class AppCan {

    public static final String ACTION_APPCAN_SDK="action.appcan.sdk";
    private static AppCan sAppCan;
    private ThirdPluginMgr mThirdPluginMgr;
    private ELinkedList<EngineEventListener> mListenerQueue;
    private WDataManager mWDataManager;
    protected ECrashHandler mCrashReport;
    private Context mContext;//Application
    private WWidgetData mWidgetData;
    private boolean mIsWidgetSdk =true;
    OnAppCanFinishListener mFinishListener;
    private AppCan(){
    }

    public static AppCan getInstance(){
        if (sAppCan ==null){
            sAppCan =new AppCan();
        }
        return sAppCan;
    }

    public WWidgetData getRootWidgetData(){
        return mWidgetData;
    }

    /**
     * 同步初始化引擎
     */
    public boolean initSync(Context context){
        mContext=context.getApplicationContext();
        if (!(mContext instanceof Application)){
            return false;
        }
        mListenerQueue = new ELinkedList<EngineEventListener>();
        PushEngineEventListener pushlistener = new PushEngineEventListener();
        mListenerQueue.add(pushlistener);
        BDebug.init();
        BConstant.app = (Application) mContext;
        ACEDes.setContext(mContext);
        EUExUtil.init(mContext);
        WebViewSdkCompat.initInApplication(mContext);
        mCrashReport = ECrashHandler.getInstance(mContext);
        initPlugin();

        Handler mainHandler=new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                reflectionPluginMethod("onApplicationCreate");//主线程调用onApplicationCreate,某些三方插件需要在主线程初始化
            }
        });

        //清除上次运行的Session 数据
        SpManager.getInstance().clearSession();
        WDataManager wDataManager = new WDataManager(mContext);
        mWidgetData = wDataManager.getWidgetData();
        boolean success=isInitSuccess();
        if (success) {
            BUtility.initWidgetOneFile(mContext, mWidgetData.m_appId);
        }
        return success;
    }

    private boolean isInitSuccess(){
        return mWidgetData != null && mWidgetData.m_indexUrl != null;
    }

    /**
     * 异步初始化引擎
     * @param initListener
     */
    public void init(final Context context, final OnAppCanInitListener initListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result=initSync(context);
                if (result) {
                    if (initListener!=null){
                        initListener.onInit();
                    }
                }else{
                    if (initListener!=null){
                        initListener.onError();
                    }
                }
            }
        }).start();
    }

    /**
     *
     * @param activity
     * @param bundle 传递给网页的数据
     */
    public void start(final Activity activity, final Bundle bundle){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebViewSdkCompat.initInActivity(activity);
                Intent intent = new Intent(mContext, EBrowserActivity.class);
                if (mIsWidgetSdk){
                    intent.setAction(ACTION_APPCAN_SDK);
                }
                if (null != bundle) {
                    intent.putExtras(bundle);
                }
                if (mWidgetData != null) {
                    intent.putExtra(KEY_INTENT_WIDGET_DATA, mWidgetData);
                }
                activity.startActivity(intent);
                activity.overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_no_anim")
                        , EUExUtil.getResAnimID("platform_myspace_no_anim"));
            }
        });

    }

    public void registerFinishCallback(OnAppCanFinishListener listener){
        if (listener!=null){
            AppCan.this.mFinishListener=listener;
        }
    }

    /**
     *
     * @param activity
     * @param indexUrl 起始页
     * @param bundle 传递给网页的数据
     */
    public void start(Activity activity,String indexUrl,Bundle bundle){
        if (mWidgetData!=null){
            mWidgetData.m_indexUrl=indexUrl;
        }
        start(activity,bundle);
    }

    private void reflectionPluginMethod(String method) {
        ThirdPluginMgr tpm = getThirdPlugins();
        Map<String, ThirdPluginObject> thirdPlugins = tpm.getPlugins();
        Set<Map.Entry<String, ThirdPluginObject>> pluginSet = thirdPlugins
                .entrySet();
        for (Map.Entry<String, ThirdPluginObject> entry : pluginSet) {
            try {
                String javaName = entry.getValue().jclass;
                Class c = Class.forName(javaName, true, getClassLoader());
                Method m = c.getMethod(method, new Class[]{Context.class});
                if (null != m) {
                    m.invoke(c, new Object[]{mContext});
                }
            } catch (Exception e) {
                if (BDebug.DEBUG){
                    e.printStackTrace();
                }
            }
        }
    }

    private final void initPlugin() {
        if (null == mThirdPluginMgr) {
            long time = System.currentTimeMillis();
            long cost = 0;
            mThirdPluginMgr = new ThirdPluginMgr(mContext);
            // 开始拷贝和加载旧版dex动态库插件
            mThirdPluginMgr.loadInitAllDexPluginClass();
            // 开始拷贝和加载动态库插件
            mThirdPluginMgr.loadInitAllDynamicPluginClass(mListenerQueue);
            // 开始加载打包内置的xml中的plugin文件
            XmlPullParser plugins = null;
            int id = EUExUtil.getResXmlID("plugin");
            if (id == 0) {
                throw new RuntimeException(EUExUtil.getString("plugin_config_no_exist"));
            }
            plugins = getResources().getXml(id);
            mThirdPluginMgr.initClass(plugins, mListenerQueue, null);
            cost = System.currentTimeMillis() - time;
            BDebug.i("DL", "plugins loading total costs " + cost);
        }
    }

    public final WDataManager getWDataManager() {
        if (null == mWDataManager) {
            mWDataManager = new WDataManager(mContext);
        }
        return mWDataManager;
    }

    public final ThirdPluginMgr getThirdPlugins() {
        if (null == mThirdPluginMgr) {
            initPlugin();
        }
        return mThirdPluginMgr;
    }

    public final void exitApp() {
        stopAnalyticsAgent();
        WebViewSdkCompat.stopSync();
    }

    public AssetManager getAssets() {
        if (mThirdPluginMgr==null||mThirdPluginMgr.getAssets()==null){
            return mContext.getAssets();
        }else{
            return mThirdPluginMgr.getAssets();
        }
    }

    public Resources getResources() {
        Resources resources = AppCan.getInstance().getThirdPlugins() == null ? mContext.getResources()
                : AppCan.getInstance().getThirdPlugins().getResources();
        return resources == null ? mContext.getResources() : resources;
    }

    public ClassLoader getClassLoader() {
        ClassLoader classLoader = mThirdPluginMgr == null ? mContext
                .getClassLoader() : mThirdPluginMgr.getClassLoader();
        return classLoader == null ? mContext.getClassLoader() : classLoader;
    }

    private final void stopAnalyticsAgent() {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onAppStop();
        }
    }

    public final void widgetRegist(WWidgetData wgtData, Activity activity) {
        if (null == wgtData) {
            return;
        }
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onWidgetStart(EngineEventListener.WGT_TYPE_MAIN, wgtData,
                    activity);
        }
    }

    public final void widgetReport(WWidgetData wgtData, Activity activity) {
        if (null == wgtData) {
            return;
        }
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onWidgetStart(EngineEventListener.WGT_TYPE_SUB, wgtData,
                    activity);
        }
    }

    public final void disPatchWindowOpen(String beEndUrl, String beShowUrl,
                                         String[] beShowPopupUrls) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onWindowOpen(beEndUrl, beShowUrl, beShowPopupUrls);
        }
    }

    public final void disPatchWindowClose(String beEndUrl, String beShowUrl,
                                          String[] beEndPopupUrls, String[] beShowPopupUrls) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onWindowClose(beEndUrl, beShowUrl, beEndPopupUrls,
                    beShowPopupUrls);
        }
    }

    public final void disPatchWindowBack(String beEndUrl, String beShowUrl,
                                         String[] beEndPopupUrls, String[] beShowPopupUrls) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onWindowBack(beEndUrl, beShowUrl, beEndPopupUrls,
                    beShowPopupUrls);
        }
    }

    public final void disPatchWindowForward(String beEndUrl, String beShowUrl,
                                            String[] beEndPopupUrls, String[] beShowPopupUrls) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onWindowForward(beEndUrl, beShowUrl, beEndPopupUrls,
                    beShowPopupUrls);
        }
    }

    public final void disPatchPopupOpen(String curWindowUrl,
                                        String beShowPopupUrl) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onPopupOpen(curWindowUrl, beShowPopupUrl);
        }
    }

    public final void disPatchPopupClose(String beEndPopupUrl) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onPopupClose(beEndPopupUrl);
        }
    }

    public final void disPatchAppResume(String beEndUrl, String beShowUrl,
                                        String[] beShowPopupUrls) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onAppResume(beEndUrl, beShowUrl, beShowPopupUrls);
        }
    }

    public final void disPatchAppPause(String beEndUrl, String beShowUrl,
                                       String[] beEndPopupUrls) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onAppPause(beEndUrl, beShowUrl, beEndPopupUrls);
        }
    }

    public final void disPatchAppStart(String startUrl) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.onAppStart(startUrl);
        }
    }

    public final void setPushInfo(String userId, String userNick,
                                  Context mContext, EBrowserView mBrwView) {
        List<NameValuePairVO> nameValuePairs = new ArrayList<NameValuePairVO>();

        nameValuePairs.add(new NameValuePairVO("userId", userId));
        nameValuePairs.add(new NameValuePairVO("userNick", userNick));
        String id = WDataManager.F_SPACE_APPID
                .equals(WDataManager.sRootWgt.m_appId) ? mBrwView
                .getCurrentWidget().m_appId : WDataManager.sRootWgt.m_appId;
        nameValuePairs.add(new NameValuePairVO("appId", id));
        nameValuePairs.add(new NameValuePairVO("platform", "1"));
        nameValuePairs.add(new NameValuePairVO("pushType", "mqtt"));
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.setPushInfo(mContext, nameValuePairs);
        }
    }

    public final void delPushInfo(String userId, String userNick,
                                  Context mContext, EBrowserView mBrwView) {
        List<NameValuePairVO> nameValuePairs = new ArrayList<NameValuePairVO>();
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.delPushInfo(mContext, nameValuePairs);
        }
    }

    public final void deviceBind(String userId, String userNick, Context mContext, EBrowserView mBrwView) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.deviceBind(userId, userNick, mContext);
        }
    }

    public final void deviceUnBind(Context mContext, EBrowserView mBrwView) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.deviceUnBind(mContext);
        }
    }

    public final void setPushState(int state) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.setPushState(mContext, state);
        }
    }

    public final void getPushInfo(String userInfo, String occuredAt) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.getPushInfo(mContext, userInfo, occuredAt);
        }
    }

    public void setWidgetSdk(boolean widgetSdk) {
        this.mIsWidgetSdk = widgetSdk;
    }

    public boolean isWidgetSdk(){
        return mIsWidgetSdk;
    }

}
