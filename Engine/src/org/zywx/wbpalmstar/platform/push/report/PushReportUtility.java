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

package org.zywx.wbpalmstar.platform.push.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.zywx.wbpalmstar.base.BUtility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;

public class PushReportUtility {
    private static boolean isLog = true;
    public final static String KEY_APPVERIFY = "appverify";

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            // Log.e(LOG_TAG, ex.toString());
        }
        return null;
    }

    public static String getNetName(Context activity) {
        if (activity == null) {
            return null;
        }
        String netName = null;
        try {
            ConnectivityManager cm = (ConnectivityManager) activity
                    .getApplicationContext().getSystemService(
                            Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    switch (info.getType()) {
                        case ConnectivityManager.TYPE_MOBILE:
                            TelephonyManager telephonyManager = (TelephonyManager) activity
                                    .getSystemService(Context.TELEPHONY_SERVICE);
                            switch (telephonyManager.getNetworkType()) {
                                case PushReportConstants.NETWORK_TYPE_1xRTT:
                                case PushReportConstants.NETWORK_TYPE_CDMA:
                                case PushReportConstants.NETWORK_TYPE_EDGE:
                                case PushReportConstants.NETWORK_TYPE_GPRS:
                                    netName = "GPRS";
                                    break;
                                case PushReportConstants.NETWORK_TYPE_EVDO_0:
                                case PushReportConstants.NETWORK_TYPE_EVDO_A:
                                case PushReportConstants.NETWORK_TYPE_HSDPA:
                                case PushReportConstants.NETWORK_TYPE_HSPA:
                                case PushReportConstants.NETWORK_TYPE_HSUPA:
                                case PushReportConstants.NETWORK_TYPE_UMTS:
                                    netName = "3G";
                                    break;
                            }
                            break;
                        case ConnectivityManager.TYPE_WIFI:
                            netName = "WIFI";
                            break;
                    }
                }
            }
        } catch (SecurityException e) {
            netName = null;
        }
        return netName;
    }

    public static String getDeviceResolution(Context activity) {
        if (activity == null) {
            return null;
        }

        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        return dm.widthPixels + "*" + dm.heightPixels;
    }

    @SuppressWarnings("rawtypes")
    public static String getMapToString(Map inAttriDict) {
        String attriDict = "";
        if (inAttriDict != null) {
            @SuppressWarnings("unchecked")
            Iterator<Object> iterator = inAttriDict.keySet().iterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                if ("".equals(attriDict)) {

                    attriDict = object + ":" + inAttriDict.get(object);
                } else {
                    attriDict += "," + object + ":" + inAttriDict.get(object);
                }
            }

        }
        return attriDict;
    }

    public static String getNowTime() {
        Time time = new Time();
        time.setToNow();
        int year = time.year;
        int month = time.month + 1;
        int day = time.monthDay;
        int minute = time.minute;
        int hour = time.hour;
        int sec = time.second;
        return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":"
                + sec;
    }

    public static String getCurYearAndMonth() {
        Time time = new Time();
        time.setToNow();
        int year = time.year;
        int month = time.month + 1;
        return year + "_" + month;
    }

    public static String getAppNameVer(Context activity, String name, String ver) {
        PackageManager pm = activity.getPackageManager();
        PackageInfo pinfo = null;
        try {
            pinfo = pm.getPackageInfo(activity.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            String appName = pinfo.applicationInfo.loadLabel(
                    activity.getPackageManager()).toString();
            SharedPreferences preferences = activity.getSharedPreferences(name,
                    Context.MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putString("appName", appName);
            editor.commit();
            return appName + ";" + ver;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * 获取主应用的softToken
     *
     * @param activity
     * @param appKey
     * @return
     */
    public static String getSoftToken(Context activity, String appKey) {
        SharedPreferences preferences = activity.getSharedPreferences(
                PushReportConstants.SP_APP, Context.MODE_PRIVATE);
        String softToken = preferences.getString("softToken", null);
        if (softToken != null) {
            return softToken;
        }

        String[] val = new String[4];
        try {

            val[0] = getMacAddress(activity);
            TelephonyManager telephonyManager = (TelephonyManager) activity
                    .getSystemService(Context.TELEPHONY_SERVICE);
            val[1] = telephonyManager.getDeviceId();
            val[2] = getCPUSerial();
            val[3] = appKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        softToken = getMD5Code(val);
        Editor editor = preferences.edit();
        editor.putString("softToken", softToken);
        editor.commit();
        return softToken;
    }

    /**
     * 获取当前应用的softToken
     *
     * @param activity
     * @param appKey
     * @return
     */
    public static String getWidgetSoftToken(Context activity, String appKey) {
        String[] val = new String[4];
        try {

            val[0] = getMacAddress(activity);
            TelephonyManager telephonyManager = (TelephonyManager) activity
                    .getSystemService(Context.TELEPHONY_SERVICE);
            val[1] = telephonyManager.getDeviceId();
            val[2] = getCPUSerial();
            val[3] = appKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        String softToken = getMD5Code(val);
        return softToken;
    }

    public static String getMacAddress(Context activity) {
        String macSerial = null;
        try {
            WifiManager wifi = (WifiManager) activity
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            macSerial = info.getMacAddress();
        } catch (Exception e) {
            // TODO: handle exception
        }
        if (macSerial == null) {
            macSerial = readFileContent("/sys/class/net/wlan0/address").trim();
        }
        return macSerial;
    }

    private static String readFileContent(String path) {
        String content = "";
        try {
            if ((new File(path)).exists()) {
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[8192];
                int byteCount = fis.read(buffer);
                if (byteCount > 0) {
                    content = new String(buffer, 0, byteCount, "utf-8");
                }
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public static String getMD5Code(String[] value) {
        if (value == null || value.length == 0) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            for (String va : value) {
                if (va == null) {
                    va = "";
                }
                md.update(va.getBytes());
            }
            byte[] md5Bytes = md.digest();
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取CPU序列号
     *
     * @return CPU序列号(16位) 读取失败为"0000000000000000"
     */
    private static String getCPUSerial() {
        String str = "", cpuAddress = "0000000000000000";
        try {
            // 读取CPU信息
            str = readFileContent("/proc/cpuinfo");
            if (str != null && str.length() > 0) {
                String[] strs = str.split("\n");
                for (int i = 0; i < strs.length; i++) {
                    // 提取到序列号所在行的内容
                    if (strs[i].startsWith("Serial")) {
                        // 提取序列号
                        cpuAddress = strs[i].substring(strs[i].indexOf(":") + 1,
                                strs[i].length()).trim();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return cpuAddress;
    }

    public static String getAppIdAppKeyMD5(String appId, String appKey) {
        String[] value = new String[]{appId + ":" + appKey};
        return getMD5Code(value);
    }

    public static String decodeStr(String key) {
        char map[] = {'d', 'b', 'e', 'a', 'f', 'c'};
        char nmap[] = {'2', '4', '0', '9', '7', '1', '5', '8', '3', '6'};
        String dest = "";
        String swapstr = "";
        String output = "";
        for (int j = 0; j < key.length(); j++) {
            if (key.charAt(j) == '-')
                continue;
            swapstr = swapstr + key.charAt(j);
        }
        for (int j = 0; j < swapstr.length(); j++) {
            if (j == 8 || j == 12 || j == 16 || j == 20)
                dest = dest + "-";
            dest = dest + swapstr.charAt(swapstr.length() - j - 1);
        }
        for (int i = 0; i < dest.length(); i++) {
            char t = dest.charAt(i);
            if (t >= 'a' && t <= 'f') {
                t = map[t - 'a'];
            } else if (t >= '0' && t <= '9') {
                t = nmap[t - '0'];
            }
            output = output + t;
        }
        return output;
    }

    public static String getMobileOperatorName(Context mContext) {
        String name = "unKnown";
        TelephonyManager telephonyManager = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
            // IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber
            // Identification
            // Number）是区别移动用户的标志，
            // 储存在SIM卡中，可用于区别移动用户的有效信息。
            // IMSI由MCC、MNC组成，
            // 其中MCC为移动国家号码，由3位数字组成唯一地识别移动客户所属的国家，我国为460；
            // MNC为网络id，由2位数字组成, 用于识别移动客户所归属的移动网络，中国移动为00和02，中国联通为01,中国电信为03
            String imsi = telephonyManager.getNetworkOperator();
            if (imsi.equals("46000") || imsi.equals("46002")) {
                name = "中国移动";
            } else if (imsi.equals("46001")) {
                name = "中国联通";
            } else if (imsi.equals("46003")) {
                name = "中国电信";
            } else {
                // 其他电信运营商直接显示其名称，一般为英文形式
                name = telephonyManager.getSimOperatorName();
            }
        }
        return name;
    }

    public static void deleteFile(File file, String path, String libPath) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (!libPath.equals(files[i].getPath())) {
                        deleteFile(files[i], path, libPath);
                    }

                }
            }
            if (!path.equals(file.getPath())) {
                file.delete();
            }

        }
    }

    /**
     * sd卡记录信息
     *
     * @param text
     */
    public static void log(String text) {
        Log.i("push", text);
        if (!isLog) {
            return;
        }
        if (!TextUtils.isEmpty(text) && BUtility.sdCardIsWork()) {
            String developPath = BUtility.getSdCardRootPath()
                    + "widgetone/log/";
            File dir = new File(developPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File log = new File(developPath + "push_log_"
                    + getCurYearAndMonth() + ".log");
            try {
                if (!log.exists()) {
                    log.createNewFile();
                }
                BufferedWriter m_fout = new BufferedWriter(new FileWriter(log,
                        true));
                m_fout.write("\r" + getNowTime() + "\r"
                        + text);
                m_fout.flush();
                m_fout.close();
                m_fout = null;
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

    /**
     * sd卡输出异常错误信息
     *
     * @param methodName
     * @param e
     */
    public static void oe(String methodName, Exception e) {
        String outputExceptionStr = methodName + " Exception: "
                + e.getClass().getName() + " Details:" + e.getMessage()
                + " CauseBy: " + e.getCause();
        log(outputExceptionStr);
    }

    public static String getSerialNumber() {

        String serial = null;

        try {

            Class<?> c = Class.forName("android.os.SystemProperties");

            Method get = c.getMethod("get", String.class);

            serial = (String) get.invoke(c, "ro.serialno");

            System.out.println(serial);

        } catch (Exception ignored) {

        }

        return serial;

    }

    /**
     * 添加验证头
     * 
     * @param appid
     * @param appkey
     * @param timeStamp
     *            当前时间戳
     * @return
     */
    public static String getAppVerifyValue(String appid, String appkey, long timeStamp) {
        String value = null;
        String md5 = getMD5Code(appid + ":" + appkey + ":" + timeStamp);
        value = "md5=" + md5 + ";ts=" + timeStamp;
        return value;
    }

    public static String getMD5Code(String value) {
        if (value == null) {
            value = "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(value.getBytes());
            byte[] md5Bytes = md.digest();
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
