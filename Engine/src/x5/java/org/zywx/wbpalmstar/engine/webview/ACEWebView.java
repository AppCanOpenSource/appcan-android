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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebView;

import org.zywx.wbpalmstar.acedes.EXWebViewClient;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.vo.KernelInfoVO;
import org.zywx.wbpalmstar.engine.CBrowserMainFrame;
import org.zywx.wbpalmstar.engine.CBrowserMainFrame7;
import org.zywx.wbpalmstar.engine.CBrowserWindow;
import org.zywx.wbpalmstar.engine.CBrowserWindow7;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserBaseSetting;
import org.zywx.wbpalmstar.engine.EBrowserSetting;
import org.zywx.wbpalmstar.engine.EBrowserSetting7;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.EBrowserWindow;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;


/**
 * Created by ylt on 15/8/24.
 */
public class ACEWebView extends WebView implements DownloadListener {
    private EXWebViewClient mEXWebViewClient;
    private EBrowserBaseSetting mBaSetting;
    private Context mContext;
    private boolean mWebApp;
    private EBrowserView mBroView;
    private EBrowserWindow mBroWind;
    private int mDownloadCallback = 0;  // 0 下载不回调，使用引擎下载; 1 下载回调给主窗口，前端自己下载; 2 下载回调给当前窗口，前端自己下载;

    public ACEWebView(Context context) {
		super(context);
        this.mContext=context;
	}

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        	//Debug使用，用于在debug时在页面呈现内核类型和版本
                	boolean ret = super.drawChild(canvas, child, drawingTime);
            if (WDataManager.sRootWgt==null){
                return ret;
            }
        	int debug = WDataManager.sRootWgt.m_appdebug;
        	if (debug == 1&& BDebug.DEBUG) {
            		canvas.save();
            		Paint paint = new Paint();
            		paint.setColor(0x7fff0000);
            		paint.setTextSize(24.f);
            		paint.setAntiAlias(true);
            		if (getX5WebViewExtension() != null) {
                			canvas.drawText(this.getContext().getPackageName() + "-pid:"
                        					+ android.os.Process.myPid(), 10, 50, paint);
                			canvas.drawText(
                        					"X5  Core:" + QbSdk.getTbsVersion(this.getContext()),
                        					10, 100, paint);
                		} else {
                			canvas.drawText(this.getContext().getPackageName() + "-pid:"
                        					+ android.os.Process.myPid(), 10, 50, paint);
                			canvas.drawText("Sys Core", 10, 100, paint);
                		}
            		canvas.drawText(Build.MANUFACTURER, 10, 150, paint);
            		canvas.drawText(Build.MODEL, 10, 200, paint);
            		canvas.restore();
            	}
        	return ret;
        }


    public void init(EBrowserView eBrowserView) {
        if (getX5WebViewExtension() != null) {
            mWebApp = true;
        }
        mBroView = eBrowserView;
        if (Build.VERSION.SDK_INT <= 7) {
            if (mBaSetting == null) {
                mBaSetting = new EBrowserSetting(eBrowserView);
                mBaSetting.initBaseSetting(mWebApp);
                setWebViewClient(mEXWebViewClient = new CBrowserWindow());
                setWebChromeClient(new CBrowserMainFrame(eBrowserView.getContext()));
            }

        } else {

            if (mBaSetting == null) {
                mBaSetting = new EBrowserSetting7(eBrowserView);
                mBaSetting.initBaseSetting(mWebApp);
                setWebViewClient(mEXWebViewClient = new CBrowserWindow7());
                setWebChromeClient(new CBrowserMainFrame7(eBrowserView.getContext()));
            }

        }
    }


    public void setDownloadListener() {
        setDownloadListener(this);
    }

    public void setRemoteDebug(boolean remoteDebug) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(remoteDebug);
        }
    }

    public void setDefaultFontSize(int defaultFontSize) {
        if (mBaSetting!=null){
            mBaSetting.setDefaultFontSize(defaultFontSize);
        }
    }

    public void setSupportZoom() {
        if (mBaSetting!=null){
            mBaSetting.setSupportZoom();
        }
    }

    public void setUserAgent(String userAgent) {
        if (mBaSetting!=null){
            mBaSetting.setUserAgent(userAgent);
        }
    }

    public void setWebApp(boolean flag) {
        mWebApp = flag;
    }

    public boolean isWebApp() {
        return mWebApp;
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (mDownloadCallback == 0) {
            mEXWebViewClient.onDownloadStart(mContext, url, userAgent,
                    contentDisposition, mimetype, contentLength);
        } else {
            if (null != mBroWind && null != mBroView) {
                mBroWind.executeCbDownloadCallbackJs(mBroView, mDownloadCallback,
                        url, userAgent, contentDisposition, mimetype, contentLength);
            }
        }
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

    @Override
    public void destroy() {
        mBaSetting=null;
        mContext=null;
        super.destroy();
    }

    @Override
    public void loadUrl(String url) {
        if (url != null && url.startsWith("javascript:")) {
            super.evaluateJavascript(url, null);
        } else {
            super.loadUrl(url);
        }
    }

    public float getScaleWrap() {
        if (Build.VERSION.SDK_INT <= 18 || getX5WebViewExtension() != null) {
            return getScale();
        }
        return 1.0f;
    }

    public int getHeightWrap(){
        return getView().getHeight();
    }

    public int getScrollYWrap() {
        return getView().getScrollY();
    }

    public int getScrollXWrap() {
        return getView().getScrollX();
    }

    public void addViewWrap(View child, android.widget.AbsoluteLayout.LayoutParams params) {
        ((ViewGroup) getView()).addView(child, params);
    }

    public void removeViewWrap(View child) {
        ((ViewGroup) getView()).removeView(child);
    }

    public int getChildCountWrap() {
        return ((ViewGroup) getView()).getChildCount();
    }

    public View getChildAtWrap(int index) {
        return ((ViewGroup) getView()).getChildAt(index);
    }

    public void setHorizontalScrollBarEnabledWrap(boolean visible) {
        getView().setHorizontalScrollBarEnabled(visible);
    }

    public void setVerticalScrollBarEnabledWrap(boolean visible) {
        getView().setVerticalScrollBarEnabled(visible);
    }

    public String getWebViewKernelInfo() {
        KernelInfoVO infoVO = new KernelInfoVO();
        if (getX5WebViewExtension() != null) {
            infoVO.setKernelType("X5");
            infoVO.setKernelVersion(QbSdk.getTbsVersion(this.getContext()) + "");
        } else {
            if (Build.VERSION.SDK_INT > 18) {
                infoVO.setKernelType("System(Blink)");
                try {
                    PackageManager pm = this.getContext().getPackageManager();
                    PackageInfo pinfo = pm.getPackageInfo("com.google.android.webview",
                            PackageManager.GET_CONFIGURATIONS);
                    infoVO.setKernelVersion(pinfo.versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                infoVO.setKernelType("System(Webkit)");
            }
        }
        String info = DataHelper.gson.toJson(infoVO);
        return info;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 腾讯X5内核，WebView不执行onTouchEvent()方法
        onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
    public View getRealWebView(){
        return getView();
    }
}