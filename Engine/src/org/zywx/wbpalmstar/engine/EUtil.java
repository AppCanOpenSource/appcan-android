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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import org.apache.http.HttpHost;
import org.apache.http.util.ByteArrayBuffer;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.base.vo.ShareInputVO;
import org.zywx.wbpalmstar.platform.encryption.PEncryption;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class EUtil {

    public static boolean debug = false;

    public static void logi(String msg) {
        if (!debug) {
            return;
        }
        Log.i("ldx", "" + msg);
    }

    public static void logd(String msg) {
        if (!debug) {
            return;
        }
        Log.d("ldx", "" + msg);
    }

    public static void loge(String msg) {
        if (!debug) {
            return;
        }
        Log.e("ldx", "" + msg);
    }

    public static void logw(String msg) {
        if (!debug) {
            return;
        }
        Log.w("ldx", "" + msg);
    }

    public static void printeBackup(Bundle data, String lev) {
        if (!debug) {
            return;
        }
        Log.d("backup", "---- " + lev + " begin ----");
        if (null != data) {
            for (String key : data.keySet()) {
                Object value = data.get(key);
                Log.d("backup", "key = " + key + " , value = " + value);
            }
        }
    }

    public static void testDecode(Context ctx) {
        String url = "up.html";
        AssetManager asset = ctx.getAssets();
        InputStream pinput = null;
        ByteArrayBuffer buffer = new ByteArrayBuffer(1024 * 8);
        try {
            pinput = asset.open(url);
            int lenth = 0;
            while (lenth != -1) {
                byte[] buf = new byte[2048];
                lenth = pinput.read(buf, 0, buf.length);
                if (lenth != -1) {
                    buffer.append(buf, 0, lenth);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bt = buffer.toByteArray();
        long begin = System.currentTimeMillis();
        String dec = ACEDes.htmlDecode(bt, "up");
        long end = System.currentTimeMillis();
        Log.e("ldx", "use time: " + (end - begin));
        Log.e("ldx", dec);
    }

    public static void viewBaseSetting(View target) {
        target.setFadingEdgeLength(0);
//		target.setWillNotDraw(true);
        target.setWillNotCacheDrawing(true);
        target.setBackgroundColor(0x00000000);
    }

    public static DexClassLoader loadDex(Context ctx, String dexAssertPath) {
        int index = dexAssertPath.lastIndexOf('/');
        if (index < 0) {
            return null;
        }
        String dexName = dexAssertPath.substring(index);
        String dexPath = ctx.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath() + dexName;
        File f = new File(dexPath);
        if (!f.exists()) {
            boolean ok = copyDex(ctx, dexAssertPath, dexPath);
            if (!ok) {
                return null;
            }
        }
        String dexOutputDir = ctx.getDir("outdex", Context.MODE_PRIVATE).getAbsolutePath();
        DexClassLoader cl = new DexClassLoader(dexPath, dexOutputDir, null, ctx.getClassLoader());
        return cl;
    }

    private static boolean copyDex(Context ctx, String dexAssertPath, String dexPath) {
        AssetManager assets = ctx.getAssets();
        InputStream inStream = null;
        OutputStream dexWriter = null;
        boolean suc = true;
        try {
            inStream = assets.open(dexAssertPath);
            FileOutputStream outStream = new FileOutputStream(dexPath);
            dexWriter = new BufferedOutputStream(outStream);
            byte[] buf = new byte[1024 * 8];
            int len;
            while ((len = inStream.read(buf)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            suc = false;
        } finally {
            try {
                if (null != inStream) {
                    inStream.close();
                }
                if (null != dexWriter) {
                    dexWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return suc;
    }

    public static void printAllPermission(Context context) {
        if (!debug) {
            return;
        }
        PackageManager pm = context.getPackageManager();
        CharSequence csPermissionGroupLabel;
        CharSequence csPermissionLabel;
        List<PermissionGroupInfo> lstGroups = pm.getAllPermissionGroups(PackageManager.GET_PERMISSIONS);
        for (PermissionGroupInfo pgi : lstGroups) {
            csPermissionGroupLabel = pgi.loadLabel(pm);
            Log.d("ldx", "PermissionGroup: " + pgi.name + "  [" + csPermissionGroupLabel.toString() + "]");
            try {
                List<PermissionInfo> lstPermissions = pm.queryPermissionsByGroup(pgi.name, 0);
                for (PermissionInfo pi : lstPermissions) {
                    csPermissionLabel = pi.loadLabel(pm);
                    Log.d("ldx", "     PermissionChild" + pi.name + "  [" + csPermissionLabel.toString() + "]");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String execRootCmd(String cmd) {
        String result = "result : ";
        try {
            Process p = Runtime.getRuntime().exec("su ");
            OutputStream outStream = p.getOutputStream();
            DataOutputStream dOutStream = new DataOutputStream(outStream);
            InputStream inStream = p.getInputStream();
            DataInputStream dInStream = new DataInputStream(inStream);
            String str1 = String.valueOf(cmd);
            String str2 = str1 + "\n";
            dOutStream.writeBytes(str2);
            dOutStream.flush();
            String str3 = null;
            String line = "";
            while ((line = dInStream.readLine()) != null) {
                Log.d("result", str3);
                str3 += line;
            }
            dOutStream.writeBytes("exit\n");
            dOutStream.flush();
            p.waitFor();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    public static int execRootCmdSilent(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec("su ");
            Object obj = p.getOutputStream();
            DataOutputStream dOutStream = new DataOutputStream((OutputStream) obj);
            String str = String.valueOf(cmd);
            obj = str + "\n";
            dOutStream.writeBytes((String) obj);
            dOutStream.flush();
            dOutStream.writeBytes("exit\n");
            dOutStream.flush();
            p.waitFor();
            int result = p.exitValue();
            return (Integer) result;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static boolean isSilent = true;

    public static boolean deviceBeRoot() {
        if (isSilent) {
            return isRootSystem();
        }
        int i = execRootCmdSilent("echo test");
        if (i != -1) {
            return true;
        }
        return false;
    }

    public static boolean isRootSystem() {
        String paths[] = {"/system/bin/",
                "/system/xbin/",
                "/system/sbin/",
                "/sbin/",
                "/vendor/bin/"};
        try {
            File file = null;
            String su = "su";
            for (String dir : paths) {
                file = new File(dir + su);
                if (file != null && file.exists()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //	public static String htmlDecode(byte[] bit, String name){
//		String result = "";
//		
//		if(bit == null || 0 == bit.length){
//			return result;
//		}
//		String zywx = "3G2WIN Safe Guard";
//		int actualLen = bit.length;
//		int zyLen = zywx.length();
//		
//		if(actualLen <= zyLen){
//			return new String(bit); 
//		}
//		
//		int start = actualLen - zyLen;
//		String endStr = new String(bit, start, zyLen);
//		if(!zywx.equals(endStr)){
//			return new String(bit);
//		}
//		
//		int skip = zyLen + 256;
//		if(actualLen <= skip){
//			
//			return new String(bit);
//		}
//		int realLen = actualLen - skip;
//		result = nativeHtmlDecode(bit, name, Integer.toString(realLen));
//		return result;
//	}
//	
//	static{
//		System.loadLibrary("appcan");
//	}
//	
    public static String getCertificatePsw(Context context, String appId) {
        String psw = ResoureFinder.getInstance().getString(
                context, "certificate_psw");
        byte[] pswToByte = hexStringToBinary(psw);
        return new String(PEncryption.os_decrypt(
                pswToByte, pswToByte.length, appId));
    }

    /**
     * @param hexString
     * @return 将十六进制转换为字节数组
     */
    private static byte[] hexStringToBinary(String hexString) {
        String hexStr = "0123456789ABCDEF";
        // hexString的长度对2取整，作为bytes的长度
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

    public static java.net.Proxy checkJavaProxy(Context context) {
        java.net.Proxy proxy = null;
        if (!wifiEnable(context)) {// 获取当前正在使用的APN接入点
            Uri uri = Uri.parse("content://telephony/carriers/preferapn");
            Cursor mCursor = context.getContentResolver().query(uri, null, null, null, null);
            if (mCursor != null && mCursor.moveToFirst()) {
                String proxyStr = mCursor.getString(mCursor.getColumnIndex("proxy"));
                int proxyPort = mCursor.getInt(mCursor.getColumnIndex("port"));
                if (proxyStr != null && proxyStr.trim().length() > 0) {
                    if (0 == proxyPort) {
                        proxyPort = 80;
                    }
                    proxy = new java.net.Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyStr, proxyPort));
                }
                mCursor.close();
            }
        }
        return proxy;
    }

    public static HttpHost checkAndroidProxy(Context context) {
        HttpHost proxy = null;
        if (!wifiEnable(context)) {// 获取当前正在使用的APN接入点
            Uri uri = Uri.parse("content://telephony/carriers/preferapn");
            Cursor mCursor = context.getContentResolver().query(uri, null, null, null, null);
            if (mCursor != null && mCursor.moveToFirst()) {
                String proxyStr = mCursor.getString(mCursor.getColumnIndex("proxy"));
                int proxyPort = mCursor.getInt(mCursor.getColumnIndex("port"));
                if (proxyStr != null && proxyStr.trim().length() > 0) {
                    if (0 == proxyPort) {
                        proxyPort = 80;
                    }
                    proxy = new HttpHost(proxyStr, proxyPort);
                }
                mCursor.close();
            }
        }
        return proxy;
    }

    public static boolean wifiEnable(Context context) {

        return NETWORK_CLASS_WIFI == getConnectedType(context);
    }

    /**
     * Unknown network class
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    /**
     * Class of broadly defined "2G" networks
     */
    public static final int NETWORK_CLASS_2_G = 1;
    /**
     * Class of broadly defined "3G" networks
     */
    public static final int NETWORK_CLASS_3_G = 2;
    /**
     * Class of broadly defined "4G" networks
     */
    public static final int NETWORK_CLASS_4_G = 3;
    /**
     * Class of broadly defined "WiFi" networks
     */
    public static final int NETWORK_CLASS_WIFI = 4;

    public static int getConnectedType(Context context) {
        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cManager.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isAvailable()) {
            int type = nInfo.getType();
            int subType = nInfo.getSubtype();
            switch (type) {
                case ConnectivityManager.TYPE_MOBILE:
                    switch (subType) {
                        case 1://TelephonyManager.NETWORK_TYPE_GPRS:
                        case 2://TelephonyManager.NETWORK_TYPE_EDGE:
                        case 4://TelephonyManager.NETWORK_TYPE_CDMA:
                        case 7://TelephonyManager.NETWORK_TYPE_1xRTT:
                        case 11://TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORK_CLASS_2_G;
                        case 3://TelephonyManager.NETWORK_TYPE_UMTS:
                        case 5://TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case 6://TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case 8://TelephonyManager.NETWORK_TYPE_HSDPA:
                        case 9://TelephonyManager.NETWORK_TYPE_HSUPA:
                        case 10://TelephonyManager.NETWORK_TYPE_HSPA:
                        case 12://TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case 14://TelephonyManager.NETWORK_TYPE_EHRPD:
                        case 15://TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORK_CLASS_3_G;
                        case 13://TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORK_CLASS_4_G;
                        default:
                            return NETWORK_CLASS_UNKNOWN;
                    }
                case ConnectivityManager.TYPE_WIFI:

                    return NETWORK_CLASS_WIFI;
            }
        }
        return NETWORK_CLASS_UNKNOWN;
    }

//	private native static String nativeHtmlDecode(byte[] bit, String name, String lenStr);

    public static void installApp(Context context, String inAppPath) {
        if (null == inAppPath || 0 == inAppPath.trim().length()) {
            return;
        }
        String reallyPath = "";
        File file = new File(inAppPath);
        if (file.exists()) {
            reallyPath = inAppPath;
        } else {
            reallyPath = copyFileToStorage(context, inAppPath);
            if (null == reallyPath) {
                return;
            }
        }
        // install apk.
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MimeTypeMap type = MimeTypeMap.getSingleton();
        String mime = type.getMimeTypeFromExtension("apk");
        reallyPath = reallyPath.contains("file://") ? reallyPath : ("file://" + reallyPath);
        intent.setDataAndType(Uri.parse(reallyPath), mime);
        context.startActivity(intent);
    }

    public static String copyFileToStorage(Context context, String inFilePath) {
        if (!BUtility.sdCardIsWork()) {
            return null;
        }
        String ext = Environment.getExternalStorageDirectory() + File.separator + "download";
        String fileName = subFileName(inFilePath);
        String newFilePath = ext + File.separator + fileName;
        File file = new File(newFilePath);
        if (file.exists()) {
            return newFilePath;
        }
        try {
            AssetManager assrt = context.getAssets();
            InputStream input = assrt.open(inFilePath);
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file);
            byte[] temp = new byte[8 * 1024];
            int i = 0;
            while ((i = input.read(temp)) > 0) {
                output.write(temp, 0, i);
            }
            output.close();
            input.close();
        } catch (Exception e) {
            return null;
        }
        return newFilePath;
    }

    public static String subFileName(String inCertPath) {
        if (null == inCertPath) {
            return null;
        }
        int index = inCertPath.lastIndexOf(File.separator);
        if (index < 0) {
            return inCertPath;
        }
        return inCertPath.substring(index + 1);
    }

    public static void uninstallApp(Context context, String packageName) {
        // Create new intent to launch Uninstaller activity
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }


    public final void createSystemSwitcherShortCut(Context context, String shortCutName) {
        Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        Parcelable icon = Intent.ShortcutIconResource.fromContext(context, EResources.icon);
        addIntent.putExtra("duplicate", false);
        Intent targetIntent = new Intent(context, EBrowserActivity.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortCutName);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, targetIntent);
        context.sendBroadcast(addIntent);
    }

    public static void share(Context context, ShareInputVO inputVO){
        Intent intent=new Intent();
        if (inputVO.getImgPaths()!=null&&inputVO.getImgPaths().size()>0){
            //分享多张图片
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> imagePathList = new ArrayList<Uri>();
            for(String picPath: inputVO.getImgPaths()){
                File file=new File(picPath);
                imagePathList.add(Uri.fromFile(file));
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,imagePathList);
        }else{
            intent.setAction(Intent.ACTION_SEND);
        }
         if (!TextUtils.isEmpty(inputVO.getPackageName())&&
                !TextUtils.isEmpty(inputVO.getClassName())) {
            intent.setComponent(new ComponentName(inputVO.getPackageName(), inputVO.getClassName()));
        }
        if (!TextUtils.isEmpty(inputVO.getTitle())){
            intent.putExtra(Intent.EXTRA_TITLE,inputVO.getTitle());
        }
        if (!TextUtils.isEmpty(inputVO.getText())){
            intent.putExtra(Intent.EXTRA_TEXT,inputVO.getText());
        }
        if (!TextUtils.isEmpty(inputVO.getSubject())){
            intent.putExtra(Intent.EXTRA_SUBJECT,inputVO.getSubject());
        }
        if (!TextUtils.isEmpty(inputVO.getImgPath())||inputVO.getImgPaths()!=null) {
            intent.setType("image/*");
        }else{
            intent.setType("text/plain");
        }
        if (inputVO.getType()==0){
            //微信朋友圈
            intent.putExtra("Kdescription", inputVO.getText());
            intent.setComponent(new ComponentName("com.tencent.mm",
                    "com.tencent.mm.ui.tools.ShareToTimeLineUI"));
        }

        if (intent.getComponent()!=null){
            context.startActivity(intent);
        }else{
            context.startActivity(Intent.createChooser(intent,"请选择"));
        }

    }



}
