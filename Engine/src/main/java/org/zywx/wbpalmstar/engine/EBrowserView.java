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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.json.JSONObject;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.EBrowserHistory.EHistoryEntry;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExManager;
import org.zywx.wbpalmstar.engine.universalex.EUExWindow;
import org.zywx.wbpalmstar.engine.webview.ACEWebView;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.lang.reflect.Method;
import java.util.Map;

public class EBrowserView extends ACEWebView implements View.OnLongClickListener{

    public static final String CONTENT_MIMETYPE_HTML = "text/html";
    public static final String CONTENT_DEFAULT_CODE = "utf-8";

    private int mType;
    private String mName;
    private String mQuery;
    private String mRelativeUrl;
    private Context mContext;
    private EUExManager mUExMgr;
    private EBrowserWindow mBroWind;
    private boolean mShouldOpenInSystem;
    private boolean mOpaque;
    private boolean mOAuth;
    private boolean mSupportZoom;
    private int mDateType;
    private boolean mDestroyed;
    private EBrwViewAnim mViewAnim;
    private Method mDismissZoomControl;
    private int mMyCountId;

    private int mScrollDistance = 10;
    private EUExWindow callback;
    private boolean mIsNeedScroll = false;
    private boolean isMultilPopoverFlippingEnbaled = false;
    private boolean isSupportSlideCallback = false;//is need callback,set by API interface.
    private boolean disturbLongPressGesture = false;
    private int mThreshold = 5;
    private OnEBrowserViewChangeListener mBrowserViewChangeListener;

    public static boolean sHardwareAccelerate = true;//配置全部WebView是否硬件加速,默认开启，config.xml 配置关闭
    private String mExeJS;//打开窗口时由前端传入想要注入的JS字符串，WebView加载完成的时候执行这段JS。
    public EBrowserView(Context context, int inType, EBrowserWindow inParent) {
        super(context);
        mMyCountId = EBrowser.assignCountID();
        mBroWind = inParent;
        mContext = context;
        mType = inType;
        initPrivateVoid();
        setOnLongClickListener(this);
        super.setDownloadListener();
        setACEHardwareAccelerate();
    }

    private void setRemoteDebug(){
        if (mBroWind != null) {
            WWidgetData widgetData = mBroWind.getWidget();
            if (widgetData != null) {
                int debug = widgetData.m_appdebug;
                super.setRemoteDebug(debug == 1);
            }
        }
    }

    public EUExManager getEUExManager() {
        return mUExMgr;
    }

    public void setScrollCallBackContex(EUExWindow callback) {
        this.callback = callback;
    }

    public void init() {
        super.init(this);
        setEBrowserWindow(mBroWind);
        setInitialScale(100);
        setVerticalScrollbarOverlay(true);
        setHorizontalScrollbarOverlay(true);
        setLayoutAnimation(null);
        setAnimation(null);
        setNetworkAvailable(true);
        setRemoteDebug();
        mUExMgr = new EUExManager(mContext);
        mUExMgr.addJavascriptInterface(this);
    }

