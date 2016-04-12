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

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class WWidgetData implements Parcelable {

    // 表示我的空间按钮显示，单击按钮进入我的空间
    public static final int F_SPACESTATUS_OPEN = 0x1;

    // 表示我的空间功能关闭
    public static final int F_SPACESTATUS_CLOSE = 0x2;

    // 表示开我的空间内的更多按钮显示
    public static final int F_MYSPACEMOREAPP_OPEN = 0x4;
    // 表示开我的空间内的更多按钮不显示
    public static final int F_MYSPACEMOREAPP_CLOSE = 0x8;

    // 广告开启
    public static final int F_WIDGETADSTATUS_OPEN = 1;
    // 广告关闭
    public static final int F_WIDGETADSTATUS_CLOSE = 0;

    public static final String TAG_WIN_BG = "windowbackground";
    public static final String TAG_WIN_BG_OPAQUE = "opaque";
    public static final String TAG_WIN_BG_COLOR = "bgColor";
    // 数据库中的主键id
    public int m_id;
    // 手机端WidgetOne系统的唯一标识
    public String m_widgetOneId;
    // 应用软件唯一的标识，对于不同的手机或者同一手机上的不同应用，该值唯一
    public String m_widgetId;
    // 应用程序标识
    public String m_appId;
    // Widget版本号（String类型）
    public String m_ver;
    // 渠道号
    public String m_channelCode;
    // 手机IMEI号码
    public String m_imei;
    // 上传参数校验码
    public String m_md5Code;
    // widget 名称
    public String m_widgetName;
    //
    public String m_description;
    //
    public String m_email;
    //
    public String m_author;
    //
    public String m_license;
    // widget 的Icon 路径
    public String m_iconPath;
    // widget 在sdcard的路径
    public String m_widgetPath;
    // widget首页 路径
    public String m_indexUrl;
    // 是否加密
    public int m_obfuscation;
    // log服务器ip
    public String m_logServerIp;
    // widget类型（0-主widget；1-我的空间；2-空间的widget；3-Plug-in）
    public int m_wgtType;
    // widget更新地址
    public String m_updateurl;
    // 主widget是否显示space:(0：表示我的空间功能关闭;1：表示我的空间按钮显示，单击按钮进入我的空间;2：表示开启我的空间二级菜单功能，可展开二级菜单)
    public int m_spaceStatus;
    //
    public int m_orientation = 1;
    // 是否显示广告(0,关闭；1，开启)
    public int m_widgetAdStatus;
    // 是否是webApp(0-不是; 1-是)
    public int m_webapp = 0;
    /**
     * 被禁用的插件
     * @deprecated
     */
    public String[] disablePlugins;
    public ArrayList<String> disablePluginsList = new ArrayList<String>();
    /**
     * 被禁用的窗口
     * @deprecated
     */
    public String[] disableRootWindows;
    public ArrayList<String> disableRootWindowsList = new ArrayList<String>();
    /**
     * 被禁用的子窗口
     * @deprecated
     */
    public String[] disableSonWindows;
    public ArrayList<String> disableSonWindowsList = new ArrayList<String>();

    public String m_appkey;

    public int m_appdebug = 0;

    public String m_opaque = "";

    public String m_bgColor = "#00000000";

    public static int m_remove_loading = 1;//1,引擎关闭loading页；0，web调接口关闭loading页

    public static final Parcelable.Creator<WWidgetData> CREATOR = new Creator<WWidgetData>() {
        public WWidgetData createFromParcel(Parcel source) {
            WWidgetData widget = new WWidgetData();
            widget.m_id = source.readInt();
            widget.m_widgetOneId = source.readString();
            widget.m_widgetId = source.readString();
            widget.m_appId = source.readString();
            widget.m_ver = source.readString();
            widget.m_channelCode = source.readString();
            widget.m_imei = source.readString();
            widget.m_md5Code = source.readString();
            widget.m_widgetName = source.readString();
            widget.m_description = source.readString();
            widget.m_email = source.readString();
            widget.m_author = source.readString();
            widget.m_license = source.readString();
            widget.m_iconPath = source.readString();
            widget.m_widgetPath = source.readString();
            widget.m_indexUrl = source.readString();
            widget.m_obfuscation = source.readInt();
            widget.m_logServerIp = source.readString();
            widget.m_wgtType = source.readInt();
            widget.m_updateurl = source.readString();
            widget.m_spaceStatus = source.readInt();
            widget.m_orientation = source.readInt();
            widget.m_widgetAdStatus = source.readInt();
            widget.m_webapp = source.readInt();
            widget.m_opaque = source.readString();
            widget.m_bgColor = source.readString();
            widget.m_appkey = source.readString();
            if (widget.disablePluginsList != null) {
                source.readStringList(widget.disablePluginsList);
            }
            if (widget.disableRootWindowsList != null) {
                source.readStringList(widget.disableRootWindowsList);
            }
            if (widget.disableSonWindowsList != null) {
                source.readStringList(widget.disableSonWindowsList);
            }
            return widget;
        }

        public WWidgetData[] newArray(int size) {
            return new WWidgetData[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public String getWidgetPath() {

        return m_widgetPath;
    }

    public boolean getOpaque() {
        return Boolean.valueOf(m_opaque);
    }

    public int getSpaceStatus() {
        if ((m_spaceStatus & F_SPACESTATUS_OPEN) == F_SPACESTATUS_OPEN) {
            return F_SPACESTATUS_OPEN;
        } else {
            return F_SPACESTATUS_CLOSE;
        }
    }

    public int getSpaceMoreAppStatus() {
        if ((m_spaceStatus & F_MYSPACEMOREAPP_OPEN) == F_MYSPACEMOREAPP_OPEN) {
            return F_MYSPACEMOREAPP_OPEN;
        } else {
            return F_MYSPACEMOREAPP_CLOSE;
        }
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(m_id);
        parcel.writeString(m_widgetOneId);
        parcel.writeString(m_widgetId);
        parcel.writeString(m_appId);
        parcel.writeString(m_ver);
        parcel.writeString(m_channelCode);
        parcel.writeString(m_imei);
        parcel.writeString(m_md5Code);
        parcel.writeString(m_widgetName);
        parcel.writeString(m_description);
        parcel.writeString(m_email);
        parcel.writeString(m_author);
        parcel.writeString(m_license);
        parcel.writeString(m_iconPath);
        parcel.writeString(m_widgetPath);
        parcel.writeString(m_indexUrl);
        parcel.writeInt(m_obfuscation);
        parcel.writeString(m_logServerIp);
        parcel.writeInt(m_wgtType);
        parcel.writeString(m_updateurl);
        parcel.writeInt(m_spaceStatus);
        parcel.writeInt(m_orientation);
        parcel.writeInt(m_widgetAdStatus);
        parcel.writeInt(m_webapp);
        parcel.writeString(m_opaque);
        parcel.writeString(m_bgColor);
        parcel.writeString(m_appkey);
        parcel.writeStringList(disablePluginsList);
        parcel.writeStringList(disableRootWindowsList);
        parcel.writeStringList(disableSonWindowsList);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("widgetInfo: ");
        sb.append("\n");
        sb.append("m_id: " + m_id);
        sb.append("\n");
        sb.append("m_widgetOneId: " + m_widgetOneId);
        sb.append("\n");
        sb.append("m_widgetId: " + m_widgetId);
        sb.append("\n");
        sb.append("m_appId: " + m_appId);
        sb.append("\n");
        sb.append("m_ver: " + m_ver);
        sb.append("\n");
        sb.append("m_channelCode: " + m_channelCode);
        sb.append("\n");
        sb.append("m_imei: " + m_imei);
        sb.append("\n");
        sb.append("m_md5Code: " + m_md5Code);
        sb.append("\n");
        sb.append("m_widgetName: " + m_widgetName);
        sb.append("\n");
        sb.append("m_description: " + m_description);
        sb.append("\n");
        sb.append("m_email: " + m_email);
        sb.append("\n");
        sb.append("m_author: " + m_author);
        sb.append("\n");
        sb.append("m_license: " + m_license);
        sb.append("\n");
        sb.append("m_iconPath: " + m_iconPath);
        sb.append("\n");
        sb.append("m_widgetPath: " + m_widgetPath);
        sb.append("\n");
        sb.append("m_indexUrl: " + m_indexUrl);
        sb.append("\n");
        sb.append("m_obfuscation: " + m_obfuscation);
        sb.append("\n");
        sb.append("m_opaque: " + m_opaque);
        sb.append("\n");
        sb.append("m_bgColor: " + m_bgColor);
        sb.append("\n");
        sb.append("m_logServerIp: " + m_logServerIp);
        sb.append("\n");
        sb.append("m_wgtType: " + m_wgtType);
        sb.append("\n");
        sb.append("m_updateurl: " + m_updateurl);
        sb.append("\n");
        sb.append("m_spaceStatus: " + m_spaceStatus);
        sb.append("\n");
        sb.append("m_orientation: " + m_orientation);
        sb.append("\n");
        sb.append("m_widgetAdStatus: " + m_widgetAdStatus);
        sb.append("\n");
        sb.append("m_webapp: " + m_webapp);
        sb.append("\n");
        sb.append("m_appkey: " + m_appkey);
        sb.append("\n");
        sb.append("m_id: " + m_id);
        sb.append("\n");
        sb.append("m_id: " + m_id);
        sb.append("\n");
        sb.append("m_remove_loading:" + m_remove_loading);
        sb.append("\n");
        return sb.toString();
    }


}
