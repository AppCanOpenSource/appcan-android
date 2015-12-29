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
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;

import java.lang.reflect.Method;


public class EBrowserSetting7 extends EBrowserSetting {


    public EBrowserSetting7(EBrowserView inView) {
        super(inView);
    }

    public void initBaseSetting(boolean webApp) {
        super.initBaseSetting(webApp);
        mWebSetting.setAppCacheEnabled(true);
        mWebSetting.setAppCachePath(mBrwView.getContext().getDir("cache", 0).getPath());
        mWebSetting.setDatabaseEnabled(true);
        mWebSetting.setDomStorageEnabled(true);
        mWebSetting.setLoadWithOverviewMode(false);
        mWebSetting.setDatabasePath(mBrwView.getContext().getDir("database", 0).getPath());
        if (Build.VERSION.SDK_INT >= 8) {
            try {
                mWebSetting.setPluginState(PluginState.ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Build.VERSION.SDK_INT > 10) {
            invoke();
        }
    }

    @SuppressWarnings("rawtypes")
    private void invoke() {
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
