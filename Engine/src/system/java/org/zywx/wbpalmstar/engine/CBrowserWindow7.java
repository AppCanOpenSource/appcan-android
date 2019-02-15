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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.widget.Toast;

import org.zywx.wbpalmstar.acedes.ACEDESBrowserWindow7;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.universalex.EUExScript;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CBrowserWindow7 extends ACEDESBrowserWindow7 {

    protected String mReferenceUrl;
    protected String mParms;

    private boolean mIsPageOnload;

    /**
     * android version >= 2.1 use
     */
    public CBrowserWindow7() {
        mReferenceUrl = "";
    }

    @SuppressLint("NewApi")
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
        boolean isUrl = url.startsWith("file") || url.startsWith("http")
                || url.startsWith("content://");
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
        if (url.startsWith("http"))return false;
        EBrowserView target = (EBrowserView) view;
        if (target.isObfuscation()) {
            target.updateObfuscationHistroy(url,
                    EBrowserHistory.UPDATE_STEP_ADD, false);
        }
        if (target.shouldOpenInSystem()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            activity.startActivity(intent);
            return true;
        }
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 11) {
            if (url != null) {
                int index = url.indexOf("?");
                if (index > 0) {
                    mParms = url.substring(index + 1);
                    if (!url.startsWith("http")) {
                        url = url.substring(0, index);
                    }
                }
            }
        }
        String cUrl = view.getOriginalUrl();
        if (null != cUrl && url.startsWith("http") && sdkVersion >= 8) {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Referer", cUrl);
            view.loadUrl(url, headers);
        } else {
            view.loadUrl(url);
        }
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mIsPageOnload = false;
        if (view == null) {
            return;
        }
        EBrowserView target = (EBrowserView) view;
        target.loadExeJS();
        target.onPageStarted(target, url);
        if (null != mParms) {
            target.setQuery(mParms);
        }
        mParms = null;
        ESystemInfo info = ESystemInfo.getIntence();
        if (info.mFinished) {
            info.mScaled = true;
        }
        if (url != null) {
            mReferenceUrl = url;
            if (url.startsWith("http")) {
                EBrowserWindow bWindow = target.getBrowserWindow();
                if (bWindow != null && 1 == bWindow.getWidget().m_webapp) {
                    bWindow.showProgress();
                }
            }
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (view == null) {
            return;
        }
        EBrowserView target = (EBrowserView) view;
        EBrowserWindow bWindow = target.getBrowserWindow();
        if (url != null) {
            if (url.startsWith("http")) {
                if (bWindow != null && 1 == bWindow.getWidget().m_webapp) {
                    bWindow.hiddenProgress();
                }
            }
            String oUrl = view.getOriginalUrl();
            if ((!mReferenceUrl.equals(url) || target.beDestroy() || !url.equals(oUrl)) && mIsPageOnload) {
                return;
            }
        }
        mIsPageOnload = true;
        ESystemInfo info = ESystemInfo.getIntence();

        if (!target.isWebApp()) { //4.3及4.3以下手机
            int defaultFontSize = (int) (info.mDefaultFontSize / target.getScaleWrap());
            info.mScaled = true;
            target.setDefaultFontSize(defaultFontSize);
        }

        if (!info.mFinished) {
            if (WWidgetData.m_remove_loading == 1) {
                if (target.getContext()instanceof EBrowserActivity) {
                    ((EBrowserActivity) target.getContext()).setContentViewVisible(200);
                }
            }
        }

        info.mFinished = true;
        target.loadUrl(EUExScript.F_UEX_DISPATCHER_SCRIPT);
        target.loadUrl(EUExScript.F_UEX_SCRIPT);
        target.loadExeJS();
        target.onPageFinished(target, url);

        if (bWindow != null && bWindow.getWidget().m_appdebug == 1) {
            String debugUrlString = "http://"
                    + bWindow.getWidget().m_logServerIp
                    + ":30060/target/target-script-min.js#anonymous";
            String weinreString = "javascript:var x = document.createElement(\"SCRIPT\");x.setAttribute('src',\""
                    + debugUrlString + "\"" + ");document.body.appendChild(x);";
            target.loadUrl(weinreString);
        }

        CookieSyncManager.getInstance().sync();
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float
            newScale) {
        String windowName = null;
        if (view instanceof EBrowserView){
            windowName = ((EBrowserView) view).getName();
            notifyScaleChangedToJS((EBrowserView) view);
//            BDebug.i("windowName = " + windowName + " oldScale = " + oldScale + " newScale = " + newScale);
            Log.e("TAG","windowName = " + windowName + " oldScale = " + oldScale + " newScale = " + newScale);
            EBrowserView target = (EBrowserView) view;
            ESystemInfo info = ESystemInfo.getIntence();
            int defaultFontSize;
            defaultFontSize = (int) (info.mDefaultFontSize / target.getScaleWrap());
            info.mScaled = true;
            target.setDefaultFontSize(defaultFontSize);
        }
    }

    private void notifyScaleChangedToJS(EBrowserView webview){
        String js = "javascript:if(window.onresize){window.onresize()}else{console.log('AppCanEngine-->notifyScaleChangedToJS else')}";
        webview.addUriTask(js);
    }



	/*
     * This method is unstable and generally leads to error, so deprecate.
	 * 
	 * @Override public void onScaleChanged(WebView view, float oldScale, float
	 * newScale) { ESystemInfo info = ESystemInfo.getIntence(); float willScale
	 * = info.mFinished ? (newScale / oldScale) : newScale;
	 * 
	 * adptScaled(view, willScale); }
	 * 
	 * private void adptScaled(WebView view, float newScale){ ESystemInfo info =
	 * ESystemInfo.getIntence(); EBrowserView target = (EBrowserView)view;
	 * if(!info.mScaled&&!target.isWebApp() && Build.VERSION.SDK_INT<=18){ int
	 * size = (int)(info.mDefaultFontSize / newScale); info.mDefaultFontSize =
	 * size; target.setDefaultFontSize(size); info.mScaled = true; } }
	 */

    protected EDownloadDialog mDialog;

    public void onDownloadStart(Context context, String url, String userAgent,
                                String contentDisposition, String mimetype, long contentLength) {
        if (contentDisposition == null
                || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10)) {
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            String filename = url;
            Uri path = Uri.parse(filename);
            if (path.getScheme() == null) {
                path = Uri.fromFile(new File(filename));
            }
            installIntent.setDataAndType(path, mimetype);
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
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        EBrowserView errorView = (EBrowserView) view;
        errorView.receivedError(errorCode, description, failingUrl);
        WWidgetData wgt = errorView.getCurrentWidget();
        printError(errorCode, description, failingUrl, wgt);
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg,
                                   Message continueMsg) {

        continueMsg.sendToTarget();
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                   SslError error) {
        super.onReceivedSslError(view,handler,error);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view,
                                          HttpAuthHandler handler, String host, String realm) {

        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    private void printError(int errorCode, String description, String failingUrl, WWidgetData errorWgt) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String time = formatter.format(new Date());
            String fileName = time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String ePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String path = ePath + "/widgetone/log/pageloaderror/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                StringBuffer sb = new StringBuffer();
                sb.append("failingDes: " + description);
                sb.append("\n");
                sb.append("failingUrl: " + failingUrl);
                sb.append("\n");
                sb.append("errorCode: " + errorCode);
                sb.append("\n");
                if (null != errorWgt) {
                    sb.append(errorWgt.toString());
                }
                if (BDebug.DEBUG) {
                    BDebug.e(sb.toString());
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
