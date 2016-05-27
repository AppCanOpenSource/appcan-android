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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.slidingmenu.lib.SlidingMenu;

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BConstant;
import org.zywx.wbpalmstar.base.view.SwipeView;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExWidget.SpaceClickListener;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

public class EBrowserWidget extends AbsoluteLayout {

    public static final int F_WINDOW_POOL_TYPE_ROOT = 0;
    public static final int F_WINDOW_POOL_TYPE_NEW = 1;

    public static final int F_WIDGET_POOL_TYPE_ROOT = 0;
    public static final int F_WIDGET_POOL_TYPE_NEW = 1;

    public static final int F_WIDGET_FLAG_NONE = 0x0;
    public static final int F_WIDGET_FLAG_NEW = 0x1;
    public static final int F_WIDGET_FLAG_INIT = 0x2;

    private int mFlag;
    private int mWidgetType;
    private boolean mWidgetStatusBG;

    private EBrowser mBrw;
    private WidgetHandler mWidgetLoop;
    private WWidgetData mWidgetData;
    private EWgtResultInfo mResultInfo;
    private EBrowserWindow mBroWindow;
    private Context mContext;
    private EWindowStack mEWindowStack;
    private EWindGarbHeap mGarbViewHeap;
    private String mPushNotifyWindName;
    private String mPushNotifyFunctionName;
    private SharedPreferences mPres;

    public EBrowserWidget(Context context, WWidgetData inWidget,
                          EWgtResultInfo inResult) {
        super(context);
        mWidgetData = inWidget;
        mResultInfo = inResult;
        mContext = context;
        mEWindowStack = new EWindowStack();
        setAnimationCacheEnabled(false);
        setAlwaysDrawnWithCacheEnabled(false);
        EUtil.viewBaseSetting(this);
        mPres = mContext.getSharedPreferences("saveData",
                Context.MODE_MULTI_PROCESS);
        mPushNotifyWindName = mPres.getString(BConstant.F_PUSH_WIN_NAME, "");
        mPushNotifyFunctionName = mPres.getString(BConstant.F_PUSH_NOTI_FUN_NAME, "");
    }

    public boolean checkFlag(int flag) {

        return (mFlag & flag) != 0;
    }

    public void setFlag(int flag) {
        mFlag |= flag;
    }

    public void clearFlag() {

        mFlag &= F_WIDGET_FLAG_NONE;
    }

    public void init(EBrowser eBrw) {
        mBrw = eBrw;
        mWidgetLoop = new WidgetHandler(Looper.getMainLooper());
        EBrowserWindow rootWindow = new EBrowserWindow(mContext, this);
        AbsoluteLayout.LayoutParams parm = new AbsoluteLayout.LayoutParams(
                Compat.FILL, Compat.FILL, 0, 0);
        rootWindow.setLayoutParams(parm);
        rootWindow.setVisibility(VISIBLE);
        addView(rootWindow);
        rootWindow.setWindPoType(F_WINDOW_POOL_TYPE_ROOT);
        rootWindow.init(eBrw, null);
        rootWindow.setAbleToSwipe(false);
        windowStorage(rootWindow);
        mBroWindow = rootWindow;
    }

    public boolean checkWidgetType(int flag) {

        return mWidgetType == flag;
    }

    public void onCloseWindow(EBrowserWindow window) {
//		Message msg = mWidgetLoop.obtainMessage();
//		msg.what = F_WIDGET_HANDLER_WINDOW_CLOSE;
//		msg.obj = window;
//		mWidgetLoop.sendMessage(msg);

        closeWindow(window);
    }

    public void createSlidingWindow(EBrwViewEntry entry) {

        if (!entry.checkFlag(EBrwViewEntry.F_FLAG_HIDDEN)) {
            EBrowser.setFlag(EBrowser.F_BRW_FLAG_OPENING);
        }
        Message msg = mWidgetLoop.obtainMessage();
        msg.obj = entry;
        msg.what = F_WIDGET_HANDLER_WINDOW_CREATE_SLIDING;
        mWidgetLoop.sendMessage(msg);

    }

    public void createWindow(EBrowserWindow preWindow, EBrowserView target,
                             EBrwViewEntry entry) {

        if (!entry.checkFlag(EBrwViewEntry.F_FLAG_HIDDEN)) {
            EBrowser.setFlag(EBrowser.F_BRW_FLAG_OPENING);
        }
//		Message msg = mWidgetLoop.obtainMessage();
//		msg.obj = entry;
//		msg.what = F_WIDGET_HANDLER_WINDOW_CREATE;
//		mWidgetLoop.sendMessage(msg);

        boolean isRootWidget = checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);
        if (isRootWidget) {
            mBrw.windowOpenAnalytics(preWindow, entry);
        }

