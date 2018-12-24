/*
 * Copyright (c) 2015.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.zywx.wbpalmstar.engine.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

import org.xwalk.core.XWalkDownloadListener;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;
import org.zywx.wbpalmstar.acedes.EXWebViewClient;
import org.zywx.wbpalmstar.base.vo.KernelInfoVO;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserBaseSetting;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.EBrowserWindow;
import org.zywx.wbpalmstar.engine.ESystemInfo;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by ylt on 15/8/24.
 */
public class ACEWebView extends XWalkView {

	// use for debug
	protected Method mDumpDisplayTree;
	protected Method mDumpDomTree;
	protected Method mDumpRenderTree;
	protected Method mDrawPage;

	protected Method mDismissZoomControl;

	private EBrowserBaseSetting mBaSetting;
	private EXWebViewClient mEXWebViewClient;

	private CBrowserWindow mCBrowserWindow;
    private EBrowserWindow mBroWind;
    private int mDownloadCallback = 0;  // 0 下载不回调，使用引擎下载; 1 下载回调给主窗口，前端自己下载; 2 下载回调给当前窗口，前端自己下载;
    private boolean mWebApp;
	
	public ACEWebView(Context context) {
		super(context);
	}

	protected void init(EBrowserView eBrowserView) {
		setBackgroundColor(0);
		setAlpha(0.99f);
		setDrawingCacheBackgroundColor(Color.TRANSPARENT);
		setScrollbarFadingEnabled(false);
		setFadingEdgeLength(0);
		setWebViewClient();
		setWebChromeClient();
        final EBrowserView eBroView = eBrowserView;
		setDownloadListener(new XWalkDownloadListener(getContext()) {

			@Override
			public void onDownloadStart(String arg0, String arg1, String arg2,
					String arg3, long arg4) {
                if (mDownloadCallback == 0) {
                    mCBrowserWindow.onDownloadStart(getContext(), arg0, arg1, arg2, arg3, arg4);
                } else {
                    if (null != mBroWind) {
                        mBroWind.executeCbDownloadCallbackJs(eBroView, mDownloadCallback,
                                arg0, arg1, arg2, arg3, arg4);
                    }
                }
			}
			
		});
		if (mWebApp) {
			return;
		}
	}

    public void setWebApp(boolean flag) {
        mWebApp = flag;
    }

    public boolean isWebApp() {
        return mWebApp;
    }

	@SuppressLint("NewApi")
	public void pauseCore() {
	}

	public void resumeCore() {

	}

	public void initPrivateVoid() {

	}

	public void setDefaultFontSize(int size) {
		getSettings().setDefaultFontSize(size);
		getSettings().setDefaultFixedFontSize(size);
	}

	public void setSupportZoom() {
        getSettings().setSupportZoom(true);
	}

    public void setUserAgent(String userAgent) {
        getSettings().setUserAgentString(userAgent);
    }

	/**
	 * XWalkView 用load()方法load 比较长的js会有问题
	 * @param url
	 */
	public void loadUrl(String url) {
		if (url != null && url.startsWith("javascript:")) {
			super.evaluateJavascript(url, null);
		} else {
			super.load(url, null);
		}
	}

	public void loadUrl(String url, Map<String, String> extraHeaders) {
		super.load(url,null,extraHeaders);
	}

	@Override
	public void addJavascriptInterface(Object object, String name) {
		super.addJavascriptInterface(object, name);
	}


	public void onPause() {

	}

	public void onResume() {

	}

	public void goForward() {
		getNavigationHistory().navigate(
				XWalkNavigationHistory.Direction.FORWARD, 1);
	}

	public void clearHistory() {
		if (getNavigationHistory()!=null) {
			getNavigationHistory().clear();
		}
	}

	public void clearView() {
		super.removeAllViews();
	}

	public void clearMatches() {

	}

	public float getScale() {
//		float density= ESystemInfo.getIntence().mDensity;
		return super.getScaleX();
	}

	public int getContentHeight() {
		return super.getContentHeight();
	}

	public void setWebViewClient() {
		mCBrowserWindow=new CBrowserWindow(this);
		setResourceClient(mCBrowserWindow);
	}

	public void setWebChromeClient() {
		setUIClient(new CBrowserMainFrame(this));
	}

	public void setInitialScale(int scale) {
		getSettings().setInitialPageScale(scale);
	}

	public void setDownloadListener() {
		
	}

    public int getScrollYWrap(){
        return computeVerticalScrollOffset();
    }

    public int getScrollXWrap(){
        return computeHorizontalScrollOffset();
    }

	public void destroy() {
		super.onDestroy();
	}

	public void removeJavascriptInterface(String uexName) {

	}

	public void loadData(String data, String mimeType, String encoding) {
		super.load(null, data);
	}

	public void loadDataWithBaseURL(String baseUrl, String data,
			String mimeType, String encoding, String failUrl) {
		super.load(baseUrl, data);
	}

	public void goBack() {
		getNavigationHistory().navigate(
				XWalkNavigationHistory.Direction.BACKWARD, 1);
	}

	public void setVerticalScrollbarOverlay(boolean flag) {
		
	}

	public void setHorizontalScrollbarOverlay(boolean flag) {

	}

	public void reload() {
		super.reload(0);
	}

	public boolean canGoBack() {
		return getNavigationHistory().canGoBack();
	}

	public boolean canGoForward() {
		return getNavigationHistory().canGoForward();
	}

	public boolean isHardwareAccelerated() {
		return true;
	}

	public void setRemoteDebug(boolean debug) {
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, debug);
	}

    public float getScaleWrap() {
        return getScale();
    }

	public int getHeightWrap() {
		return getHeight();
	}

    public void addViewWrap(View child, android.widget.AbsoluteLayout.LayoutParams params) {
        addView(child, params);
    }

    public void removeViewWrap(View child) {
        removeView(child);
    }

    public int getChildCountWrap() {
        return getChildCount();
    }

    public View getChildAtWrap(int index) {
        return getChildAt(index);
    }

    public void setHorizontalScrollBarEnabledWrap(boolean visible) {
        setHorizontalScrollBarEnabled(visible);
    }

    public void setVerticalScrollBarEnabledWrap(boolean visible) {
        setVerticalScrollBarEnabled(visible);
    }

    public int getDownloadCallback() {
        return mDownloadCallback;
    }

    public void setDownloadCallback(int downloadCallback) {
        this.mDownloadCallback = downloadCallback;
    }

    public void setEBrowserWindow(EBrowserWindow broWind) {
        this.mBroWind = broWind;
    }

    public String getWebViewKernelInfo() {
        KernelInfoVO infoVO = new KernelInfoVO();
        infoVO.setKernelType("CrossWalk");
        infoVO.setKernelVersion(getXWalkVersion());
        String info = DataHelper.gson.toJson(infoVO);
        return info;
    }

	public View getRealWebView(){
		return this;
	}
}