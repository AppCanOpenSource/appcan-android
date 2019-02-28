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

package org.zywx.wbpalmstar.engine.universalex;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.base.view.BaseFragment;
import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.EBrowserAnimation;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.EBrowserWindow;
import org.zywx.wbpalmstar.engine.EWgtResultInfo;
import org.zywx.wbpalmstar.engine.container.ContainerAdapter;
import org.zywx.wbpalmstar.engine.container.ContainerViewPager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.File;
import java.util.Vector;

public abstract class EUExBase {

    public static final int F_UEX_EVENT_TYPE_APP_EXIT = 0;
    public static final int F_UEX_EVENT_TYPE_APP_ON_RESUME = 1;
    public static final int F_UEX_EVENT_TYPE_APP_ON_PAUSE = 2;
    public static final int F_UEX_EVENT_TYPE_APP_ON_READY = 3;


    private String mUexName;
    /**
     * 全局上下文,实际内存地址为AppCan引擎的主Activity(继承自ActivityGroup).<br>
     * 可根据实际情况强转为相应的类.
     */
    protected Context mContext;
    /**
     * 本js扩展对象所在的WebView.
     */
    public EBrowserView mBrwView;

    protected boolean mDestroyed;
    protected boolean mStopped;
    protected c mHandler;

    public static final String SCRIPT_TAIL = ")}";
    public static final String SCRIPT_HEADER = "javascript:";
    public static final String SCRIPT_ERROR_HEADER = "javascript:if(uexWidgetOne.cbError){uexWidgetOne.cbError(";

    public EUExBase(Context context, EBrowserView inParent) {
        mContext = context;
        mBrwView = inParent;
        mHandler = new c(Looper.getMainLooper());
    }

    /**
     * java回调网页js函数或对象的接口,所有plugin进行完相关操作后如果需要回调网页,需统一走此接口.
     *
     * @param inCallbackName 将要被调用的页面js函数.如
     * @param inOpCode
     * @param inDataType
     * @param inData
     */
    public final void jsCallback(String inCallbackName, int inOpCode,
                                 int inDataType, int inData) {
        String js = SCRIPT_HEADER + "if(" + inCallbackName + "){"
                + inCallbackName + "(" + inOpCode + "," + inDataType + ","
                + inData + SCRIPT_TAIL;
        // mBrwView.loadUrl(js);
        callbackToJs(js);
    }

    public final void jsCallbackAsyn(String inCallbackName, int inOpCode,
                                     int inDataType, String inData) {
        String js = SCRIPT_HEADER + "if(" + inCallbackName + "){"
                + inCallbackName + "(" + inOpCode + "," + inDataType + ",'"
                + inData + "'" + SCRIPT_TAIL;
        // mBrwView.loadUrl(js);
        callbackToJsAsyn(js);
    }

    public final void jsSpeciCallback(String winName, String inCallbackName, int inOpCode,
                                      int inDataType, String inData) {
        String js = SCRIPT_HEADER + "if(" + inCallbackName + "){"
                + inCallbackName + "(" + inOpCode + "," + inDataType + ",'"
                + inData + "'" + SCRIPT_TAIL;
        callbackToJsSpeci(winName, js);
    }

    public final void jsCallback(String inCallbackName, int inOpCode,
                                 int inDataType, String inData) {
        String js = SCRIPT_HEADER + "if(" + inCallbackName + "){"
                + inCallbackName + "(" + inOpCode + "," + inDataType + ",'"
                + inData + "'" + SCRIPT_TAIL;
        // mBrwView.loadUrl(js);
        callbackToJs(js);
    }

    public void callBackJs(String methodName, String jsonData) {
        callBackJs(mBrwView,methodName,jsonData);
    }

    public void callBackJsObject(String methodName,Object object){
        callBackJsObject(mBrwView,methodName,object);
    }