        createWindowInner(preWindow, target, entry);
    }

    public void createWindowInner(EBrowserWindow preWindow, EBrowserView target,
                                  EBrwViewEntry entry) {

        boolean hidden = entry.checkFlag(EBrwViewEntry.F_FLAG_HIDDEN);
        if (!hidden) {
            mEWindowStack.clearFractureLink();
        }
        if (checkWindow(entry)) {
            return;
        }
        EBrowserWindow newWindow = getInvalid();
        if (null != newWindow) {
            newWindow.setEBrowserWidget(EBrowserWidget.this);
        } else {
            newWindow = new EBrowserWindow(mContext,
                    EBrowserWidget.this);
        }
        if (entry.checkFlag(EBrwViewEntry.F_FLAG_NAV_TYPE)) {
            EBrowserWindow.sNavFlag = true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            newWindow.setX(0f);
        }
        boolean prevHidden = entry
                .checkFlag(EBrwViewEntry.F_FLAG_NOT_HIDDEN);
//		if (entry.mBgColor==null){
//			prevHidden=true;
//		}
        final EBrowserWindow finalNewWindow = newWindow;
        newWindow.setOnViewClosedListener(new SwipeView.OnViewClosedListener() {
            @Override
            public void onViewClosed() {
                finalNewWindow.onCloseWindow(0, 0);
            }
        });
        newWindow.setPrevWindowWillHidden(prevHidden);
        newWindow.setDateType(entry.mDataType);
        newWindow.setWindPoType(F_WINDOW_POOL_TYPE_NEW);
        newWindow.setAnimId(entry.mAnimId);
        newWindow.setAnimDuration(entry.mAnimDuration);
        newWindow.setHidden(hidden);
        if (entry.checkFlag(EBrwViewEntry.F_FLAG_OAUTH)) {
            newWindow.setOAuth(true);
            newWindow.registUrlChangeNotify(entry.mPreWindName);
        }

        if (entry.checkFlag(EBrwViewEntry.F_FLAG_PREOP)) {
            newWindow.setFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP);
        }
        if (entry.checkFlag(EBrwViewEntry.F_FLAG_OPAQUE)) {
            newWindow.setBackgroundColor(0xFFFFFFFF);
        }
        LayoutParams parm = new LayoutParams(
                Compat.FILL, Compat.FILL, 0, 0);
        newWindow.setLayoutParams(parm);
        newWindow.init(mBrw, entry);
        newWindow.setWindowHWEnable(entry.mHardware);
        if (entry.checkFlag(EBrwViewEntry.F_FLAG_GESTURE)) {
            newWindow.setSupportZoom();
        }
        if (entry.checkFlag(EBrwViewEntry.F_FLAG_SHOULD_OP_SYS)) {
            newWindow.setShouldOpenUrlInSystem(true);
        }
        newWindow.setVisibility(INVISIBLE);
        newWindow.setQuery(EBrwViewEntry.VIEW_TYPE_MAIN, entry.mQuery);
        ViewParent parent = newWindow.getParent();
        if (null == parent) {
            addView(newWindow);
        }
        addWindow(newWindow, hidden);
//        Delay ent = new Delay(newWindow, entry);
//        sendMessageDelayed(
//                obtainMessage(F_WIDGET_HANDLER_LOAD_DELAY, ent), 90);


