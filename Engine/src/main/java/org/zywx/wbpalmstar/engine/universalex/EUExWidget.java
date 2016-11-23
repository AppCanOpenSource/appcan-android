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
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.JsConst;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.base.util.AppCanAPI;
import org.zywx.wbpalmstar.base.vo.AppInstalledVO;
import org.zywx.wbpalmstar.base.vo.ErrorResultVO;
import org.zywx.wbpalmstar.base.vo.StartAppVO;
import org.zywx.wbpalmstar.base.vo.WidgetCheckUpdateResultVO;
import org.zywx.wbpalmstar.base.vo.WidgetFinishVO;
import org.zywx.wbpalmstar.base.vo.WidgetStartVO;
import org.zywx.wbpalmstar.engine.*;
import org.zywx.wbpalmstar.engine.universalex.wrapper.WidgetJsonWrapper;
import org.zywx.wbpalmstar.platform.push.report.PushReportConstants;
import org.zywx.wbpalmstar.widgetone.dataservice.ReData;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EUExWidget extends EUExBase {
    public static final String tag = "uexWidget";

    public static final int LOADAPP_RQ_CODE = 1000001;

    public static final String function_getOpenerInfo = "uexWidget.cbGetOpenerInfo";
    public static final String function_checkUpdate = "uexWidget.cbCheckUpdate";
    public static final String function_startWidget = "uexWidget.cbStartWidget";
    public static final String function_removeWidget = "uexWidget.cbRemoveWidget";
    public static final String function_getPushInfo = "uexWidget.cbGetPushInfo";
    public static final String function_getPushState = "uexWidget.cbGetPushState";
    public static final String function_onSpaceClick = "uexWidget.onSpaceClick";
    public static final String function_loadApp = "uexWidget.cbLoadApp";
    public static final String function_getMBaaSHost = "uexWidget.cbGetMBaaSHost";
    private static final String BUNDLE_DATA = "data";
    private static final String BUNDLE_MESSAGE = "message";
    private static final String PUSH_MSG_BODY = "0";
    private static final String PUSH_MSG_ALL = "1";
    private static final int MSG_IS_APP_INSTALLED = 0;
    private static final int MSG_RELOAD_WIDGET_BY_APPID= 1;

    public EUExWidget(Context context, EBrowserView inParent) {
        super(context, inParent);
    }

    @AppCanAPI
    public boolean startWidget(String[] parm) {
        int callbackId=-1;
        if (isJsonString(parm[0])){
            if (parm.length>1){
                callbackId=valueOfCallbackId(parm[1]);
            }
            WidgetStartVO startVO= DataHelper.gson.fromJson(parm[0], WidgetStartVO.class);
            parm=new String[]{
                    startVO.appId,
                    startVO.animId,
                    startVO.funcName,
                    startVO.info,
                    startVO.animDuration
            };
        }

        if (parm.length < 4) {
            return false;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return false;
        }
        String inAppId = parm[0];
        String inAnimiId = parm[1];
        String inForResult = parm[2];
        String inInfo = parm[3];
        String animDuration = null;
        String appKey = null;
        if (parm.length >= 5) {
            animDuration = parm[4];
        }
        if (parm.length >= 6) {
            appKey = parm[5];
        }
        int animId = EBrowserAnimation.ANIM_ID_NONE;
        long duration = EBrowserAnimation.defaultDuration;
        try {
            if (null != inAnimiId && inAnimiId.length() != 0) {
                animId = Integer.parseInt(inAnimiId);
            }
            if (null != animDuration && animDuration.length() != 0) {
                duration = Long.parseLong(animDuration);
            }
        } catch (Exception e) {
            ;
        }
        try {
            WDataManager widgetData = new WDataManager(mContext);
            // WWidgetData data = widgetData.getWidgetDataByAppId(inAppId,
            // mBrwView.getCurrentWidget());
            // 修改可以子widget可以打开子widget
            WWidgetData data = widgetData.getWidgetDataByAppId(inAppId,
                    mBrwView.getRootWidget());
            if (data == null) {
                showErrorAlert(String.format(EUExUtil.getString("platform_widget_not_exist")
                        , inAppId + ""));
                resultStartWidget(false,callbackId);
                return false;
            }
            data.m_appkey = appKey;
            EWgtResultInfo info = new EWgtResultInfo(inForResult, inInfo);
            info.setAnimiId(animId);
            info.setDuration(duration);
            if (startWidget(data, info)) {
                resultStartWidget(true,callbackId);
                return true;
            } else {
                resultStartWidget(false,callbackId);
                return false;
            }
        } catch (Exception e) {
            if (BDebug.DEBUG) {
                e.printStackTrace();
            }
            resultStartWidget(false,callbackId);
            showErrorAlert(EUExUtil.getString("platform_widget_search_failed"));
            return false;
        }
    }

    private void resultStartWidget(boolean result,int callbackId){
        if (callbackId==-1){
            jsCallback(function_startWidget, 0, EUExCallback.F_C_INT,
                    EUExCallback.F_C_FAILED);
        }else{
            callbackToJs(callbackId,false,result?0:1);
        }
    }


    private void showErrorAlert(final String msg) {
        /*Runnable ui = new Runnable() {
            @Override
			public void run() {
				final AlertDialog.Builder dialog = new AlertDialog.Builder(
						mContext);
				dialog.setTitle("提示");
				dialog.setMessage(msg);
				dialog.setCancelable(false);
				dialog.setPositiveButton("确定", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.create();
				dialog.show();
			}
		};
		Activity uiThread = (Activity) mContext;
		uiThread.runOnUiThread(ui);*/
    }

    public void setSpaceEnable(String[] params) {
        mBrwView.getBrowserWindow().setSpaceEnable(new SpaceClickListener() {
            @Override
            public void onSpaceClick() {
                jsCallback(function_onSpaceClick, 0, 0, 0);
            }
        });
    }

    public void setLogServerIp(String[] params) {
        if (params.length < 2) {
            return;
        }
        int debug = 0;

        WWidgetData widgetData = mBrwView.getCurrentWidget();
        widgetData.m_logServerIp = params[0];
        try {
            debug = Integer.parseInt(params[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        widgetData.m_appdebug = debug;
        mBrwView.setRemoteDebug(debug == 1);
    }

    public void delPushInfo(String[] params) {
        String host_pushBindUser = ResoureFinder.getInstance().getString(mContext,
                "bindUser_host");
        if (host_pushBindUser.indexOf("push") > 0) {
            String uId = "";
            String uNickName = "";
            if (params.length > 0) {
                uId = params[0];
            }
            if (params.length > 1) {
                uNickName = params[1];
            }
            AppCan.getInstance().delPushInfo(uId, uNickName, mContext, mBrwView);
        } else {
            AppCan.getInstance().deviceUnBind(mContext, mBrwView);
        }
    }

    @AppCanAPI
    public boolean startApp(String[] params) {
        if (params.length < 2) {
            BDebug.e(tag, "startApp has error params!!!");
            return false;
        }
        String startMode = params[0];
        Intent intent = null;
        String extraJson = null;
        // 是否通过NEW_TASK启动第三方Activity的开关_by_waka
        boolean switchNewTask = true;// 默认为使用NEW_TASk启动
        if (!TextUtils.isEmpty(startMode)) {
            if ("0".equals(startMode)) {
                String pkgName = params[1];
                String clsName = null;
                if (TextUtils.isEmpty(pkgName)) {
                    BDebug.e(tag, "startApp has error pkgName!!!");
                    callBackPluginJs(JsConst.CALLBACK_START_APP, "error params");
                    return false;
                }

                if (params.length > 2) {
                    clsName = params[2];
                }
                if (TextUtils.isEmpty(clsName)) {
                    clsName = getMainActivity(pkgName);
                }
                if (TextUtils.isEmpty(clsName)) {
                    BDebug.e(tag, "startApp has error clsName!!!");
                    callBackPluginJs(JsConst.CALLBACK_START_APP, "package is not exist!");
                    return false;
                }
                ComponentName component = new ComponentName(pkgName, clsName);
                intent = new Intent();
                intent.setComponent(component);
            } else if ("1".equals(startMode)) {
                String action = params[1];
                String filterJson = null;
                intent = new Intent(action);
                if (params.length > 2) {
                    filterJson = params[2];
                }
                if (!TextUtils.isEmpty(filterJson)) {
                    intent = setIntentFilter(intent, filterJson);
                }
            } else {
                BDebug.e(tag, "startApp has error startMode!!!");
                callBackPluginJs(JsConst.CALLBACK_START_APP, "error params!");
                return false;
            }
        }
        StartAppVO extraVO = null;
        if (params.length > 4) {
            extraVO = DataHelper.gson.fromJson(params[4], StartAppVO.class);
        }
        if (extraVO != null && extraVO.getData() != null) {
            Uri contentUrl = Uri.parse(extraVO.getData());
            intent.setData(contentUrl);
        }
        // 如果isNewTask.equals("0") by waka
        if (extraVO != null && "0".equals(extraVO.getIsNewTask())) {
            switchNewTask = false;// NEW_TASK开关置为false
        }
        if (intent == null) {
            BDebug.e(tag, "startApp has error params!!!");
            callBackPluginJs(JsConst.CALLBACK_START_APP, "error params!");
            return false;
        }
        if (params.length > 3) {
            extraJson = params[3];
            if (!TextUtils.isEmpty(extraJson)) {
                intent = setIntentExtras(intent, extraJson);
            }
        }
        try {
            // 如果NEW_TASK开关打开
            if (switchNewTask) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 添加NEW_TASK_FLAG
            }
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            callBackPluginJs(JsConst.CALLBACK_START_APP, e.getMessage());
            return false;
        }
    }

    private Intent setIntentFilter(Intent intent, String filterJson) {
        try {
            JSONObject json = new JSONObject(filterJson);
            if (json.has("category")) {
                JSONArray category = json.getJSONArray("category");
                if (category != null) {
                    for (int i = 0; i < category.length(); i++) {
                        String ctg = category.opt(i).toString();
                        if (!TextUtils.isEmpty(ctg)) {
                            intent.addCategory(ctg);
                        }
                    }
                }
            }
            if (json.has("data")) {
                JSONObject dataJson = json.getJSONObject("data");
                String mimeType = null;
                String scheme = null;
                if (dataJson != null) {
                    if (dataJson.has("mimeType")) {
                        mimeType = dataJson.getString("mimeType");
                    }
                    if (dataJson.has("scheme")) {
                        scheme = dataJson.getString("scheme");
                    }
                    if (TextUtils.isEmpty(mimeType) && !TextUtils.isEmpty(scheme)) {
                        intent.setData(Uri.parse(scheme));
                    } else if (!TextUtils.isEmpty(mimeType) && !TextUtils.isEmpty(scheme)) {
                        intent.setDataAndType(Uri.parse(scheme), mimeType);
                    } else if (!TextUtils.isEmpty(mimeType) && TextUtils.isEmpty(scheme)) {
                        intent.setType(mimeType);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return intent;
    }

    private String getMainActivity(String pkgName) {
        String className = null;
        PackageInfo pi = null;
        try {
            pi = mContext.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (pi == null) return null;

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);

        List<ResolveInfo> apps = mContext.getPackageManager().queryIntentActivities(resolveIntent, 0);
        if (apps == null || apps.size() == 0) return null;

        ResolveInfo ri = apps.iterator().next();
        if (ri != null) {
            className = ri.activityInfo.name;
        }
        return className;
    }

    private Intent setIntentExtras(Intent intent, String extraJson) {
        String arrayString = "[" + extraJson + "]";
        try {
            JSONArray array = new JSONArray(arrayString);
            int length = array.length();
            for (int i = 0; i < length; i++) {
                JSONObject json = array.getJSONObject(i);
                Iterator<?> keys = json.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = json.optString(key);
                    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                        intent.putExtra(key, value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return intent;
    }

    public void startWidgetWithPath(String[] parm) {
        if (parm.length < 4) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String path = parm[0];
        String inAnimiId = parm[1];
        String inForResult = parm[2];
        String inInfo = parm[3];
        String animDuration = null;
        String appKey = null;
        if (parm.length >= 5) {
            animDuration = parm[4];
        }
        if (parm.length >= 6) {
            appKey = parm[5];
        }
        int animId = EBrowserAnimation.ANIM_ID_NONE;
        long duration = EBrowserAnimation.defaultDuration;
        try {
            if (null != inAnimiId && inAnimiId.length() != 0) {
                animId = Integer.parseInt(inAnimiId);
            }
            if (null != animDuration && animDuration.length() != 0) {
                duration = Long.parseLong(animDuration);
            }
        } catch (Exception e) {
            ;
        }
        try {
            WDataManager widgetData = new WDataManager(mContext);
            WWidgetData data = widgetData.getWidgetDataByAppPath(path);
            if (data == null) {
                showErrorAlert(String.format(EUExUtil.getString("platform_widget_path_not_exist"), path));
                jsCallback(function_startWidget, 0, EUExCallback.F_C_INT,
                        EUExCallback.F_C_FAILED);
                return;
            }
            data.m_appkey = appKey;
            EWgtResultInfo info = new EWgtResultInfo(inForResult, inInfo);
            info.setAnimiId(animId);
            info.setDuration(duration);
            if (startWidget(data, info)) {
                jsCallback(function_startWidget, 0, EUExCallback.F_C_INT,
                        EUExCallback.F_C_SUCCESS);
            } else {
                jsCallback(function_startWidget, 0, EUExCallback.F_C_INT,
                        EUExCallback.F_C_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert(EUExUtil.getString("platform_widget_search_failed"));
            jsCallback(function_startWidget, 0, EUExCallback.F_C_INT,
                    EUExCallback.F_C_FAILED);
        }
    }

    public void finishWidget(String[] parm) {
        if (isFirstParamExistAndIsJson(parm)){
            WidgetFinishVO finishVO=DataHelper.gson.fromJson(parm[0],WidgetFinishVO.class);
            WidgetJsonWrapper.finishWidget(this,finishVO);
        }

        if (parm.length < 1) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inResultInfo = parm[0];
        String appId = null;
        boolean isWgtBG = false;
        String inAnimiId = "";
        if (parm.length > 1) {
            if (!TextUtils.isEmpty(parm[1]) && parm[1].trim().length() != 0) {
                appId = parm[1];
            }
        }
        if (parm.length > 2) {
            if (!TextUtils.isEmpty(parm[2]) && parm[2].trim().length() != 0) {
                isWgtBG = "1".equals(parm[2]);
            }
        }
        if (parm.length > 3) {
            if (!TextUtils.isEmpty(parm[3]) && parm[3].trim().length() != 0) {
                inAnimiId = parm[3];
            }
        }
        finishWidget(inResultInfo, appId, isWgtBG, inAnimiId);
    }

    @AppCanAPI
    public boolean removeWidget(String[] parm) {
        if (parm.length < 1) {
            return false;
        }
        String inAppId = parm[0];
        WDataManager dm = new WDataManager(mContext);
        String info = dm.removeWgtByAppID(inAppId);
        if (info.equals("0")) {
            jsCallback(function_removeWidget, 0, EUExCallback.F_C_INT,
                    EUExCallback.F_C_SUCCESS);
            return true;
        } else {
            jsCallback(function_removeWidget, 0, EUExCallback.F_C_INT,
                    EUExCallback.F_C_FAILED);
            return false;
        }
    }

    public void closeLoading(String[] params) {
        ((EBrowserActivity) mContext).setContentViewVisible(0);
    }


    public void loadApp(String[] parm) {
        if (parm.length < 3) {
            return;
        }
        String inAppInfo = parm[0];
        String inFilter = parm[1];
        String inDataUri = parm[2];
        String data = null;
        if (4 == parm.length) {
            data = parm[3];
        }
        if (null == inAppInfo || 0 == inAppInfo.length()) {

            return;
        }
        Intent intent = new Intent(inAppInfo);
        if ((null != inDataUri && 0 != inDataUri.length())
                && (null != inFilter && 0 != inFilter.length())) {
            Uri uri = Uri.parse(inDataUri);
            intent.setDataAndType(uri, inFilter);
        } else {
            if (null != inDataUri && 0 != inDataUri.length()) {
                Uri uri = Uri.parse(inDataUri);
                intent.setData(uri);
            }
            if (null != inFilter && 0 != inFilter.length()) {
                intent.setType(inFilter);
            }
        }
        if (null != data) {
            try {
                JSONObject json = new JSONObject(data);
                Iterator<?> keys = json.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = json.optString(key);
                    if (null != key && null != value) {
                        intent.putExtra(key, value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            startActivityForResult(Intent.createChooser(intent, EUExUtil.getString("platform_choose_app")),
                    LOADAPP_RQ_CODE);
        } catch (Exception e) {
            Toast.makeText(mContext, "not find any app", Toast.LENGTH_SHORT)
                    .show();
        }
        /**
         * load UC intent.putExtra("UC_LOADURL", inDataInfo);
         * intent.putExtra("time_stamp", System.currentTimeMillis());
         * intent.putExtra("recall_action", "com.test.openintenttouc");
         */
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOADAPP_RQ_CODE) {
            JSONObject json = new JSONObject();
            JSONObject jValue = new JSONObject();
            try {
                if (null != data) {
                    Bundle bundle = data.getExtras();
                    if (null != bundle) {
                        Set<String> keys = bundle.keySet();
                        if (null != keys) {
                            for (String key : keys) {
                                Object value = bundle.get(key);
                                jValue.put(key, value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                json.put("value", jValue);
                if (resultCode == Activity.RESULT_OK) {
                    json.put("resultCode", 1);
                } else {
                    json.put("resultCode", 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsCallback(function_loadApp, 0, EUExCallback.F_C_JSON,
                    json.toString());
        }
    }

    public void installApp(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inAppPath = parm[0];
        if (null == inAppPath || 0 == inAppPath.trim().length()) {
            return;
        }
        inAppPath = BUtility.makeRealPath(inAppPath,
                mBrwView.getCurrentWidget().m_widgetPath,
                curWind.getWidgetType());
        if (inAppPath.contains("wgtRes")) {
            InputStream is;
            try {
                // String tPath = m_eContext.getCacheDir().getAbsolutePath() +
                // "/temp.apk";
                String tPath = Environment.getExternalStorageDirectory().getPath() + "/temp.apk";
                is = mContext.getAssets().open(inAppPath);
                File file = new File(tPath);
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] temp = new byte[8 * 1024];
                int i = 0;
                while ((i = is.read(temp)) > 0) {
                    fos.write(temp, 0, i);
                }
                fos.close();
                is.close();
                inAppPath = tPath;
                // modify permission
                // String command = "chmod777" + " " + inAppPath;
                // Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                Toast.makeText(
                        mContext,
                        ResoureFinder.getInstance().getString(mContext,
                                "error_sdcard_is_not_available"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // install apk.
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MimeTypeMap type = MimeTypeMap.getSingleton();
        String mime = type.getMimeTypeFromExtension("apk");
        intent.setDataAndType(Uri.parse("file://" + inAppPath), mime);
        mContext.startActivity(intent);
    }

    public void setMySpaceInfo(String[] parm) {
        if (parm.length < 3) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inForResult = parm[0];
        String inAnimiId = parm[1];
        String inInfo = parm[2];
        curWind.getBrowser().setMySpaceInfo(inForResult, inAnimiId, inInfo);
    }

    @AppCanAPI
    public String getOpenerInfo(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return null;
        }
        String opener=curWind.getOpener();
        jsCallback(function_getOpenerInfo, 0, EUExCallback.F_C_TEXT,
                opener);
        return opener;
    }

    public void setPushNotifyCallback(String[] parm) {
        mBrwView.getBrowserWindow().getEBrowserWidget()
                .setPushNotify(mBrwView.getWindowName(), parm[0],
                mBrwView.getCurrentWidget().m_appId);
    }

    public void checkUpdate(String[] parm) {
        int callbackId=-1;
        if (parm.length>0){
            callbackId=valueOfCallbackId(parm[0]);
        }
        final WWidgetData widgetData = mBrwView.getCurrentWidget();
        final int finalCallbackId = callbackId;
        new Thread("Appcan-uexWidgetCheckUpdate") {
            public void run() {
                WidgetCheckUpdateResultVO resultVO = new WidgetCheckUpdateResultVO();
                ErrorResultVO errorResultVO = new ErrorResultVO();
                if (widgetData != null) {
                    // if (widgetData.m_appId != null
                    // && widgetData.m_appId.length() > 0) {
                    WDataManager dataManager = new WDataManager(mContext);
                    ReData reData = dataManager.ChekeUpdate(mContext,
                            widgetData.m_updateurl, widgetData.m_appId,
                            widgetData.m_ver);
                    if (reData == null) {
                        errorResultVO.errorCode = 2;
                        resultVO.result = 2;
                        if (finalCallbackId != -1) {
                            callbackToJs(finalCallbackId, false, DataHelper.gson.toJsonTree(errorResultVO),
                                    DataHelper.gson.toJsonTree(resultVO));
                        } else {
                            jsCallback(function_checkUpdate, 0,
                                    EUExCallback.F_C_JSON, DataHelper.gson.toJson(resultVO));
                        }
                        return;
                    } else if (!TextUtils.isEmpty(reData.fileUrl)) {
                        resultVO.result = 0;
                        resultVO.name = reData.fileName;
                        resultVO.size = reData.fileSize;
                        resultVO.url = reData.fileUrl;
                        resultVO.version = reData.version;
                    } else {
                        resultVO.result = 1;
                    }
                    if (finalCallbackId != -1) {
                        callbackToJs(finalCallbackId, false, null, DataHelper.gson.toJsonTree(resultVO));
                    } else {
                        jsCallback(function_checkUpdate, 0,
                                EUExCallback.F_C_JSON, DataHelper.gson.toJson(resultVO));
                    }
                }
            }
        }.start();
    }

    public void setPushInfo(String[] parm) {
        if (parm.length != 2) {
            parm = new String[]{"", ""};
        }
        final String userId = parm[0];
        final String userNick = parm[1];
        String host_pushBindUser = ResoureFinder.getInstance().getString(mContext,
                "bindUser_host");
        if (host_pushBindUser.indexOf("push") > 0) {
            AppCan.getInstance().setPushInfo(userId, userNick, mContext, mBrwView);
        } else {
            AppCan.getInstance().deviceBind(userId, userNick, mContext, mBrwView);
        }
    }

    public void setPushState(String[] parm) {
        if (parm.length != 1) {
            return;
        }
        AppCan.getInstance().setPushState(Integer.parseInt(parm[0]));

    }

    public void setKeyboardMode(final String[] param) {
        if (param.length <= 0) {
            return;
        }
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(param[0]);
                    int mode = jsonObject.optInt("mode", 0);
                    if (mode == 0) {
                        ((Activity) mContext).getWindow().setSoftInputMode(WindowManager.LayoutParams
                                .SOFT_INPUT_ADJUST_RESIZE |
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    } else {
                        ((Activity) mContext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                    }
                } catch (JSONException e) {

                }
            }
        });
    }

    @AppCanAPI
    public String getMBaaSHost(String[] parm) {
        String mbaas_host = ResoureFinder.getInstance().getString(mContext, "mbaas_host");
        jsCallback(function_getMBaaSHost, 0, EUExCallback.F_C_TEXT, mbaas_host);
        return mbaas_host;
    }

    @AppCanAPI
    public boolean getPushState(String[] parm) {
        SharedPreferences sp = mContext.getSharedPreferences("saveData",
                Context.MODE_MULTI_PROCESS);
        String pushMes = sp.getString("pushMes", "0");
        String localPushMes = sp.getString("localPushMes", pushMes);
        jsCallback(function_getPushState, 0, EUExCallback.F_C_INT,
                Integer.parseInt(localPushMes));
        return "1".equals(localPushMes);
    }

    @AppCanAPI
    public String getPushInfo(String[] parm) {
        String type = PUSH_MSG_BODY;
        if (parm.length >= 1) {
            type = parm[0];
        }
        SharedPreferences sp = mContext.getSharedPreferences(
                PushReportConstants.PUSH_DATA_SHAREPRE, Context.MODE_PRIVATE);
        String userInfo = null;
        if (PUSH_MSG_ALL.equals(type)) {
            // 获取推送消息所有内容
            userInfo = sp.getString(
                    PushReportConstants.PUSH_DATA_SHAREPRE_MESSAGE, "");
        } else {
            userInfo = sp.getString(
                    PushReportConstants.PUSH_DATA_SHAREPRE_DATA, "");
        }
        if (!TextUtils.isEmpty(userInfo)) {
            AppCan.getInstance().getPushInfo(
                    userInfo, System.currentTimeMillis() + "");
            jsCallback(function_getPushInfo, 0, EUExCallback.F_C_TEXT, userInfo);
        }
        return userInfo;
    }

    public void share(String inShareTitle, String inSubject, String inContent) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                "txt"));
        intent.putExtra(Intent.EXTRA_SUBJECT, inSubject);
        intent.putExtra(Intent.EXTRA_TEXT, inContent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(Intent.createChooser(intent, inShareTitle));
    }

    public void moveToBack(String[] params) {
        ((Activity) mContext).moveTaskToBack(true);
    }

    public void openFile(String path) {
        if (null == path || path.trim().length() == 0) {
            return;
        }
        Intent in = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap type = MimeTypeMap.getSingleton();
        String mime = MimeTypeMap.getFileExtensionFromUrl(path);
        mime = type.getMimeTypeFromExtension(mime);
        if (null != mime && mime.length() != 0) {
            File file = new File(path);
            Uri ri = Uri.fromFile(file);
            in.setDataAndType(ri, mime);
        }
        if (appExist(in)) {
            mContext.startActivity(Intent.createChooser(in, "choose one:"));
        } else {
            ;
        }
    }

    private boolean appExist(Intent intent) {
        List<ResolveInfo> list = mContext.getPackageManager()
                .queryIntentActivities(intent, 0);
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    @AppCanAPI
    public boolean isAppInstalled(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        String json = params[0];
        AppInstalledVO data = DataHelper.gson.fromJson(json, AppInstalledVO.class);
        String packageName = data.getAppData();
        if (TextUtils.isEmpty(packageName)) {
            errorCallback(0, 0, "error params!");
            return false;
        }
        JSONObject jsonObject = new JSONObject();
        int result;
        try {
            mContext.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES);
            result = EUExCallback.F_C_SUCCESS;
        } catch (NameNotFoundException e) {
            result = EUExCallback.F_C_FAILED;
        }
        try {
            jsonObject.put(JsConst.INSTALLED, result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callBackPluginJs(JsConst.CALLBACK_IS_APP_INSTALLED, jsonObject.toString());
        return result==0;
    }

    public void reloadWidgetByAppId(String[] params){
        if (params.length < 1) {
            return;
        }
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_RELOAD_WIDGET_BY_APPID;
        msg.obj = this;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void reloadWidgetByAppIdMsg(String[] params) {
        if (params == null || params.length < 1){
            errorCallback(0, 0 , "error params!");
            return;
        }
        String appId = params[0];
        if (TextUtils.isEmpty(appId)) {
            Log.e("reloadWidgetByAppId", "appId is empty!!!");
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        EBrowserWidget widget = curWind.getWGT(appId);
        if (null == widget) {
            return;
        }
        widget.reloadWidget();
    }
    private void callBackPluginJs(String methodName, String jsonData) {
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

    @Override
    public boolean clean() {

        return true;
    }

    @Override
    public void onHandleMessage(Message message) {
        if (message == null) {
            return;
        }
        Bundle bundle = message.getData();
        switch (message.what) {
            case MSG_RELOAD_WIDGET_BY_APPID:
                reloadWidgetByAppIdMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            default:
                super.onHandleMessage(message);
        }
    }

    class CheckUpdateThread extends Thread {

        public CheckUpdateThread() {
            setName("Appcan-uexWidgetCheckUpdate");
        }

        public void shutDown() {

        }

        @Override
        public void run() {
            JSONObject obj = new JSONObject();
            try {
                WWidgetData widgetData = mBrwView.getCurrentWidget();
                if (widgetData != null) {
                    // if (widgetData.m_appId != null
                    // && widgetData.m_appId.length() > 0) {
                    WDataManager dataManager = new WDataManager(mContext);
                    ReData reData = dataManager.ChekeUpdate(mContext,
                            widgetData.m_updateurl, widgetData.m_appId,
                            widgetData.m_ver);
                    if (reData == null) {
                        obj.put(EUExCallback.F_JK_RESULT,
                                EUExCallback.F_JV_ERROR);
                        jsCallback(function_checkUpdate, 0,
                                EUExCallback.F_C_JSON, obj.toString());
                        return;
                    } else if (!TextUtils.isEmpty(reData.fileUrl)) {
                        obj.put(EUExCallback.F_JK_RESULT,
                                EUExCallback.F_JV_UPDATE);
                        obj.put(EUExCallback.F_JK_NAME, reData.fileName);
                        obj.put(EUExCallback.F_JK_SIZE, reData.fileSize);
                        obj.put(EUExCallback.F_JK_URL, reData.fileUrl);
                        obj.put(EUExCallback.F_JK_VERSION, reData.version);
                    } else {
                        obj.put(EUExCallback.F_JK_RESULT,
                                EUExCallback.F_JV_NO_UPDATE);
                    }
                    jsCallback(function_checkUpdate, 0, EUExCallback.F_C_JSON,
                            obj.toString());

                    // } else {
                    // obj.put(EUExCallback.F_JK_RESULT,
                    // EUExCallback.F_JV_NO_REGIST);
                    // jsCallback(function_checkUpdate, 0,
                    // EUExCallback.F_C_JSON, obj.toString());
                    // }
                    return;
                }
                obj.put(EUExCallback.F_JK_RESULT, EUExCallback.F_JV_ERROR);
                jsCallback(function_checkUpdate, 0, EUExCallback.F_C_JSON,
                        obj.toString());
            } catch (Exception e) {
                try {
                    obj.put(EUExCallback.F_JK_RESULT, EUExCallback.F_JV_ERROR);
                } catch (JSONException e1) {
                }
                jsCallback(function_checkUpdate, 0, EUExCallback.F_C_JSON,
                        obj.toString());
            }
        }
    }

    public interface SpaceClickListener {
        public void onSpaceClick();
    }

}
