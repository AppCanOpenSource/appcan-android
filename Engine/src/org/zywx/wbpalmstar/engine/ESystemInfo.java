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

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.webkit.WebSettings.ZoomDensity;

import org.zywx.wbpalmstar.base.BDebug;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;

public class ESystemInfo {

    public int mHeightPixels;
    public int mViewHeight;
    public int mWidthPixels;
    public float mXdpi;
    public float mYdpi;
    public float mDensity;
    public int mDensityDpi;
    public float mScaledDensity;
    public int mStatusBarHeight;
    public int mSysVersion;
    public int mPhoneType;
    public boolean mIsDevelop;
    public DisplayMetrics mDisplayMetrics;
    public int cpuMHZ;
    public int mDefaultFontSize;
    public int mDefaultBounceHeight;
    public int mDefaultNatvieFontSize;
    public ZoomDensity mDefaultzoom;
    public boolean mScaled;
    public boolean mFinished;
    public int mSwipeRate = 1000;
    private static ESystemInfo entance;

    private ESystemInfo() {
        ;
    }

    public void init(Context context) {
        mScaled = false;
        mFinished = false;
        DisplayMetrics dispm = context.getResources().getDisplayMetrics();
        mDisplayMetrics = dispm;
        mHeightPixels = dispm.heightPixels;
        mWidthPixels = dispm.widthPixels;
        mXdpi = dispm.xdpi;
        mYdpi = dispm.ydpi;
        mDensity = dispm.density;
        mDensityDpi = dispm.densityDpi;
        mScaledDensity = dispm.scaledDensity;
        mSysVersion = Build.VERSION.SDK_INT;
        mPhoneType = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType();
        mStatusBarHeight = getStateBarHeight(context);
        mViewHeight = mHeightPixels - mStatusBarHeight;
        cpuMHZ = getCPUFrequency();
        mIsDevelop = EBrowserActivity.develop;
        Build bd = new Build();
        String model = bd.MODEL;
        if ("KBMC-709plus".equals(model) && mDensityDpi < 240) {
            //for aibeibei adapt in pad "KBMC-709plus"
            mDensityDpi = 240;
        }
        switch (mDensityDpi) {
            case DisplayMetrics.DENSITY_LOW: {
                mDefaultFontSize = 14;
                mDefaultNatvieFontSize = 10;
                mDefaultzoom = ZoomDensity.CLOSE;
                mDefaultBounceHeight = 40;
            }
            break;
            case DisplayMetrics.DENSITY_MEDIUM: {
                mDefaultFontSize = 16;
                mDefaultNatvieFontSize = 13;
                mDefaultzoom = ZoomDensity.MEDIUM;
                mDefaultBounceHeight = 50;
            }
            break;
            case DisplayMetrics.DENSITY_HIGH: {
                mDefaultFontSize = 24;
                mDefaultNatvieFontSize = 16;
                mDefaultzoom = ZoomDensity.FAR;
                mDefaultBounceHeight = 60;
            }
            break;
            case 213: //DisplayMetrics.DENSITY_TV from 13
            {
                mDefaultFontSize = 32;
                mDefaultNatvieFontSize = 16;
                mDefaultzoom = ZoomDensity.FAR;
                mDefaultBounceHeight = 70;
            }
            break;
            case 320: //DisplayMetrics.DENSITY_XHIGH from 9
            {
                mDefaultFontSize = 32;
                mDefaultNatvieFontSize = 16;
                mDefaultzoom = ZoomDensity.FAR;
                mDefaultBounceHeight = 76;
            }
            break;
            case 480: //DisplayMetrics.DENSITY_XXHIGH from 16
            {
                mDefaultFontSize = 48;
                mDefaultNatvieFontSize = 17;
                mDefaultzoom = ZoomDensity.FAR;
                mDefaultBounceHeight = 112;
            }
            break;
            case 640: {
                mDefaultFontSize = 64;
                mDefaultNatvieFontSize = 17;
                mDefaultzoom = ZoomDensity.FAR;
                mDefaultBounceHeight = 150;
            }
            break;
            default: {
                mDefaultFontSize = 48;
                mDefaultNatvieFontSize = 17;
                mDefaultzoom = ZoomDensity.FAR;
                mDefaultBounceHeight = 105;

                if (mDensity > 3) { //适配更高密度设备

                    mDefaultFontSize = (int) (16 * mDensity);

                }
            }

            break;
        }
    }

    public static ESystemInfo getIntence() {
        if (null == entance) {
            entance = new ESystemInfo();
        }
        return entance;
    }

    private int getStateBarHeight(Context context) {
        Class<?> classl;
        Object dimen;
        Field field;
        int dimenH = 0, height = 0;
        try {
            classl = Class.forName("com.android.internal.R$dimen");
            dimen = classl.newInstance();
            field = classl.getField("status_bar_height");
            dimenH = Integer.parseInt(field.get(dimen).toString());
            height = context.getResources().getDimensionPixelSize(dimenH);
        } catch (Exception e) {
            ;
        }
        return height;
    }

    /**
     * 获得设备CPU的频率
     *
     * @return
     */
    private int getCPUFrequency() {
        int mhz = 0;
        LineNumberReader isr = null;
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
            isr = new LineNumberReader(new InputStreamReader(pp.getInputStream()));
            String line = isr.readLine();
            if (line != null && line.length() > 0) {
                try {
                    mhz = Integer.parseInt(line.trim()) / 1000;
                } catch (Exception e) {
                    BDebug.e("SystemInfo", "EUExDeviceInfo---getCPUFrequency()---NumberFormatException ");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mhz;
    }
}
