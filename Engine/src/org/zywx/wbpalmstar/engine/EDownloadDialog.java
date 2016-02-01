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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.cookie.SM;
import org.apache.http.protocol.HTTP;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

public class EDownloadDialog extends ProgressDialog implements Runnable {


    public URL mClient;
    public HttpURLConnection mConnection;
    public InputStream mInStream;
    private String url;
    public String userAgent;
    public String contentDisposition;
    public String mimetype;
    public long contentLength;
    public File mTmpFile;
    public boolean mFromStop;
    public ECallback mCallBack;

    public EDownloadDialog(Context context, String url) {
        super(context);
        init(url);
    }

    public EDownloadDialog(Context context, int theme, String url) {
        super(context, theme);
        init(url);
    }

    public void setDoneCallback(ECallback callback) {

        mCallBack = callback;
    }

    private void init(String inUrl) {
        url = inUrl;
        String suffix = makeFileSuffix(url);
        MimeTypeMap mtm = MimeTypeMap.getSingleton();
        mimetype = mtm.getMimeTypeFromExtension(suffix);
        if (null == mimetype) {

        }
        setProgress(0);
        setIcon(EResources.icon);
        setCancelable(false);
        setTitle(EUExUtil.getString("platform_downloading_file"));
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        setMax(100);
        setButton(EUExUtil.getString("cancel"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mFromStop = true;
                stopDownload();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        new Thread(this).start();
    }

    private void stopDownload() {
        dismiss();
        try {
            if (null != mInStream) {
                mInStream.close();
            }
            if (null != mConnection) {
                mConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != mCallBack) {
            mCallBack.callback(null);
        }
    }

    private Handler mProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setProgress(msg.what);
            if (100 == msg.what) {
                sendEmptyMessageDelayed(-1, 1000);
            } else if (-1 == msg.what) {
                downloadDone();
            } else if (-2 == msg.what) {
                Toast.makeText(getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
            } else if (-3 == msg.what) {
                stopDownload();
            }
        }
    };

    @Override
    public void run() {
        try {
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                mProgressHandler.sendMessage(mProgressHandler.obtainMessage(-2, EUExUtil.getString
                        ("error_sdcard_is_not_available")));
                mProgressHandler.sendEmptyMessage(-3);
                return;
            }
            mClient = new URL(url);
            mConnection = (HttpURLConnection) mClient.openConnection();
            mConnection.setRequestMethod("GET");
            mConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            String cookie = getCookie(url);
            if (null != cookie) {
                mConnection.setRequestProperty(SM.COOKIE, cookie);
            }
            mConnection.setUseCaches(false);
            mConnection.setRequestProperty("Connection", "Keep-Alive");
            mConnection.setRequestProperty("Charset", HTTP.UTF_8);
            mConnection.setRequestProperty("User-Agent", EBrowserSetting.USERAGENT_NEW);
            mConnection.setReadTimeout(1000 * 30);
            mConnection.setConnectTimeout(1000 * 30);
            mConnection.setInstanceFollowRedirects(false);
            mConnection.connect();
            int responseCode = mConnection.getResponseCode();
            if (200 == responseCode) {
                saveToFile();
            } else {
                mProgressHandler.sendMessage(mProgressHandler.obtainMessage(-2, EUExUtil.getString("platform_connect_failed")));
                mProgressHandler.sendEmptyMessage(-3);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!mFromStop) {
                mProgressHandler.sendMessage(mProgressHandler.obtainMessage(-2, EUExUtil.getString("platform_download_failed")));
            }
            mProgressHandler.sendEmptyMessage(-3);
        }
    }

    private int downLoaderSise = 0;

    private void saveToFile() throws Exception {
        mInStream = mConnection.getInputStream();
        if (mInStream == null) {
            return;
        }
        String encoding = mConnection.getContentEncoding();
        if ("gzip".equalsIgnoreCase(encoding)) {
            mInStream = new GZIPInputStream(mInStream);
        } else if ("deflate".equalsIgnoreCase(encoding)) {
            mInStream = new DeflaterInputStream(mInStream);
        }
        if (contentLength <= 0) {
            String cLength = mConnection.getHeaderField("Content-Length");
            if (cLength != null) {
                contentLength = Long.parseLong(cLength);
            }
        }
        File tm = Environment.getExternalStorageDirectory();
        File target = new File(tm.getAbsoluteFile() + "/Download/");
        if (!target.exists()) {
            target.mkdirs();
        }
        String extension = null;
        if (mimetype != null) {
            MimeTypeMap mtm = MimeTypeMap.getSingleton();
            extension = mtm.getExtensionFromMimeType(mimetype);
        }
        if (extension == null) {
            String fileName = URLUtil.guessFileName(url, contentDisposition,
                    mimetype);
            if (!TextUtils.isEmpty(fileName)) {
                fileName.replaceAll("/", "");
                mTmpFile = new File(target, fileName);
            }
        } else {
            mTmpFile = File.createTempFile("/Download/", "." + extension, tm);
        }

        OutputStream outStream = new FileOutputStream(mTmpFile);
        byte buffer[] = new byte[1024 * 3];
        while (true) {
            int numread = mInStream.read(buffer);
            if (numread == -1) {
                mProgressHandler.sendEmptyMessage(100);
                break;
            }
            outStream.write(buffer, 0, numread);
            downLoaderSise += numread;
            int p = (int) (((float) downLoaderSise / contentLength) * 100);
            mProgressHandler.sendEmptyMessage(p);
        }
        if (contentLength <= 0) {
            mProgressHandler.sendEmptyMessage(100);
        }
    }

    private void downloadDone() {
        stopDownload();
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        String filename = mTmpFile.getAbsolutePath();
        Uri path = Uri.parse(filename);
        if (path.getScheme() == null) {
            path = Uri.fromFile(new File(filename));
        }
        String suffix = makeFileSuffix(filename).toLowerCase(Locale.US);
        mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        installIntent.setDataAndType(path, mimetype);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(installIntent);
        } catch (Exception e) {
            e.printStackTrace();
            mProgressHandler.sendMessage(mProgressHandler.obtainMessage(-2, EUExUtil.getString("can_not_find_suitable_app_perform_this_operation")));
        }
    }

    private String makeFileSuffix(String url) {
        int index = url.lastIndexOf(".");
        if (index < 0) {

            return null;
        }
        return url.substring(index + 1);
    }

    private final String getCookie(String inUrl) {

        return CookieManager.getInstance().getCookie(inUrl);
    }
}
