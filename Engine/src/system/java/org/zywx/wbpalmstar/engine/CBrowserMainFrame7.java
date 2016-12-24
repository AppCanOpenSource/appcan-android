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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebStorage.QuotaUpdater;
import android.widget.FrameLayout;

import org.zywx.wbpalmstar.base.BConstant;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

public class CBrowserMainFrame7 extends CBrowserMainFrame {

    final long MAX_QUOTA = 104857600L;

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

//	public void openFileChooser(ValueCallback<Uri> uploadFile) {
//		if(null != mFile){
//			return;
//		}
//		mFile = uploadFile;
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("*/*");
//        ((Activity)m_eContext).startActivityForResult(Intent.createChooser(intent, ""), EBrowser.F_ACT_REQ_CODE_UEX_NATIVE_FILE_EXPLORER);
//	}
//	
//	public void openFileCallBack(Uri uri){
//		mFile.onReceiveValue(uri);
//		mFile = null;
//	}

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt();
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("是否允许获取您的位置信息?");
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
        builder.setPositiveButton("允许", dialogButtonOnClickListener);
        builder.setNegativeButton("拒绝", dialogButtonOnClickListener);
        builder.show();
        super.onGeolocationPermissionsShowPrompt(origin, callback);
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
