package org.zywx.wbpalmstar.engine.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.webkit.ValueCallback;
import android.widget.Toast;

import org.xwalk.core.XWalkView;
import org.zywx.wbpalmstar.acedes.ACEDESBrowserWindow7;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.EBrowserHistory;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.EBrowserWindow;
import org.zywx.wbpalmstar.engine.ECallback;
import org.zywx.wbpalmstar.engine.EDownloadDialog;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CBrowserWindow extends ACEDESBrowserWindow7 {

	protected String mParms;
	
	public CBrowserWindow(XWalkView view) {
		super(view);
	}
	
	@Override
	public void onLoadStarted(XWalkView view, String url) {
		BDebug.i(url);
		super.onLoadStarted(view, url);
	}

	@Override
	public void onProgressChanged(XWalkView view, int progressInPercent) {
		if (view != null && view instanceof EBrowserView) {
			EBrowserView target = (EBrowserView)view;
			EBrowserWindow bWindow = target.getBrowserWindow();
			if (bWindow != null) {
				bWindow.setGlobalProgress(progressInPercent);
				if (100 == progressInPercent) {
					bWindow.hiddenProgress();
				}
			}
		}else{
			if (view!=null) {
				BDebug.i("CBrowserWindow onProgressChanged: view is not instanceof EBrowserView,type is",
						view.getClass().getName());
			}
		}
	}
	
	@Override
	public void onReceivedLoadError(XWalkView view, int errorCode,
			String description, String failingUrl) {
		super.onReceivedLoadError(view, errorCode, description, failingUrl);
		BDebug.e("error  " + description);
		super.onReceivedLoadError(view, errorCode, description,
				failingUrl);
		
		EBrowserView errorView = (EBrowserView) view;
		errorView.receivedError(errorCode, description, failingUrl);
		WWidgetData wgt = errorView.getCurrentWidget();
		printError(errorCode, description, failingUrl, wgt);
	}
	
	@Override
	public void onReceivedSslError(XWalkView view,
			ValueCallback<Boolean> callback, SslError error) {
		super.onReceivedSslError(view, callback, error);
	}
	
	@Override
	public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
        super.shouldOverrideUrlLoading(view, url);
		BDebug.i(url);
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
		boolean isUrl = url.startsWith("file") || url.startsWith("http")|| url.startsWith("content://");
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
		if (view instanceof EBrowserView) {
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
				if (url.startsWith("file")) {
					int index = url.indexOf("?");
					if (index > 0) {
						mParms = url.substring(index + 1);
						url = url.substring(0, index);
					}
				}
			}
//			String cUrl = view.getOriginalUrl();
//			if (null != cUrl && url.startsWith("http") && sdkVersion >= 8) {
//				Map<String, String> headers = new HashMap<String, String>();
//				headers.put("Referer", cUrl);
//				target.loadUrl(url, headers);
//			} else {
//				target.loadUrl(url);
//			}
			return false;
		}else{
			BDebug.i("XWalkView is not instance of EBrowserView,class is",view.getClass().getName());
			return false;
		}
		
	}
	
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
					Toast.makeText(context, "未找到可执行的应用", Toast.LENGTH_SHORT).show();
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
