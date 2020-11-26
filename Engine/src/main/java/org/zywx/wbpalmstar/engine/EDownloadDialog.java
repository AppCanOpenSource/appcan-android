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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.BConstant;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

public class EDownloadDialog extends ProgressDialog implements Runnable {

    private static final String TAG = "EDownloadDialog";
    public final static String STREAM_MIME_TYPE = "application/octet-stream";

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
                mConnection.setRequestProperty("Cookie", cookie);
            }
            mConnection.setUseCaches(false);
            mConnection.setRequestProperty("Connection", "Keep-Alive");
            mConnection.setRequestProperty("Charset", "UTF-8");
            mConnection.setRequestProperty("User-Agent", BConstant.USERAGENT_NEW);
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
        // 下载文件的位置
        File tm = Environment.getExternalStorageDirectory();
        File target = new File(tm.getAbsoluteFile() + "/Download/");
        if (!target.exists()) {
            target.mkdirs();
        }
        // 开始处理下载文件的文件名
        String extension = null;
        if (!TextUtils.isEmpty(contentDisposition)){
            // 如果返回了建议文件名，则直接使用
            contentDisposition = URLDecoder.decode(contentDisposition, "UTF-8");
            String fileName = null;
            // 由于application/octet-stream类型的情况下，WebView内部会识别为bin后缀，但实际上contentDisposition字段中有可能已经包含了正确的文件名和文件后缀。为了避免错误，故排除这种类型的mimetype情况。guessFileName传入null的mimetype就会把contentDisposition中的文件名作为文件名。但如果此文件名中没有后缀，则该文件不会附带后缀。 by yipeng
            if (STREAM_MIME_TYPE.equals(mimetype)) {
                fileName = URLUtil.guessFileName(url, contentDisposition,
                        null);
            }else{
                fileName = URLUtil.guessFileName(url, contentDisposition,
                        mimetype);
            }
            // 如果contentDisposition建议的文件名中携带了/字符，需要将其转义，否则会导致File对象对路径判断错误的问题。此处不允许通过/来远程创建本地目录，既不安全，也容易导致bug。 by yipeng
            if (!TextUtils.isEmpty(fileName)) {
                fileName.replaceAll("/", "%2F");
                mTmpFile = new File(target, fileName);
            }
        }else{
            // 没有返回建议文件名时，则随机生成文件名，以及推测可能的后缀
            if (mimetype != null) {
                MimeTypeMap mtm = MimeTypeMap.getSingleton();
                extension = mtm.getExtensionFromMimeType(mimetype);
            }
            // 有后缀则拼接后缀，无法推测出后缀则生成无后缀文件名
            if (!TextUtils.isEmpty(extension)){
                mTmpFile = new File(target, "ACEDownloadFile-" + UUID.randomUUID() + "." + extension);
            }else{
                mTmpFile = new File(target, "ACEDownloadFile-" + UUID.randomUUID());
            }
        }
        // 写入文件流
        OutputStream outStream = new FileOutputStream(mTmpFile);
        byte[] buffer = new byte[1024 * 3];
        while (true) {
            int numRead = mInStream.read(buffer);
            if (numRead == -1) {
                mProgressHandler.sendEmptyMessage(100);
                break;
            }
            outStream.write(buffer, 0, numRead);
            downLoaderSise += numRead;
            int p = (int) (((float) downLoaderSise / contentLength) * 100);
            mProgressHandler.sendEmptyMessage(p);
        }
        // 写入完成
        if (contentLength <= 0) {
            mProgressHandler.sendEmptyMessage(100);
        }
    }

    /**
     * 文件下载完成后打开文件的相关操作处理
     */
    private void downloadDone() {
        stopDownload();
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        String mTmpFileAbsolutePath = mTmpFile.getAbsolutePath();
        BDebug.i(TAG, "downloadDone: " + mTmpFileAbsolutePath);
        Uri pathUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pathUri = BUtility.getUriForFileWithFileProvider(getContext(), mTmpFileAbsolutePath);
        } else {
            pathUri = Uri.fromFile(mTmpFile);
        }
        BDebug.i(TAG, "downloadDone prepareOpenUri: " + pathUri);
        String suffix = makeFileSuffix(mTmpFileAbsolutePath).toLowerCase(Locale.US);
        mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        installIntent.setDataAndType(pathUri, mimetype);
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

        return WebViewSdkCompat.getCookie(inUrl);
    }
}
