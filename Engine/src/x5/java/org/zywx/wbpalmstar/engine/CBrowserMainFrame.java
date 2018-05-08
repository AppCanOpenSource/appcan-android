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


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;

import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.engine.universalex.EUExManager;
import org.zywx.wbpalmstar.engine.universalex.EUExScript;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

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
            EBrowserView browserView = (EBrowserView) view;
            final EUExManager uexManager = browserView.getEUExManager();
            if (uexManager != null) {
                result.confirm(uexManager.dispatch(parseStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // For Android 3.0-
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        ((EBrowserActivity) mContext).setmUploadMessage(getCompatCallback(uploadMsg));
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        ((EBrowserActivity) mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        ((EBrowserActivity) mContext).setmUploadMessage(getCompatCallback(uploadMsg));
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        ((EBrowserActivity) mContext).startActivityForResult(Intent.createChooser(i, "File Browser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

    // For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        ((EBrowserActivity) mContext).setmUploadMessage(getCompatCallback(uploadMsg));
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        ((EBrowserActivity) mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

    public WebViewSdkCompat.ValueCallback<Uri> getCompatCallback(final ValueCallback<Uri> uploadMsg){
        return new WebViewSdkCompat.ValueCallback<Uri>() {
            @Override
            public void onReceiveValue(Uri uri) {
                uploadMsg.onReceiveValue(uri);
            }
        };
    }

}
