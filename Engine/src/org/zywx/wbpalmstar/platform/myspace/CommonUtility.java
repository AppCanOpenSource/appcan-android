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

package org.zywx.wbpalmstar.platform.myspace;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.zip.CnZipInputStream;
import org.zywx.wbpalmstar.base.zip.ZipEntry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.FileObserver;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class CommonUtility {

    public static final String URL_RECOMMEND_APP_LIST = "http://open.appcan.cn/myspace/getAppList.action?";
    public static final String URL_GET_SESSION_KEY = "http://open.appcan.cn/oauth2/getTxSessionKey.do?";
    public static final String URL_REPORT_INSTALL_WIDGET = "http://open.appcan.cn/myspace/installWidget.action?";
    public static final String URL_REPORT_UNISTALL_WIDGET = "http://open.appcan.cn/myspace/unInstallWidget.action?";
    public static final String URL_REPORT_START_WIDGET = "http://open.appcan.cn/myspace/startWidget.action?";
    public static final String URL_GET_MYAPPS_LIST = "http://open.appcan.cn/myspace/getMyAppList.action?";
    public static final String URL_OPEN_LOGIN_URL = "http://open.appcan.cn/oauth2/getLoginList.do?";
    public static final String URL_MORE_WIDGET_URL = "http://open.appcan.cn/common/appcenter.html?";
    public static final String URL_DELAY_START_REPORT = "http://open.appcan.cn/myspace/delayStartWidget.action";
    public static final String URL_DELAY_INSTALL_REPORT = "http://open.appcan.cn/myspace/delayInstallWidget.action";
    public static final String URL_DELAY_UNISTALL_REPORT = "http://open.appcan.cn/myspace/delayUnInstallWidget.action";

    private static final String TAG = "NetUtility";
    public static final String WIDGET_SAVE_PATH = "/sdcard/widgetone/widgetapp/";

    /**
     * 需要加入的权限android.permission.ACCESS_NETWORK_STATE
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean isAvailable = false;
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                isAvailable = true;
            }
        }
        return isAvailable;
    }

    public static byte[] downloadImage(String url) {
        if (url == null || url.length() == 0) {
            return null;
        }
        byte[] data = null;
        int resCode = -1;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(httpGet);
            resCode = response.getStatusLine().getStatusCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                baos = new ByteArrayOutputStream(4096);
                is = response.getEntity().getContent();
                byte[] buffer = new byte[4096];
                int actulSize = 0;
                while ((actulSize = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, actulSize);
                }
                data = baos.toByteArray();
            }
        } catch (IOException e) {
            BDebug.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            BDebug.e(TAG, "OutOfMemoryError:" + error.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

    public static byte[] requestData(String url) {
        if (url == null || url.length() == 0) {
            return null;
        }
        byte[] data = null;
        HttpURLConnection conn = null;
        int resCode = -1;
        InputStream is = null;
        try {
            final URL netUrl = new URL(url);
            conn = (HttpURLConnection) netUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setReadTimeout(30000);
            resCode = conn.getResponseCode();
            BDebug.d(TAG, "requestData()--->rspCode:" + resCode + "  URL:" + url);
            if (resCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                data = BUtility.transStreamToBytes(is, 4096);
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            BDebug.e(TAG, "requestData()--->ResCode:" + resCode + "  URL:" + url);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return data;
    }

    public static boolean postData(String url, byte[] data) {
        if (url == null || data == null) {
            throw new IllegalArgumentException("params can't be null....");
        }
        boolean isOk = false;
        HttpURLConnection conn = null;
        OutputStream os = null;
        try {
            final URL netUrl = new URL(url);
            conn = (HttpURLConnection) netUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("content-type", "text/html");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            os = conn.getOutputStream();
            os.write(data);
            os.close();
            final int rspCode = conn.getResponseCode();
            BDebug.d(TAG, "rspCode:" + rspCode);
            if (rspCode == HttpURLConnection.HTTP_OK) {
                isOk = true;
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return isOk;
    }

    public static boolean getRequestResult(String url) {
        boolean isOk = false;
        HttpURLConnection conn = null;
        try {
            final URL netUrl = new URL(url);
            conn = (HttpURLConnection) netUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                isOk = true;
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        Log.i(TAG, "requestData()-->result:" + isOk + "   Url: " + url);
        return isOk;
    }

    public static Bitmap GetRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);

            return output;
        } catch (Exception e) {
            return null;
        }
    }

    public static int ParseInt(String str) {
        int i = -1;
        if (str == null || str.length() == 0) {
            return i;
        }

        try {
            i = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return i;
    }

    public static synchronized String unzip(InputStream inputStream, String decompression, String encoding) {
        if (encoding == null || encoding.equals(""))
            encoding = "UTF-8";
        File dir = new File(decompression);
        String installPath = null;
        try {
            // // 检查是否是ZIP文件
            // ZipFile zip = new ZipFile(infile);
            // zip.close();
            // 建立与目标文件的输入连接
            CnZipInputStream in = new CnZipInputStream(inputStream, encoding);
            ZipEntry file = in.getNextEntry();
            if (file.isDirectory()) {
                installPath = dir.getAbsolutePath() + "/" + file.getName();
            }
            byte[] c = new byte[1024];
            int slen;
            while (file != null) {
                String zename = file.getName();
                if (file.isDirectory()) {
                    File files = new File(dir.getAbsolutePath() + "/" + zename); // 在指定解压路径下建子文件夹
                    files.mkdirs();// 新建文件夹
                } else {
                    File files = new File(dir.getAbsolutePath() + "/" + zename).getParentFile();// 当前文件所在目录
                    if (!files.exists()) {// 如果目录文件夹不存在，则创建
                        files.mkdirs();
                    }
                    FileOutputStream out = new FileOutputStream(dir.getAbsolutePath() + "/" + zename);
                    while ((slen = in.read(c, 0, c.length)) != -1)
                        out.write(c, 0, slen);
                    out.close();
                }
                file = in.getNextEntry();
            }
            in.close();
        } catch (ZipException zipe) {
            return null;
        } catch (IOException ioe) {
            return null;
        } catch (Exception i) {
            Log.i("debug", "EUExZipMgr ========= over");
            return null;
        } finally {

        }
        return installPath;
    }

    public static String saveImgFile(Bitmap bitmap) {
        String savePath = null;
        File file = null;
        FileOutputStream fos = null;
        try {
            File Folder = new File("/widgetone/tmp/");
            if (!Folder.exists()) {
                Folder.mkdirs();
            }
            file = new File(Folder, System.currentTimeMillis() + ".cache");
            file.createNewFile();
            fos = new FileOutputStream(file);
            if (bitmap.compress(CompressFormat.PNG, 100, fos)) {
                savePath = file.getAbsolutePath();
            }
        } catch (IOException e) {
            BDebug.e(TAG, "saveImgFile() error:" + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (savePath == null && file != null) {
                file.delete();
            }
        }
        return savePath;
    }

    public static File createCacheFile(Activity activity) {
        return new File(activity.getCacheDir(), System.currentTimeMillis() + ".tmp");
    }

    public static void clearAllCacheFile(Activity activity) {
        File folder = activity.getCacheDir();
        try {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".tmp")) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            BDebug.e(TAG, "clearAllCacheFile:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class AnimationListenerAdapter implements AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }
}
