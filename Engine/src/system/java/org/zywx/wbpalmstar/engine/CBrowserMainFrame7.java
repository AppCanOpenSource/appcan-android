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


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;

import static org.zywx.wbpalmstar.engine.EBrowserActivity.FILECHOOSER_RESULTCODE;
import static org.zywx.wbpalmstar.engine.EBrowserActivity.REQUEST_SELECT_FILE;

public class CBrowserMainFrame7 extends CBrowserMainFrame {

    final long MAX_QUOTA = 104857600L;

    private static final String TAG = "CBrowserMainFrame7";

    private AlertDialog mGeoPromptAlertDialog;
    private AlertDialog mResourcesPromptAlertDialog;

    /**
     * android version < 2.1 use
     *
     * @param context
     */
    public CBrowserMainFrame7(Context context) {
        super(context);
    }

//	private ValueCallback<Uri> mFile;

    public void onHideCustomView() {
        ((EBrowserActivity) mContext).hideCustomView();
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        String packg = mContext.getPackageName();
        Resources res = mContext.getResources();
        int id = res.getIdentifier("plugin_file_video", "drawable", packg);
        Bitmap map = BitmapFactory.decodeResource(res, id);
        return map;
    }

    @Override
    public View getVideoLoadingProgressView() {
        EBrowserToast progress = new EBrowserToast(mContext);
        progress.setMsg(mContext.getString(EUExUtil.getResStringID("platform_myspace_loading")));
        progress.setInLargeModul();
        progress.showProgress();
        return progress;
    }

    public void onShowCustomView(View view, int requestedOrientation, final CustomViewCallback callback) {
        FrameLayout container = new FrameLayout(mContext);
        container.setBackgroundColor(0xff000000);
        FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        container.setLayoutParams(parm);
        container.setClickable(true);
        container.addView(view);
        //	((EBrowserActivity)mContext).requestWindowFeature()
        WebViewSdkCompat.CustomViewCallback compatCallback=new WebViewSdkCompat.CustomViewCallback() {
            @Override
            public void onCustomViewHidden() {
                callback.onCustomViewHidden();
            }
        };
        ((EBrowserActivity) mContext).showCustomView(container, compatCallback);

    }