    @Keep
    public static void callBackJs(EBrowserView eBrowserView,String methodName, String jsonData){
        if (eBrowserView == null) {
            BDebug.e("mBrwView is null...");
            return;
        }
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}else{console.log('function "+methodName +" not found.')}";
        callbackToJs(eBrowserView,js);
    }

    @Keep
    public static void callBackJsObject(EBrowserView eBrowserView,String methodName, Object value){
        if (eBrowserView == null) {
            BDebug.e("mBrwView is null...");
            return;
        }
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "(" + value + ");}else{console.log('function "+methodName +" not found.')}";
        callbackToJs(eBrowserView,js);
    }

    public final void errorCallback(int inOpCode, int InErrorCode,
                                    String inErrorInfo) {
        String js = SCRIPT_ERROR_HEADER + inOpCode + "," + InErrorCode + ",'"
                + inErrorInfo + "'" + SCRIPT_TAIL;
        // mBrwView.loadUrl(js);
        callbackToJs(js);
    }

    public final void onCallback(String inScript) {
        // mBrwView.loadUrl(inScript);
        callbackToJs(inScript);
    }

    private void callbackToJs(String js) {
        if (null != mBrwView) {
            mBrwView.addUriTask(js);
        }
    }

    public static void callbackToJs(EBrowserView eBrowserView,String js) {
        if (null != eBrowserView) {
            eBrowserView.addUriTask(js);
        }
    }

    private void callbackToJsAsyn(String js) {
        if (null != mBrwView) {
            mBrwView.addUriTaskAsyn(js);
        }
    }

    private void callbackToJsSpeci(String winName, String js) {
        if (null != mBrwView) {
            mBrwView.getBrowserWindow().addUriTaskSpeci(winName, js);
        }
    }

    /**
     * 异步回调到JS
     * @param callbackId 回调ID，该值由插件接口被调用时传入
     * @param hasNext 是否有下一次回调。没有传false ，有传true
     * @param args 参数可以是任何对象，直接回调对象可使用DataHelper.gson.toJsonTree()方法
     */
    @Keep
    public void callbackToJs(int callbackId,boolean hasNext,Object... args){
        if (null != mBrwView) {

            int flag=hasNext?1:0;
            final StringBuilder sb=new StringBuilder("javascript:uexCallback.callback(");
            sb.append(callbackId).append(",").append(flag);
            for (Object obj:args){
                sb.append(",");
                boolean isStrArg = obj instanceof String;
                if (isStrArg) {
                    sb.append("\'");
                }
                sb.append(String.valueOf(obj));
                if (isStrArg) {
                    sb.append("\'");
                }
             }
            sb.append(");");
            BDebug.i(sb.toString());
            //在主线程回调
            if (mContext!=null&&mContext instanceof Activity){
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mBrwView) {
                            mBrwView.loadUrl(sb.toString());
                        }
                    }
                });
            }else{
                callbackToJs(mBrwView,sb.toString());
            }
         }
    }

    /**
     * 运行一个Activity,并要求被运行的Activity在finish时有返回值.<br>
     * 你的plugin中,如果需要运行另一个Activity,并且需要此Activity返回数据时,必须要通过此接口调用,
     * 返回的数据将通过onActivityResult函数回调,可在onActivityResult函数中做相关处理.
     *
     * @param intent      目标Intent
     * @param requestCode 你为目标Activity分配的请求码,大于0的int型数据,将在onActivityResult时返回.
     */
    public final void startActivityForResult(Intent intent, int requestCode) {
        if (null == mBrwView) {
            return;
        }
        ((EBrowserActivity) mContext).startActivityForResult(this, intent,
                requestCode);
    }

    /**
     * 修复startActivityForResult是通过三方应用调起时，收不到回调的问题
     */
    public final void registerActivityResult() {
        ((EBrowserActivity) mContext).registerActivityForResult(this);
    }

    /**
     * 运行一个Activity
     *
     * @param intent 目标Intent
     */
    public final void startActivity(Intent intent) {
        if (null == mContext) {
            return;
        }
        ((EBrowserActivity) mContext).startActivity(intent);
    }

    public final void exitApp() {
        if (null == mContext) {
            return;
        }
        ((EBrowserActivity) mContext).exitBrowser();
    }

    /**
     * 添加一个view覆盖到当前window中.
     *
     * @param child
     * @param parms
     */
    public final void addViewToCurrentWindow(View child,
                                             RelativeLayout.LayoutParams parms) {
        if (null == mBrwView) {
            return;
        }

        float sc = mBrwView.getScaleWrap();
        int l = (int) (parms.leftMargin * sc);
        int t = (int) (parms.topMargin * sc);
        int w = parms.width;
        int h = parms.height;
        if (w > 0) {
            w = (int) (parms.width * sc);
        }
        if (h > 0) {
            h = (int) (parms.height * sc);
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
        lp.gravity = Gravity.NO_GRAVITY;
        lp.leftMargin = l;
        lp.topMargin = t;
        adptLayoutParams(parms, lp);
        mBrwView.addViewToCurrentWindow(child, lp);
    }

    /**
     * 添加一个view到指定id的容器中
     *
     * @param child
     * @param index
     * @param opid
     */
    public final void addSubviewToContainer(final View child, final int index,
                                            final String opid, final FrameLayout.LayoutParams parms) {
        if (null == mBrwView || opid == null || index < 0 || parms == null) {
            return;
        }
        ((EBrowserActivity) mContext).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                EBrowserWindow mWindow = mBrwView.getBrowserWindow();
                int count = mWindow.getChildCount();
                int l = (int) (parms.leftMargin);
                int t = (int) (parms.topMargin);
                int w = parms.width;
                int h = parms.height;
                for (int i = 0; i < count; i++) {
                    View view = mWindow.getChildAt(i);
                    if (view instanceof ContainerViewPager) {
                        final ContainerViewPager pager = (ContainerViewPager) view;
                        if (opid.equals(pager.getContainerVO().getId())) {
                            ContainerAdapter adapter = (ContainerAdapter) pager.getAdapter();
                            Vector<FrameLayout> views = adapter.getViewList();
                            boolean needAnim = views.size() == 0;//第一次添加view时播放动画
                            child.setLayoutParams(parms);
                            FrameLayout layout = new FrameLayout(mContext);
                            layout.addView(child);
                            if (views.size() <= index) {
                                for (int j = views.size(); j <= index; j++) {
                                    if (j == index) {
                                        views.add(layout);
                                    } else {
                                        views.add(new FrameLayout(mContext));
                                    }
                                }
                            } else {
                                views.set(index, layout);
                            }
                            adapter.setViewList(views);
                            adapter.notifyDataSetChanged();
                            if (needAnim && pager.getContainerVO().getAnimTime() != 0) {
                                startAnimationDelay(pager, child);
                            }
                            return;
                        }//end equals opid
                    }//end instanceof
                }//end for
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w, h);
                lp.leftMargin = l;
                lp.topMargin = t;
                addViewToCurrentWindow(child, lp);
            }// end run 
        });// end runOnUI
    }

    /**
     * 播放动画
     *
     * @param pager
     * @param child
     */
    private void startAnimationDelay(final ContainerViewPager pager, final View child) {
        final float width = pager.getContainerVO().getW();
        child.setTranslationX(width);
        final ViewTreeObserver observer = child.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    child.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    child.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                EBrowserAnimation.animFromRight(child, width, pager.getContainerVO().getAnimTime(),
                        pager.getContainerVO().getAnimDelayTime(),
                        new EBrowserAnimation.AnimatorListener() {
                            @Override
                            public void onAnimationEnd() {

                            }
                        });
            }
        });
    }


    /**
     * 移除一个view在指定id的容器中
     *
     * @param index
     * @param opid
     */
    public final void removeSubviewFromContainer(final int index, final String opid) {
        if (null == mBrwView || opid == null) {
            return;
        }
        ((EBrowserActivity) mContext).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                EBrowserWindow mWindow = mBrwView.getBrowserWindow();
                int count = mWindow.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = mWindow.getChildAt(i);
                    if (view instanceof ContainerViewPager) {
                        ContainerViewPager pager = (ContainerViewPager) view;
                        if (opid.equals((String) pager.getContainerVO().getId())) {
                            ContainerAdapter adapter = (ContainerAdapter) pager.getAdapter();
                            Vector<FrameLayout> views = adapter.getViewList();
                            if (index < views.size() && index >= 0) {
                                adapter.destroyItem(pager, index, null);
                                views.get(index).removeAllViews();
                                views.set(index, new FrameLayout(mContext));
                            } else {
                                return;
                            }
                            adapter.setViewList(views);
                            adapter.notifyDataSetChanged();
                            return;
                        }//end equals opid
                    }//end instanceof
                }//end for
            }// end run 
        });// end runOnUI
    }

    /**
     * 将View嵌入到webview随view一起滚动
     *
     * @param child
     * @param params
     * @param id     标识要添加的view，删除时会用到
     */
    public final void addViewToWebView(View child,
                                       android.widget.AbsoluteLayout.LayoutParams params,
                                       String id) {
        float sc = mBrwView.getScaleWrap();
        int x = (int) (params.x * sc);
        int y = (int) (params.y * sc);
        int w = params.width;
        int h = params.height;
        if (w > 0) {
            w = (int) (params.width * sc);
        }
        if (h > 0) {
            h = (int) (params.height * sc);
        }
        params.x = x;
        params.y = y;
        params.width = w;
        params.height = h;

        if (mBrwView == null) {
            return;
        }
        if (id != null) {
            child.setTag(id);
        }
        mBrwView.addViewWrap(child, params);
    }

    /**
     * 将制定id的view从webview中删除
     *
     * @param id
     */
    public final void removeViewFromWebView(String id) {
        if (!TextUtils.isEmpty(id)) {
            int viewCount = mBrwView.getChildCountWrap();
            for (int i = viewCount - 1; i >= 0; i--) {
                if (id.equals(mBrwView.getChildAtWrap(i).getTag())) {
                    mBrwView.removeViewWrap(mBrwView.getChildAtWrap(i));
                    break;
                }
            }
        }
    }

    public void addFragmentToCurrentWindow(BaseFragment fragment,
                                           final RelativeLayout.LayoutParams params,
                                           String tag) {
        addFragment(fragment, tag);
        fragment.setOnViewCreatedListener(new BaseFragment.OnViewCreatedListener() {
            @Override
            public void onViewCreated(View view) {
                addViewToCurrentWindow(view, params);
            }
        });
    }

    public void removeFragmentFromWindow(BaseFragment fragment) {
        if (fragment != null) {
            if (fragment.getView() != null) {
                removeViewFromCurrentWindow(fragment.getView());
            }
            removeFragment(fragment);
        }
    }

    /**
     * @param fragment
     * @param params
     * @param tag      作为Fragment的Tag，和添加到WebView的tag,必须保证唯一性
     */
    public void addFragmentToWebView(BaseFragment fragment,
                                     final android.widget.AbsoluteLayout.LayoutParams params,
                                     final String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        addFragment(fragment, tag);
        fragment.setOnViewCreatedListener(new BaseFragment.OnViewCreatedListener() {
            @Override
            public void onViewCreated(View view) {
                addViewToWebView(view, params, tag);
            }
        });
    }

    public void removeFragmentFromWebView(String tag) {
        removeViewFromWebView(tag);
        removeFragment(tag);
    }

    private void addFragment(Fragment fragment, String tag) {
        ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                .add(fragment, tag).commitAllowingStateLoss();
    }

    private void removeFragment(Fragment fragment) {
        if (mContext==null){
            return;
        }
        ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                .remove(fragment).commitAllowingStateLoss();
    }

    private void removeFragment(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        Fragment fragment = ((FragmentActivity) mContext).
                getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            removeFragment(fragment);
        }
    }


    /**
     * 加载一个widget
     *
     * @param inData
     */
    public final boolean startWidget(final WWidgetData inData,
                                     final EWgtResultInfo inResult) {
        if (null == mBrwView) {
            return false;
        }
        if (inData == null) {
            return false;
        }
        String index = inData.m_indexUrl;
        if (null == index || 0 == index.trim().length()) {
            return false;
        } else {
            if (!index.startsWith("http")) {
                File file = null;
                index = index.replace("file://", "");
                if (index.startsWith("/sdcard")) {
                    file = new File(index);
                } else if (index.startsWith("/data/data")) {
                    file = new File(index);
                } else if (index.contains("android_asset")) {
                    ;
                } else {
                    file = new File(index);
                }
                if (null != file) {
                    if (!file.exists()) {
                        return false;
                    }
                }
            }
        }
        // final EBrowserActivity screen = (EBrowserActivity) mContext;
        // final int wgtOrientation = inData.m_orientation;
        // Runnable mainThread = new Runnable() {
        // @Override
        // public void run() {
        // screen.changeConfiguration(wgtOrientation);
        // }
        // };
        // screen.runOnUiThread(mainThread);
        mBrwView.startWidget(inData, inResult);
        return true;
    }

    public void finishWidget(String inResultInfo, String appId, boolean isWgtBG) {
        if (null == mBrwView) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.getBrowser().finishWidget(inResultInfo, appId, isWgtBG, "");
    }

    public void finishWidget(String inResultInfo, String appId, boolean isWgtBG, String inAnimiId) {
        if (null == mBrwView) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.getBrowser().finishWidget(inResultInfo, appId, isWgtBG, inAnimiId);
    }

    /**
     * 从当前window移除一个view.
     *
     * @param child
     */
    public final void removeViewFromCurrentWindow(View child) {
        if (null == mBrwView) {
            return;
        }
        mBrwView.removeViewFromCurrentWindow(child);
    }

    /**
     * 根据url获取对应的cookie.
     *
     * @param inUrl url地址.
     * @return 对应的cookie或者null.
     */
    public final String getCookie(String inUrl) {

        return WebViewSdkCompat.getCookie(inUrl);
    }

    public final void clearCookie(){
        WebViewSdkCompat.clearCookie();
    }

    /**
     * 添加相应url的cookie.
     *
     * @param inUrl  url地址.
     * @param cookie cookie值.
     */
    public final void setCookie(String inUrl, String cookie) {
        if (null == cookie) {
            return;
        }
        /**
         * String tmp = new String(cookie).trim().toLowerCase(); int index =
         * tmp.indexOf("domain"); if(index <= 0){ try{ Uri i = Uri.parse(inUrl);
         * String host = i.getHost(); cookie = cookie + "; Domain=" + host +
         * ";"; }catch (Exception e) { ; } }
         **/
        WebViewSdkCompat.setCookie(inUrl,cookie);
    }

    /**
     * 注册一个监听事件,监听应用初始化完毕/暂停/恢复/退出等事件.
     *
     * @param listener EUExEventListener.
     */
    public final void registerAppEventListener(EUExEventListener listener) {
        if (null != listener && null != mContext) {
            ((EBrowserActivity) mContext).registerAppEventListener(listener);
        }
    }

    /**
     * 移除某个监听事件
     *
     * @param listener EUExEventListener.
     */
    public final void unRegisterAppEventListener(EUExEventListener listener) {
        if (null != listener && null != mContext) {
            ((EBrowserActivity) mContext).unRegisterAppEventListener(listener);
        }
    }

    /**
     * 获取本地图片,支持res://,file://等.
     *
     * @param inLocalPath 图片路径.
     */
    public final Bitmap getBitmap(String inLocalPath) {
        if (null == mContext) {
            return null;
        }
        return ((EBrowserActivity) mContext).getImage(inLocalPath);
    }

    /**
     * 当页面切换或者窗口被关闭时,平台会主动调用此函数进行与此plugin相关的清理工作.
     * 你的代码如有资源需要在窗口被关闭或者页面切换时释放,那么请在此函数中进行相关操作.
     *
     * @return
     */
    protected abstract boolean clean();

    /**
     * 通过startActivityForResult函数运行一个Activity,当此Activity finish后回调的数据将通过此接口返回.<br>
     * 需要Activity返回值的操作,请重写此接口,并进行相关操作.
     * <p/>
     * 如果是第三方应用去startActivityForResult，需要先调registerActivityResult才能收到回调
     *
     * @param requestCode startActivityForResult时为目标Activity分配的请求码.用于判断是哪个Activity的返回.
     * @param resultCode  目标Activity finish时设置的状态码.如RESULT_OK = -1,RESULT_CANCELED =
     *                    0等等.
     * @param data        目标Activity finish时返回的数据.
     */
    @Keep
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ;
    }

    /**
     * 在window中执行一段javascript脚本
     *
     * @param inWindowName window的name，如果是当前window，传null或者""。
     * @param type         window中的main/slibing。
     * @param inScript     将要执行的脚本。
     */
    public final void evaluateScript(String inWindowName, int type,
                                     String inScript) {
        if (null == mBrwView) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        if (inScript == null || !inScript.startsWith(SCRIPT_HEADER)) {
            inScript+=SCRIPT_HEADER+inScript;
        }
        curWind.evaluateScript(mBrwView, inWindowName, type, inScript);
    }

    /**
     * 在浮动窗口中执行一段javascript脚本
     *
     * @param inWindowName window的name，如果是当前window，传null或者""。
     * @param inPopName    浮动窗口的name，不可为null。
     * @param inScript     将要执行的脚本。
     */
    public final void evaluatePopoverScript(String inWindowName,
                                            String inPopName, String inScript) {
        if (null == mBrwView) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.evaluatePopoverScript(mBrwView, inWindowName, inPopName,
                SCRIPT_HEADER + inScript);
    }

    public final String getUexName() {

        return mUexName;
    }

    public final void setUexName(String uexName) {

        mUexName = uexName;
    }

    public void destroy() {
        mContext = null;
        mBrwView = null;
        mStopped = true;
        mDestroyed = true;
    }

    public final void stop() {

        mStopped = true;
    }

    public final void reset() {
        mStopped = false;
        mDestroyed = false;
    }

    public final boolean termination() {
        // return true;
        return mStopped || mDestroyed;
    }

    protected final void adptLayoutParams(RelativeLayout.LayoutParams rParms,
                                          FrameLayout.LayoutParams outParm) {
        if (null == rParms) {
            return;
        }
        int TRUE = RelativeLayout.TRUE;
        int ALIGN_PARENT_LEFT = RelativeLayout.ALIGN_PARENT_LEFT;
        int ALIGN_PARENT_TOP = RelativeLayout.ALIGN_PARENT_TOP;
        int ALIGN_PARENT_RIGHT = RelativeLayout.ALIGN_PARENT_RIGHT;
        int ALIGN_PARENT_BOTTOM = RelativeLayout.ALIGN_PARENT_BOTTOM;
        int CENTER_IN_PARENT = RelativeLayout.CENTER_IN_PARENT;
        int CENTER_HORIZONTAL = RelativeLayout.CENTER_HORIZONTAL;
        int CENTER_VERTICAL = RelativeLayout.CENTER_VERTICAL;
        try {
            int[] rules = rParms.getRules();
            if (rules[ALIGN_PARENT_LEFT] == TRUE) {
                outParm.gravity |= Gravity.LEFT;
            }
            if (rules[ALIGN_PARENT_TOP] == TRUE) {
                outParm.gravity |= Gravity.TOP;
            }
            if (rules[ALIGN_PARENT_RIGHT] == TRUE) {
                outParm.gravity |= Gravity.RIGHT;
            }
            if (rules[ALIGN_PARENT_BOTTOM] == TRUE) {
                outParm.gravity |= Gravity.BOTTOM;
            }
            if (rules[CENTER_IN_PARENT] == TRUE) {
                outParm.gravity |= Gravity.CENTER;
            }
            if (rules[CENTER_HORIZONTAL] == TRUE) {
                outParm.gravity |= Gravity.CENTER_HORIZONTAL;
            }
            if (rules[CENTER_VERTICAL] == TRUE) {
                outParm.gravity |= Gravity.CENTER_VERTICAL;
            }
        } catch (Exception e) {
            ;
        }
    }

    public void onHandleMessage(Message msg) {
    }

    /**
     * 区分接口收到的参数,简单判断字符串是否是json格式的String,没有必要完整校验一遍
     * @param str
     * @return
     */
    @Keep
    boolean isJsonString(String str){
        if (TextUtils.isEmpty(str)){
            return false;
        }
        return str.startsWith("{") && str.endsWith("}")||
                str.startsWith("[") && str.endsWith("]");
    }

    /**
     * 判断数组的第一个是否存在并且是Json格式
     * @param params
     * @return
     */
    @Keep
    boolean isFirstParamExistAndIsJson(String[] params){
        if (params==null||params.length==0){
            return false;
        }
        return isJsonString(params[0]);
    }

    /**
     * 获取callbackId ,-1为无效值
     * @param callbackIdStr
     * @return 为空或转换失败时返回-1
     */
    @Keep
    int valueOfCallbackId(String callbackIdStr){
        int callbackId=-1;
        if (TextUtils.isEmpty(callbackIdStr)||callbackIdStr.equals("null")){
            return callbackId;
        }
        try{
            callbackId=Integer.parseInt(callbackIdStr);
        }catch (Exception e){
            if (BDebug.DEBUG){
                e.printStackTrace();
            }
        }
        return callbackId;
    }


    public interface RequestPerssionsCallBackListener{
        void RequestPerssionsSucess();
        void RequestPerssionsFaile();
    }
    public void requsetPerssions(String perssions,String message,int requestCode){
        ((EBrowserActivity) mContext).requsetPerssions(perssions,this,message,requestCode);

    }

    @Keep
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults[0]== PackageManager.PERMISSION_DENIED){
            String js = "javascript:if(uexWidgetOne.cbPerssionsDenied){uexWidgetOne.cbPerssionsDenied(' "+permissions[0]+" ')}";
            callbackToJs(js);
        }
    }



}
