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
import android.content.res.XmlResourceParser;
import android.os.Message;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import dalvik.system.DexClassLoader;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.zywx.wbpalmstar.base.BConstant;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.ELinkedList;
import org.zywx.wbpalmstar.engine.EngineEventListener;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginMgr;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginObject;
import org.zywx.wbpalmstar.platform.push.PushEngineEventListener;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.*;
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
    private String cachePath = null;
    private String dexJar = "dexfile/jar";
    private String dexLib = "dexfile/armeabi";
    private String optFile = "dexfile/out";
    private String[] pluginJars = null;

    public WidgetOneApplication() {
        mListenerQueue = new ELinkedList<EngineEventListener>();
        PushEngineEventListener pushlistener = new PushEngineEventListener();
        mListenerQueue.add(pushlistener);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EUExUtil.init(this);
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().removeSessionCookie();
        CookieManager.getInstance().removeExpiredCookie();
        mCrashReport = ECrashHandler.getInstance(this);
        cachePath = getCacheDir().getAbsolutePath();
        copyLib();
        copyJar();
        initClassLoader();
        initPlugin();
        reflectionPluginMethod("onApplicationCreate");
        BConstant.app = this;
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

    private void copyLib() {

        InputStream in = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        String libPath = cachePath + File.separator + dexLib;
        File dirFile = new File(libPath);
        if (dirFile != null)
            dirFile.delete();
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        try {
            String[] libs = getAssets().list(dexLib);
            if (null != libs && libs.length > 0) {
                for (int i = 0; i < libs.length; i++) {
                    in = getAssets().open(dexLib + File.separator + libs[i]);
                    File file = new File(libPath + File.separator + libs[i]);

                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    bis = new BufferedInputStream(in);
                    fos = new FileOutputStream(file);

                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = bis.read(b)) != -1) {
                        fos.write(b, 0, len);
                    }
                    fos.flush();
                    in.close();
                    bis.close();

                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (in != null && bis != null && fos != null) {
                    in.close();
                    bis.close();
                    fos.close();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void copyJar() {
        InputStream in = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        String jarPath = cachePath + File.separator + dexJar;
        File dirFile = new File(jarPath);
        pluginJars = null;
        if (dirFile != null)
            dirFile.delete();
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        try {
            pluginJars = getAssets().list(dexJar);

            if (pluginJars != null && pluginJars.length > 0) {

                for (int i = 0; i < pluginJars.length; i++) {
                    in = getAssets().open(
                            dexJar + File.separator + pluginJars[i]);
                    File file = new File(jarPath + File.separator
                            + pluginJars[i]);

                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    bis = new BufferedInputStream(in);
                    fos = new FileOutputStream(file);

                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = bis.read(b)) != -1) {
                        fos.write(b, 0, len);
                    }
                    fos.flush();
                    in.close();
                    bis.close();

                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

            if (in != null && bis != null && fos != null) {
                try {
                    in.close();
                    bis.close();
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

    }

    private final void initPlugin() {
        int id = EUExUtil.getResXmlID("plugin");
        if (id == 0) {
            throw new RuntimeException(EUExUtil.getString("plugin_config_no_exist"));
        }
        XmlResourceParser plugins = getResources().getXml(id);
        if (null == mThirdPluginMgr) {
            mThirdPluginMgr = new ThirdPluginMgr(plugins, mListenerQueue, this);
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
        CookieSyncManager.getInstance().stopSync();
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
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("userId", userId));
        nameValuePairs.add(new BasicNameValuePair("userNick", userNick));
        String id = WDataManager.F_SPACE_APPID
                .equals(WDataManager.m_rootWgt.m_appId) ? mBrwView
                .getCurrentWidget().m_appId : WDataManager.m_rootWgt.m_appId;
        nameValuePairs.add(new BasicNameValuePair("appId", id));
        nameValuePairs.add(new BasicNameValuePair("platform", "1"));
        nameValuePairs.add(new BasicNameValuePair("pushType", "mqtt"));
        for (EngineEventListener Listener : mListenerQueue) {
            Listener.setPushInfo(mContext, nameValuePairs);
        }
    }

    public final void delPushInfo(String userId, String userNick,
                                  Context mContext, EBrowserView mBrwView) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
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

    // 因为之前的方法无法替换子进程的classloader，故改成以下的方式。由于每一个进程初始化的时候都会初始化一次他的application，而且默认的classloader是和application的classloader一样的
    // 故在application初始化的时候，替换掉application的classloader。之前只有在主进程中才替换掉application的loader，所以子进程还是无法加载动态插件
    private void initClassLoader() {
        try {
            pluginJars = getAssets().list(dexJar);

            if (pluginJars != null && pluginJars.length > 0) {

                // create the dexPath

                int PluginCount = pluginJars.length;
                String dexPath = cachePath + File.separator + dexJar;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < PluginCount; i++) {
                    sb.append(dexPath).append(File.separator)
                            .append(pluginJars[i]).append(File.pathSeparator);
                }
                dexPath = sb.toString();

                // create the optPath

                String optPath = cachePath + File.separator + optFile;
                File dirFile = new File(optPath);
                if (!dirFile.exists()) {
                    dirFile.mkdirs();
                }
                String libPath = cachePath + File.separator + dexLib;

                // create the dexclassloader
                DexClassLoader dexCl = new DexClassLoader(dexPath, optPath,
                        libPath, getClassLoader());

                // use reflection tech replace the current classloader

                Context mBase = new Smith<Context>(this, "mBase").get();

                Object mPackageInfo = new Smith<Object>(mBase, "mPackageInfo")
                        .get();

                Smith<ClassLoader> sClassLoader = new Smith<ClassLoader>(
                        mPackageInfo, "mClassLoader");
                sClassLoader.set(dexCl);

            }

			/*
             * Field mMainThread =
			 * Activity.class.getDeclaredField("mMainThread");
			 * mMainThread.setAccessible(true); Object mainThread =
			 * mMainThread.get((EBrowserActivity) context); Class threadClass =
			 * mainThread.getClass(); Field mPackages =
			 * threadClass.getDeclaredField("mPackages");
			 * mPackages.setAccessible(true); WeakReference<?> ref; Map<String,
			 * ?> map = (Map<String, ?>) mPackages.get(mainThread); ref =
			 * (WeakReference<?>) map.get(context.getPackageName()); Object apk
			 * = ref.get(); Class apkClass = apk.getClass();
			 * 
			 * Field mClassLoader = apkClass.getDeclaredField("mClassLoader");
			 * mClassLoader.setAccessible(true); mClassLoader.set(apk, dexCl);
			 */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
