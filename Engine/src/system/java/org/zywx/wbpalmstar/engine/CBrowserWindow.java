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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Toast;

import org.zywx.wbpalmstar.acedes.EXWebViewClient;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.universalex.EUExScript;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.io.File;
import java.util.List;

public class CBrowserWindow extends EXWebViewClient {

    private static final String TAG = "CBrowserWindow";

    private String mReferenceUrl;

    /**
     * android version < 2.1 use
     */
    public CBrowserWindow() {
        mReferenceUrl = "";
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        super.shouldOverrideUrlLoading(view, url);
        Activity activity = (Activity) view.getContext();
        if (url.startsWith("tel:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (url.startsWith("geo:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (url.startsWith("mailto:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (url.startsWith("sms:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String address = null;
                int parmIndex = url.indexOf('?');
                if (parmIndex == -1) {
                    address = url.substring(4);
                } else {
                    address = url.substring(4, parmIndex);
                    Uri uri = Uri.parse(url);
                    String query = uri.getQuery();
                    if ((query != null) && (query.startsWith("body="))) {
                        intent.putExtra("sms_body", query.substring(5));
                    }
                }
                intent.setData(Uri.parse("sms:" + address));
                intent.putExtra("address", address);
                intent.setType("vnd.android-dir/mms-sms");
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        boolean isUrl = url.startsWith("file") || url.startsWith("http") || url.startsWith("content://");
        boolean isCustomUrl = url.startsWith("alipay://") || url.startsWith("weixin://");
        if (!isUrl) {
            if (isCustomUrl) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                activity.startActivity(intent);
                return true;
            }
            return true;
        }
        EBrowserView target = (EBrowserView) view;
        if (target.isObfuscation()) {
            target.updateObfuscationHistroy(url, EBrowserHistory.UPDATE_STEP_ADD, false);
        }
        if (target.shouldOpenInSystem() && url.startsWith("http")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            activity.startActivity(intent);
            return true;
        }
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (view == null) {
            return;
        }
        EBrowserView target = (EBrowserView) view;
        if (url != null) {
            mReferenceUrl = url;
        }
        target.onPageStarted(target, url);
        ESystemInfo info = ESystemInfo.getIntence();
        if (info.mFinished) {
            info.mScaled = true;
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (view == null) {
            return;
        }
        EBrowserView target = (EBrowserView) view;
        if (url != null) {
            String oUrl = view.getOriginalUrl();
            if (!mReferenceUrl.equals(url) || target.beDestroy() || !url.equals(oUrl)) {
                return;
            }
            if (!mReferenceUrl.equals(url) || target.beDestroy()) {
                return;
            }
        }
        ESystemInfo info = ESystemInfo.getIntence();

        if (!target.isWebApp()) {
            if (!info.mScaled) {
                info.mDefaultFontSize = (int) (info.mDefaultFontSize / target.getScaleWrap());
                info.mScaled = true;
            }
            target.setDefaultFontSize(info.mDefaultFontSize);
        }

        info.mFinished = true;
        view.loadUrl(EUExScript.F_UEX_DISPATCHER_SCRIPT);
        view.loadUrl(EUExScript.F_UEX_SCRIPT);
        target.onPageFinished(target, url);
        CookieSyncManager.getInstance().sync();
    }

    protected EDownloadDialog mDialog;

    public void onDownloadStart(Context context, String url, String userAgent,
                                String contentDisposition, String mimetype, long contentLength) {
        BDebug.i(TAG, "onDownloadStart", "\n url: " + url, "\n userAgent: " + userAgent, "\n contentDisposition: " + contentDisposition, "\n mimetype: " + mimetype, "\n contentLength: " + contentLength);
        if (contentDisposition == null
                || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10)) {
            // 下载信息返回不全，无法处理，直接发送Intent抛给系统处理。
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            String filename = url;
            Uri pathUri = Uri.parse(filename);
            if (pathUri.getScheme() == null) {
                // scheme为空，则推测可能是本地路径
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pathUri = BUtility.getUriForFileWithFileProvider(context, filename);
                } else {
                    pathUri = Uri.fromFile(new File(filename));
                }
            }
            installIntent.setDataAndType(pathUri, mimetype);
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (checkInstallApp(context, installIntent)) {
                try {
                    context.startActivity(installIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, EUExUtil.getString("can_not_find_suitable_app_perform_this_operation"), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        if (null != mDialog) {
            return;
        }
        mDialog = new EDownloadDialog(context, url);
        mDialog.userAgent = userAgent;
        mDialog.contentDisposition = contentDisposition;
        mDialog.mimetype = mimetype;
        mDialog.contentLength = contentLength;
        ECallback callback = new ECallback() {
            @Override
            public void callback(Object obj) {
                mDialog = null;
            }
        };
        mDialog.setDoneCallback(callback);
        mDialog.show();
    }

    private boolean checkInstallApp(Context context, Intent target) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(target,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (null != list && list.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {

        super.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {

        ((EBrowserView) view).receivedError(errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg,
                                   Message continueMsg) {

        continueMsg.sendToTarget();
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("SSL证书错误，是否继续？");
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

	/*
     * This method is unstable and generally leads to error, so deprecate.
	 * 
	 * @Override public void onScaleChanged(WebView view, float oldScale, float
	 * newScale) { ESystemInfo info = ESystemInfo.getIntence(); float willScale
	 * = info.mFinished ? (newScale / oldScale) : newScale; adptScaled(view,
	 * willScale); }
	 * 
	 * private void adptScaled(WebView view, float newScale){ ESystemInfo info =
	 * ESystemInfo.getIntence(); EBrowserView target = (EBrowserView)view;
	 * if(!info.mScaled&&!target.isWebApp()){ int size =
	 * (int)(info.mDefaultFontSize / newScale); info.mDefaultFontSize = size;
	 * target.setDefaultFontSize(size); info.mScaled = true; } }
	 */

}