    @Override
    public void onShowCustomView(View view, final CustomViewCallback callback) {
        FullscreenHolder container = new FullscreenHolder(mContext);
        FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        container.setLayoutParams(parm);
        container.addView(view);
        WebViewSdkCompat.CustomViewCallback compatCallback=new WebViewSdkCompat.CustomViewCallback() {
            @Override
            public void onCustomViewHidden() {
                callback.onCustomViewHidden();
            }
        };
        ((EBrowserActivity) mContext).showCustomView(container, compatCallback);

    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota,
                                        QuotaUpdater quotaUpdater) {
        if (estimatedSize < MAX_QUOTA) {
            long newQuota = estimatedSize;
            quotaUpdater.updateQuota(newQuota * 2);
        } else {
            quotaUpdater.updateQuota(currentQuota);
        }
    }

    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(0xFF000000);
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }

    }

    // For Android 3.0-
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, null, null);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, acceptType, null);
    }

    // For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        ((EBrowserActivity) mContext).setmUploadMessage(getCompatCallback(uploadMsg));
        // 前往选择文件
        String title = EUExUtil.getString("ac_engine_webview_file_chooser_title");
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if (!TextUtils.isEmpty(acceptType)){
            i.setType(acceptType);
        }else{
            i.setType("*/*");
        }
        try {
            ((EBrowserActivity)mContext).startActivityForResult(Intent.createChooser(i, title), FILECHOOSER_RESULTCODE);
        } catch (Exception e) {
            BDebug.w(TAG, "openFileChooser exception", e);
            uploadMsg.onReceiveValue(null);
        }
    }

    /**
     * API21以上选择文件走这里
     *
     * @param webView webview实例
     * @param filePathCallback 选择回调
     * @param fileChooserParams 选择文件的类型
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        ValueCallback<Uri[]> uploadMessage = ((EBrowserActivity) mContext).getUploadMessage();
        if (uploadMessage != null) {
            uploadMessage.onReceiveValue(null);
            uploadMessage = null;
        }
        ((EBrowserActivity) mContext).setUploadMessage(filePathCallback);
        // TODO 此处还应当进一步处理fileChooserParams的多种情况，目前暂未实现。仅实现了单选文件。
        // 前往选择文件
        String title = EUExUtil.getString("ac_engine_webview_file_chooser_title");
        try {
            Intent i = fileChooserParams.createIntent();
            ((EBrowserActivity)mContext).startActivityForResult(Intent.createChooser(i, title), REQUEST_SELECT_FILE);
        } catch (Exception e) {
            BDebug.w(TAG, "onShowFileChooser exception", e);
            filePathCallback.onReceiveValue(null);
        }
        return true;
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        // 这里需要对应的隐藏交互对话框。
        // 因为页面跳转等原因取消了请求时，会进行此回调
        if (mGeoPromptAlertDialog != null && mGeoPromptAlertDialog.isShowing()){
            mGeoPromptAlertDialog.dismiss();
            mGeoPromptAlertDialog = null;
        }
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 是否允许获取您的位置信息?
        builder.setMessage(EUExUtil.getString("ac_engine_webview_prompt_to_request_location_permission"));
        DialogInterface.OnClickListener dialogButtonOnClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int clickedButton) {
                if (DialogInterface.BUTTON_POSITIVE == clickedButton) {
                    callback.invoke(origin, true, true);
                } else if (DialogInterface.BUTTON_NEGATIVE == clickedButton) {
                    callback.invoke(origin, false, false);
                }
            }
        };
        builder.setPositiveButton(EUExUtil.getString("ac_engine_webview_allow"), dialogButtonOnClickListener);
        builder.setNegativeButton(EUExUtil.getString("ac_engine_webview_deny"), dialogButtonOnClickListener);
        mGeoPromptAlertDialog = builder.create();
        mGeoPromptAlertDialog.show();
    }

    /**
     * 权限申请的可读文字转换
     *
     * @param permissionRes WebView返回的权限Resource
     */
    private String parsePermissionName(String permissionRes){
        if (permissionRes == null){
            return null;
        }else if (permissionRes.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)){
            return EUExUtil.getString("ac_engine_webview_prompt_to_request_video");
        }else if (permissionRes.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)){
            return EUExUtil.getString("ac_engine_webview_prompt_to_request_audio");
        }else{
            String permissionResStr = EUExUtil.getString("ac_engine_webview_prompt_to_request_unknown") + permissionRes;
            return permissionResStr;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        // 是否授权本页面访问以下资源？
        StringBuilder messageStrb = new StringBuilder();
        messageStrb.append(EUExUtil.getString("ac_engine_webview_prompt_to_request_resource_permission"));
        String[] resources = request.getResources();
        if (resources != null){
            for (String res : resources){
                String resStr = parsePermissionName(res);
                messageStrb.append("\r\n");
                messageStrb.append(resStr);
            }
        }
        builder.setMessage(messageStrb.toString());
        DialogInterface.OnClickListener dialogButtonOnClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int clickedButton) {
                if (DialogInterface.BUTTON_POSITIVE == clickedButton) {
                    request.grant(request.getResources());
                } else if (DialogInterface.BUTTON_NEGATIVE == clickedButton) {
                    request.deny();
                }
            }
        };
        builder.setPositiveButton(EUExUtil.getString("ac_engine_webview_allow"), dialogButtonOnClickListener);
        builder.setNegativeButton(EUExUtil.getString("ac_engine_webview_deny"), dialogButtonOnClickListener);
        mResourcesPromptAlertDialog = builder.create();
        mResourcesPromptAlertDialog.show();
    }

    @Override
    public void onPermissionRequestCanceled(PermissionRequest request) {
        // 因为页面跳转等原因取消了请求时，会进行此回调
        if (mResourcesPromptAlertDialog != null && mResourcesPromptAlertDialog.isShowing()){
            mResourcesPromptAlertDialog.dismiss();
            mResourcesPromptAlertDialog = null;
        }
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (WDataManager.sRootWgt!=null&&WDataManager.sRootWgt.m_appdebug==1 && !TextUtils.isEmpty(WDataManager.sRootWgt.m_logServerIp)) {
            if (consoleMessage.messageLevel() != ConsoleMessage.MessageLevel.WARNING) {//过滤掉warning
                BDebug.sendUDPLog(formatConsole(consoleMessage));
            }
        }
        return super.onConsoleMessage(consoleMessage);
    }

    private static String formatConsole(ConsoleMessage consoleMessage){
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("[ ")
                .append(simpleSourceInfo(consoleMessage.sourceId()))
                .append(" line : ")
                .append(consoleMessage.lineNumber())
                .append(" ")
                .append(consoleMessage.messageLevel().toString().toLowerCase())
                .append(" ]\n")
                .append(consoleMessage.message())
                .append("\n");
        return stringBuilder.toString();
    }


    private static String simpleSourceInfo(String source){
        if (TextUtils.isEmpty(source)){
            return "";
        }
        if (source.contains("/")){
            return source.substring(source.lastIndexOf("/")+1);
        }
        return source;
    }
}
