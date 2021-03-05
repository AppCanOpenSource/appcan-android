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

package org.zywx.wbpalmstar.engine;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.slidingmenu.lib.SlidingMenu;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.base.util.ActivityActionRecorder;
import org.zywx.wbpalmstar.base.util.ConfigXmlUtil;
import org.zywx.wbpalmstar.base.vo.ValueCallbackVO;
import org.zywx.wbpalmstar.engine.callback.IActivityCallback;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExEventListener;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.engine.universalex.IACEContext;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginMgr;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginObject;
import org.zywx.wbpalmstar.platform.push.PushDataInfo;
import org.zywx.wbpalmstar.platform.push.PushRecieveMsgReceiver;
import org.zywx.wbpalmstar.platform.push.report.PushReportConstants;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EBrowserActivity extends BaseActivity implements IACEContext {

    public static final String KET_WIDGET_DATE="key_widget_data";

    public static final int F_OAUTH_CODE = 100001;
    public final static String APP_TYPE_NOT_START = "0";
    public final static String APP_TYPE_START_BACKGROUND = "1";
    public final static String APP_TYPE_START_FORGROUND= "2";

    private EBrowser mBrowser;
    private boolean mKeyDown;
    private EHandler mEHandler;
    private EBrowserAround mBrowserAround;
    private IActivityCallback mActivityCallback;
    private boolean mCallbackRuning;
    private EBrowserMainFrame mEBrwMainFrame;
    private boolean mFinish;
    private boolean mVisable;
    private boolean mPageFinish;
    private String mAuthorID;
    private boolean mSipBranch;

    /**
     * IDE引擎标识。
     */
    public static boolean develop = false;
    public static boolean analytics = true;
    private JSONObject OtherAppData;
    public static boolean isForground = false;

    public SlidingMenu globalSlidingMenu;

    public static boolean mLoadingRemoved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        if (!EResources.init(this)) {
            loadResError();
            return;
        }
        if (!AppCan.ACTION_APPCAN_SDK.equals(getIntent().getAction())) {
            startMaskActivity();
        } else {
            startCustomMaskActivity(getIntent());
        }
        mVisable = true;
        Window activityWindow = getWindow();
        ESystemInfo.getIntence().init(this);
        mBrowser = new EBrowser(this);
        mEHandler = new EHandler(Looper.getMainLooper());
        initEngineUI();
        mBrowserAround = new EBrowserAround(this);
        setContentView(mEBrwMainFrame);
        initInternalBranch();

        //启动图超时关闭处理。特殊的：有config.xml中的removeloading为true时，则认为启动图由JS代码控制关闭，引擎不做超时了
        Message loadDelayMsg = mEHandler
                .obtainMessage(EHandler.F_MSG_LOAD_TIMEOUT_HIDE_SH);
        long delay = 3 * 1000L;
        if (mSipBranch) {
            delay = 1000L;
        }
        mEHandler.sendMessageDelayed(loadDelayMsg, delay);

        WWidgetData wWidgetData=getIntent().getParcelableExtra(KET_WIDGET_DATE);
        getIntent().removeExtra(KET_WIDGET_DATE);//删除intent中的数据，避免传递给网页
        // maybe start by other app
        if (wWidgetData == null || TextUtils.isEmpty(wWidgetData.m_indexUrl)) {
            wWidgetData = AppCan.getInstance().getRootWidgetData();
        }
        initEngine(wWidgetData);

        EUtil.printeBackup(savedInstanceState, "onCreate");
        // EUtil.checkAndroidProxy(getBaseContext());

        handleIntent(getIntent());
        PushRecieveMsgReceiver.setContext(this);


        globalSlidingMenu = new SlidingMenu(this);
        globalSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);


//        globalSlidingMenu.setShadowWidthRes(EUExUtil.getResDimenID("shadow_width"));
//        globalSlidingMenu.setShadowDrawable(EUExUtil.getResDrawableID("shadow"));

