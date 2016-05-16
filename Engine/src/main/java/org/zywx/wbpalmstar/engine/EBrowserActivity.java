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
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.slidingmenu.lib.SlidingMenu;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExEventListener;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginMgr;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginObject;
import org.zywx.wbpalmstar.platform.push.PushDataInfo;
import org.zywx.wbpalmstar.platform.push.PushRecieveMsgReceiver;
import org.zywx.wbpalmstar.platform.push.report.PushReportConstants;
import org.zywx.wbpalmstar.widgetone.WidgetOneApplication;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class EBrowserActivity extends FragmentActivity {

    public static final int F_OAUTH_CODE = 100001;
    public final static int FILECHOOSER_RESULTCODE = 233;
    public final static String APP_TYPE_NOT_START = "0";
    public final static String APP_TYPE_START_BACKGROUND = "1";
    public final static String APP_TYPE_START_FORGROUND= "2";

    private EBrowser mBrowser;
    private boolean mKeyDown;
    private EHandler mEHandler;
    private EBrowserAround mBrowserAround;
    private EUExBase mActivityCallback;
    private boolean mCallbackRuning;
    private EBrowserMainFrame mEBrwMainFrame;
    private FrameLayout mScreen;
    private boolean mFinish;
    private boolean mVisable;
    private boolean mPageFinish;
    private String mAuthorID;
    private boolean mSipBranch;

    public static boolean develop = false;
    public static boolean analytics = true;
    private JSONObject OtherAppData;
    public static boolean isForground = false;

    public SlidingMenu globalSlidingMenu;
    private ValueCallback<Uri> mUploadMessage;
    private boolean mLoadingRemoved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        if (!EResources.init(this)) {
            loadResError();
            return;
        }
        Intent intent = new Intent(EBrowserActivity.this, TempActivity.class);
        intent.putExtra("isTemp", true);
        startActivity(intent);
        overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_no_anim")
                , EUExUtil.getResAnimID("platform_myspace_no_anim"));

        mVisable = true;
        Window activityWindow = getWindow();
        ESystemInfo.getIntence().init(this);
        mBrowser = new EBrowser(this);
        mEHandler = new EHandler(Looper.getMainLooper());
        View splash = initEngineUI();
        mBrowserAround = new EBrowserAround(splash);
