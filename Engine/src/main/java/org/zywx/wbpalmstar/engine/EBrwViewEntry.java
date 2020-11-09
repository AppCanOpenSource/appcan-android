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


import org.zywx.wbpalmstar.base.vo.WindowOptionsVO;

public class EBrwViewEntry {

    public static final int VIEW_TYPE_ROOT = -1;//标识此窗口为本widget中的root窗口（首页）
    public static final int VIEW_TYPE_MAIN = 0;
    public static final int VIEW_TYPE_TOP = 1;
    public static final int VIEW_TYPE_BOTTOM = 2;
    public static final int VIEW_TYPE_POP = 3;
    public static final int VIEW_TYPE_ADD = 4;

    public static final int WINDOW_SYTLE_NORMAL = 0;//默认值，标准样式
    public static final int WINDOW_SYTLE_MEDIA_PLATFORM = 1; //仿微信公众号样式

    public static final int WINDOW_DATA_TYPE_URL = 0;
    public static final int WINDOW_DATA_TYPE_DATA = 1;
    public static final int WINDOW_DATA_TYPE_DATA_URL = 2;

    public static final int F_FLAG_NORMAL = 0x0;
    public static final int F_FLAG_OAUTH = 0x1;
    public static final int F_FLAG_OBFUSCATION = 0x2;
    public static final int F_FLAG_RElOAD = 0x4;
    public static final int F_FLAG_SHOULD_OP_SYS = 0x8;
    public static final int F_FLAG_OPAQUE = 0x10;
    public static final int F_FLAG_HIDDEN = 0x20;
    public static final int F_FLAG_PREOP = 0x40;
    public static final int F_FLAG_GESTURE = 0x80;
    public static final int F_FLAG_NOT_HIDDEN = 0x100;
    public static final int F_FLAG_WEBAPP = 0x200;
    public static final int F_FLAG_NAV_TYPE = 0x400;
    public static final String TAG_EXTRAINFO = "extraInfo";
    public static final String TAG_DELAYTIME = "delayTime";

    /**
     * 引擎默认模式（默认值）：忽略本地缓存，只走网络。
     */
    public static final int AC_LOAD_ENGINE_DEFAULT = 0;
    /**
     * WebView默认缓存模式：会优先考虑Cache-Control等头信息的配置。如果没有指定配置，则优先检查本地有效的缓存，不存在有效缓存的话则走网络。
     */
    public static final int AC_LOAD_WEBVIEW_DEFAULT = 1;
    /**
     * 优先走本地缓存，没有缓存走网络。
     */
    public static final int AC_LOAD_CACHE_ELSE_NETWORK = 2;
    /**
     * 忽略网络，只走缓存。
     */
    public static final int AC_LOAD_CACHE_ONLY = 3;


    public int mType;
    public int mWindowStyle;//窗口样式

    public int mX;
    public int mY;
    public int mBottom;
    public int mFontSize;
    public String mViewName;

    public int mWidth;
    public int mHeight;
    public int mDataType;
    public int mFlag;
    public int mAnimId;
    public String mUrl;
    public String mData;
    public String mWindName;
    public String mPreWindName;
    public String mQuery;
    public String mRelativeUrl;
    public boolean hasExtraInfo = false;
    public boolean mOpaque = false;
    public String mBgColor = "#00000000";
    public long mAnimDuration;
    public Object mObj;

    public int mHardware = -1;//硬件加速，-1不处理，0关闭，1开启
    public int mDownloadCallback = 0;// 0 下载不回调，使用引擎下载; 1 下载回调给主窗口，前端自己下载; 2 下载回调给当前窗口，前端自己下载;
    public String mUserAgent = "";

    public WindowOptionsVO mWindowOptions;
    public String mExeJS = "";//打开窗口时由前端传入想要注入的JS字符串，WebView加载完成的时候执行这段JS。
    public int mExeScale=-1; // setInitialScale 设置初始化的缩放值 TODO 使用场景不太明确，暂未覆盖全部接口。
    public int mCacheMode = AC_LOAD_ENGINE_DEFAULT; // 缓存模式，分为4种配置，传值为0 1 2 3，解释见后面文字。 0：AppCan默认模式：忽略本地缓存，只请求网络数据； 1：浏览器默认模式：根据服务端返回的配置执行； 2： 优先本地缓存，如果本地缓存无效，则请求网络； 3： 只使用本地缓存，不请求网络数据

    public EBrwViewEntry(int inType) {
        mType = inType;
    }

    public boolean isRootWindow(){
        return mType == VIEW_TYPE_ROOT;
    }

    public boolean checkData() {

        return (mData != null && mData.length() > 0);
    }

    public boolean checkUrl() {

        return (mUrl != null && mUrl.length() > 0);
    }

    public boolean checkFlag(int inFlag) {

        return ((mFlag & inFlag) != 0) ? true : false;
    }

    public boolean checkDataType(int type) {

        return mDataType == type;
    }

    public static boolean isUrl(int type) {

        return WINDOW_DATA_TYPE_URL == type;
    }

    public static boolean isData(int type) {

        return WINDOW_DATA_TYPE_DATA == type;
    }

    @Override
    public String toString() {
        return mType + "," + mX + "," + mY + "," + mFontSize + "," + mViewName + "," +
                mWidth + "," + mHeight + "," + mDataType + "," + mFlag + "," + mAnimId + "," +
                mUrl + "," + mData + "," + mWindName + "," + mPreWindName + "," + mQuery +
                "," + mRelativeUrl + "," + mAnimDuration + "," + mObj + "," + mWindowOptions;
    }

}

