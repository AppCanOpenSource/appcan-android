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

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.platform.myspace.MySpaceView;
import org.zywx.wbpalmstar.platform.myspace.MySpaceView.OnLoginOutCallback;

import android.content.Context;

public class EUExAppCenter extends EUExBase {

    public static final String tag = "uexAppCenter";
    public static final String F_CALLBACK_LOGIN_OUT = "uexAppCenter.cbLoginOut";
    public static final String F_CALLBACK_GET_SESSION_KEY = "uexAppCenter.cbGetSessionKey";

    public EUExAppCenter(Context context, EBrowserView inParent) {
        super(context, inParent);
    }

    public void getSessionKey(String[] params) {
        MySpaceView myspaceView = mBrwView.getBrowserWindow().getBrowser().getAppCenter();
        if (myspaceView != null) {
            String sessionKey = myspaceView.getSessionKey();
            jsCallback(F_CALLBACK_GET_SESSION_KEY, 0, EUExCallback.F_C_TEXT, sessionKey);
        }
    }

    /**
     * 登录返回用户登录信息接口
     *
     * @param userId
     */
    public void appCenterLoginResult(String[] params) {
        if (params.length > 0) {
            MySpaceView myspaceView = mBrwView.getBrowserWindow().getBrowser().getAppCenter();
            if (myspaceView != null) {
                myspaceView.onUserLoginCallback(params[0]);
            }
        } else {
            BDebug.e(tag, "appCenterLoginResult()--->params error!");
        }
    }

    /**
     * 下载应用
     *
     * @param json JSON格式的应用下载信息
     */
    public void downloadApp(String[] params) {
        if (params.length > 0) {
            BDebug.d(tag, "JSON:" + params[0]);
            MySpaceView myspaceView = mBrwView.getBrowserWindow().getBrowser().getAppCenter();
            if (myspaceView != null) {
                myspaceView.notifyDownloadApp(params[0]);
            }
        } else {
            BDebug.e(tag, "downloadApp()--->params error!");
        }
    }

    public void loginOut(String[] params) {
        BDebug.d(tag, "onLoginOut----------->");
        MySpaceView myspaceView = mBrwView.getBrowserWindow().getBrowser().getAppCenter();
        if (myspaceView != null) {
            myspaceView.notifyLoginOut(new OnLoginOutCallback() {
                @Override
                public void onLoginOut(boolean result) {
                    jsCallback(F_CALLBACK_LOGIN_OUT, 0, EUExCallback.F_C_INT, result ? EUExCallback.F_C_SUCCESS
                            : EUExCallback.F_C_FAILED);
                }
            });
        }
    }

    @Override
    protected boolean clean() {

        return true;
    }

}
