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

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BConstant;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.util.AppCanAPI;
import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.EBrowserWindow;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class EUExWidgetOne extends EUExBase {
    public static final String tag = "uexWidgetOne";
    public static final String function_getId = "uexWidgetOne.cbGetId";
    public static final String function_getPlatForm = "uexWidgetOne.cbGetPlatform";
    public static final String function_getVersion = "uexWidgetOne.cbGetVersion";
    public static final String function_getWidgetNum = "uexWidgetOne.cbGetWidgetNumber";
    public static final String function_getWidgetInfo = "uexWidgetOne.cbGetWidgetInfo";
    public static final String function_getCurrentWidgetInfo = "uexWidgetOne.cbGetCurrentWidgetInfo";
    public static final String function_clearCache = "uexWidgetOne.cbCleanCache";
    public static final String function_getMainWidgetId = "uexWidgetOne.cbGetMainWidgetId";
    public static final String function_getPerssionsDenied = "uexWidgetOne.cbGetPerssionsDenied";

    public EUExWidgetOne(Context context, EBrowserView inParent) {
        super(context, inParent);

    }

    public void getId(String[] parm) {
//		try {
//			String id = WDataManager.getWidgetOneId();
//			jsCallback(function_getId, 0, EUExCallback.F_C_TEXT, id);
//		} catch (Exception e) {
//			jsCallback(function_getWidgetInfo, 0, EUExCallback.F_C_INT,
//					EUExCallback.F_C_FAILED);
//		}
    }

    public void getWidgetNumber(String[] parm) {
        try {
            WDataManager widgetData = new WDataManager(mContext);
            jsCallback(function_getWidgetNum, 0, EUExCallback.F_C_INT,
                    widgetData.getWidgetNumber());
        } catch (Exception e) {
            jsCallback(function_getWidgetInfo, 0, EUExCallback.F_C_INT,
                    EUExCallback.F_C_FAILED);
        }
    }

    public void getWidgetInfo(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        String inIndex = parm[0];
        try {
            WDataManager widgetData = new WDataManager(mContext);
            WWidgetData data = widgetData.getWidgetInfoById(Integer
                    .valueOf(inIndex));
            JSONObject obj = new JSONObject();
            obj.put(EUExCallback.F_JK_WIDGET_ID, data.m_widgetId);
            obj.put(EUExCallback.F_JK_APP_ID, data.m_appId);
            obj.put(EUExCallback.F_JK_VERSION, data.m_ver);
            obj.put(EUExCallback.F_JK_NAME, data.m_widgetName);
            obj.put(EUExCallback.F_JK_ICON, data.m_iconPath);
            jsCallback(function_getWidgetInfo, 0, EUExCallback.F_C_JSON,
                    obj.toString());
        } catch (Exception e) {
            jsCallback(function_getWidgetInfo, 0, EUExCallback.F_C_INT,
                    EUExCallback.F_C_FAILED);
        }
    }

    @AppCanAPI
    public JSONObject getCurrentWidgetInfo(String[] parm) {
        WWidgetData wgtData = mBrwView.getCurrentWidget();
        JSONObject obj = new JSONObject();
        try {
            if (wgtData.m_widgetId != null) {
                obj.put(EUExCallback.F_JK_WIDGET_ID, wgtData.m_widgetId);
            } else {
                obj.put(EUExCallback.F_JK_WIDGET_ID, "");
            }

            obj.put(EUExCallback.F_JK_APP_ID, wgtData.m_appId);
            obj.put(EUExCallback.F_JK_VERSION, wgtData.m_ver);
            obj.put(EUExCallback.F_JK_NAME, wgtData.m_widgetName);
            obj.put(EUExCallback.F_JK_ICON, wgtData.m_iconPath);
            jsCallback(function_getCurrentWidgetInfo, 0, EUExCallback.F_C_JSON,
                    obj.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            jsCallback(function_getCurrentWidgetInfo, 0, EUExCallback.F_C_INT,
                    EUExCallback.F_C_FAILED);
        }
        return obj;
    }

    public void getVersion(String[] parm) {
//		jsCallback(function_getVersion, 0, EUExCallback.F_C_TEXT, ResoureFinder.getInstance().getString(mContext, "widgetone_version"));
    }

    @AppCanAPI
    public int getPlatform(String[] parm) {
        jsCallback(function_getPlatForm, 0, EUExCallback.F_C_INT,
                EUExCallback.F_JV_ANDROID);

        return EUExCallback.F_JV_ANDROID;
    }

    public void cleanCache(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        boolean includeDiskFiles = true;
        if (parm.length > 0) {
            try {
                int flag = Integer.parseInt(parm[0]);
                includeDiskFiles = 0 == flag ? includeDiskFiles = false : true;
            } catch (Exception e) {
                ;
            }
        }
//		EBrowserWidget eWgt = curWind.getEBrowserWidget();
//		if (eWgt.checkWidgetType(EBrowserWidget.F_WINDOW_POOL_TYPE_ROOT)) {
        mBrwView.clearCache(includeDiskFiles);
        jsCallback(function_clearCache, 0, EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
//		} else {
//			jsCallback(function_clearCache, 0, EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
//			return;
//		}
    }

    public void exit(String[] parm) {
        int len = parm.length;
        boolean showDialog = !(len > 0 && "0".equals(parm[0]));
        ((EBrowserActivity) mContext).exitApp(showDialog);
    }

    public void reStartApp() {
        Intent oldData = ((Activity) mContext).getIntent();
        Intent intent = new Intent(oldData);
        int flag = ((Activity) mContext).getIntent().getFlags();
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, flag);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        long delay = System.currentTimeMillis() + 1000L;
        alarmManager.set(AlarmManager.RTC, delay, pendingIntent);
        ((EBrowserActivity) mContext).exitApp(false);
        ;
    }

    @AppCanAPI
    public String getMainWidgetId(String[] parm) {
        String appId=null;
        if (WDataManager.sRootWgt != null) {
            appId=WDataManager.sRootWgt.m_appId;
            jsCallback(function_getMainWidgetId, 0, EUExCallback.F_C_TEXT,
                    WDataManager.sRootWgt.m_appId);
        } else {
            appId=null;
            jsCallback(function_getMainWidgetId, 0, EUExCallback.F_C_TEXT, -1);
        }
        return appId;
    }

    @AppCanAPI
    public void restart(String[] params){
        BUtility.restartAPP(mContext,500);
    }

    @AppCanAPI
    public String getEngineVersion(String[] params){
        return BConstant.ENGINE_VERSION;
    }
    @AppCanAPI
    public int getEngineVersionCode(String[] params){
        return BConstant.ENGINE_VERSION_CODE;
    }

    @Override
    protected boolean clean() {

        return false;
    }
}
