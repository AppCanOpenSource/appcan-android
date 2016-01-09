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


import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.callback.EUExDispatcherCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExDispatcher;
import org.zywx.wbpalmstar.engine.universalex.EUExManager;
import org.zywx.wbpalmstar.engine.universalex.EUExScript;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.engine.universalex.ThirdPluginObject;

public class CBrowserMainFrame extends WebChromeClient {

    public Context mContext;

    /**
     * android version < 2.1 use
     */
    public CBrowserMainFrame(Context context) {
        this.mContext = context;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (view != null) {
            EBrowserView target = (EBrowserView) view;
            EBrowserWindow bWindow = target.getBrowserWindow();
            if (bWindow != null) {
                bWindow.setGlobalProgress(newProgress);
                if (100 == newProgress) {
                    bWindow.hiddenProgress();
                }
            }
        }
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        if (!((EBrowserActivity) view.getContext()).isVisable()) {
            result.confirm();
        }
        AlertDialog.Builder dia = new AlertDialog.Builder(view.getContext());
        dia.setTitle(EUExUtil.getResStringID("prompt"));
        dia.setMessage(message);
        dia.setCancelable(false);
        dia.setPositiveButton(EUExUtil.getResStringID("confirm"), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                result.confirm();
            }
        });
        dia.create();
        dia.show();
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        if (!((EBrowserActivity) view.getContext()).isVisable()) {
            result.cancel();
            return true;
        }
        AlertDialog.Builder dia = new AlertDialog.Builder(view.getContext());
        dia.setMessage(message);
        dia.setTitle(EUExUtil.getResStringID("prompt"));
        dia.setCancelable(false);
        dia.setPositiveButton(EUExUtil.getResStringID("confirm"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
        dia.setNegativeButton(EUExUtil.getResStringID("cancel"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
        dia.create();
        dia.show();
        return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
        if (message != null
                && message.startsWith(EUExScript.JS_APPCAN_ONJSPARSE)) {
            appCanJsParse(result, view,
                    message.substring(EUExScript.JS_APPCAN_ONJSPARSE.length()));
            result.cancel();
        } else {
            if (!((EBrowserActivity) view.getContext()).isVisable()) {
                result.cancel();
                return true;
            }
            AlertDialog.Builder dia = new AlertDialog.Builder(view.getContext());
            dia.setTitle(null);
            dia.setMessage(message);
            final EditText input = new EditText(view.getContext());
            if (defaultValue != null) {
                input.setText(defaultValue);
            }
            input.setSelectAllOnFocus(true);
            dia.setView(input);
            dia.setCancelable(false);
            dia.setPositiveButton(EUExUtil.getResStringID("confirm"),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm(input.getText().toString());
                        }
                    });
            dia.setNegativeButton(EUExUtil.getResStringID("cancel"),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    });
            dia.create();
            dia.show();
        }
        return true;
    }

    private void appCanJsParse(final JsPromptResult result, WebView view, String parseStr) {
        try {
            if (!(view instanceof EBrowserView)) {
                return;
            }
            JSONObject json = new JSONObject(parseStr);
            String uexName = json.optString("uexName");
            String method = json.optString("method");
            JSONArray jsonArray = json.getJSONArray("args");
            JSONArray typesArray = json.getJSONArray("types");
            int length = jsonArray.length();
            String[] args = new String[length];
            for (int i = 0; i < length; i++) {
                String type = typesArray.getString(i);
                String arg = jsonArray.getString(i);
                if ("undefined".equals(type) && "null".equals(arg)) {
                    args[i] = null;
                } else {
                    args[i] = arg;
                }
            }
            EBrowserView browserView = (EBrowserView) view;
            final EUExManager uexManager = browserView.getEUExManager();
            if (uexManager != null) {
                EUExDispatcher uexDispatcher = new EUExDispatcher(
                        new EUExDispatcherCallback() {
                            @Override
                            public Object onDispatch(String pluginName,
                                    String methodName, String[] params) {
                                ELinkedList<EUExBase> plugins = uexManager
                                        .getThirdPlugins();
                                for (EUExBase plugin : plugins) {
                                    if (plugin.getUexName().equals(pluginName)) {
                                        Object object = uexManager.callMethod(plugin,
                                                methodName, params);
                                        if (null != object) {
                                            result.confirm(object.toString());
                                        }
                                        return object;
                                    }
                                }
                                // 调用单实例插件
                                Map<String, ThirdPluginObject> thirdPlugins = uexManager
                                        .getPlugins();
                                ThirdPluginObject thirdPluginObject = thirdPlugins
                                        .get(pluginName);
                                if (thirdPluginObject != null
                                        && thirdPluginObject.isGlobal
                                        && thirdPluginObject.pluginObj != null) {
                                    Object object = uexManager.callMethod(
                                            thirdPluginObject.pluginObj,
                                            methodName, params);
                                    if (null != object) {
                                        result.confirm(object.toString());
                                    }
                                    return object;
                                }
                                BDebug.e("plugin", pluginName, "not exist...");
                                return null;
                            }
                        });
                uexDispatcher.dispatch(uexName, method, args);
                BDebug.i("appCanJsParse", "dispatch parseStr " + parseStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // For Android 3.0-
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        ((EBrowserActivity) mContext).setmUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        ((EBrowserActivity) mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        ((EBrowserActivity) mContext).setmUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        ((EBrowserActivity) mContext).startActivityForResult(Intent.createChooser(i, "File Browser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

    // For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        ((EBrowserActivity) mContext).setmUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        ((EBrowserActivity) mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

}