//        globalSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
//        globalSlidingMenu.setShadowDrawable(R.drawable.shadow);
//        globalSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
//        globalSlidingMenu.setFadeDegree(0.35f);
//        globalSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
//        globalSlidingMenu.setMenu(R.layout.menu_frame);
//        globalSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
//
//        globalSlidingMenu.setSecondaryMenu(R.layout.menu_frame_two);
//        globalSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadowright);
//        globalSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_width);
//        globalSlidingMenu.setBehindWidthRes(0);
        reflectionPluginMethod("onActivityCreate");
        try {
            activityWindow.clearFlags(
                    WindowManager.LayoutParams.class.getField(
                            "FLAG_NEEDS_MENU_KEY").getInt(null));
        } catch (Exception e) {
        }
        //TODO 新的更精准的前后台监听，待完善
        ActivityActionRecorder.getInstance().registerTriggerListener(new ActivityActionRecorder.AppBackgroundStatusListener() {
            @Override
            public void onEnterBackground() {
                isForground = false;
                if (null != mBrowser) {
                    mBrowser.onAppPause();
                }
                if (null != mBrowserAround) {
                    mBrowserAround.onPause();
                }
            }

            @Override
            public void onEnterForground() {
                if (null != mBrowser) {
                    mBrowser.onAppResume();
                }
                if (null != mBrowserAround) {
                    mBrowserAround.onResume();
                }
                isForground = true;
            }
        });
    }

    private void reflectionPluginMethod(String method) {
        ThirdPluginMgr tpm = AppCan.getInstance().getThirdPlugins();
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

    private void reflectionPluginMethod(String method, Intent intent) {
        ThirdPluginMgr tpm = AppCan.getInstance().getThirdPlugins();
        Map<String, ThirdPluginObject> thirdPlugins = tpm.getPlugins();
        Set<Map.Entry<String, ThirdPluginObject>> pluginSet = thirdPlugins
                .entrySet();
        for (Map.Entry<String, ThirdPluginObject> entry : pluginSet) {
            try {
                String javaName = entry.getValue().jclass;
                Class c = Class.forName(javaName, true, getClassLoader());
                Object[] objs = new Object[2];
                objs[0] = this;
                objs[1] = intent;
                Class[] argsClass = new Class[objs.length];
                argsClass[0] = Context.class;
                argsClass[1] = Intent.class;
                Method m = c.getMethod(method, argsClass);

                if (null != m) {
                    m.invoke(c, objs);
                }
            } catch (Exception e) {
            }
        }
    }

    private final void initInternalBranch() {
        int sipId = EUExUtil.getResStringID("sip");
        if (0 != sipId) {
            String sipStr = getResources().getString(sipId);
            if (null != sipStr && sipStr.equals("true")) {
                mSipBranch = true;
            }
        }
    }

    private final void initEngine(WWidgetData rootWidget) {
        if (rootWidget == null || TextUtils.isEmpty(rootWidget.m_indexUrl)) {
            loadResError();
            return;
        }
        ConfigXmlUtil.setFullScreen(this);

        ACEDes.getObfuscationList();

        // String[] plugins = {"uexXmlHttpMgr", "uexCamera"};
        // rootWidget.disablePlugins = plugins;
        changeConfiguration(rootWidget.m_orientation);
        EBrowserWidgetPool eBrwWidPo = new EBrowserWidgetPool(mBrowser,
                mEBrwMainFrame, mBrowserAround);
        mBrowser.init(eBrwWidPo);
        // rootWidget.m_indexUrl = "http://xhsnbjlxt.cloud7.com.cn";
        // rootWidget.m_indexUrl = "http://192.168.1.38:8080/ldx/index.html";
        eBrwWidPo.init(rootWidget);
        mBrowserAround.init(eBrwWidPo);
        mEBrwMainFrame.init(mBrowser);
        mBrowserAround.setSpaceFlag(rootWidget.getSpaceStatus());
        mEHandler.sendMessageDelayed(
                mEHandler.obtainMessage(EHandler.F_MSG_LOAD_DELAY), 100);
        AppCan.getInstance().widgetRegist(rootWidget, this);
    }

    public final void hideCustomView() {

        mEBrwMainFrame.hideCustomView();
    }

    public final boolean customViewShown() {

        return mEBrwMainFrame.customViewShown();
    }

    public void setContentViewVisible(int delayTime) {
        if (mLoadingRemoved) {
            return;
        }
        ConfigXmlUtil.setStatusBarColor(this,WWidgetData.sStatusBarColor,WWidgetData.sStatusfontBlack);
        mLoadingRemoved = true;
        getWindow().setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        sendFinishLoadingBroadcast(delayTime);
    }


    public final void showCustomView(View view, WebViewSdkCompat.CustomViewCallback callback) {

        mEBrwMainFrame.showCustomView(view, callback);
    }

    public final boolean isVisable() {

        return mVisable;
    }

    public final void setPageFinish(boolean flag) {
        mPageFinish = flag;
        if (null != mAuthorID) {
            uexOnAuthorize(mAuthorID);
            mAuthorID = null;
        }
    }

    public final boolean isPageFinish() {

        return mPageFinish;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
        EUtil.printeBackup(outState, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // super.onRestoreInstanceState(savedInstanceState);
        EUtil.printeBackup(savedInstanceState, "onSaveInstanceState");
    }

    @Override
    public final boolean onKeyDown(int keyCode, KeyEvent event) {
        mKeyDown = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public final boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!mKeyDown) {
            return true;
        }
        mKeyDown = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (customViewShown()) {
                    hideCustomView();
                } else {
                    giveKeyEnventToBrowser();
                }
                return true;
            case KeyEvent.KEYCODE_MENU:
                if (mBrowser.isLockMenuKey() && !mBrowser.isSpaceShown()) {
                    mBrowser.onAppKeyPress(EUExCallback.F_C_Key_Menu);
                    return true;
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private final void giveKeyEnventToBrowser() {
        boolean b1 = mBrowser.isLockBackKey();
        boolean b2 = mBrowser.isSpaceShown();
        if (b1 && !b2) {
            mBrowser.onAppKeyPress(EUExCallback.F_C_Key_Back);
        } else {
            mBrowser.goBack();
        }
    }

    public void uexOnAuthorize(String id) {
        if (null != mBrowser) {
            if (isPageFinish()) {
                mBrowser.uexOnAuthorize(id);
            } else {
                mAuthorID = id;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null != mBrowser) {
            mBrowser.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EUtil.loge("App onStart");
        reflectionPluginMethod("onActivityStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        EUtil.loge("App onRestart");
        reflectionPluginMethod("onActivityReStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        EUtil.loge("App onStop");
        reflectionPluginMethod("onActivityStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        requsetPerssions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        EUtil.loge("App onResume");
        mVisable = true;
//        if (null != mBrowser) {
//            mBrowser.onAppResume();
//        }
//        if (null != mBrowserAround) {
//            mBrowserAround.onResume();
//        }
//        isForground = true;
        reflectionPluginMethod("onActivityResume");
    }

    @Override
    protected void onDestroy() {
        EUtil.loge("App onDestroy");
        super.onDestroy();
        reflectionPluginMethod("onActivityDestroy");
        if (!AppCan.getInstance().isWidgetSdk()) {
            Process.killProcess(Process.myPid());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EUtil.loge("App onPause");
        mVisable = false;
        if (mFinish) {
            return;
        }
//        isForground = false;
//        if (null != mBrowser) {
//            mBrowser.onAppPause();
//        }
//        if (null != mBrowserAround) {
//            mBrowserAround.onPause();
//        }
        reflectionPluginMethod("onActivityPause");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        reflectionPluginMethod("onActivityNewIntent", intent);
    }

    public void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            Intent firstIntent = getIntent();
            int type = intent.getIntExtra("ntype", 0);
            switch (type) {
                case ENotification.F_TYPE_PUSH:
                    handlePushNotify(intent);
                    break;
                case ENotification.F_TYPE_USER:
                    break;
                case ENotification.F_TYPE_SYS:
                    break;
                default:
                    getIntentData(intent);
                    firstIntent.putExtras(intent);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePushNotify(Intent intent) {
        if (null != mBrowser) {
            try {
                String data = intent.getStringExtra("data");
                String pushMessage = intent.getStringExtra("message");
                SharedPreferences sp = getSharedPreferences(
                        PushReportConstants.PUSH_DATA_SHAREPRE,
                        Context.MODE_PRIVATE);
                Editor editor = sp.edit();
                editor.putString(
                        PushReportConstants.PUSH_DATA_SHAREPRE_DATA, data);
                editor.putString(
                        PushReportConstants.PUSH_DATA_SHAREPRE_MESSAGE,
                        pushMessage);
                if (intent.hasExtra(PushReportConstants.PUSH_DATA_INFO_KEY)) {
                    PushDataInfo dataInfo = (PushDataInfo) intent.getExtras()
                            .get(PushReportConstants.PUSH_DATA_INFO_KEY);
                    String taskId = dataInfo.getTaskId();
                    editor.putString(PushReportConstants.PUSH_DATA_SHAREPRE_TASKID,
                            taskId);
                    String tenantId = dataInfo.getTenantId();
                    editor.putString(PushReportConstants.PUSH_DATA_SHAREPRE_TENANTID,
                            tenantId);
                }
                editor.commit();
                String appType = "";
                if (mVisable && isForground) {
                    //应用在前台
                    appType = APP_TYPE_START_FORGROUND;
                } else if (!mVisable && !isForground) {
                    //应用Home键退到后台再点通知进入
                    appType = APP_TYPE_START_BACKGROUND;
                } else if (mVisable && !isForground) {
                    //应用Back键退出再点通知进入
                    appType = APP_TYPE_NOT_START;
                }
                mBrowser.pushNotify(appType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public final void exitApp(boolean showDilog) {
        Message msg = mEHandler.obtainMessage(EHandler.F_MSG_EXIT_APP,
                showDilog);
        msg.sendToTarget();
    }

    private final void loadResError() {
        AlertDialog.Builder dia = new AlertDialog.Builder(this);
        ResoureFinder finder = ResoureFinder.getInstance();
        dia.setTitle(finder.getString(this, "browser_dialog_error"));
        dia.setMessage(finder.getString(this, "browser_init_error"));
        dia.setCancelable(false);
        dia.setPositiveButton(finder.getString(this, "confirm"),
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Process.killProcess(Process.myPid());
                    }
                });
        dia.create();
        dia.show();
    }

    private final void readyExit(boolean showDialog) {
        if (null != mBrowserAround) {
            if (mBrowserAround.onExit()) {
                return;
            }
        }
        if (!showDialog||AppCan.getInstance().isWidgetSdk()) {
            exitBrowser();
            return;
        }
        try {
            AlertDialog.Builder tDialog = new AlertDialog.Builder(this);
            ResoureFinder finder = ResoureFinder.getInstance();
            tDialog.setTitle(finder.getString(this, "browser_exitdialog_msg"));
            tDialog.setNegativeButton(finder.getString(this, "cancel"), null);
            tDialog.setMessage(finder.getString(this, "browser_exitdialog_app_text"));
            tDialog.setPositiveButton(finder.getString(this, "confirm"),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitBrowser();
                        }
                    });
            tDialog.show();
        } catch (Exception e) {
        }
    }

    @Override
    public final void exitBrowser() {
        if (mSipBranch) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return;
        }
        if (null != mBrowser) {
            mBrowser.onAppStop();
        }
        mBrowserAround.removeViewImmediate();
        clean();
        finish();
        if (AppCan.getInstance().isWidgetSdk()){
            if (AppCan.getInstance().mFinishListener!=null){
                AppCan.getInstance().mFinishListener.onFinish(0,null);
            }
        }
    }

    private final void clean() {
        if (null != mBrowser) {
            mBrowser.clean();
        }
        AppCan.getInstance().exitApp();
        mEHandler.clean();
        mBrowserAround.clean();
        mFinish = true;
        Runtime.getRuntime().gc();
    }

    public final void setAutorotateEnable(int enabled) {
        int ori = ActivityInfo.SCREEN_ORIENTATION_USER;
        if (enabled == 1) {
            ori = getOrientationForRotation();
        }
        final int orientation = ori;
        new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                setRequestedOrientation(orientation);
                ;
            }
        }.sendEmptyMessageDelayed(0, 100);
    }

    private int getOrientationForRotation() {
        int ori = ActivityInfo.SCREEN_ORIENTATION_USER;
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_0) {
            ori = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (rotation == Surface.ROTATION_90) {
            ori = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (rotation == Surface.ROTATION_180) {
            ori = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        } else if (rotation == Surface.ROTATION_270) {
            ori = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
        return ori;
    }

    public final void changeConfiguration(int orientation) {
        final int ori = intoOrientation(orientation);
        new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                setRequestedOrientation(ori);
                ;
            }
        }.sendEmptyMessageDelayed(0, 100);
    }

    public final int intoOrientation(int flag) {
        int or = ActivityInfo.SCREEN_ORIENTATION_USER;
        if (flag == 1) {// portrait
            or = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (flag == 2) {// landscape
            or = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (flag == 4) {// reverse portrait
            or = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        } else if (flag == 8) {// reverse landscape
            or = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else if (flag == 5) {// portrait and reverse portrait, Some devices only portrait effective
            if (Build.VERSION.SDK_INT >= 18) {
                or = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
            } else {
                or = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
            }
        } else if (flag == 10) {// landscape and reverse landscape
            if (Build.VERSION.SDK_INT >= 18) {
                or = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
            } else {
                or = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            }
        }
        return or;
    }

    @Override
    protected final void onActivityResult(int requestCode, int resultCode,
                                          Intent data) {
        if (F_OAUTH_CODE == requestCode) {
            if (null != data) {
                int result = data.getIntExtra("result", 0);
                if (0 == result) {
                    exitBrowser();
                    return;
                }
                String authorizeID = data.getStringExtra("authorizeID");
                uexOnAuthorize(authorizeID);
            }
            return;
        }
        if (mCallbackRuning && null != mActivityCallback) {
            mActivityCallback.onActivityResult(requestCode, resultCode, data);
            mCallbackRuning = false;
            mActivityCallback = null;
        }
    }

    @Override
    public final void startActivityForResult(IActivityCallback callack, Intent intent,
                                             int requestCode) {
        if (mCallbackRuning) {
            return;
        }
        if (null != callack) {
            mActivityCallback = callack;
            mCallbackRuning = true;
            super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public final void registerActivityForResult(IActivityCallback callback) {
        if (mCallbackRuning) {
            return;
        }
        if (null != callback) {
            mActivityCallback = callback;
            mCallbackRuning = true;
        }
    }

    @Override
    public final void registerAppEventListener(EUExEventListener listener) {
        if (null != mBrowserAround) {
            mBrowserAround.registerAppEventListener(listener);
        }
    }

    @Override
    public final void unRegisterAppEventListener(EUExEventListener listener) {
        if (null != mBrowserAround) {
            mBrowserAround.unRegisterAppEventListener(listener);
        }
    }

    public final Bitmap getImage(String bgPath) {
        if (null != mBrowser) {
            return mBrowser.getImage(bgPath);
        }
        return null;
    }

    private final void initEngineUI() {
        mEBrwMainFrame = new EBrowserMainFrame(this);
        // 若为了解决沉浸状态栏适配问题，增加了此属性为true，则会导致系统自动将此布局内容向下平移出状态栏的高度，露出背景色。这样就无法让H5来自定义状态栏区域的内容了。所以此方法不适合本场景。
//        mEBrwMainFrame.setFitsSystemWindows(true);
        FrameLayout.LayoutParams mainPagePa = new FrameLayout.LayoutParams(
                Compat.FILL, Compat.FILL);
        EUtil.viewBaseSetting(mEBrwMainFrame);
        mEBrwMainFrame.setLayoutParams(mainPagePa);
    }

    private void getIntentData(Intent in) {
        if (null != in) {
            Bundle bundle = in.getExtras();
            if (null != bundle) {
                OtherAppData = new JSONObject();
                Set<String> set = bundle.keySet();
                Iterator<String> it = set.iterator();
                while (it.hasNext()) {
                    try {
                        String key = it.next();
                        Object object = bundle.get(key);
                        if (object != null) {
                            String data = object.toString();
                            OtherAppData.put(key, data);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                loadByOtherApp();
            }

            //url打开APP参数处理
            if (in.getData()!=null){
                if (OtherAppData==null){
                    OtherAppData = new JSONObject();
                }
                Set<String> keys=in.getData().getQueryParameterNames();
                if (keys!=null){
                    for (String key:keys){
                        try {
                            OtherAppData.put(key,in.getData().getQueryParameter(key));
                        } catch (JSONException e) {
                            BDebug.e(e.toString());
                        }
                    }
                }
                loadByOtherApp();
            }
        }
    }

    public class EHandler extends Handler {

        static final int F_MSG_INIT_APP = 0;
        static final int F_MSG_LOAD_DELAY = 1;
        static final int F_MSG_LOAD_HIDE_SH = 2;
        static final int F_MSG_EXIT_APP = 3;
        static final int F_MSG_LOAD_TIMEOUT_HIDE_SH = 4;//超时处理


        public EHandler(Looper loop) {
            super(loop);
        }

        public void clean() {
            removeMessages(F_MSG_INIT_APP);
            removeMessages(F_MSG_LOAD_DELAY);
            removeMessages(F_MSG_LOAD_HIDE_SH);
            removeMessages(F_MSG_EXIT_APP);
            removeMessages(F_MSG_LOAD_TIMEOUT_HIDE_SH);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case F_MSG_LOAD_DELAY:
                    try {
                        Intent intent = getIntent();
                        int type = intent.getIntExtra("ntype", 0);
                        switch (type) {
                            case ENotification.F_TYPE_PUSH:
                                mBrowser.setFromPush(true);
                                break;
                            case ENotification.F_TYPE_USER:
                                // onNewIntent(intent);
                                break;
                        }
                        mBrowser.start();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                case F_MSG_LOAD_HIDE_SH:
                    setContentViewVisible(0);
                    if (mBrowserAround.checkTimeFlag()) {
                        mBrowser.hiddenShelter();
                    } else {
                        mBrowserAround.setTimeFlag(true);
                    }
                    break;
                case F_MSG_LOAD_TIMEOUT_HIDE_SH:
                    if (WWidgetData.m_remove_loading == 1){
                        //默认1，正常逻辑；否则是有config.xml配置
                        setContentViewVisible(0);
                        if (mBrowserAround.checkTimeFlag()) {
                            mBrowser.hiddenShelter();
                        } else {
                            mBrowserAround.setTimeFlag(true);
                        }
                    }else{
                        BDebug.i("removeloading in config.xml is true, cancel loadingImage timeout");
                    }
                    break;
                case F_MSG_EXIT_APP:
                    readyExit((Boolean) msg.obj);
                    break;
            }
        }
    }

    public void loadByOtherApp() {
        if (OtherAppData != null) {
            if (isPageFinish()) {
                mBrowser.onLoadAppData(OtherAppData);
                OtherAppData = null;
            }
        }
    }

    public void onSlidingWindowStateChanged(int position) {
        if (null != mBrowser) {
            mBrowser.onSlidingWindowStateChanged(position);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (null != mActivityCallback) {
            mActivityCallback.onRequestPermissionResult(requestCode, permissions, grantResults);
            mActivityCallback = null;
        }else{
            BDebug.w("onRequestPermissionsResult error: mActivityCallback is null. Do you forget to call registerActivityResult() of EUExBase's instance?");
        }

    }

    /**
     * 请求多个权限
     *
     * @deprecated Wrong Spelling. use requestPermissionsMore method instead.
     */
    @Deprecated
    public void requsetPerssionsMore(final String[] perssions, IActivityCallback callack, String message, final int requestCode){
        acRequestPermissionsMore(perssions, callack, message, requestCode);
    }

    /**
     * 请求单个权限
     *
     * @deprecated Wrong Spelling. use requestPermissions method instead.
     */
    @Deprecated
    public void requsetPerssions(final String perssions, IActivityCallback callack, String message, final int requestCode){
        acRequestPermissions(perssions, callack, message, requestCode);
    }

    /**
     * 请求多个权限
     *
     * @param permissions 权限列表
     * @param message 权限请求提示语
     * @param requestCode 请求编号
     */
    @Override
    public void acRequestPermissionsMore(final String[] permissions, IActivityCallback callack, String message, final int requestCode){
        if (null != callack) {
            mActivityCallback = callack;
        }
        //系统运行环境小于6.0不需要权限申请
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mActivityCallback.onRequestPermissionResult(requestCode, permissions, new int[]{0});
            return;
        }

        List<String> permissionLists = new ArrayList<String>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 若有未授权的，则存起来重新申请
                permissionLists.add(permission);
            }
        }
        if (!permissionLists.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionLists.toArray(new String[permissionLists.size()]), requestCode);
        } else {
            //未授权列表为空，表示全都授权了
            mActivityCallback.onRequestPermissionResult(requestCode, permissions, new int[]{0});
        }
    }

    /**
     * 请求权限
     *
     * @param permission 权限
     * @param message 权限请求提示语
     * @param requestCode 请求编号
     */
    @Override
    public void acRequestPermissions(final String permission, IActivityCallback callback, String message, final int requestCode){
        if (null != callback) {
            mActivityCallback = callback;
        }
        //系统运行环境小于6.0不需要权限申请
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mActivityCallback.onRequestPermissionResult(requestCode, new String[]{permission}, new int[]{0});
            return;
        }
        //检查权限是否授权
        int checkPermissionResult = ContextCompat.checkSelfPermission(this, permission);
        if(checkPermissionResult != PackageManager.PERMISSION_GRANTED){
            //判断是不是第一次申请权限，如果是第一次申请权限则返回false，如果之前拒绝再次申请则返回true
            ActivityCompat.requestPermissions(EBrowserActivity.this, new String[]{permission}, requestCode);
        }else {
            mActivityCallback.onRequestPermissionResult(requestCode, new String[]{permission}, new int[]{0});
        }
    }

}