//        //动画行为
//        int animId = newWindow.getAnimId();
//        long duration = newWindow.getAnimDuration();
//        Animation[] animPair = EBrowserAnimation.getAnimPair(animId, duration);
//
//        newWindow.startAnimation(animPair[0]);
//        newWindow.setVisibility(VISIBLE);
//        mBroWindow.startAnimation(animPair[1]);

        EBrwViewEntry enty = entry;
        EBrowserWindow wind = newWindow;
        if (EBrwViewEntry.isUrl(entry.mDataType)) {
//					if (enty.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION)) {
            if (getWidget().m_obfuscation == 1) {
                wind.needToEncrypt(enty.mData);
            } else {
                wind.start(enty.mData);
            }
        } else {
            wind.loadDataWithBaseURL(null, enty.mData, "text/html", "utf-8", null);
        }

    }

    public void setWindowFrame(EBrowserWindow window, int x, int y, int duration) {
        EViewEntry en = new EViewEntry();
        en.x = x;
        en.y = y;
        en.duration = duration;
        en.obj = window;
        Message msg = mWidgetLoop.obtainMessage(F_WIDGET_HANDLER_SET_WINDOW);
        msg.obj = en;
        msg.sendToTarget();
    }

    public EBrowserWindow getEBrowserWindow(String inWindowName) {

        return mEWindowStack.get(inWindowName);
    }

    public void notifyWindowStart(EBrowserWindow window, EBrowserView view,
                                  String url) {
        ;
    }

    public void notifyWindowFinish(EBrowserWindow window, EBrowserView view,
                                   String url) {
        if (!window.isHidden()) {
            boolean issame = window
                    .checkFlag(EBrowserWindow.F_WINDOW_FLAG_SAME);
            boolean isnew = window.checkFlag(EBrowserWindow.F_WINDOW_FLAG_NEW);
            boolean isSliding = window.checkFlag(EBrowserWindow.F_WINDOW_FLAG_SLIDING_WIN);

            if (isSliding) {
                EBrowser.clearFlag();
                window.clearHistory();
            } else if (issame || isnew) {
                showOnly(window, SHOW_TYPE_NEXT);
                window.clearHistory();
            } else if (checkFlag(F_WIDGET_FLAG_NEW)) {
                mBrw.showWidget();
            } else if (checkFlag(F_WIDGET_FLAG_INIT)) {
                mBrw.hiddenShelter();
                if (mBrw.isFromPush()) {
                    mBrw.setFromPush(false);
                    mBrw.pushNotify(EBrowserActivity.APP_TYPE_START_FORGROUND);
                }
            }
            clearFlag();
        } else {
            window.clearHistory();
            clearFlag();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        mBroWindow.notifyScreenOrientationChange(newConfig);
    }

    private void closeWindow(EBrowserWindow window) {
        int anim = window.getAnimId();
        if (window.isAnimFill()) {
            anim = EBrowserAnimation.contrary(anim);
            window.setAnimId(anim);
        }
        if (window == mBroWindow) {
            showOnly(window, SHOW_TYPE_CLOSE);
        } else {
            putInvalid(window);
        }
        mEWindowStack.remove(window);
//		putInvalid(window);
        EBrowser.clearFlag();
    }

    public void goMySpace(View view) {
        addView(view);
        TranslateAnimation anim = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        anim.setDuration(250);
        DecelerateInterpolator di = new DecelerateInterpolator();
        anim.setInterpolator(di);
        view.startAnimation(anim);
        mBroWindow.setVisibility(GONE);
    }

    public boolean exitMySpace(View view) {
        if (view.getParent() == this) {
            mBroWindow.setVisibility(VISIBLE);
            TranslateAnimation anim = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 0.0f,
                    Animation.RELATIVE_TO_PARENT, 1.0f);
            anim.setDuration(300);
            DecelerateInterpolator di = new DecelerateInterpolator();
            anim.setInterpolator(di);
            view.startAnimation(anim);
            removeView(view);
            return true;
        }
        return false;
    }

    public boolean goBack() {
        if (mBroWindow.canGoBack()) {
            mBroWindow.goBack();
            return true;
        } else {
            if (mBroWindow.checkWindPoType(F_WINDOW_POOL_TYPE_NEW)) {
                mBroWindow.setAnimFill(true);
                closeWindow(mBroWindow);
                return true;
            } else {
                EBrowserWindow preWind = mEWindowStack.prev(mBroWindow);
                if (null != preWind) {
                    int aid = preWind.getAnimId();
                    aid = EBrowserAnimation.contrary(aid);
                    preWind.setAnimId(aid);
                    showOnly(preWind, SHOW_TYPE_PRE);
                    return true;
                }
            }
        }
        return false;
    }

    public void goForward() {
        if (mBroWindow.canGoForward()) {
            mBroWindow.goForward();
        }
    }

    public void windowGoBack(EBrowserWindow inWhich, int inAnimID, long duration) {
        if (inWhich != mBroWindow)
            return;
        EBrowserWindow preWind = mEWindowStack.prev(mBroWindow);
        if (null == preWind)
            return;
        int animId = inWhich.getAnimId();
        if (EBrowserAnimation.isFillAnim(inAnimID)) {
            inAnimID = EBrowserAnimation.contrary(animId);
            preWind.setAnimFill(true);
        }
        preWind.setAnimId(inAnimID);
        preWind.setAnimDuration(duration);
        Message msg = mWidgetLoop.obtainMessage();
        msg.what = F_WIDGET_HANDLER_WINDOW_HISTROY;
        msg.obj = preWind;
        msg.arg1 = 0;
        mWidgetLoop.sendMessage(msg);

        mBrw.windowBackAnalytics(preWind, mBroWindow);
    }

    public void windowGoForward(EBrowserWindow inWhich, int inAnimitionID,
                                long duration) {
        if (inWhich != mBroWindow)
            return;
        EBrowserWindow nextWind = mEWindowStack.next(mBroWindow);
        if (null == nextWind)
            return;
        nextWind.setAnimId(inAnimitionID);
        nextWind.setAnimDuration(duration);
        Message msg = mWidgetLoop.obtainMessage();
        msg.what = F_WIDGET_HANDLER_WINDOW_HISTROY;
        msg.obj = nextWind;
        msg.arg1 = 1;
        mWidgetLoop.sendMessage(msg);

        mBrw.windowBackAnalytics(mBroWindow, nextWind);
    }

    public void insertWindowAboveWindow(String wName1, String wName2) {
        EBrowserWindow bv = mEWindowStack.get(wName1);
        EBrowserWindow bv1 = mEWindowStack.get(wName2);
        if (bv != null && bv1 != null) {
            EViewEntry ent = new EViewEntry();
            ent.obj = bv;
            ent.obj1 = bv1;
            Message msg = mWidgetLoop.obtainMessage();
            msg.what = F_WIDGET_HANDLER_INSERT_WINDOW_ABOVE_POPOVER;
            msg.obj = ent;
            mWidgetLoop.sendMessage(msg);
        }
    }

    public void insertWindowBelowWindow(String wName1, String wName2) {
        EBrowserWindow bv = mEWindowStack.get(wName1);
        EBrowserWindow bv1 = mEWindowStack.get(wName2);
        if (bv != null && bv1 != null) {
            EViewEntry ent = new EViewEntry();
            ent.obj = bv;
            ent.obj1 = bv1;
            Message msg = mWidgetLoop.obtainMessage();
            msg.what = F_WIDGET_HANDLER_INSERT_WINDOW_BELOW_POPOVER;
            msg.obj = ent;
            mWidgetLoop.sendMessage(msg);
        }
    }

    public void stopLoad() {

        mBroWindow.stopLoad();
    }

    public void refresh() {

        mBroWindow.refresh();
    }

    public boolean canGoBack() {

        return mBroWindow.canGoBack();
    }

    public boolean canGoForward() {

        return mBroWindow.canGoForward();
    }

    public void onAppPause() {
        mBroWindow.onAppPause();
        EBrowserWindow root = mEWindowStack.get("root");
        if (mBroWindow != root) {
            root.onAppPause();
        }
    }

    public void onAppStop() {
        mBroWindow.onAppStop();
        EBrowserWindow root = mEWindowStack.get("root");
        if (mBroWindow != root) {
            root.onAppStop();
        }
    }

    public void onAppResume() {
        mBroWindow.onAppResume();
        EBrowserWindow root = mEWindowStack.get("root");
        if (mBroWindow != root) {
            root.onAppResume();
        }
    }

    public void onWidgetResult(String callback, String inResultInfo) {
        mBroWindow.onWidgetResult(callback, inResultInfo);
    }

    public void onAppKeyPress(int keyCode) {
        mBroWindow.onAppKeyPress(keyCode);
    }

    public boolean isLockBackKey() {
        return mBroWindow.isLockBackKey();
    }

    public boolean isLockMenuKey() {
        return mBroWindow.isLockMenuKey();
    }

    public EBrowserView getEBrowserView() {
        return mBroWindow.getMainView();
    }

    public void start() {
        String indexUrl = mWidgetData.m_indexUrl;
        if (null != indexUrl && indexUrl.startsWith("http")) {
            mBroWindow.setBackgroundColor(0xFFFFFFFF);
        }
        mBroWindow.start(indexUrl);

        boolean isRootWidget = checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);
        if (isRootWidget) {
            mBrw.startAnalytics(indexUrl);
        }
    }

    public WWidgetData getWidget() {

        return mWidgetData;
    }

    public WWidgetData getRootWidget() {

        return mBrw.getRootWidget();
    }

    public EWidgetStack getWidgetStack() {
        return mBrw.getWidgetStack();
    }

    public EWgtResultInfo getResult() {

        return mResultInfo;
    }

    public void evaluateScript(WebView inWhich, String inWindowName,
                               int inType, String inScript) {
        EBrowserWindow window = mEWindowStack.get(inWindowName);
        if (null != window) {
            window.evaluateScript(inWhich, null, inType, inScript);
        }
    }

    public void evaluatePopoverScript(WebView inWhich, String inWndName,
                                      String inPopName, String inScript) {
        EBrowserWindow window = mEWindowStack.get(inWndName);
        if (null != window) {
            window.evaluatePopoverScript(inWhich, null, inPopName, inScript);
        }
    }

    public void evaluateMultiPopoverScript(WebView inWhich, String inWndName,
                                           String inMultiPopName, String inPopName, String inScript) {
        EBrowserWindow window = mEWindowStack.get(inWndName);
        if (null != window) {
            window.evaluateMultiPopoverScript(inWhich, null, inMultiPopName, inPopName, inScript);
        }
    }

    public void setPushNotify(String windName, String function, String appId) {
        mPushNotifyWindName = windName;
        mPushNotifyFunctionName = function;
        SharedPreferences.Editor editor = mPres.edit();
        editor.putString(BConstant.F_PUSH_APPID, appId);
        editor.putString(BConstant.F_PUSH_WIN_NAME, windName);
        editor.putString(BConstant.F_PUSH_NOTI_FUN_NAME, function);
        editor.commit();
    }

    public void pushNotify(String info) {
        EBrowserWindow beNotify = mEWindowStack.get(mPushNotifyWindName);
        if (null != beNotify) {
            beNotify.pushNotify(mPushNotifyFunctionName, info);
        }
    }

    public void uexOnAuthorize(String id) {
        EBrowserWindow beNotify = mEWindowStack.get("root");
        beNotify.uexOnAuthorize(id);
    }

    public void dumpPageInfo(int type) {
        mBroWindow.dumpPageInfo(type);
    }

    public void notifyVisibilityChanged(int visibility) {

        mBroWindow.notifyVisibilityChanged(visibility);
    }

    public void setWidgetType(int inType) {
        mWidgetType = inType;
    }

    public int getWidgetType() {

        return mWidgetType;
    }

    public void setWidgetStatusBG(boolean statusBG) {
        mWidgetStatusBG = statusBG;
    }

    public boolean getWidgetStatusBG() {

        return mWidgetStatusBG;
    }

    public EWindowStack getWindowStack() {

        return mEWindowStack;
    }

    public static final int SHOW_TYPE_CLOSE = 0;
    public static final int SHOW_TYPE_NEXT = 1;
    public static final int SHOW_TYPE_PRE = -1;

    private void showOnly(EBrowserWindow inWhich, int type) {

        int animId = inWhich.getAnimId();
        long duration = inWhich.getAnimDuration();
        Animation[] animPair = EBrowserAnimation.getAnimPair(animId, duration);
        switch (type) {
            case SHOW_TYPE_NEXT:// next
                if (inWhich == mBroWindow)
                    break;
                if (inWhich.checkFlag(EBrowserWindow.F_WINDOW_FLAG_NEW)
                        && !inWhich.checkFlag(EBrowserWindow.F_WINDOW_FLAG_SAME)) {
                    if (null != animPair[0]) {
                        // animPair[0].setStartOffset(300);
                    }
                    if (null != animPair[1]) {
                        // animPair[1].setStartOffset(300);
                    }
                }
                inWhich.startAnimation(animPair[0]);
                inWhich.setVisibility(VISIBLE);
                mBroWindow.startAnimation(animPair[1]);
                invalidate();

                if (EBrowserWindow.sNavFlag) {
                    setPreWindVisible(mBroWindow, GONE);
                } else {
                    if (!inWhich.isPrevWindowWillHidden()) {
                        mBroWindow.setVisibility(GONE);
                    }
                }

                inWhich.notifyVisibilityChanged(0);
                inWhich.clearFlag();
                mBroWindow.notifyVisibilityChanged(1);
                mBroWindow = null;
                mBroWindow = inWhich;
                break;
            case SHOW_TYPE_PRE:// back
                if (inWhich == mBroWindow)
                    break;
                inWhich.startAnimation(animPair[0]);
                inWhich.setVisibility(VISIBLE);
                inWhich.notifyVisibilityChanged(0);
                mBroWindow.startAnimation(animPair[1]);
                invalidate();
                mBroWindow.setFlag(EBrowserWindow.F_WINDOW_FLAG_WILL_REMOWE);
                mBroWindow.setVisibility(GONE);
                if (EBrowserWindow.sNavFlag) {
                    setPreWindVisible(inWhich, VISIBLE);
                }
                mBroWindow.notifyVisibilityChanged(1);
                mBroWindow = null;
                mBroWindow = inWhich;
                break;
            case SHOW_TYPE_CLOSE:// close
                EBrowserWindow preWind = mEWindowStack.prev(inWhich);
                if (null == preWind) {
                    preWind = mEWindowStack.next(inWhich);
                    if (null == preWind)
                        break;
                }
                preWind.startAnimation(animPair[0]);
                preWind.setVisibility(VISIBLE);
                preWind.notifyVisibilityChanged(0);
//			mBroWindow.startAnimation(animPair[1]);
                mBroWindow.closeWindowByAnimation(animPair[1]);

                if (EBrowserWindow.sNavFlag) {
                    setPreWindVisible(preWind, VISIBLE);
                }
//			invalidate();
//			inWhich.setVisibility(GONE);

                boolean isRootWidget = checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);
                if (isRootWidget) {
                    mBrw.windowCloseAnalytics(preWind, mBroWindow);
                }

                mBroWindow = null;
                mBroWindow = preWind;
                break;
        }
        EBrowser.clearFlag();
    }

    private void setPreWindVisible(EBrowserWindow currentWind, int visible) {
        //侧滑关闭需要把上一个也显示出来
        EBrowserWindow prePreWind = mEWindowStack.prev(currentWind);
        if (prePreWind != null) {
            prePreWind.setVisibility(visible);
        }
    }


    private boolean checkWindow(EBrwViewEntry inEntry) {
        EBrowserWindow window = mEWindowStack.contains(inEntry.mWindName);
        if (null != window) {
            window.setDateType(inEntry.mDataType);
            window.setAnimId(inEntry.mAnimId);
            window.setAnimDuration(inEntry.mAnimDuration);
            boolean hidden = inEntry.checkFlag(EBrwViewEntry.F_FLAG_HIDDEN);
            window.setHidden(hidden);
            boolean prevHidden = inEntry
                    .checkFlag(EBrwViewEntry.F_FLAG_NOT_HIDDEN);
            window.setPrevWindowWillHidden(prevHidden);
            addWindow(window, hidden);
            boolean urlEmpty = inEntry.checkData();
            boolean isReload = inEntry.checkFlag(EBrwViewEntry.F_FLAG_RElOAD);
            if (!urlEmpty && !hidden && !isReload) {
                showOnly(window, SHOW_TYPE_NEXT);
                return true;
            }
            window.setFlag(EBrowserWindow.F_WINDOW_FLAG_SAME);
            if (inEntry.checkDataType(EBrwViewEntry.WINDOW_DATA_TYPE_URL)) {
                if (getWidget().m_obfuscation == 1) {
                    window.needToEncrypt(inEntry.mData);
                } else {
                    String url = window.getAbsoluteUrl();
                    boolean b1 = null != url && !url.equals(inEntry.mData);
                    boolean b2 = inEntry.checkFlag(EBrwViewEntry.F_FLAG_RElOAD);
                    if (b2 || b1) {
                        if (null != inEntry.mData
                                && 0 != inEntry.mData.length()) {
                            window.start1(inEntry.mData);
                        } else {
                            window.start1(url);
                        }
                    } else {
                        if (!hidden) {
                            showOnly(window, SHOW_TYPE_NEXT);
                        }
                    }
                }
            } else {
                window.newLoadData(inEntry.mData);
            }
            return true;
        }
        return false;
    }

    private void addWindow(EBrowserWindow inWindow, boolean isHidden) {
        if (isHidden) {
            mEWindowStack.addOnlyMap(inWindow);
        } else {
            windowStorage(inWindow);
        }
        inWindow.setFlag(EBrowserWindow.F_WINDOW_FLAG_NEW);
    }

    private void windowStorage(EBrowserWindow inWindow) {

        mEWindowStack.add(inWindow);
    }

    public void putInvalid(EBrowserWindow view) {
        if (null == mGarbViewHeap) {
            mGarbViewHeap = new EWindGarbHeap();
        }
        mGarbViewHeap.put(view);
    }

    private EBrowserWindow getInvalid() {
        if (null != mGarbViewHeap) {
            return mGarbViewHeap.get();
        }
        return null;
    }

    public void destroy() {
        mWidgetLoop.clean();
        if (null != mGarbViewHeap) {
            mGarbViewHeap.destroy();
        }
        mEWindowStack.destroy();
        mResultInfo = null;
        EBrowser.clearFlag();
    }

    //	public static final int F_WIDGET_HANDLER_WINDOW_CREATE = 0;
