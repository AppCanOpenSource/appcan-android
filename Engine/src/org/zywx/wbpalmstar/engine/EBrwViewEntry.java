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


public class EBrwViewEntry {
    public static final int VIEW_TYPE_MAIN = 0;
    public static final int VIEW_TYPE_TOP = 1;
    public static final int VIEW_TYPE_BOTTOM = 2;
    public static final int VIEW_TYPE_POP = 3;
    public static final int VIEW_TYPE_ADD = 4;

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


    public int mType;

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


    public EBrwViewEntry(int inType) {
        mType = inType;
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
                "," + mRelativeUrl + "," + mAnimDuration + "," + mObj;
    }

}