    private void setACEHardwareAccelerate() {
        if (!sHardwareAccelerate) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        } else {
            closeHardwareForSpecificString();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (!isHardwareAccelerated()) {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                BDebug.i("setLayerType","LAYER_TYPE_SOFTWARE");
            } else {
                closeHardwareForSpecificString();
            }
        }
        super.onAttachedToWindow();
    }

    private void closeHardwareForSpecificString() {
        WWidgetData widgetData = getCurrentWidget();
        if (widgetData != null) {
            for (String noHardware : widgetData.noHardwareList) {
                String str = noHardware.trim();
                // 手机型号、Android系统定制商、硬件制造商
                if (Build.MODEL.trim().equals(str)
                        || Build.BRAND.trim().equals(str)
                        || Build.MANUFACTURER.trim().equals(str)) {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    BDebug.i("setLayerType", "LAYER_TYPE_SOFTWARE");
                    break;
                }
            }
        }
    }

    @Override
    public boolean isHardwareAccelerated() {
        //confirm view is attached to a window
        boolean isHardwareAccelerated = super.isHardwareAccelerated();
        return isHardwareAccelerated;
    }

    @Override
    public void loadUrl(String url) {
        if (mDestroyed) {
            return;
        }
        try {
            if (url.startsWith("javascript:")&&Build.VERSION.SDK_INT>=19) {
                evaluateJavascript(url, null);
            }else {
                super.loadUrl(url);
            }
        } catch (Exception e) {
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void loadUrl(String url, Map<String, String> extraHeaders) {
        if (mDestroyed) {
            return;
        }
        try {
            super.loadUrl(url, extraHeaders);
        } catch (Exception e) {
            ;
        }
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        if (mDestroyed) {
            return;
        }
        try {
            super.loadData(data, mimeType, encoding);
        } catch (Exception e) {
            ;
        }
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data,
                                    String mimeType, String encoding, String historyUrl) {
        if (mDestroyed) {
            return;
        }
        try {
            super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding,
                    historyUrl);
        } catch (Exception e) {
            ;
        }
    }

    public boolean checkType(int inType) {

        return inType == mType;
    }

    public int getMyId() {

        return mMyCountId;
    }

    public void setDefaultFontSize(int size) {
        if (mDestroyed) {
            return;
        }
        super.setDefaultFontSize(size);
    }

    public void setSupportZoom() {
        mSupportZoom = true;
        super.setSupportZoom();
    }

    public boolean supportZoom() {

        return mSupportZoom;
    }

    public void initPrivateVoid() {
        Class[] nullParm = {};
        try {
            mDismissZoomControl = WebView.class.getDeclaredMethod(
                    "dismissZoomControl", nullParm);
            mDismissZoomControl.setAccessible(true);
        } catch (Exception e) {
            ;
        }

        if (Build.VERSION.SDK_INT >= 9) {
            setOverScrollMode(2);
            return;
        }
        try {
            Class[] intParam = {int.class};
            Method setOverScrollMode = WebView.class.getDeclaredMethod(
                    "setOverScrollMode", intParam);
            setOverScrollMode.invoke(this, 2);
        } catch (Exception e) {
            ;
        }
    }

//	protected void setLayerTypeForHeighVersion() {
//		// if(Build.VERSION.SDK_INT < 11){
//		// return;
//		// }
//		// String MODEL = Build.MODEL;
//		// String MANUFACTURER = Build.MANUFACTURER;
//		// if(null != MODEL && null != MANUFACTURER){
//		// MODEL = MODEL.toLowerCase();
//		// MANUFACTURER = MANUFACTURER.toLowerCase();
//		// if((MODEL.contains("9508") || MODEL.contains("9500")) &&
//		// MANUFACTURER.contains("samsung")){
//		// return;
//		// }
//		// }
//		// Paint paint = new Paint();
//		// paint.setColor(0x00000000);
//		// if(isHardwareAccelerated()){
//		// setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//		// }
//	}

//	@SuppressLint("NewApi")
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		if (Build.VERSION.SDK_INT >= 11) {
//			String MODEL = Build.MODEL;
//			String MANUFACTURER = Build.MANUFACTURER;
//			if (null != MODEL && null != MANUFACTURER) {
//				MODEL = MODEL.toLowerCase();
//				MANUFACTURER = MANUFACTURER.toLowerCase();
//				if ((MODEL.contains("9508") || MODEL.contains("9500"))
//						&& MANUFACTURER.contains("samsung")) {
//					if (isHardwareAccelerated()) {
//						setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//					}
//					super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//					return;
//				}
//			}
//			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//			invalidate();
//		}
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//	}

    @SuppressLint("NewApi")
    public void destroyControl() {
        if (null != mDismissZoomControl) {
            try {
                mDismissZoomControl.invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    public void pauseCore() {
        if (Build.VERSION.SDK_INT >= 11) {
            super.onPause();
        } else {
            try {
                Class[] nullParm = {};
                Method pause = WebView.class.getDeclaredMethod("onPause",
                        nullParm);
                pause.setAccessible(true);
                pause.invoke(this);
            } catch (Exception e) {
                ;
            }
        }
    }

    @SuppressLint("NewApi")
    public void resumeCore() {
        if (Build.VERSION.SDK_INT >= 11) {
            super.onResume();
        } else {
            try {
                Class[] nullParm = {};
                Method resume = WebView.class.getDeclaredMethod("onResume",
                        nullParm);
                resume.setAccessible(true);
                resume.invoke(this);
            } catch (Exception e) {
                ;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDestroyed) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isFocused()) {
                    strugglefoucs();
                }
                onScrollChanged(getScrollXWrap(), getScrollYWrap(), getScrollXWrap(), getScrollYWrap());
                if (mIsNeedScroll) {
                    //modify no-response-for-onclick-event
                    int temp_ScrollY = this.getScrollYWrap();
                    getRealWebView().scrollTo(this.getScrollXWrap(), this.getScrollYWrap() + 1);
                    getRealWebView().scrollTo(this.getScrollXWrap(), temp_ScrollY);
                }
                setMultilPopoverFlippingEnbaled();
                break;
            case MotionEvent.ACTION_MOVE:
                setMultilPopoverFlippingEnbaled();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return super.onTouchEvent(ev);

    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return super.onGenericMotionEvent(event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void setIsMultilPopoverFlippingEnbaled(boolean isEnabled) {
        isMultilPopoverFlippingEnbaled = isEnabled;
        setMultilPopoverFlippingEnbaled();
    }

    private void setMultilPopoverFlippingEnbaled() {
        View parentBounceView = (View) this.getParent();
        if (parentBounceView != null && parentBounceView instanceof EBounceView) {
            ViewParent parentViewPager = parentBounceView.getParent();
            if (parentViewPager != null && parentViewPager instanceof ViewPager) {
                parentViewPager.requestDisallowInterceptTouchEvent(isMultilPopoverFlippingEnbaled);
            }
        }
    }

    private void strugglefoucs() {
        requestFocus();
        /**
         * InputManager.get().hideSoftInput(getWindowToken(), 0, null);
         * Log.d("ldx", "-------------- view in: " + mName);
         *
         * Log.d("ldx", "hasFocus: " + hasFocus()); Log.d("ldx", "isFocused: " +
         * isFocused());
         *
         * try{ Class[] nullParam = {}; Method clearHelpers =
         * WebView.class.getDeclaredMethod("clearHelpers", nullParam);
         * clearHelpers.setAccessible(true); clearHelpers.invoke(this); }catch
         * (Exception e) { e.printStackTrace(); } Log.d("ldx",
         * "-------------- --------------");
         *
         * boolean Ac1 = InputManager.get().isActive(); boolean Ac2 =
         * InputManager.get().isActive(this); if(Ac1){
         * InputManager.get().hideSoftInput(this.getWindowToken(), 0, null); }
         * Log.d("ldx", "imm Ac1: " + Ac1); Log.d("ldx", "imm Ac2: " + Ac2); int
         * childCount = getChildCount(); Log.d("ldx", "childCount: " +
         * childCount); for(int i = 0; i < childCount; ++i){ View child =
         * getChildAt(i); boolean Ac3 = InputManager.get().isActive(child);
         * Log.d("ldx", "imm Ac3: " + Ac3); if(Ac3){
         * InputManager.get().hideSoftInput(child.getWindowToken(), 0, null); }
         * child.clearFocus(); } boolean requestFocusOk = requestFocus();
         * removeAllViews();
         *
         * Log.d("ldx", "requestFocusOk: " + requestFocusOk);
         **/
        // int childCount1 = getChildCount();
        // Log.d("ldx", "childCount1: " + childCount1);

        Log.d("ldx", "hasFocus: " + hasFocus());
        Log.d("ldx", "isFocused: " + isFocused());

        Log.d("ldx", "-------------- view out: " + mName);

    }

    @Override
    public boolean onLongClick(View v) {
        return disturbLongPressGesture;
    }

    public void setDisturbLongPressGesture(boolean disturbLongPress) {
        disturbLongPressGesture = disturbLongPress;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onVisibilityChanged(View v, int visibility) {
        super.onVisibilityChanged(v, visibility);
        if ((v == this || v == mBroWind)
                && (visibility == INVISIBLE || visibility == GONE)) {
            hideSoftKeyboard();
        }
    }

    private void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPageStarted(EBrowserView view, String url) {
        if (mDestroyed) {
            return;
        }
        mUExMgr.notifyDocChange();
        if (checkType(EBrwViewEntry.VIEW_TYPE_POP) && mOAuth) {
            mBroWind.onUrlChange(mName, url);
            return;
        }
        if (!checkType(EBrwViewEntry.VIEW_TYPE_MAIN)) {
            return;
        }
        if (mBroWind!=null) {
            mBroWind.onPageStarted(view, url);
        }
    }

    public void onPageFinished(EBrowserView view, String url) {
        if (mDestroyed) {
            return;
        }
        if (mBroWind!=null){
            mBroWind.onPageFinished(view, url);
        }
        if (mBrowserViewChangeListener != null) {
            mBrowserViewChangeListener.onPageFinish();
        }
        if (AppCan.getInstance().getSubWidgetToStart()!=null){
            EUExBase.callBackJsObject(this,"uexWidgetOne.OnSubWidgetToStart",DataHelper.gson.toJsonTree(AppCan
                    .getInstance()
                    .getSubWidgetToStart()));
            AppCan.getInstance().setSubWidgetToStart(null);
        }
    }

    public boolean isObfuscation() {
        if (mDestroyed) {
            return false;
        }
        return mBroWind.isObfuscation();
    }

    public boolean isOAth() {
        if (mDestroyed) {
            return false;
        }
        return mBroWind.isOAuth();
    }

    public String getName() {

        return mName;
    }

    public void setName(String name) {

        mName = name;
    }

    @Override
    public void goBack() {
        if (mDestroyed) {
            return;
        }
        if (isObfuscation()) {
            EHistoryEntry enty = mBroWind.getHistory(-1);
            if (null != enty) {
                String url = enty.mUrl;
                if (Build.VERSION.SDK_INT >= 11) {
                    if (url.startsWith("file")) {
                        int index = url.indexOf("?");
                        if (index > 0) {
                            mQuery = url.substring(index + 1);
                            url = url.substring(0, index);
                        }
                    }
                }
                if (enty.mIsObfuscation) {
                    needToEncrypt(this, url, EBrowserHistory.UPDATE_STEP_BACK);
                } else {
                    loadUrl(url);
                    updateObfuscationHistroy(url,
                            EBrowserHistory.UPDATE_STEP_BACK, false);
                }
            }
        } else {
            super.goBack();
        }
    }

    @Override
    public void goForward() {
        if (mDestroyed) {
            return;
        }
        if (isObfuscation()) {
            EHistoryEntry enty = mBroWind.getHistory(1);
            if (null != enty) {
                if (enty.mIsObfuscation) {
                    needToEncrypt(this, enty.mUrl,
                            EBrowserHistory.UPDATE_STEP_FORWARD);
                } else {
                    loadUrl(enty.mUrl);
                    updateObfuscationHistroy(enty.mUrl,
                            EBrowserHistory.UPDATE_STEP_FORWARD, false);
                }
            }
        } else {
            super.goForward();
        }
    }

    public void updateObfuscationHistroy(String inUrl, int step,
                                         boolean isObfuscation) {
        if (mDestroyed) {
            return;
        }
        if (!checkType(EBrwViewEntry.VIEW_TYPE_MAIN)) {
            return;
        }
        mBroWind.updateObfuscationHistroy(inUrl, step, isObfuscation);
    }

    protected void clearObfuscationHistroy() {
        if (mDestroyed) {
            return;
        }
        mBroWind.clearObfuscationHistroy();
    }

    public void addViewToCurrentWindow(View child,
                                       FrameLayout.LayoutParams parms) {
        if (mDestroyed) {
            return;
        }
        if (parms != null) {
            child.setLayoutParams(parms);
        }
        mBroWind.addViewToCurrentWindow(child);
    }

    public void removeViewFromCurrentWindow(View child) {
        if (mDestroyed) {
            return;
        }
        mBroWind.removeViewFromCurrentWindow(child);
    }

    public final void startWidget(WWidgetData inData, EWgtResultInfo inResult) {
        if (mDestroyed) {
            return;
        }
        mBroWind.startWidget(inData, inResult);
    }

    protected void start1(String url) {
        if (mDestroyed) {
            return;
        }
        if (null == url || 0 == url.length()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 11) {
            if (url != null) {
                int index = url.indexOf("?");
                if (index > 0) {
                    setQuery(url.substring(index + 1));
                    if (!url.startsWith("http")) {
                        url = url.substring(0, index);
                    }
                }
            }
        }
        addUriTask(url);
    }

    private void eClearHistory() {
        if (mDestroyed) {
            return;
        }
        if (isObfuscation()) {
            clearObfuscationHistroy();
        } else {
            clearHistory();
        }
    }

    protected void start(String url) {
        if (mDestroyed) {
            return;
        }
        if (null == url || 0 == url.length()) {
            return;
        }
        if (isObfuscation()) {
            clearObfuscationHistroy();
            if (url.startsWith("http")) {
                addUriTask(url);
                updateObfuscationHistroy(url, EBrowserHistory.UPDATE_STEP_INIT,
                        false);
            } else {
                needToEncrypt(this, url, EBrowserHistory.UPDATE_STEP_INIT); // may
                // be
                // crash
            }
        } else {
            if (Build.VERSION.SDK_INT >= 11) {
                if (url != null) {
                    int index = url.indexOf("?");
                    if (index > 0) {
                        setQuery(url.substring(index + 1));
                        if (!url.startsWith("http")) {
                            url = url.substring(0, index);
                        }
                    }
                }
            }
            addUriTask(url);
            clearHistory();
        }
    }

    public void newLoadUrl(String url) {
        if (mDestroyed) {
            return;
        }
        if (null == url || 0 == url.length()) {
            return;
        }
        addUriTask(url);
    }

    public void newLoadData(String inData) {
        if (mDestroyed) {
            return;
        }
        loadData(inData, CONTENT_MIMETYPE_HTML, CONTENT_DEFAULT_CODE);
    }

    public void receivedError(int errorCode, String description,
                              String failingUrl) {
        if (mDestroyed) {
            return;
        }
        if (checkType(EBrwViewEntry.VIEW_TYPE_ADD)) {
            loadUrl("about:bank");
            mBroWind.closeAd();
            return;
        }
        String errorPath="file:///android_asset/error/error.html";
        if (mBroWind!=null&&!TextUtils.isEmpty(getRootWidget().mErrorPath)){
            errorPath="file:///android_asset/widget/"+getRootWidget().mErrorPath;
            loadUrl(errorPath);
        }else{
            loadUrl(errorPath);
        }
      }

    public int getType() {

        return mType;
    }

    public void addUriTask(String uri) {
        if (null != mBroWind && !mDestroyed) {
            mBroWind.addUriTask(this, uri);
        }else{
            BDebug.e("mBroWind is null or Destroyed");
        }
    }

    public void addUriTaskAsyn(String uri) {
        if (null != mBroWind && !mDestroyed) {
            mBroWind.addUriTaskAsyn(this, uri);
        }
    }

    /**
     * 设置是否开启硬件加速
     *
     * @param flag -1不处理，0关闭，1开启
     */
    public void setHWEnable(int flag) {
        if (flag == -1) {
            return;
        }
        if (flag == 1) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }


    protected void needToEncrypt(EBrowserView view, String url, int inFlag) {
        if (mDestroyed) {
            return;
        }
        int index = url.indexOf("?");
        String turl = url;
        if (index > 0) {
            setQuery(url.substring(index + 1));
            turl = turl.substring(0, index);
        }

        String data = ACEDes.decrypt(turl, mContext, false, null);
        ;

        if (ACEDes.isSpecifiedEncrypt()) {

//			data = SpecifiedEncrypt.parseSpecifiedEncryptHtml(data);

        }
//		if (SpecifiedEncrypt.isSpecifiedEncrypt()) {
//
//			data = SpecifiedEncrypt.parseSpecifiedEncrypt(turl);
//
//		} else {
//			data = BHtmlDecrypt.decrypt(turl, mContext, false, null);
//		}

        view.loadDataWithBaseURL(url, data, CONTENT_MIMETYPE_HTML,
                CONTENT_DEFAULT_CODE, url);
        if (mType == EBrwViewEntry.VIEW_TYPE_MAIN) {
            updateObfuscationHistroy(url, inFlag, true);
        }
    }

    public EBrowserWindow getBrowserWindow() {

        return mBroWind;
    }

    public int getWidgetType() {

        int type = mBroWind.getWidgetType();
        return type;
    }

    public String getWindowName() {
        if (mDestroyed) {
            return null;
        }
        return mBroWind.getName();
    }

    public void setQuery(String query) {
        if (mDestroyed) {
            return;
        }
        mQuery = query;
    }

    public String getQuery() {
        if (mDestroyed) {
            return null;
        }
        return mQuery;
    }

    public String getRelativeUrl() {
        if (mDestroyed) {
            return null;
        }
        return mRelativeUrl;
    }

    public void setRelativeUrl(String url) {
        if (mDestroyed) {
            return;
        }
        mRelativeUrl = url;
    }

    public String getCurrentUrl() {
        if (mDestroyed) {
            return "";
        }
        //修改浮动窗口中不能打开窗口问题
        //if (!checkType(EBrwViewEntry.VIEW_TYPE_MAIN)) {
        //	return mBroWind.location();
        //} else {
        String url = getUrl();
        int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        int indexS = url.indexOf("#");
        if (-1 != indexS) {
            url = url.substring(0, indexS);
        }
        return url;
        //}
    }

    public String getCurrentUrl(String baseUrl) {
        if (mDestroyed) {
            return "";
        }
        //修改浮动窗口中不能打开窗口问题
        //if (!checkType(EBrwViewEntry.VIEW_TYPE_MAIN)) {
        //    return mBroWind.location();
        //} else {
        String url = getUrl();
        if (TextUtils.isEmpty(url)) {
            url = baseUrl;
        }
        int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        int indexS = url.indexOf("#");
        if (-1 != indexS) {
            url = url.substring(0, indexS);
        }
        return url;
        //}
    }

    public String getWidgetPath() {
        if (mDestroyed) {
            return "";
        }
        String ret = getCurrentWidget().m_widgetPath;
        return ret;
    }

    public WWidgetData getCurrentWidget() {
        if (mDestroyed) {
            return new WWidgetData();
        }
        if (mBroWind==null){
            return null;
        }
        return mBroWind.getWidget();
    }

    public WWidgetData getRootWidget() {
        if (mDestroyed) {
            return new WWidgetData();
        }
        return mBroWind.getRootWidget();
    }

    public boolean isOAuth() {

        return mOAuth;
    }

    public void setOAuth(boolean flag) {

        mOAuth = flag;
    }

    public boolean shouldOpenInSystem() {

        return mShouldOpenInSystem;
    }

    public void setShouldOpenInSystem(boolean flag) {

        mShouldOpenInSystem = flag;
    }

    public void setOpaque(boolean flag) {
        mOpaque = flag;
        if (mOpaque) {
            setBackgroundColor(0xFFFFFFFF);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**wanglei del 20151124*/
//    public void setBrwViewBackground(boolean flag, String bgColor, String baseUrl) {
//        if (flag) {
//            if(bgColor.startsWith("#") || bgColor.startsWith("rgb")){
//                int color = BUtility.parseColor(bgColor);
//                setBackgroundColor(color);
//            }else{
//                String path = BUtility.makeRealPath(BUtility.makeUrl(getCurrentUrl(baseUrl),bgColor),
//                        getCurrentWidget().m_widgetPath, getCurrentWidget().m_wgtType);
//                Bitmap bitmap = BUtility.getLocalImg(mContext, path);
//                Drawable d = null;
//                if(bitmap != null){
//                    d = new BitmapDrawable(mContext.getResources(), bitmap);
//                }
//                int version = Build.VERSION.SDK_INT;
//                if(version < 16){
//                    setBackgroundDrawable(d);
//                    setBackgroundColor(Color.argb(0, 0, 0, 0));
//                }else{
//                    setBackground(d);
//                    setBackgroundColor(Color.argb(0, 0, 0, 0));
//                }
//            }
//        } else {
//            setBackgroundColor(Color.TRANSPARENT);
//        }
//    }

    public void setWebApp(boolean flag) {
        super.setWebApp(flag);
    }

    public boolean isWebApp() {
        return super.isWebApp();
    }

    public int getDateType() {

        return mDateType;
    }

    public void setDateType(int dateType) {

        mDateType = dateType;
    }

    public void beginAnimition() {
        if (mDestroyed) {
            return;
        }
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null == mViewAnim) {
                    mViewAnim = new EBrwViewAnim();
                }
                mViewAnim.beginAnimition(v);
            }
        });
    }

    public void setAnimitionDelay(final long del) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.setAnimitionDelay(v, del);
                }
            }
        });
    }

    public void setAnimitionDuration(final long dur) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.setAnimitionDuration(v, dur);
                }
            }
        });
    }

    public void setAnimitionCurve(final int cur) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.setAnimitionCurve(v, cur);
                }
            }
        });
    }

    public void setAnimitionRepeatCount(final int count) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.setAnimitionRepeatCount(v, count);
                }
            }
        });
    }

    public void setAnimitionAutoReverse(final boolean flag) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.setAnimitionAutoReverse(v, flag);
                }
            }
        });
    }

    public void makeTranslation(final float tx, final float ty, final float tz) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.makeTranslation(v, tx, ty, tz);
                }
            }
        });
    }

    public void makeScale(final float tx, final float ty, final float tz) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.makeScale(v, tx, ty, tz);
                }
            }
        });
    }

    public void makeRotate(final float fd, final float px, final float py,
                           final float pz) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.makeRotate(v, fd, px, py, pz);
                }
            }
        });
    }

    public void makeAlpha(final float fc) {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.makeAlpha(v, fc);
                }
            }
        });
    }

    public void commitAnimition() {
        final EBrowserView v = this;
        post(new Runnable() {
            @Override
            public void run() {
                if (null != mViewAnim && !mDestroyed) {
                    mViewAnim.commitAnimition(v);
                    mBroWind.invalidate();
                }
            }
        });
    }

    public void cbBounceState(int inData) {
        String js = "javascript:if(" + EUExWindow.function_cbBounceState + "){"
                + EUExWindow.function_cbBounceState + "(" + 0 + "," + 2 + ","
                + inData + ")}";
        addUriTask(js);
    }

    public int getBounce() {
        if (mDestroyed) {
            return 0;
        }
        EViewEntry bounceEntry = new EViewEntry();
        bounceEntry.obj = getParent();
        mBroWind.addBounceTask(bounceEntry,
                EViewEntry.F_BOUNCE_TASK_GET_BOUNCE_VIEW);
        if (bounceEntry.obj instanceof EBounceView){
            return ((EBounceView) bounceEntry.obj).getBounce();
        }
        return 0;
    }

    public void setBounce(int flag) {
        if (mDestroyed) {
            return;
        }
        EViewEntry bounceEntry = new EViewEntry();
        bounceEntry.obj = getParent();
        bounceEntry.flag = flag;
        mBroWind.addBounceTask(bounceEntry,
                EViewEntry.F_BOUNCE_TASK_SET_BOUNCE_VIEW);
    }

    public void notifyBounceEvent(int inType, int inStatus) {
        if (mDestroyed) {
            return;
        }
        EViewEntry bounceEntry = new EViewEntry();
        bounceEntry.type = inType;
        bounceEntry.obj = getParent();
        bounceEntry.flag = inStatus;
        mBroWind.addBounceTask(bounceEntry,
                EViewEntry.F_BOUNCE_TASK_NOTIFY_BOUNCE_VIEW);
    }

    public void showBounceView(int inType, String inColor, int inFlag) {
        if (mDestroyed) {
            return;
        }
        EViewEntry bounceEntry = new EViewEntry();
        bounceEntry.type = inType;
        bounceEntry.obj = getParent();
        bounceEntry.color = parseColor(inColor);
        bounceEntry.flag = inFlag;
        mBroWind.addBounceTask(bounceEntry,
                EViewEntry.F_BOUNCE_TASK_SHOW_BOUNCE_VIEW);
    }

    public void onBounceStateChange(int type, int state) {
        String js = "javascript:if(uexWindow.onBounceStateChange){uexWindow.onBounceStateChange("
                + type + "," + state + ");}";
        loadUrl(js);
    }

    public int parseColor(String inColor) {
        int reColor = 0;
        try {
            if (inColor != null && inColor.length() != 0) {
                inColor = inColor.replace(" ", "");
                if (inColor.charAt(0) == 'r') { // rgba
                    int start = inColor.indexOf('(') + 1;
                    int off = inColor.indexOf(')');
                    inColor = inColor.substring(start, off);
                    String[] rgba = inColor.split(",");
                    int r = Integer.parseInt(rgba[0]);
                    int g = Integer.parseInt(rgba[1]);
                    int b = Integer.parseInt(rgba[2]);
                    int a = Integer.parseInt(rgba[3]);
                    reColor = (a << 24) | (r << 16) | (g << 8) | b;
                } else { // #
                    inColor = inColor.substring(1);
                    if (3 == inColor.length()) {
                        char[] t = new char[6];
                        t[0] = inColor.charAt(0);
                        t[1] = inColor.charAt(0);
                        t[2] = inColor.charAt(1);
                        t[3] = inColor.charAt(1);
                        t[4] = inColor.charAt(2);
                        t[5] = inColor.charAt(2);
                        inColor = String.valueOf(t);
                    } else if (6 == inColor.length()) {
                        ;
                    }
                    long color = Long.parseLong(inColor, 16);
                    reColor = (int) (color | 0x00000000ff000000);
                }
            }
        } catch (Exception e) {
            ;
        }
        return reColor;
    }

    public void resetBounceView(int inType) {
        if (mDestroyed) {
            return;
        }
        EViewEntry bounceEntry = new EViewEntry();
        bounceEntry.type = inType;
        bounceEntry.obj = getParent();
        mBroWind.addBounceTask(bounceEntry,
                EViewEntry.F_BOUNCE_TASK_RESET_BOUNCE_VIEW);
    }

    public void hiddenBounceView(int inType) {
        if (mDestroyed) {
            return;
        }
        EViewEntry bounceEntry = new EViewEntry();
        bounceEntry.type = inType;
        bounceEntry.obj = getParent();
        mBroWind.addBounceTask(bounceEntry,
                EViewEntry.F_BOUNCE_TASK_HIDDEN_BOUNCE_VIEW);
    }

    public void setBounceParams(int inType, JSONObject json, String guestId) {
        if (mDestroyed) {
            return;
        }
        EViewEntry bounceEntry = new EViewEntry();
        bounceEntry.type = inType;
        bounceEntry.obj = getParent();
        bounceEntry.obj1 = json;
        bounceEntry.arg1 = guestId;
        mBroWind.addBounceTask(bounceEntry,
                EViewEntry.F_BOUNCE_TASK_SET_BOUNCE_PARMS);
    }

    public void topBounceViewRefresh() {
        if (mDestroyed) {
            return;
        }
        EViewEntry bounceEntry = new EViewEntry();
        bounceEntry.obj = getParent();
        mBroWind.addBounceTask(bounceEntry,
                EViewEntry.F_BOUNCE_TASK_TOP_BOUNCE_VIEW_REFRESH);
    }

    public boolean beDestroy() {

        return mDestroyed;
    }

    protected void reset() {
        mDestroyed = false;
        View bv = (View) getParent();
        if (bv != null && bv instanceof EBounceView) {
            ((EBounceView) bv).release();
        }
        clearView();
        clearMatches();
        mQuery = null;
        mName = null;
        mRelativeUrl = null;
        mShouldOpenInSystem = false;
        mOpaque = false;
        mOAuth = false;
        setWebApp(false);
        mSupportZoom = false;
        isSupportSlideCallback = false;
        disturbLongPressGesture = false;
        eClearHistory();
        resumeCore();
        mUExMgr.notifyReset();
    }

    @Override
    public void stopLoading() {
        super.stopLoading();
        mUExMgr.notifyStop();
        pauseCore();
    }

    @Override
    public void destroy() {
        if (mDestroyed) {
            return;
        }
        mDestroyed = true;
        mBroWind = null;
        mContext = null;
        clearView();
        clearHistory();
        ViewGroup parent = (ViewGroup) getParent();
        if (null != parent) {
            parent.removeView(this);
        }
        mUExMgr.notifyDestroy(this);
        mUExMgr = null;
        super.destroy();
    }

    protected void printThreadStackTrace() {
        StackTraceElement[] stak = Thread.currentThread().getStackTrace();
        String s = "";
        int len = stak.length;
        for (int i = 0; i < len; ++i) {
            StackTraceElement one = stak[i];
            String className = one.getClassName();
            String methodName = one.getMethodName();
            int line = one.getLineNumber();
            String x = s + className + "." + methodName + " [" + line + "]";
            x.charAt(0);
            if (i == 0 || i == 1 || i == 2) {
                s += " ";
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //if (!EBrowserWindow.isShowDialog) {
        int versionA = Build.VERSION.SDK_INT;
        boolean isSlideCallback = false;

        if (versionA >= 19) {
            //system above 4.4, is support callback depend on isSupportSlideCallback which
            // set by developer.
            // 4.4以上手机系统，是否回调取决于前端接口设置。
            isSlideCallback = isSupportSlideCallback;
        } else {
            //system below 4.4, is support callback depend on isSupportSlideCallback and
            //isShowDialog, isShowDialog indicate is pop-up keyboard or whether to switch
            // the screen.
            // 4.4以下手机系统，是否回调即取决于前端接口设置，也取决于当前键盘是否弹出或者是否变换屏幕。因此在该
            // 条件下屏幕旋转之后，上滑下滑的监听不生效。
            isSlideCallback = isSupportSlideCallback && !EBrowserWindow.isShowDialog;
        }
        if (isSlideCallback) {
            float contentHeight = getContentHeight() * getScaleWrap();
            boolean isSlipedDownEdge = t != oldt && t > 0
                    && contentHeight <= t + getHeightWrap() + mThreshold;
            if (isSlipedDownEdge) {
                callback.jsCallback(EUExWindow.function_cbslipedDownEdge, 0,
                        EUExCallback.F_C_INT, 0);

                callback.jsCallback(EUExWindow.function_onSlipedDownEdge, 0,
                        EUExCallback.F_C_INT, 0);

            } else if (getScrollYWrap() == 0) {
                callback.jsCallback(EUExWindow.function_cbslipedUpEdge, 0,
                        EUExCallback.F_C_INT, 0);
                callback.jsCallback(EUExWindow.function_onSlipedUpEdge, 0,
                        EUExCallback.F_C_INT, 0);

            } else if (oldt - t > mScrollDistance) {
                callback.jsCallback(EUExWindow.function_cbslipedDownward, 0,
                        EUExCallback.F_C_INT, 0);
                callback.jsCallback(EUExWindow.function_onSlipedDownward, 0,
                        EUExCallback.F_C_INT, 0);
            } else if (oldt - t < -mScrollDistance) {
                callback.jsCallback(EUExWindow.function_cbslipedUpward, 0,
                        EUExCallback.F_C_INT, 0);
                callback.jsCallback(EUExWindow.function_onSlipedUpward, 0,
                        EUExCallback.F_C_INT, 0);
            }
        }
        //}

        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void setNeedScroll(boolean b) {
        this.mIsNeedScroll = b;
    }

    public void setIsSupportSlideCallback(boolean isSupport) {
        isSupportSlideCallback = isSupport;
    }

    public void setEBrowserViewChangeListener(OnEBrowserViewChangeListener browserViewChangeListener) {
        mBrowserViewChangeListener = browserViewChangeListener;
    }

    public interface OnEBrowserViewChangeListener {
        void onPageFinish();
    }

    /**
     * CrossWalk 需要返回对应的scale值，webview api<=18时才需要返回对应的scale值
     * @return Crosswalk返回对应的scale值，webview api大于18时返回1，<=18时返回对应的scale值
     */
    public float getScaleWrap() {
        return super.getScaleWrap();
    }

    /**
     * CrossWalk 对应的getScrollY()实现不一样，需要调用另外的接口
     * @return
     */
    public int getScrollYWrap(){
        return super.getScrollYWrap();
    }

    public int getScrollXWrap(){
        return super.getScrollXWrap();
    }

    public void setUserAgent(String userAgent) {
        super.setUserAgent(userAgent);
    }

    public int getDownloadCallback() {
        if (mDestroyed) {
            return 0;
        }
        return super.getDownloadCallback();
    }

    public void setDownloadCallback(int downloadCallback) {
        if (mDestroyed) {
            return;
        }
        super.setDownloadCallback(downloadCallback);
    }

    public String getWebViewKernelInfo() {
        return super.getWebViewKernelInfo();
    }
    public void setExeJS(String exeJS){
        this.mExeJS = exeJS;
    }

    public void loadExeJS(){
        if (!TextUtils.isEmpty(mExeJS)){
            loadUrl("javascript:"+mExeJS+"//"+System.currentTimeMillis());
        }
    }
}