//	public static final int F_WIDGET_HANDLER_WINDOW_CLOSE = 1;
    public static final int F_WIDGET_HANDLER_WINDOW_HISTROY = 2;
    public static final int F_WIDGET_HANDLER_SHOW_DELAY = 3;
    public static final int F_WIDGET_HANDLER_LOAD_DELAY = 4;
    public static final int F_WIDGET_HANDLER_SET_WINDOW = 5;


    public static final int F_WIDGET_HANDLER_INSERT_WINDOW_ABOVE_POPOVER = 6;
    public static final int F_WIDGET_HANDLER_INSERT_WINDOW_BELOW_POPOVER = 7;
    public static final int F_WIDGET_HANDLER_WINDOW_CREATE_SLIDING = 8;

    public class WidgetHandler extends Handler {

        public WidgetHandler(Looper loop) {
            super(loop);
        }

        public void clean() {
//			removeMessages(F_WIDGET_HANDLER_WINDOW_CREATE);
//			removeMessages(F_WIDGET_HANDLER_WINDOW_CLOSE);
            removeMessages(F_WIDGET_HANDLER_WINDOW_HISTROY);
            removeMessages(F_WIDGET_HANDLER_SHOW_DELAY);
            removeMessages(F_WIDGET_HANDLER_LOAD_DELAY);
            removeMessages(F_WIDGET_HANDLER_SET_WINDOW);
            removeMessages(F_WIDGET_HANDLER_INSERT_WINDOW_ABOVE_POPOVER);
            removeMessages(F_WIDGET_HANDLER_INSERT_WINDOW_BELOW_POPOVER);
            removeMessages(F_WIDGET_HANDLER_WINDOW_CREATE_SLIDING);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
//			case F_WIDGET_HANDLER_WINDOW_CREATE:// create
//            {
//				EBrwViewEntry entry = (EBrwViewEntry) msg.obj;
//				boolean hidden = entry.checkFlag(EBrwViewEntry.F_FLAG_HIDDEN);
//				if (!hidden) {
//					mEWindowStack.clearFractureLink();
//				}
//				if (checkWindow(entry)) {
//					break;
//				}
//				EBrowserWindow newWindow = getInvalid();
//				if (null != newWindow) {
//					newWindow.setEBrowserWidget(EBrowserWidget.this);
//				} else {
//					newWindow = new EBrowserWindow(mContext,
//							EBrowserWidget.this);
//				}
//				boolean prevHidden = entry
//						.checkFlag(EBrwViewEntry.F_FLAG_NOT_HIDDEN);
//				newWindow.setPrevWindowWillHidden(prevHidden);
//				newWindow.setDateType(entry.mDataType);
//				newWindow.setWindPoType(F_WINDOW_POOL_TYPE_NEW);
//				newWindow.setAnimId(entry.mAnimId);
//				newWindow.setAnimDuration(entry.mAnimDuration);
//				newWindow.setHidden(hidden);
//				if (entry.checkFlag(EBrwViewEntry.F_FLAG_OAUTH)) {
//					newWindow.setOAuth(true);
//					newWindow.registUrlChangeNotify(entry.mPreWindName);
//				}
//
//				if (entry.checkFlag(EBrwViewEntry.F_FLAG_PREOP)) {
//					newWindow.setFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP);
//				}
//				if (entry.checkFlag(EBrwViewEntry.F_FLAG_OPAQUE)) {
//					newWindow.setBackgroundColor(0xFFFFFFFF);
//				}
//				AbsoluteLayout.LayoutParams parm = new AbsoluteLayout.LayoutParams(
//						Compat.FILL, Compat.FILL, 0, 0);
//				newWindow.setLayoutParams(parm);
//				newWindow.init(mBrw, entry);
//				if (entry.checkFlag(EBrwViewEntry.F_FLAG_GESTURE)) {
//					newWindow.setSupportZoom();
//				}
//				if (entry.checkFlag(EBrwViewEntry.F_FLAG_SHOULD_OP_SYS)) {
//					newWindow.setShouldOpenUrlInSystem(true);
//				}
//				newWindow.setVisibility(INVISIBLE);
//				newWindow.setQuery(EBrwViewEntry.VIEW_TYPE_MAIN, entry.mQuery);
//				ViewParent parent = newWindow.getParent();
//				if (null == parent) {
//					addView(newWindow);
//				}
//				Bitmap map = mBrw.getImage(entry.mBgPath);
//				if (null != map) {
//					newWindow.setBackgroundDrawable(new BitmapDrawable(map));
//				}
//				addWindow(newWindow, hidden);
//				Delay ent = new Delay(newWindow, entry);
//				sendMessageDelayed(
//						obtainMessage(F_WIDGET_HANDLER_LOAD_DELAY, ent), 90);
//            }
//				break;
                case F_WIDGET_HANDLER_WINDOW_HISTROY:// window histroy
                    if (0 == msg.arg1) {// back
                        showOnly((EBrowserWindow) msg.obj, SHOW_TYPE_PRE);
                    } else { // forward
                        showOnly((EBrowserWindow) msg.obj, SHOW_TYPE_NEXT);
                    }
                    return;
                case F_WIDGET_HANDLER_SHOW_DELAY:
                    showOnly((EBrowserWindow) msg.obj, msg.arg1);
                    break;
                case F_WIDGET_HANDLER_LOAD_DELAY:
                    Delay dely = (Delay) msg.obj;
                    EBrwViewEntry enty = dely.mViewEnty;
                    EBrowserWindow wind = dely.mWind;
                    if (EBrwViewEntry.isUrl(enty.mDataType)) {
//					if (enty.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION)) {
                        if (getWidget().m_obfuscation == 1) {
                            wind.needToEncrypt(enty.mData);
                        } else {
                            wind.start(enty.mData);
                        }
                    } else {
                        wind.newLoadData(enty.mData);
                    }
                    break;
                case F_WIDGET_HANDLER_SET_WINDOW:
                    final EViewEntry en = (EViewEntry) msg.obj;
                    final EBrowserWindow window = (EBrowserWindow) en.obj;
                    AbsoluteLayout.LayoutParams oldLp = (LayoutParams) window
                            .getLayoutParams();
                    ObjectAnimator animatorX=ObjectAnimator.ofFloat(window,"translationX",oldLp.x,en.x);
                    ObjectAnimator animatorY=ObjectAnimator.ofFloat(window,"translationY",oldLp.y,en.y);
                    AnimatorSet animatorSet=new AnimatorSet();
                    animatorSet.playTogether(animatorX,animatorY);
                    animatorSet.setDuration(en.duration);
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            window.onSetWindowFrameFinish();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animatorSet.start();
                    break;
//			case F_WIDGET_HANDLER_WINDOW_CLOSE:// close window
//				closeWindow((EBrowserWindow) msg.obj);
//				break;
                case F_WIDGET_HANDLER_INSERT_WINDOW_ABOVE_POPOVER:
                    EViewEntry c = (EViewEntry) msg.obj;
                    View bn1 = (View) c.obj;
                    View bn2 = (View) c.obj1;
                    removeView(bn1);
                    int i2 = indexOfChild(bn2);
                    addView(bn1, i2 + 1);
                    break;
                case F_WIDGET_HANDLER_INSERT_WINDOW_BELOW_POPOVER:
                    EViewEntry d = (EViewEntry) msg.obj;
                    View bo1 = (View) d.obj;
                    View bo2 = (View) d.obj1;
                    removeView(bo1);
                    int j2 = indexOfChild(bo2);
                    addView(bo1, j2);
                    break;
                case F_WIDGET_HANDLER_WINDOW_CREATE_SLIDING: {
                    EBrowserActivity activity = (EBrowserActivity) mContext;

                    SlidingMenu slidingMenu = activity.globalSlidingMenu;

                    if (slidingMenu == null) {
                        return;
                    }

                    EBrwViewEntry entry = (EBrwViewEntry) msg.obj;
                    boolean hidden = entry.checkFlag(EBrwViewEntry.F_FLAG_HIDDEN);
                    if (!hidden) {
                        mEWindowStack.clearFractureLink();
                    }
                    if (checkWindow(entry)) {
                        break;
                    }
                    EBrowserWindow newWindow = getInvalid();
                    if (null != newWindow) {
                        newWindow.setEBrowserWidget(EBrowserWidget.this);
                    } else {
                        newWindow = new EBrowserWindow(mContext,
                                EBrowserWidget.this);
                    }
                    boolean prevHidden = entry
                            .checkFlag(EBrwViewEntry.F_FLAG_NOT_HIDDEN);
                    newWindow.setPrevWindowWillHidden(prevHidden);
                    newWindow.setSwipeEnabled(false);
                    newWindow.setDateType(entry.mDataType);
                    newWindow.setWindPoType(F_WINDOW_POOL_TYPE_NEW);
                    newWindow.setAnimId(entry.mAnimId);
                    newWindow.setAnimDuration(entry.mAnimDuration);
                    newWindow.setHidden(hidden);
                    if (entry.checkFlag(EBrwViewEntry.F_FLAG_OAUTH)) {
                        newWindow.setOAuth(true);
                        newWindow.registUrlChangeNotify(entry.mPreWindName);
                    }

                    if (entry.checkFlag(EBrwViewEntry.F_FLAG_PREOP)) {
                        newWindow.setFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP);
                    }
                    if (entry.checkFlag(EBrwViewEntry.F_FLAG_OPAQUE)) {
                        newWindow.setBackgroundColor(0xFFFFFFFF);
                    }
                    FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(
                            Compat.FILL, Compat.FILL);
                    newWindow.setLayoutParams(parm);
                    newWindow.init(mBrw, entry);
                    if (entry.checkFlag(EBrwViewEntry.F_FLAG_GESTURE)) {
                        newWindow.setSupportZoom();
                    }
                    if (entry.checkFlag(EBrwViewEntry.F_FLAG_SHOULD_OP_SYS)) {
                        newWindow.setShouldOpenUrlInSystem(true);
                    }
                    newWindow.setVisibility(VISIBLE);
                    newWindow.setQuery(EBrwViewEntry.VIEW_TYPE_MAIN, entry.mQuery);
                    ViewParent parent = newWindow.getParent();
                    if (null == parent) {

                        FrameLayout menuView = null;

                        if (entry.mWindName.equals(EBrowserWindow.rootLeftSlidingWinName)) {

                            slidingMenu.setMenu(newWindow);

                        } else if (entry.mWindName.equals(EBrowserWindow.rootRightSlidingWinName)) {

                            slidingMenu.setSecondaryMenu(newWindow);

                        }


                    }


//                windowStorage(newWindow);
                    mEWindowStack.addSlidingWindMap(newWindow);
                    newWindow.setFlag(EBrowserWindow.F_WINDOW_FLAG_SLIDING_WIN);

//                Delay ent = new Delay(newWindow, entry);
//                sendMessageDelayed(
//                        obtainMessage(F_WIDGET_HANDLER_LOAD_DELAY, ent), 90);

                    if (EBrwViewEntry.isUrl(entry.mDataType)) {
//					if (enty.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION)) {
                        if (getWidget().m_obfuscation == 1) {
                            newWindow.needToEncrypt(entry.mData);
                        } else {
                            newWindow.start(entry.mData);
                        }
                    } else {
                        newWindow.newLoadData(entry.mData);
                    }
                }
                break;
            }
        }
    }

    class Delay {
        public EBrowserWindow mWind;
        public EBrwViewEntry mViewEnty;

        public Delay(EBrowserWindow wind, EBrwViewEntry enty) {
            mWind = wind;
            mViewEnty = enty;
        }
    }

    public void onLoadAppData(JSONObject json) {
        // TODO Auto-generated method stub
        ELinkedList<EBrowserWindow> eBrwWins = getWindowStack().getAll();
        eBrwWins.get(eBrwWins.size() - 1).onLoadAppData(json);
    }

    public void setSpaceEnable(SpaceClickListener listener) {
        mBrw.setSpaceEnable(listener);
    }

    public void onSlidingWindowStateChanged(int position) {
        EBrowserWindow root = mEWindowStack.get("root");
        if (null != root) {
            root.onSlidingWindowStateChanged(position);
        }
    }

    public void reloadWidget() {
        //Sliding window
        EBrowserWindow leftSlidingWin = mEWindowStack
                .getSlidingWind(EBrowserWindow.rootLeftSlidingWinName);
        if (leftSlidingWin != null) {
            leftSlidingWin.reloadWindow();
        }
        EBrowserWindow rightSlidingWin = mEWindowStack
                .getSlidingWind(EBrowserWindow.rootRightSlidingWinName);
        if (rightSlidingWin != null) {
            rightSlidingWin.reloadWindow();
        }
        // normal window
        ELinkedList<EBrowserWindow> eBrwWins = mEWindowStack.getAll();
        for (int i = 0; i < eBrwWins.size(); i++) {
            EBrowserWindow eBrwWin = eBrwWins.get(i);
            eBrwWin.reloadWindow();
        }
    }
}
