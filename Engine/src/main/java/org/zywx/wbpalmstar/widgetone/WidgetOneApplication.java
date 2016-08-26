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

package org.zywx.wbpalmstar.widgetone;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Message;
import android.support.annotation.Keep;

import org.xmlpull.v1.XmlPullParser;
import org.zywx.wbpalmstar.base.BConstant;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.base.vo.NameValuePairVO;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.ELinkedList;
import org.zywx.wbpalmstar.engine.EngineEventListener;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginMgr;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginObject;
import org.zywx.wbpalmstar.platform.push.PushEngineEventListener;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WidgetOneApplication extends Application {

    private ThirdPluginMgr mThirdPluginMgr;
    private WDataManager mWDataManager;
    protected ECrashHandler mCrashReport;
    private ELinkedList<EngineEventListener> mListenerQueue;

    public WidgetOneApplication() {
        mListenerQueue = new ELinkedList<EngineEventListener>();
        PushEngineEventListener pushlistener = new PushEngineEventListener();
        mListenerQueue.add(pushlistener);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BConstant.app = this;
        EUExUtil.init(this);
        WebViewSdkCompat.initInApplication(this);
        mCrashReport = ECrashHandler.getInstance(this);
        initPlugin();
        reflectionPluginMethod("onApplicationCreate");
        BDebug.init();
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
                    m.invoke(c, new Object[]{this});
                }
            } catch (Exception e) {
            }
        }
    }

    private final void initPlugin() {
		if (null == mThirdPluginMgr) {
			long time = System.currentTimeMillis();
			long cost = 0;
			mThirdPluginMgr = new ThirdPluginMgr(this);
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

    public final void initApp(final Context ctx, final Message resultMsg) {

        new Thread("Appcan-WidgetOneInit") {
            public void run() {
                resultMsg.arg1 = 0;// default fail
                WDataManager wDataManager = new WDataManager(ctx);
                WWidgetData widgetData = wDataManager.getWidgetData();
                if (widgetData != null && widgetData.m_indexUrl != null) {
                    resultMsg.arg1 = 1;// success
                    resultMsg.obj = widgetData;
                    BUtility.initWidgetOneFile(ctx, widgetData.m_appId);
                }
                resultMsg.sendToTarget();
            }

            ;
        }.start();
    }

    @Keep
    public final WDataManager getWDataManager() {
        if (null == mWDataManager) {
            mWDataManager = new WDataManager(this);
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

    public final void setPushState(int state) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.setPushState(this, state);
        }
    }

    public final void getPushInfo(String userInfo, String occuredAt) {
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.getPushInfo(this, userInfo, occuredAt);
        }
    }

	@Override
	public AssetManager getAssets() {
		// TODO Auto-generated method stub
		AssetManager assetManager = mThirdPluginMgr == null ? super.getAssets()
				: mThirdPluginMgr.getAssets();
		return assetManager == null ? super.getAssets() : assetManager;
	}

	@Override
	public Resources getResources() {
		// TODO Auto-generated method stub
		Resources resources = mThirdPluginMgr == null ? super.getResources()
				: mThirdPluginMgr.getResources();
		return resources == null ? super.getResources() : resources;
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		ClassLoader classLoader = mThirdPluginMgr == null ? super
				.getClassLoader() : mThirdPluginMgr.getClassLoader();
		return classLoader == null ? super.getClassLoader() : classLoader;
	}
}
