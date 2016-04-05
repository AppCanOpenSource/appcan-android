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

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BConstant;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExWidget.SpaceClickListener;
import org.zywx.wbpalmstar.platform.myspace.MySpaceView;
import org.zywx.wbpalmstar.widgetone.WidgetOneApplication;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

public class EBrowserWidgetPool {

    private EBrowser mBrw;
    private EBrowserActivity mContext;
    private PoolHandler mWidPoolLoop;
    private FrameLayout mNativeWindow;
    private EBrowserAround mBrowserAround;
    private EWgtResultInfo mSpaceWidgetResu;
    private EWidgetStack mWgtStack;
    private WWidgetData mRootWidget;
    private MySpaceView mAppCenter;
    private EBrowserWidget mRootBrowserWidget;

    public EBrowserWidgetPool(EBrowser inBrw, FrameLayout window,
                              EBrowserAround inShelter) {
        mBrw = inBrw;
        mNativeWindow = window;
        mBrowserAround = inShelter;
        mSpaceWidgetResu = new EWgtResultInfo(null, null);
        mWgtStack = new EWidgetStack();
        mContext = (EBrowserActivity) window.getContext();
        mWidPoolLoop = new PoolHandler(Looper.getMainLooper());
    }

    public void init(WWidgetData inWidget) {
        mRootWidget = inWidget;
        EBrowserWidget rootWidget = new EBrowserWidget(mContext, inWidget,
                mSpaceWidgetResu);
        FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(
                Compat.FILL, Compat.FILL);
        rootWidget.setLayoutParams(parm);
        rootWidget.setVisibility(View.VISIBLE);
        mNativeWindow.addView(rootWidget);
        mRootBrowserWidget = rootWidget;
        rootWidget.init(mBrw);
        rootWidget.setWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);
        rootWidget.setFlag(EBrowserWidget.F_WIDGET_FLAG_INIT);
        addWidget(rootWidget);
    }

    public void notifyHiddenShelter() {
        if (mBrowserAround.checkTimeFlag()) {
            Message msg = mWidPoolLoop.obtainMessage();
            msg.what = F_WIDGET_POOL_LOOP_HIDDEN_STARTUP;
            mWidPoolLoop.sendMessage(msg);
        } else {
            mBrowserAround.setTimeFlag(true);
        }

    }

    public void dumpPageInfo(int type) {

        mWgtStack.peek().dumpPageInfo(type);
    }

    protected void clean() {
        mWidPoolLoop.removeMessages(F_WIDGET_POOL_LOOP_START_WIDGET);
        mWidPoolLoop.removeMessages(F_WIDGET_POOL_LOOP_FINISH_WIDGET);
        mWidPoolLoop.removeMessages(F_WIDGET_POOL_LOOP_HIDDEN_STARTUP);
        mWidPoolLoop.removeMessages(F_WIDGET_POOL_LOOP_SPACE_ENABLE);
        mWidPoolLoop = null;
        int size = mWgtStack.length();
        for (int i = 0; i < size; ++i) {
            mWgtStack.get(i).destroy();
        }
    }

    public void showHover(boolean isInSubWidget) {

        mBrowserAround.showHover(isInSubWidget);
    }

    public void hiddenHover(boolean isInSubWidget) {

        mBrowserAround.hiddenHover(isInSubWidget);
    }

    public void addWidget(EBrowserWidget inBrwWidget) {

        mWgtStack.add(inBrwWidget);
    }

    public WWidgetData getRootWidget() {

        return mRootWidget;
    }

    public EWidgetStack getWidgetStack() {
        return mWgtStack;
    }

    public EBrowserWidget getWidget(String inWidgetNmae) {

        return mWgtStack.get(inWidgetNmae);
    }

    public EBrowserWidget getWidget(int index) {

        return mWgtStack.get(index);
    }

    public void pushNotify(String appType) {
        SharedPreferences pres = mContext.getSharedPreferences("saveData",
                Context.MODE_MULTI_PROCESS);
        String appId = pres.getString(BConstant.F_PUSH_APPID, "");
        if (TextUtils.isEmpty(appId)) {
            mWgtStack.peek().pushNotify(appType);
        } else {
            EBrowserWidget widget = mWgtStack.get(appId);
            if (null != widget) {
                widget.pushNotify(appType);
            } else {
                mWgtStack.peek().pushNotify(appType);
            }
        }
    }

    public void uexOnAuthorize(String id) {

        mWgtStack.first().uexOnAuthorize(id);
    }

    public void startWidget(WWidgetData inData, EWgtResultInfo inResult) {
        if (checkWidget(inData, inResult)) {
            return;
        }
        WgtEnty obj = new WgtEnty(inData, inResult);
        Message msg = mWidPoolLoop.obtainMessage();
        msg.what = F_WIDGET_POOL_LOOP_START_WIDGET;
        msg.obj = obj;
        mWidPoolLoop.sendMessage(msg);

        // wgt report
        WidgetOneApplication app = (WidgetOneApplication) mContext
                .getApplicationContext();
        app.widgetReport(inData, mContext);
    }

    public void start() {

        mWgtStack.peek().start();
    }

    public void goMySpace() {
        if (null == mAppCenter) {
            mAppCenter = new MySpaceView(mBrw, mContext);
        }
        mWgtStack.peek().goMySpace(mAppCenter);
        hiddenHover(false);
    }

    public void exitMySpace() {
        if (null != mAppCenter) {
            if (mWgtStack.peek().exitMySpace(mAppCenter)) {
                showHover(false);
            }
        }
    }

    public boolean isSpaceShown() {
        if (null != mAppCenter) {
            return mAppCenter.isShown();
        }
        return false;
    }

    private boolean checkWidget(WWidgetData inData, EWgtResultInfo inResult) {
        String key = inData.m_appId;
        EBrowserWidget wdgObj = mWgtStack.get(key);
        if (null != wdgObj) {

            if (wdgObj.getWidgetStatusBG()) {

                WgtEnty obj = new WgtEnty(inData, inResult);
                Message msg = mWidPoolLoop.obtainMessage();
                msg.what = F_WIDGET_POOL_LOOP_RESTART_WIDGET;
                msg.obj = obj;
                mWidPoolLoop.sendMessage(msg);

                // //wgt report
                // WidgetOneApplication app =
                // (WidgetOneApplication)mContext.getApplicationContext();
                // app.widgetReport(inData, mContext);

            }

            return true;
        }
        return false;
    }

    public void setMySpaceInfo(String inForResult, String inAnimiId,
                               String inInfo) {
        int animId = EBrowserAnimation.ANIM_ID_5;
        try {
            animId = Integer.parseInt(inAnimiId);
        } catch (Exception e) {
            ;
        }
        mSpaceWidgetResu.setCallBack(inForResult);
        mSpaceWidgetResu.setAnimiId(animId);
        mSpaceWidgetResu.setOpenerInfo(inInfo);
    }

    public void showWidget() {
        if (null != mAppCenter && mAppCenter.isShown()) {
            mAppCenter.notifyWidgetLoadFinish();
        }
        final EBrowserWidget showWidget = mWgtStack.peek();
        final EBrowserWidget hiddenWidget = mWgtStack.prev();
        int animiId = 0;
        long duration = 0;
        EWgtResultInfo inResult = showWidget.getResult();
        if (null != inResult) {
            animiId = inResult.getAnimiId();
            duration = inResult.getDuration();
        }
        Animation[] animPair = EBrowserAnimation.getAnimPair(animiId, duration);
        Animation showAnim = animPair[0];
        Animation hiddenAnim = animPair[1];
        showAnim.setStartOffset(500);
        hiddenAnim.setStartOffset(500);
        hiddenAnim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                hiddenWidget.setVisibility(View.GONE);
                showWidget.notifyVisibilityChanged(0);
                hiddenWidget.notifyVisibilityChanged(1);
                showHover(true);
                WWidgetData wgtData = showWidget.getWidget();
                int wgtOrientation = wgtData.m_orientation;
                mContext.changeConfiguration(wgtOrientation);
            }
        });
        showWidget.setVisibility(View.VISIBLE);
        hiddenWidget.startAnimation(hiddenAnim);
        showWidget.startAnimation(showAnim);
    }

    public void goBack() {
        if (null != mAppCenter && mAppCenter.isShown()) {
            exitMySpace();
            return;
        }
        if (!mWgtStack.peek().goBack()) {
            closeWidget("", null, false);
        }
    }

    private void closeWidget(String inResultInfo, String appId, boolean isWdgBG) {
        EBrowserWidget target = null;
        if (null != appId) {
            target = getWidget(appId);
        } else {
            target = mWgtStack.peek();
        }
        if (null == target) {
            BDebug.e("EBrowserWidgetPool", "appId is not exsit!!!");
            return;
        }
        if (target.checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_NEW)) {
            target.setWidgetStatusBG(isWdgBG);
            WgtEnty entry = new WgtEnty(null, null);
            entry.mObj = target;
            entry.mObj1 = inResultInfo;
            Message msg = mWidPoolLoop.obtainMessage();
            msg.what = F_WIDGET_POOL_LOOP_FINISH_WIDGET;
            msg.arg1 = 0;
            msg.obj = entry;
            mWidPoolLoop.sendMessage(msg);
        } else {
            ((EBrowserActivity) mContext).exitApp(true);
        }
    }

    public void finishWidget(String inResultInfo, String appId, boolean isWdgBG) {

        closeWidget(inResultInfo, appId, isWdgBG);
    }

    private void closeCurrentWidget(EBrowserWidget closeWgt,
                                    final String inResultInfo) {
        EBrowser.clearFlag();
        final EBrowserWidget outWidget;
        final EBrowserWidget inWidget;
        if (null != closeWgt) {
            outWidget = closeWgt;
            int outIndex = mWgtStack.indexOf(outWidget);
            inWidget = mWgtStack.get(outIndex - 1);
        } else {
            outWidget = mWgtStack.peek();
            inWidget = mWgtStack.prev();
        }
        if (null == inWidget || null == outWidget) {
            return;
        }
        final EWgtResultInfo reInfo = outWidget.getResult();
        int animiId = 0;
        long duration = 0;
        if (null != reInfo) {
            animiId = reInfo.getContraryAnimiId();
            duration = reInfo.getDuration();
        }
        Animation[] animPair = EBrowserAnimation.getAnimPair(animiId, duration);
        Animation inAnim = animPair[0];
        Animation outAnim = animPair[1];
        outAnim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                outWidget.removeAllViews();
                outWidget.destroy();
                mWgtStack.remove(outWidget);
                mNativeWindow.removeView(outWidget);
                inWidget.notifyVisibilityChanged(0);
                if (inWidget
                        .checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT)) {
                    if (null == mAppCenter
                            || (null != mAppCenter && !mAppCenter.isShown())) {
                        showHover(false);
                    } else {
                        mAppCenter.notifyBackToAppCenter();
                        hiddenHover(false);
                        return;
                    }
                }
                String callback = null;
                if (null != reInfo) {
                    callback = reInfo.getCallBack();
                }
                inWidget.onWidgetResult(callback, inResultInfo);

                WWidgetData wgtData = inWidget.getWidget();
                int wgtOrientation = wgtData.m_orientation;
                mContext.changeConfiguration(wgtOrientation);
            }
        });
        inWidget.setVisibility(View.VISIBLE);
        outWidget.startAnimation(outAnim);
        inWidget.startAnimation(inAnim);

        try {
            int versionA = Build.VERSION.SDK_INT;
            if (versionA == 19) {
                InputMethodManager imm = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(((EBrowserActivity) mContext)
                                .getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goForward() {
        mWgtStack.peek().goForward();
    }

    public void stopLoad() {
        mWgtStack.peek().stopLoad();
    }

    public void refresh() {
        mWgtStack.peek().refresh();
    }

    @SuppressLint("NewApi")
    public void onConfigurationChanged(Configuration newConfig) {
        mWgtStack.peek().onConfigurationChanged(newConfig);
    }

    public void onAppPause() {
        mWgtStack.peek().onAppPause();
    }

    public void onAppStop() {
        mWgtStack.peek().onAppStop();
    }

    public void onAppResume() {
        mWgtStack.peek().onAppResume();
    }

    public void onAppKeyPress(int keyCode) {

        mWgtStack.peek().onAppKeyPress(keyCode);
    }

    public boolean isLockBackKey() {
        return mWgtStack.peek().isLockBackKey();
    }

    public boolean isLockMenuKey() {
        return mWgtStack.peek().isLockMenuKey();
    }

    public EBrowserView getEBrowserView() {
        return mWgtStack.peek().getEBrowserView();
    }

    public MySpaceView getAppCentView() {
        return mAppCenter;
    }

    static final int F_WIDGET_POOL_LOOP_START_WIDGET = 0;
    static final int F_WIDGET_POOL_LOOP_FINISH_WIDGET = 1;
    static final int F_WIDGET_POOL_LOOP_HIDDEN_STARTUP = 2;
    static final int F_WIDGET_POOL_LOOP_RESTART_WIDGET = 3;
    static final int F_WIDGET_POOL_LOOP_SPACE_ENABLE = 4;

    class PoolHandler extends Handler {

        public PoolHandler(Looper loop) {
            super(loop);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case F_WIDGET_POOL_LOOP_START_WIDGET: {
                    WgtEnty ms = (WgtEnty) msg.obj;
                    EBrowserWidget newWidget = new EBrowserWidget(mContext,
                            ms.m_data, ms.m_resultInfo);
                    newWidget.init(mBrw);
                    FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(
                            Compat.FILL, Compat.FILL);
                    newWidget.setLayoutParams(parm);
                    newWidget.setVisibility(View.INVISIBLE);
                    newWidget.setWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_NEW);
                    newWidget.setFlag(EBrowserWidget.F_WIDGET_FLAG_NEW);
                    mNativeWindow.addView(newWidget);
                    addWidget(newWidget);
                    newWidget.start();
                }
                break;
                case F_WIDGET_POOL_LOOP_FINISH_WIDGET: {
                    if (msg.arg1 == 0) {
                        WgtEnty entry = (WgtEnty) msg.obj;
                        EBrowserWidget currentWdgt = (EBrowserWidget) entry.mObj;

                        if (currentWdgt.getWidgetStatusBG()) {

                            mWgtStack.remove(mRootBrowserWidget);
                            mWgtStack.add(mRootBrowserWidget);

                            mNativeWindow.removeView(mRootBrowserWidget);
                            mRootBrowserWidget.setVisibility(View.VISIBLE);
                            mNativeWindow.addView(mRootBrowserWidget);

                            // rootWidget.invalidate();

                            // ??????widget???????
                            for (int i = 0; i < mWgtStack.length(); i++) {
                                EBrowserWidget tmpWdg = getWidget(i);

                                boolean isRootWidget = tmpWdg
                                        .checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);

                                if (!isRootWidget) {
                                    tmpWdg.setWidgetStatusBG(true);
                                }
                            }

                        } else {
                            closeCurrentWidget((EBrowserWidget) entry.mObj,
                                    (String) entry.mObj1);
                        }

                    }
                }
                break;
                case F_WIDGET_POOL_LOOP_HIDDEN_STARTUP: {
                    mBrowserAround.hiddenSplashScreen(mRootWidget.m_spaceStatus);
                }

                break;
                case F_WIDGET_POOL_LOOP_RESTART_WIDGET: {
                    WgtEnty ms = (WgtEnty) msg.obj;
                    String key = ms.m_data.m_appId;

                    EBrowserWidget widgetObj = mWgtStack.get(key);

                    if (widgetObj.getWidgetStatusBG()) {
                        widgetObj.setWidgetStatusBG(false);

                        mWgtStack.remove(widgetObj);
                        mWgtStack.add(widgetObj);

                        mNativeWindow.removeView(widgetObj);

                        widgetObj.setVisibility(View.VISIBLE);
                        mNativeWindow.addView(widgetObj);
                        // widgetObj.invalidate();

                    }

                }
                break;
                case F_WIDGET_POOL_LOOP_SPACE_ENABLE: {
                    mBrowserAround.setSpaceEnable((SpaceClickListener) msg.obj);
                }
                break;
            }
        }
    }

    public static class WgtEnty {
        public WWidgetData m_data;
        public EWgtResultInfo m_resultInfo;
        public Object mObj;
        public Object mObj1;

        public WgtEnty(WWidgetData data, EWgtResultInfo resultInfo) {
            m_data = data;
            m_resultInfo = resultInfo;
        }
    }

    public void onLoadAppData(JSONObject json) {
        // TODO Auto-generated method stub

        mWgtStack.peek().onLoadAppData(json);

    }

    public void setSpaceEnable(SpaceClickListener listener) {
        Message msg = mWidPoolLoop.obtainMessage();
        msg.what = F_WIDGET_POOL_LOOP_SPACE_ENABLE;
        msg.obj = listener;
        mWidPoolLoop.sendMessage(msg);
    }

    public void onSlidingWindowStateChanged(int position) {
        if (null != mWgtStack) {
            EBrowserWidget eBrWidget = mWgtStack.peek();
            if (null != eBrWidget) {
                eBrWidget.onSlidingWindowStateChanged(position);
            }
        }
    }
}
