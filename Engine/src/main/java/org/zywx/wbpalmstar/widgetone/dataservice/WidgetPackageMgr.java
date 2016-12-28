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

package org.zywx.wbpalmstar.widgetone.dataservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.zip.CnZipInputStream;
import org.zywx.wbpalmstar.base.zip.ZipEntry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class WidgetPackageMgr {

    public static final String WIDGET_CONFIG_FILE_NAME = "config.xml";
    public static final String WIDGET_CONFIG_PARENT_FILE_NAME = "widget";
    public static final String DYNAMIC_PLUGIN_ASSEST_PATH_NAME = "plugin";
    public static final String DYNAMIC_PLUGIN_SANDBOX_PATH_NAME = "apkfile";
    public final static String SP_WIDGET_ONE_CONFIG = "widgetOneConfig";

    /**
     * 安装主应用补丁包
     * 
     * @param context
     * @param appId
     * @param installType：安装补丁包类型1：网页包；2：插件包；3：网页和插件
     * @return 安装成功，返回版本号；失败，返回空。
     */
    public static String installWidgetPatch(Context context, String appId,
            int installType) {
        WidgetPatchConfig wConfig = getWidgetPatchConfig(context, appId,
                WDataManager.m_sboxPath + "widget/");
        if (wConfig.hasZip) {
            unZip(context, wConfig.isDynamicLoad, appId, installType);
        }
        return wConfig.version;
    }

    /**
     * 安装子应用（包括全量包、补丁包）
     * 
     * @param appId
     * @param filePath
     * @param desPath
     * @param encoding
     * @return 安装路径
     */
    public static String installSubWidget(String appId, String filePath,
            String desPath, String encoding) {
        String installPath = "";
        boolean isDynamic = isDynamicSubWidget(appId, filePath);
        if (!isDynamic) {
            installPath = unZip(filePath, desPath, null);
        } else {
            String pluginPath = WDataManager.m_sboxPath
                    + DYNAMIC_PLUGIN_SANDBOX_PATH_NAME + File.separator;
            installPath = unZipDynamic(appId, filePath,
                    desPath + File.separator + appId, pluginPath, encoding,
                    BUtility.INSTALL_PATCH_ALL);
        }
        return installPath;
    }

    /**
     * 解压补丁包
     * 
     * @param context
     * @param appId
     * @param installType：安装补丁包类型1：网页包；2：插件包；3：网页和插件
     * @return
     */
    public static boolean unZip(Context context, String appId,
            int installType) {
        WidgetPatchConfig wConfig = getWidgetPatchConfig(context, appId,
                WDataManager.m_sboxPath + "widget/");
        return unZip(context, wConfig.isDynamicLoad, appId, installType);
    }

    static class WidgetPatchConfig {
        boolean hasZip;
        String version;
        boolean isDynamicLoad;
    }

    /**
     * @param context
     * @param sboxPath
     * @param appId
     * @return
     */
    public static boolean isHasUpdateZip(Context context, String sboxPath,
            String appId) {
        return getWidgetPatchConfig(context, appId,
                sboxPath + "widget/").hasZip;
    }

    private static boolean isDynamicSubWidget(String appId, String widgetPath) {
        boolean isDynamic = true;
        FileInputStream inputStream = null;
        CnZipInputStream in = null;
        try {
            inputStream = new FileInputStream(widgetPath);
            in = new CnZipInputStream(inputStream, "UTF-8");
            ZipEntry entry = in.getNextEntry();
            String configName = appId + File.separator
                    + WIDGET_CONFIG_FILE_NAME;
            while (entry != null) {
                String zename = entry.getName();
                if (configName.equals(zename)) {
                    isDynamic = false;
                    break;
                }
                entry = in.getNextEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isDynamic;
    }

    /**
     * 判断是否有增量更新包，如果有，继续判断版本号是否大于当前APK的版本号。 主应用补丁包结构：旧：网页代码+config.xml打包。
     * 动态加载结构：widget/appId/网页代码+config.xml。
     * 
     * @param context
     * @param appId
     * @param zipPath
     * @return
     */
    private static WidgetPatchConfig getWidgetPatchConfig(Context context,
            String appId, String zipPath) {
        WidgetPatchConfig wConfig = new WidgetPatchConfig();
        wConfig.version = "";
        SharedPreferences preferences = context
                .getSharedPreferences("updateInfo", Context.MODE_PRIVATE);
        int totalSize = preferences.getInt("totalSize", 0);
        int downloaded = preferences.getInt("downloaded", 0);
        if (totalSize == 0 || downloaded == 0 || totalSize != downloaded) {
            wConfig.hasZip = false;
        } else {
            String filePath = preferences.getString("filePath", null);
            if (!TextUtils.isEmpty(filePath)) {
                try {
                    File dir = new File(zipPath);
                    // 建立与目标文件的输入连接
                    FileInputStream inputStream = new FileInputStream(filePath);
                    CnZipInputStream in = new CnZipInputStream(inputStream,
                            "UTF-8");
                    ZipEntry entry = in.getNextEntry();
                    byte[] c = new byte[1024];
                    int slen;
                    String dynamicConfigStart = WIDGET_CONFIG_PARENT_FILE_NAME
                            + File.separator + appId + File.separator;
                    while (entry != null) {
                        String zename = entry.getName();
                        if (WIDGET_CONFIG_FILE_NAME.equals(zename)
                                || (dynamicConfigStart
                                        + WIDGET_CONFIG_FILE_NAME)
                                                .equals(zename)) {
                            wConfig.isDynamicLoad = WIDGET_CONFIG_FILE_NAME
                                    .equals(zename) ? false : true;
                            if (wConfig.isDynamicLoad) {
                                zename = zename
                                        .substring(dynamicConfigStart.length());
                            }
                            File files = new File(
                                    dir.getAbsolutePath() + "/" + zename)
                                            .getParentFile();// 当前文件所在目录
                            if (!files.exists()) {// 如果目录文件夹不存在，则创建
                                files.mkdirs();
                            }
                            // 得到config.xml文件的内容
                            String configPathTmp = dir.getAbsolutePath() + "/tmp/" + zename;
                            File configDirTmp = new File(configPathTmp).getParentFile();
                            if (!configDirTmp.exists()) {// 如果目录文件夹不存在，则创建
                                configDirTmp.mkdirs();
                            }
                            File configFileTmp = new File(configPathTmp);
                            FileOutputStream out = new FileOutputStream(configFileTmp);
                            while ((slen = in.read(c, 0, c.length)) != -1)
                                out.write(c, 0, slen);

                            // 对config.xml文件进行XML解析
                            if (configFileTmp.exists()) {
                                FileInputStream input = new FileInputStream(
                                        configFileTmp);
                                // 得到增量更新包config.xml文件中的版本号
                                String m_verString = BUtility.parserXmlLabel(
                                        input, "config", "widget", "version");
                                wConfig.version = m_verString;
                                // 比较增量更新包和当前APK的版本号大小
                                String dbVerString = context
                                        .getSharedPreferences(
                                                SP_WIDGET_ONE_CONFIG,
                                                Context.MODE_PRIVATE)
                                        .getString("dbVer", null);
                                if (m_verString != null
                                        && dbVerString != null) {
                                    // 格式化版本号内容，去掉"."
                                    m_verString = formatVerString(
                                            m_verString.split("\\."));
                                    dbVerString = formatVerString(
                                            dbVerString.split("\\."));
                                    // 转换成long型
                                    long m_verLong = Long
                                            .parseLong(m_verString);
                                    long dbVerLong = Long
                                            .parseLong(dbVerString);
                                    if (m_verLong > dbVerLong) {
                                        wConfig.hasZip = true;
                                    }
                                }
                                input.close();
                                configFileTmp.delete();
                            } else {
                                wConfig.hasZip = false;
                            }
                            out.close();
                            configDirTmp.delete();
                            break;
                        }
                        entry = in.getNextEntry();
                    }
                    in.close();
                } catch (Exception i) {
                    wConfig.hasZip = false;
                }
            } else {
                wConfig.hasZip = false;
            }
        }
        return wConfig;
    }

    private static String formatVerString(String[] s) {
        if (s.length == 1 && s[0].length() == 1) {
            s[0] = 0 + s[0];
        }
        if (s.length == 2 && s[1].length() == 1) {
            s[1] = "0" + s[1];
        }
        if (s.length == 3 && s[2].length() == 1) {
            s[2] = "000" + s[2];
        }
        if (s.length == 3 && s[2].length() == 2) {
            s[2] = "00" + s[2];
        }
        if (s.length == 3 && s[2].length() == 3) {
            s[2] = "0" + s[2];
        }
        StringBuffer sbf = new StringBuffer("");
        if (s.length == 1) {
            sbf.append(s[0]).append("000000");
        } else if (s.length == 2) {
            sbf.append(s[0]).append(s[1]).append("0000");
        } else if (s.length == 3) {
            sbf.append(s[0]).append(s[1]).append(s[2]);
        }
        return sbf.toString();
    }

    /**
     * 解压补丁包
     * 
     * @param context
     * @param isDynamic
     * @param sboxPath
     * @param appId
     * @param installType：安装补丁包类型1：网页包；2：插件包；3：网页和插件
     * @return
     */
    private static boolean unZip(Context context, boolean isDynamic,
            String appId, int installType) {
        boolean unZip = false;
        SharedPreferences preferences = context
                .getSharedPreferences("updateInfo", Context.MODE_PRIVATE);
        int totalSize = preferences.getInt("totalSize", 0);
        int downloaded = preferences.getInt("downloaded", 0);

        if (totalSize == 0 || downloaded == 0 || totalSize != downloaded) {
            return false;
        }
        String filePath = preferences.getString("filePath", null);
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        if (BUtility.PATCH_PLUGIN_FLAG != installType) {
            Editor editor = preferences.edit();
            editor.clear();
            editor.commit();
        }
        String widgetPath = WDataManager.m_sboxPath + "widget/";
        if (!isDynamic) {
            unZip = (!TextUtils.isEmpty(unZip(filePath, widgetPath, null)));
        } else {
            String pluginPath = WDataManager.m_sboxPath
                    + DYNAMIC_PLUGIN_SANDBOX_PATH_NAME + File.separator;
            unZip = (!TextUtils.isEmpty(unZipDynamic(appId, filePath,
                    widgetPath, pluginPath, null, installType)));
        }

        if (unZip && (BUtility.PATCH_PLUGIN_FLAG != installType)) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
        return unZip;
    }

    /**
     * 解压补丁包
     * 
     * @param inputStream
     * @param decompression
     * @param encoding
     * @return
     */
    private static String unZip(String srcPath, String decompression,
            String encoding) {
        if (encoding == null || encoding.equals("")) {
            encoding = "UTF-8";
        }
        String filePath = "";
        CnZipInputStream in = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(srcPath);
            // 建立与目标文件的输入连接
            in = new CnZipInputStream(inputStream, encoding);
            ZipEntry file = in.getNextEntry();
            byte[] c = new byte[1024];
            String dpPath = new File(decompression).getAbsolutePath();
            filePath = dpPath + "/" + file.getName();
            while (file != null) {
                String zename = file.getName();
                getFileFromZip(in, file, c, dpPath, zename);
                file = in.getNextEntry();
            }
        } catch (Exception i) {
            i.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    /**
     * 动态加载插件解压补丁包
     * 
     * @param context
     * @param appId
     * @param inputStream
     * @param widgetPath
     * @param pluginPath
     * @param encoding
     * @param installType：安装补丁包类型1：网页包；2：插件包；3：网页和插件
     * @return
     */
    private static String unZipDynamic(String appId, String srcPath,
            String widgetPath, String pluginPath, String encoding,
            int installType) {
        if (encoding == null || encoding.equals("")) {
            encoding = "UTF-8";
        }
        String widgetAbsolutePath = new File(widgetPath).getAbsolutePath();
        String pluginAbsolutePath = new File(pluginPath).getAbsolutePath();
        CnZipInputStream in = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(srcPath);
            in = new CnZipInputStream(inputStream, encoding);
            ZipEntry file = in.getNextEntry();
            byte[] c = new byte[1024];
            String widgetPathStart = WIDGET_CONFIG_PARENT_FILE_NAME
                    + File.separator + appId + File.separator;
            String pluginPathStart = DYNAMIC_PLUGIN_ASSEST_PATH_NAME
                    + File.separator;
            while (file != null) {
                String decompression = "";
                String startStr = "";
                String zename = file.getName();
                if (zename.startsWith(widgetPathStart)
                        && ((installType & BUtility.PATCH_WIDGET_FLAG) != 0)) {
                    decompression = widgetAbsolutePath;
                    startStr = widgetPathStart;
                } else if (zename.startsWith(pluginPathStart)
                        && ((installType & BUtility.PATCH_PLUGIN_FLAG) != 0)) {
                    decompression = pluginAbsolutePath;
                    startStr = pluginPathStart;
                }
                if (!TextUtils.isEmpty(decompression)) {
                    zename = zename.substring(startStr.length());
                    getFileFromZip(in, file, c, decompression, zename);
                }
                file = in.getNextEntry();
            }
        } catch (Exception i) {
            i.printStackTrace();
            widgetAbsolutePath = "";
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return widgetAbsolutePath;
    }

    /**
     * 从zip包中获取文件内容
     * 
     * @param in
     * @param file
     * @param c
     * @param decompression
     * @param zename
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void getFileFromZip(CnZipInputStream in, ZipEntry file,
            byte[] c, String decompression, String zename)
            throws FileNotFoundException, IOException {
        int slen;
        try {
            if (file.isDirectory()) {
                File files = new File(decompression + "/" + zename); // 在指定解压路径下建子文件夹
                files.mkdirs();// 新建文件夹
            } else {
                File files = new File(decompression + "/" + zename)
                        .getParentFile();// 当前文件所在目录
                if (!files.exists()) {// 如果目录文件夹不存在，则创建
                    files.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(
                        decompression + "/" + zename);
                while ((slen = in.read(c, 0, c.length)) != -1) {
                    out.write(c, 0, slen);
                }
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}