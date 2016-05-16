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
import android.os.Build;
import android.webkit.DownloadListener;
import android.webkit.WebView;

import org.zywx.wbpalmstar.acedes.EXWebViewClient;
import org.zywx.wbpalmstar.engine.CBrowserMainFrame;
import org.zywx.wbpalmstar.engine.CBrowserMainFrame7;
import org.zywx.wbpalmstar.engine.CBrowserWindow;
import org.zywx.wbpalmstar.engine.CBrowserWindow7;
import org.zywx.wbpalmstar.engine.EBrowserBaseSetting;
import org.zywx.wbpalmstar.engine.EBrowserSetting;
import org.zywx.wbpalmstar.engine.EBrowserSetting7;
import org.zywx.wbpalmstar.engine.EBrowserView;


/**
 * Created by ylt on 15/8/24.
 */
public class ACEWebView extends WebView implements DownloadListener {
    private EXWebViewClient mEXWebViewClient;
    private EBrowserBaseSetting mBaSetting;
    private Context mContext;

    public ACEWebView(Context context) {
		super(context);
        this.mContext=context;
	}

    public void init(EBrowserView eBrowserView,boolean webApp) {
        if (Build.VERSION.SDK_INT <= 7) {
            if (mBaSetting == null) {
                mBaSetting = new EBrowserSetting(eBrowserView);
                mBaSetting.initBaseSetting(webApp);
                setWebViewClient(mEXWebViewClient = new CBrowserWindow());
                setWebChromeClient(new CBrowserMainFrame(eBrowserView.getContext()));
            }

        } else {

            if (mBaSetting == null) {
                mBaSetting = new EBrowserSetting7(eBrowserView);
                mBaSetting.initBaseSetting(webApp);
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

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        mEXWebViewClient.onDownloadStart(mContext, url, userAgent,
                contentDisposition, mimetype, contentLength);
    }


    @Override
    public void destroy() {
        mBaSetting=null;
        mContext=null;
        super.destroy();
    }

    public float getScaleWrap() {
        if (Build.VERSION.SDK_INT<=18){
            return getScale();
        }
        return 1.0f;
    }

    public int getScrollYWrap() {
        return getScrollY();
    }
}