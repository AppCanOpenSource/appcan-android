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

import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;

import org.zywx.wbpalmstar.base.BConstant;

import java.lang.reflect.Method;

public class EBrowserSetting7 implements EBrowserBaseSetting {

    public static final String USERAGENT_APPCAN = BConstant.USERAGENT_APPCAN;
    public static String USERAGENT_NEW;

    protected WebSettings mWebSetting;
    protected EBrowserView mBrwView;
    protected boolean mWebApp;

    public EBrowserSetting7(EBrowserView inView) {
        mWebSetting = inView.getSettings();
        mBrwView = inView;
        USERAGENT_NEW = mWebSetting.getUserAgentString() + USERAGENT_APPCAN;
        BConstant.USERAGENT_NEW = USERAGENT_NEW;
    }

    public void initBaseSetting(boolean webApp) {
        mWebApp = webApp;
        mWebSetting.setSaveFormData(false);
        mWebSetting.setSavePassword(false);
        mWebSetting.setLightTouchEnabled(false);
        mWebSetting.setJavaScriptEnabled(true);
        mWebSetting.setNeedInitialFocus(false);
        mWebSetting.setSupportMultipleWindows(false);
        mWebSetting.setGeolocationEnabled(true);
        // mWebSetting.setNavDump(false);
        //mWebSetting.setPluginsEnabled(true);
        mWebSetting.setJavaScriptCanOpenWindowsAutomatically(false);
        mWebSetting.setUseWideViewPort(false);
        mWebSetting.setLoadsImagesAutomatically(true);
        mWebSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        mWebSetting.setUserAgentString(USERAGENT_NEW);
        mWebSetting.setRenderPriority(RenderPriority.HIGH);
        mWebSetting.setDefaultTextEncodingName("UTF-8");
        mWebSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);// 默认缓存模式为不使用缓存

        if (webApp) {
            mWebSetting.setUseWideViewPort(true);
            //允许混合模式，解决个别页面或者视频无法正常显示和播放的问题（http与https）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWebSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            return;
        }
        mWebSetting.setTextZoom(100);
        mWebSetting.setDisplayZoomControls(false);
        // disables the actual onscreen controls from showing up
        // 默认状态为关闭缩放按钮
        mWebSetting.setBuiltInZoomControls(false);
        // disables the ability to zoom
        // 默认禁止缩放
        mWebSetting.setSupportZoom(false);
        mWebSetting.setDefaultFontSize(ESystemInfo.getIntence().mDefaultFontSize);
        mWebSetting.setDefaultFixedFontSize(ESystemInfo.getIntence().mDefaultFontSize);
        if (Build.VERSION.SDK_INT <= 18) {
            mWebSetting.setDefaultZoom(ESystemInfo.getIntence().mDefaultzoom.getValue());
        }
        mWebSetting.setDomStorageEnabled(true);//开启DOM storage API功能
        mWebSetting.setAppCacheEnabled(true);
        mWebSetting.setAppCachePath(mBrwView.getContext().getDir("cache", 0).getPath());
        mWebSetting.setDatabaseEnabled(true);
        mWebSetting.setDatabasePath(mBrwView.getContext().getDir("database", 0).getPath());
        mWebSetting.setLoadWithOverviewMode(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebSetting.setAllowFileAccessFromFileURLs(true);
            mWebSetting.setAllowUniversalAccessFromFileURLs(true);
        }
        // 默认是允许的，但是为了保险还是加上。猜测可能当时76和77版本的webview存在问题与此有关。
        mWebSetting.setAllowContentAccess(true);
        try {
            mWebSetting.setPluginState(WebSettings.PluginState.ON);
        } catch (Exception e) {
            e.printStackTrace();
        }
        invokeHideSettings();
    }

    @Override
    public void setCacheMode(int cacheMode) {
        if (cacheMode == EBrwViewEntry.AC_LOAD_ENGINE_DEFAULT){
            mWebSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }else if (cacheMode == EBrwViewEntry.AC_LOAD_WEBVIEW_DEFAULT){
            mWebSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        }else if (cacheMode == EBrwViewEntry.AC_LOAD_CACHE_ELSE_NETWORK){
            mWebSetting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }else if (cacheMode == EBrwViewEntry.AC_LOAD_CACHE_ONLY){
            mWebSetting.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        }
    }

    @Override
    public void setDefaultFontSize(int size) {
        if (mWebApp) {
            return;
        }
        mWebSetting.setDefaultFontSize(size);
        mWebSetting.setDefaultFixedFontSize(size);
    }

    @Override
    public void setSupportZoom() {
        mWebSetting.setSupportZoom(true);
        mWebSetting.setBuiltInZoomControls(true);
    }

    @Override
    public void setUserAgent(String userAgent) {
        if (TextUtils.isEmpty(userAgent)) {
            mWebSetting.setUserAgentString(USERAGENT_NEW);
        } else {
            mWebSetting.setUserAgentString(userAgent);
        }
    }

    /**
     * 隐藏设置配置
     * （之前的代码，重构后暂时保留在此，后期需要评估是否还有必要）
     */
    private void invokeHideSettings() {
        Class[] paramTypes = {boolean.class};
        try {
            Method setEnableSmoothTransition = WebSettings.class.getDeclaredMethod("setEnableSmoothTransition", paramTypes);
            setEnableSmoothTransition.invoke(mWebSetting, true);
        } catch (Exception e) {
//			e.printStackTrace();
        }
        try {
            Method setAutoFillEnabled = WebSettings.class.getDeclaredMethod("setAutoFillEnabled", paramTypes);
            setAutoFillEnabled.invoke(mWebSetting, false);
        } catch (Exception e) {
//			e.printStackTrace();
        }
        try {
            Method setHardwareAccelSkiaEnabled = WebSettings.class.getDeclaredMethod("setHardwareAccelSkiaEnabled", paramTypes);
            setHardwareAccelSkiaEnabled.invoke(mWebSetting, false);
        } catch (Exception e) {
//			e.printStackTrace();
        }
        try {
            Method setForceUserScalable = WebSettings.class.getDeclaredMethod("setForceUserScalable", paramTypes);
            setForceUserScalable.invoke(mWebSetting, false);
        } catch (Exception e) {
//			e.printStackTrace();
        }
    }
}
