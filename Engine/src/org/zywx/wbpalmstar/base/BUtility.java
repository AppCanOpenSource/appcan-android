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

package org.zywx.wbpalmstar.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.encryption.PEncryption;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BUtility {
    public final static String F_SDCARD_PATH = "file:///sdcard/";
    public final static String F_RES_PATH = "file:///res/";
    public final static String F_DATA_PATH = "file:///data/";
    public final static String F_HTTP_PATH = "http://";
    public final static String F_RTSP_PATH = "rtsp://";
    public final static String F_RES_ROOT_PATH = "file:///res/widget/";
    public final static String F_ASSET_PATH = "file:///android_asset/";
    public final static String F_RAW_PATH = "raw/";
    public final static String F_FILE_SCHEMA = "file://";
    public final static String F_APP_SCHEMA = "wgt://";
    public final static String F_WIDGET_SCHEMA = "wgts://";
    public final static String F_BASE_WGT_PATH = "widgetone/";
    public final static String F_APP_PATH = "widgetone/apps/";
    public final static String F_WIDGET_PATH = "widgetone/widgets/";
    public final static String F_WIDGET_APP_PATH = "widgetone/widgetapp/";
    public final static String F_APP_VIDEO = "video/";
    public final static String F_APP_PHOTO = "photo/";
    public final static String F_APP_AUDIO = "audio/";
    public final static String F_APP_MYSPACE = "myspace/";
    public final static String F_Widget_RES_path = "widget/wgtRes/";
    public final static String F_Widget_RES_SCHEMA = "res://";
    public final static String F_SBOX_SCHEMA = "box://";

    public static boolean isDes = false;
    public static String g_desPath = "";

    // 缩放图片
    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }

    public static InputStream compress(Context m_eContext, String path,
                                       int compress, float with) throws OutOfMemoryError, IOException {
        FileDescriptor fileDescriptor = null;
        boolean isRes = false;
        if (!path.startsWith("/")) {
            AssetFileDescriptor assetFileDescriptor = m_eContext.getAssets()
                    .openFd(path);
            fileDescriptor = assetFileDescriptor.getFileDescriptor();
            isRes = true;
        } else {
            FileInputStream fis = new FileInputStream(new File(path));
            fileDescriptor = fis.getFD();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap source = BitmapFactory.decodeFileDescriptor(fileDescriptor,
                null, options);
        if (options.outHeight <= 0 || options.outWidth <= 0) {
            if (isRes) {
                return m_eContext.getAssets().open(path);
            } else {
                return new FileInputStream(new File(path));
            }

        }

        int quality = 0;
        if (compress == 1) {
            quality = 100;
        } else if (compress == 2) {
            quality = 75;
        } else if (compress == 3) {
            quality = 50;
        } else {
            quality = 25;
        }

        float max = with == -1 ? 640 : with;
        float src_w = options.outWidth;
        float src_h = options.outHeight;
        float scaleRate = 1;
        if (src_h > src_w) {
            scaleRate = src_h / max;
        } else {
            scaleRate = src_w / max;
        }
        scaleRate = scaleRate > 1 ? scaleRate : 1;

        if (scaleRate != 1) {
            Bitmap dstbmp = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            options.inSampleSize = (int) scaleRate;
            options.inJustDecodeBounds = false;
            options.inInputShareable = true;
            options.inPurgeable = true;
            options.inPreferredConfig = Config.RGB_565;// 会失真，缩略图失真没事^_^

            source = BitmapFactory.decodeFileDescriptor(fileDescriptor, null,
                    options);
            if (source != null) {
                int srcWidth = source.getWidth();
                int srcHeight = source.getHeight();
                final float sacleRate = Math.min(max / (float) srcWidth, max
                        / (float) srcHeight);
                final int destWidth = (int) (srcWidth * sacleRate);
                final int destHeight = (int) (srcHeight * sacleRate);
                dstbmp = Bitmap.createScaledBitmap(source, destWidth,
                        destHeight, false);
                if (source != null && !source.isRecycled()) {
                    source.recycle();
                }
                if (dstbmp.compress(CompressFormat.JPEG, quality, baos)) {
                    if (dstbmp != null && !dstbmp.isRecycled()) {
                        dstbmp.recycle();
                    }
                    return new ByteArrayInputStream(baos.toByteArray());
                } else {
                    baos.close();
                    if (isRes) {
                        return m_eContext.getAssets().open(path);
                    } else {
                        return new FileInputStream(new File(path));
                    }
                }
            } else {
                if (isRes) {
                    return m_eContext.getAssets().open(path);
                } else {
                    return new FileInputStream(new File(path));
                }
            }

        } else {
            if (isRes) {
                return m_eContext.getAssets().open(path);
            } else {
                return new FileInputStream(new File(path));
            }
        }

    }

    // 初始化widget的文件夹
    public static void initWidgetOneFile(Context context, String appId) {
        String root = null;
        appId += "/";
        if (sdCardIsWork()) {
            root = getSdCardRootPath();
        } else {
            root = context.getFilesDir().getAbsolutePath() + "/";
        }
        String[] fileDir = {root + F_APP_PATH + appId, root + F_WIDGET_PATH,
                root + F_APP_PATH + appId + F_APP_VIDEO,
                root + F_APP_PATH + appId + F_APP_PHOTO,
                root + F_APP_PATH + appId + F_APP_AUDIO,
                root + F_APP_PATH + appId + F_APP_MYSPACE};
        int size = fileDir.length;
        for (int i = 0; i < size; i++) {
            File file = new File(fileDir[i]);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        String noMediaStr = root + F_BASE_WGT_PATH + ".nomedia";
        File noMedia = new File(noMediaStr);
        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int parseColor(String inColor) {
        int reColor = 0;
        try {
            if (inColor != null && inColor.length() != 0) {
                inColor = inColor.replace(" ", "");
                if (inColor.charAt(0) == 'r') { //rgba
                    int start = inColor.indexOf('(') + 1;
                    int off = inColor.indexOf(')');
                    inColor = inColor.substring(start, off);
                    String[] rgba = inColor.split(",");
                    int r = Integer.parseInt(rgba[0]);
                    int g = Integer.parseInt(rgba[1]);
                    int b = Integer.parseInt(rgba[2]);
                    int a = Integer.parseInt(rgba[3]);
                    reColor = (a << 24) | (r << 16) | (g << 8) | b;
                } else if (inColor.startsWith("#")) { // #
                    String tmpColor = inColor.substring(1);
                    if (3 == tmpColor.length()) {
                        char[] t = new char[6];
                        t[0] = tmpColor.charAt(0);
                        t[1] = tmpColor.charAt(0);
                        t[2] = tmpColor.charAt(1);
                        t[3] = tmpColor.charAt(1);
                        t[4] = tmpColor.charAt(2);
                        t[5] = tmpColor.charAt(2);
                        inColor = "#" + String.valueOf(t);
                    }
                    reColor = Color.parseColor(inColor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            reColor = 0;
        }
        return reColor;
    }

    // 获得屏幕像素密度
    public static int getDeviceDesity(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.densityDpi;
    }

    // 获得屏幕分辨率
    public static int[] getDeviceResolution(Activity activity) {
        int[] args = new int[2];
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        args[0] = dm.widthPixels;
        args[1] = dm.heightPixels;
        return args;
    }

    public static byte[] transStreamToBytes(InputStream is, int buffSize) {
        if (is == null) {
            return null;
        }
        if (buffSize <= 0) {
            throw new IllegalArgumentException(
                    "buffSize can not less than zero.....");
        }
        byte[] data = null;
        byte[] buffer = new byte[buffSize];
        int actualSize = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            while ((actualSize = is.read(buffer)) != -1) {
                baos.write(buffer, 0, actualSize);
            }
            data = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    /**
     * 判断是否是手机号码
     *
     * @param phoneNum <br>
     *                 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188 <br>
     *                 联通：130、131、132、152、155、156、185、186 <br>
     *                 电信：133、153、180、189、（1349卫通）
     */
    public static boolean isPhoneNumber(String phoneNum) {
        String expression = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        CharSequence inputStr = phoneNum;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        }
        return false;

    }

    /**
     * sd 卡是否工作
     */
    public static boolean sdCardIsWork() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 判断路径是否含有协议头
     *
     * @param uri
     * @return
     */
    public static boolean uriHasSchema(String uri) {
        if (uri == null || uri.length() == 0) {
            return false;
        }
        String mUri = null;
        int i = uri.indexOf('?');
        if (i > 0) {
            mUri = uri.substring(0, i);
        } else {
            mUri = uri;
        }
        final Uri path = Uri.parse(mUri);
        if (path != null && path.getScheme() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 得到sdcard路径
     *
     * @return
     */
    public static String getSdCardRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                .replace("/mnt", "")
                + File.separator;
    }

    /**
     * 得到去掉参数的路径
     *
     * @return
     */
    public static String makeSpecUrl(String inBaseUrl) {
        Uri path = Uri.parse(inBaseUrl);
        String port = path.getPort() != -1 ? ":"
                + String.valueOf(path.getPort()) : "";
        return path.getScheme() + "://" + path.getHost() + port
                + path.getPath();
    }

    public static String makeUrl(String inBaseUrl, String inUrl) {
        if (null == inUrl || inUrl.length() == 0) {

            return null;
        }

        if (null == inBaseUrl || inBaseUrl.length() == 0) {
            return null;
        }
        // inUrl = URLDecoder.decode(inUrl);
        if (uriHasSchema(inUrl) || inUrl.startsWith("/")) {
            return inUrl;
        }

        // String oldBaseUrl = inBaseUrl;
        // ../../
        int index = inUrl.indexOf("../");
        int layer = 0;
        while (index != -1) {
            layer++;
            inUrl = inUrl.substring(index + 3, inUrl.length());
            index = inUrl.indexOf("../");

        }
        // Uri path = Uri.parse(inBaseUrl);
        // String port = path.getPort() != -1 ?
        // ":"+String.valueOf(path.getPort()) : "";
        // inBaseUrl = path.getScheme()
        // +"://"+path.getHost()+port+path.getPath();
        int count = inBaseUrl.lastIndexOf(47);
        while (layer >= 0) {
            inBaseUrl = inBaseUrl.substring(0, count);
            count = inBaseUrl.lastIndexOf(47);
            layer--;
            if (count == -1) {
                break;
            }
        }

        inBaseUrl += "/" + inUrl;

        return inBaseUrl;
    }

    /**
     * 根据file://res/协议的路径获得AssetFileDescriptor对象用于设置DataSource
     *
     * @param context
     * @return
     */
    public static AssetFileDescriptor getFileDescriptorByResPath(
            Context context, String fullResPath) {
        if (context != null
                && fullResPath.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
            final String assetFileName = F_Widget_RES_path
                    + fullResPath.substring(F_Widget_RES_SCHEMA.length());
            AssetFileDescriptor descriptor = null;
            try {
                descriptor = context.getAssets().openFd(assetFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return descriptor;
        } else {
            return null;
        }
    }

    /**
     * 根据res协议路径获得资源输入流
     *
     * @param context
     * @param fullResPath
     * @return
     */
    public static InputStream getInputStreamByResPath(Context context,
                                                      String fullResPath) {
        if (context != null
                && fullResPath.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
            final String assetFileName = F_Widget_RES_path
                    + fullResPath.substring(F_Widget_RES_SCHEMA.length());
            InputStream is = null;
            try {
                is = context.getAssets().open(assetFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return is;
        } else {
            return null;
        }
    }

    /**
     * 根据file://data/协议的路径获得其真实路径
     *
     * @param context
     * @param totalPath
     * @return
     */
    public static String getRealPathByDataPath(Context context, String totalPath) {
        if (context == null) {
            throw new NullPointerException("context can not be null.......");
        }
        if (totalPath == null || totalPath.length() == 0) {
            return null;
        }
        if (totalPath.startsWith(BUtility.F_DATA_PATH)) {
            return totalPath.replace(BUtility.F_DATA_PATH, F_FILE_SCHEMA
                    + context.getFilesDir().getAbsolutePath() + "/");
        } else {
            return null;
        }
    }

    /**
     * 制造一个真实的路径
     *
     * @param path
     * @return
     */
    @Deprecated
    public static String makeRealPath(String path, String widgetPath,
                                      int wgtType) {
        // path = makeUrl(currentUrl, path);
        if (path == null || path.length() == 0) {
            return null;
        }
        if (path.startsWith(F_ASSET_PATH)) {
            return path.substring(F_ASSET_PATH.length());
        } else if (path.startsWith(F_FILE_SCHEMA)) {
            return path.substring(F_FILE_SCHEMA.length());
        }
        if (path.startsWith(F_APP_SCHEMA)) {
            return widgetPath + path.substring(F_APP_SCHEMA.length());
        } else if (path.startsWith(F_WIDGET_SCHEMA)) {
            return WDataManager.m_wgtsPath
                    + path.substring(F_WIDGET_SCHEMA.length());
        } else if (path.startsWith(F_Widget_RES_SCHEMA)) {
            if (wgtType == 0) {
                if (WDataManager.isUpdateWidget) {
                    return WDataManager.m_sboxPath + F_Widget_RES_path
                            + path.substring(F_Widget_RES_SCHEMA.length());
                } else {
                    return F_Widget_RES_path
                            + path.substring(F_Widget_RES_SCHEMA.length());
                }

            } else {
                return widgetPath + "wgtRes/"
                        + path.substring(F_Widget_RES_SCHEMA.length());
            }
        } else if (path.startsWith(F_SBOX_SCHEMA)) {
            return WDataManager.m_sboxPath
                    + path.substring(F_SBOX_SCHEMA.length());
        } else {
            return path;
        }
    }

    /**
     * 制造一个真实的路径
     *
     * @return
     */
    public static String makeRealPath(String path, EBrowserView browserView) {
        path = makeUrl(browserView.getCurrentUrl(), path);
        int wgtType = browserView.getCurrentWidget().m_wgtType;
        String widgetPath = browserView.getCurrentWidget().getWidgetPath();
        return makeRealPath(path, widgetPath, wgtType);
    }

    /**
     * 获得带协议的全路径
     *
     * @param inBaseUrl
     * @param inUrl
     * @return
     */
    public static String getFullPath(String inBaseUrl, String inUrl) {
        String path = makeUrl(inBaseUrl, inUrl);
        if (path == null || path.length() == 0) {
            return null;
        }
        if (path.startsWith("/")) {
            path = F_FILE_SCHEMA + path;
        }
        return path;
    }

    public static String getSDRealPath(String totalPath, String widgetPath,
                                       int wgtType) {
        String path = makeRealPath(totalPath, widgetPath, wgtType);
        if (path != null && path.startsWith("/")) {
            path = F_FILE_SCHEMA + path;
        }
        return path;
    }

    /**
     * 是否是存在于SD上的协议路径
     *
     * @param fullPath
     * @return
     */
    public static boolean isSDcardPath(String fullPath) {
        if (fullPath != null
                && fullPath.length() > 0
                && (fullPath.startsWith(BUtility.F_FILE_SCHEMA)
                || fullPath.startsWith(BUtility.F_APP_SCHEMA)
                || fullPath.startsWith(BUtility.F_WIDGET_SCHEMA) || fullPath
                .startsWith(BUtility.F_Widget_RES_SCHEMA))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 转码
     */
    public static String transcoding(String text) {

        String regEx = "\n|\r|\"|\'|\\\\|&";
        /**
         * & 38 \n 10 换行 \r 13 回车 \' 39 单引号 \" 34 双引号 \\ 92 反斜杠
         */
        Pattern p = Pattern.compile(regEx);

        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {

            if (m.group(0).equals("\n")) {
                m.appendReplacement(sb, "\\\\n");
            } else if (m.group(0).equals("\r")) {
                m.appendReplacement(sb, "\\\\r");
            } else if (m.group(0).equals("\"")) {
                m.appendReplacement(sb, "\\\\\"");
            } else if (m.group(0).equals("\'")) {
                m.appendReplacement(sb, "\\\\'");
            } else if (m.group(0).equals("\\")) {
                m.appendReplacement(sb, "\\\\\\\\");
            } else if (m.group(0).equals("&")) {
                m.appendReplacement(sb, "\\\\&");
            }

        }
        m.appendTail(sb);
        // System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * 判断String是否是数字
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public static void alertMessage(final Activity activity, String title,
                                    String message, final boolean exitOnClicked) {
        new AlertDialog.Builder(activity).setTitle(title).setMessage(message)
                .setCancelable(false)
                .setPositiveButton(EUExUtil.getString("confirm"), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (exitOnClicked) {
                            activity.finish();
                        }
                    }
                }).show();

    }

    public static Bitmap getLocalImg(Context ctx, String imgUrl) {

        if (imgUrl == null || imgUrl.length() == 0) {
            return null;
        }
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            if (imgUrl.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
                is = BUtility.getInputStreamByResPath(ctx, imgUrl);
                bitmap = BitmapFactory.decodeStream(is);
            } else if (imgUrl.startsWith(BUtility.F_FILE_SCHEMA)) {
                imgUrl = imgUrl.replace(BUtility.F_FILE_SCHEMA, "");
                bitmap = BitmapFactory.decodeFile(imgUrl);
            } else if (imgUrl.startsWith(BUtility.F_Widget_RES_path)) {
                try {
                    is = ctx.getAssets().open(imgUrl);
                    if (is != null) {
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (imgUrl.startsWith("/")) {
                bitmap = BitmapFactory.decodeFile(imgUrl);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 获取真实的路径，同时拷贝res协议文件到sd卡缓存目录
     * @param mBrwView
     * @param url AppCan协议路径
     * @return
     */
    public static String getRealPathWithCopyRes(EBrowserView mBrwView,String url){
        String realPath=makeRealPath(url,mBrwView);
        if (realPath.startsWith("/") && !realPath.startsWith ("/data")) {
            return realPath;
        }
        return getResLocalPath(mBrwView.getContext(),realPath);
    }

    /**
     * 根据res协议获取本地路径（将文件拷贝到sd卡缓存目录）
     * @param context
     * @param url Android assets路径，或者开启增量更新时的/data 开头路径
     * @return
     */
    public static String getResLocalPath(Context context,String url){
        String resPath = url;// 获取的为assets路径
        InputStream inputStream = null;
        OutputStream out = null;
        String tempPath = url;
        try {
            if(resPath.startsWith("/data")){
                inputStream = new FileInputStream(new File(resPath));
            }else{
                inputStream = context.getResources().getAssets()
                        .open(resPath);
            }
            File cacheDir=context.getExternalCacheDir();
            if (cacheDir==null){
                return null;
            }
            String cachePath =cacheDir.getAbsolutePath();
            tempPath = cachePath + File.separator + resPath;
            File file = new File(tempPath);
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            if(file.exists()){
                file.delete();
            }
            out = new FileOutputStream(file);
            int count = 0;
            byte[] buff = new byte[1024];
            while ((count = inputStream.read(buff)) != -1) {
                out.write(buff, 0, count);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(inputStream != null) inputStream.close();
                if(out != null) out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tempPath;

    }

    public static String getFileNameWithNoSuffix(String path) {
        String name = null;
        int index = path.lastIndexOf('/');
        if (index > 0) {
            name = path.substring(index + 1, path.length());
        }
        int index1 = name.lastIndexOf('.');
        if (index1 > 0) {
            name = name.substring(0, index1);
        }
        return name;
    }

    /**
     * 获取外置SD卡路径
     *
     * @return 路径列表
     */
    public static List<String> getAllExtraSdcardPath() {
        List<String> sdList = new ArrayList<String>();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                // 将常见的linux分区过滤掉
                if (line.contains("secure") || line.contains("asec")
                        || line.contains("system")
                        || line.contains("cache") || line.contains("sys")
                        || line.contains("data") || line.contains("tmpfs")
                        || line.contains("shell") || line.contains("root")
                        || line.contains("acct") || line.contains("proc")
                        || line.contains("misc") || line.contains("obb")) {
                    continue;
                }
                if (line.contains("fat") || line.contains("fuse")
                        || line.contains("ntfs") || line.contains("extSdCard")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        String path = columns[1];
                        if (path != null && !sdList.contains(path)
                                && path.toLowerCase().contains("sd")) {
                            File file = new File(path);
                            if (file.isDirectory()) {
                                sdList.add(columns[1]);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sdList;
    }

    public static Bitmap createBitmapWithStream(InputStream inputStream,
                                                int reqWidth, int reqHeight) {
        Bitmap bm = null;
        if (inputStream != null) {
            bm = decodeSamplerBitmap(transStreamToBytes(inputStream, 64 * 1024), reqWidth, reqHeight);
        }
        return bm;
    }

    public static Bitmap decodeSamplerBitmap(byte[] data, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * @param encrypt
     *            加密或解密的字符串
     * @param pKey
     *            加密解密的key
     * @return 加密或解密后的字符串
     */
    public static String decryptString(String encrypt, String pKey) {
        byte[] encryptToByte = HexStringToBinary(encrypt);
        String encryptDecrypt = new String(PEncryption.os_decrypt(
                encryptToByte, encryptToByte.length, pKey));
        return encryptDecrypt;
    }

    /**
     * @param hexString
     * @return 将十六进制转换为字节数组
     */
    private static byte[] HexStringToBinary(String hexString) {
        // hexString的长度对2取整，作为bytes的长度
        String hexStr = "0123456789ABCDEF";
        int len = hexString.length() / 2;
        byte[] bytes = new byte[len];
        byte high = 0;// 字节高四位
        byte low = 0;// 字节低四位

        for (int i = 0; i < len; i++) {
            // 右移四位得到高位
            high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
            low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
            bytes[i] = (byte) (high | low);// 高地位做或运算
        }
        return bytes;
    }

    /**
     * @param inputStream
     *            xml文件输入流
     * @param fileName
     *            xml文件名，不带后缀
     * @param label
     *            标签名
     * @param attribute
     *            属性名，获取标签时此值传空即可，获取属性时必须传值
     * @return xml文件中指定标签或者标签中属性的值
     */
    public static String parserXmlLabel(InputStream inputStream,
            String fileName, String label, String attribute) {
        String value = "";
        // 如果标签不为空并且输入流不为空
        if (!TextUtils.isEmpty(label) && inputStream != null) {
            try {
                inputStream = decodeInputStream(inputStream, fileName);
                // 使用Xml的静态方法生成语法分析器
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream, "utf-8");
                int eventType = XmlPullParser.START_DOCUMENT;
                boolean needContinue = true;
                // 循环直到找到符合的标签或者直到文档结束
                while (needContinue) {
                    eventType = parser.next();
                    switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String localName = (parser.getName())
                                .toLowerCase();
                        // 如果该标签是传入的标签，获取该标签的值或者其属性的值
                        if (localName.equals(label.toLowerCase())) {
                            if (!TextUtils.isEmpty(attribute)) {
                                value = parser.getAttributeValue(null, attribute);
                            } else {
                                value = parser.nextText();
                            }
                            needContinue = false;
                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        needContinue = false;
                        break;
                    default:
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 如果inputStream不为空，释放掉
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        inputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return value;
    }

    /**
     * @param inputStream
     *            文件输入流
     * @param fileName
     *            文件名，不带后缀
     * @return 解密后的文件流
     */
    public static InputStream decodeInputStream(InputStream inputStream,
            String fileName) {
        try {
            // 先判断是否加密,如果是加密了才解密
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
            boolean isV = ACEDes.isEncrypted(is1);
            if (isV) {
                InputStream resStream = null;
                byte[] data = null;
                String result = null;
                data = transStreamToBytes(is2, is2.available());
                result = ACEDes.htmlDecode(data, fileName);
                resStream = new ByteArrayInputStream(result.getBytes());
                return resStream;
            } else {
                return is2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return inputStream;
        }
    }
}