//		mScreen.setVisibility(View.INVISIBLE);
        setContentView(mScreen);
        initInternalBranch();

        ACEDes.setContext(this);

        Message loadDelayMsg = mEHandler
                .obtainMessage(EHandler.F_MSG_LOAD_HIDE_SH);
        long delay = 3 * 1000L;
        if (mSipBranch) {
            delay = 1000L;
        }
        mEHandler.sendMessageDelayed(loadDelayMsg, delay);
        Message initAppMsg = mEHandler.obtainMessage(EHandler.F_MSG_INIT_APP);
        WidgetOneApplication app = (WidgetOneApplication) getApplication();
        app.initApp(this, initAppMsg);

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
    }

    private void reflectionPluginMethod(String method) {
        WidgetOneApplication app = (WidgetOneApplication) getApplication();
        ThirdPluginMgr tpm = app.getThirdPlugins();
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

    private final void initInternalBranch() {
        int sipId = EUExUtil.getResStringID("sip");
        if (0 != sipId) {
            String sipStr = getResources().getString(sipId);
            if (null != sipStr && sipStr.equals("true")) {
                mSipBranch = true;
            }
        }
    }

    private final void initEngine(Message resultMsg) {
        if (resultMsg.arg1 == 0) {
            loadResError();
            return;
        }

        ACEDes.getObfuscationList();

        WWidgetData rootWidget = (WWidgetData) resultMsg.obj;
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
        WidgetOneApplication app = (WidgetOneApplication) getApplication();
        app.widgetRegist(rootWidget, this);
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
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager
                .getInstance(this);
        mEHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingRemoved = true;
                        getWindow().setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
                        Intent intent = new Intent(LoadingActivity.BROADCAST_ACTION);
                        broadcastManager.sendBroadcast(intent);
                    }
                });
            }
        }, delayTime);
    }


    public final void showCustomView(View view, CustomViewCallback callback) {

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
        EUtil.loge("App onResume");
        mVisable = true;
        if (null != mBrowser) {
            mBrowser.onAppResume();
        }
        if (null != mBrowserAround) {
            mBrowserAround.onResume();
        }
        isForground = true;
        reflectionPluginMethod("onActivityResume");
    }

    @Override
    protected void onDestroy() {
        EUtil.loge("App onDestroy");
        super.onDestroy();
        reflectionPluginMethod("onActivityDestroy");
        Process.killProcess(Process.myPid());
    }

    @Override
    protected void onPause() {
        isForground = false;
        super.onPause();
        EUtil.loge("App onPause");
        mVisable = false;
        if (mFinish) {
            return;
        }
        if (null != mBrowser) {
            mBrowser.onAppPause();
        }
        if (null != mBrowserAround) {
            mBrowserAround.onPause();
        }
        reflectionPluginMethod("onActivityPause");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
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
        dia.setTitle(EResources.display_dialog_error);
        dia.setMessage(EResources.display_init_error);
        dia.setCancelable(false);
        dia.setPositiveButton(EResources.display_confirm,
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
        if (!showDialog) {
            exitBrowser();
            return;
        }
        try {
            AlertDialog.Builder tDialog = new AlertDialog.Builder(this);
            tDialog.setTitle(EResources.display_exitdialog_msg);
            tDialog.setNegativeButton(EResources.display_cancel, null);
            tDialog.setMessage(EResources.display_exitdialog_app_text);
            tDialog.setPositiveButton(EResources.display_confirm,
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
    }

    private final void clean() {
        if (null != mBrowser) {
            mBrowser.clean();
        }
        if (null != mScreen) {
            mScreen.removeAllViews();
        }
        WidgetOneApplication app = (WidgetOneApplication) getApplication();
        app.exitApp();
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
        if (flag == 1||flag==5) {// portrait
            or = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (flag == 2||flag==10) {// landscape
            or = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (flag == 4) {// reverse portrait
            or = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        } else if (flag == 8) {// reverse landscape
            or = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else if (flag == 15) {// sensor
            or = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        } else {
            ;
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
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        if (mCallbackRuning && null != mActivityCallback) {
            mActivityCallback.onActivityResult(requestCode, resultCode, data);
            mCallbackRuning = false;
            mActivityCallback = null;
        }
    }

    public final void startActivityForResult(EUExBase callack, Intent intent,
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

    public final void registerActivityForResult(EUExBase callback) {
        if (mCallbackRuning) {
            return;
        }
        if (null != callback) {
            mActivityCallback = callback;
            mCallbackRuning = true;
        }
    }

    public final void registerAppEventListener(EUExEventListener listener) {
        if (null != mBrowserAround) {
            mBrowserAround.registerAppEventListener(listener);
        }
    }

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

    private final View initEngineUI() {
        mScreen = new FrameLayout(this);
        FrameLayout.LayoutParams screenPa = new FrameLayout.LayoutParams(
                Compat.FILL, Compat.FILL);
        mScreen.setLayoutParams(screenPa);

        mEBrwMainFrame = new EBrowserMainFrame(this);
        FrameLayout.LayoutParams mainPagePa = new FrameLayout.LayoutParams(
                Compat.FILL, Compat.FILL);
        EUtil.viewBaseSetting(mEBrwMainFrame);
        mEBrwMainFrame.setLayoutParams(mainPagePa);
        mScreen.addView(mEBrwMainFrame);

        FrameLayout splash = new FrameLayout(this);
        splash.setClickable(true);

        FrameLayout.LayoutParams shelterPa = new FrameLayout.LayoutParams(
                Compat.FILL, Compat.FILL);
        splash.setLayoutParams(shelterPa);
        mScreen.addView(splash);


		/*
		 * ImageView background = new ImageView(this);
		 * 
		 * FrameLayout.LayoutParams backgroundPa = new
		 * FrameLayout.LayoutParams(Compat.FILL, Compat.FILL);
		 * background.setLayoutParams(backgroundPa);
		 * 
		 * 
		 * 
		 * splash.addView(background);
		 */
		/*
		 * if(0 < screenWidth && screenWidth < 480){
		 * foreground.setBackgroundResource(EResources.startup_fg_small); }else
		 * if(480 <= screenWidth && screenWidth < 720){
		 * foreground.setBackgroundResource(EResources.startup_fg_normal); }else
		 * if(720 <= screenWidth && screenWidth < 1080){ if(0 !=
		 * EResources.startup_fg_large){
		 * foreground.setBackgroundResource(EResources.startup_fg_large); }else{
		 * foreground.setBackgroundResource(EResources.startup_fg_normal); }
		 * }else { if(0 != EResources.startup_fg_xlarge){
		 * foreground.setBackgroundResource(EResources.startup_fg_xlarge);
		 * }else{
		 * foreground.setBackgroundResource(EResources.startup_fg_normal); } }
		 */
		/*
		 * FrameLayout.LayoutParams foregroundPa = new
		 * FrameLayout.LayoutParams(Compat.WRAP, Compat.WRAP);
		 * foregroundPa.gravity = Gravity.CENTER;
		 * foreground.setLayoutParams(foregroundPa); splash.addView(foreground);
		 */

		/*
		 * ImageView mark = new ImageView(this);
		 * mark.setBackgroundResource(EResources.mark_bg);
		 * FrameLayout.LayoutParams markPa = new
		 * FrameLayout.LayoutParams(Compat.WRAP, Compat.WRAP); markPa.gravity =
		 * Gravity.BOTTOM | Gravity.RIGHT; mark.setLayoutParams(markPa);
		 * splash.addView(mark); if(develop){ TextView worn = new
		 * TextView(this); worn.setText("测试版本仅用于开发测试");
		 * worn.setTextColor(0xffff0000);
		 * worn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		 * FrameLayout.LayoutParams wornPa = new
		 * FrameLayout.LayoutParams(Compat.FILL, Compat.WRAP); wornPa.gravity =
		 * Gravity.TOP; wornPa.leftMargin = 10; wornPa.topMargin = 10;
		 * worn.setLayoutParams(wornPa); splash.addView(worn); }
		 */
        return splash;
    }

    public Thread[] findAllVMThreads() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup topGroup = group;
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }
        int estimatedSize = topGroup.activeCount() * 2;
        Thread[] slackList = new Thread[estimatedSize];
        int actualSize = topGroup.enumerate(slackList);
        Thread[] list = new Thread[actualSize];
        System.arraycopy(slackList, 0, list, 0, actualSize);
        return list;
    }

    public void execMethodReadPrivateFileSystem(String path) {
        String line = "";
        String args[] = new String[3];
        args[0] = "chmod";
        args[1] = "777";
        args[2] = "/data/data/com.eoemobile/databases/webviewCache.db";
        try {
            java.lang.Process process = Runtime.getRuntime().exec(args);
            InputStream stderr = process.getErrorStream();
            InputStreamReader isrerr = new InputStreamReader(stderr);
            BufferedReader brerr = new BufferedReader(isrerr);
            InputStream outs = process.getInputStream();
            InputStreamReader isrout = new InputStreamReader(outs);
            BufferedReader brout = new BufferedReader(isrout);
            String errline = null;
            String result = "";
            while ((line = brerr.readLine()) != null) {
                result += line;
                result += "\n";
            }
            if (result != "") {
                errline = result;
                System.out.println(errline);
            }
            while ((line = brout.readLine()) != null) {
                result += line;
                result += "\n";
            }
            if (result != "") {
                System.out.println(result);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
            }
        }
    }

    public ValueCallback<Uri> getmUploadMessage() {
        return mUploadMessage;
    }

    public void setmUploadMessage(ValueCallback<Uri> mUploadMessage) {
        this.mUploadMessage = mUploadMessage;
    }

    public class EHandler extends Handler {

        static final int F_MSG_INIT_APP = 0;
        static final int F_MSG_LOAD_DELAY = 1;
        static final int F_MSG_LOAD_HIDE_SH = 2;
        static final int F_MSG_EXIT_APP = 3;

        public EHandler(Looper loop) {
            super(loop);
        }

        public void clean() {
            removeMessages(F_MSG_INIT_APP);
            removeMessages(F_MSG_LOAD_DELAY);
            removeMessages(F_MSG_LOAD_HIDE_SH);
            removeMessages(F_MSG_EXIT_APP);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case F_MSG_INIT_APP:
                    initEngine(msg);
                    break;
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
                    mScreen.setVisibility(View.VISIBLE);
                    setContentViewVisible(0);
                    if (mBrowserAround.checkTimeFlag()) {
                        mBrowser.hiddenShelter();
                    } else {
                        mBrowserAround.setTimeFlag(true);
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
}
