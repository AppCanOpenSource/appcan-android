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
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.util.Locale;

public class EResources {

    public static int startup_bg_16_9;
    public static int startup_bg_3_2;
    /*public static int startup_fg_small;
    public static int startup_fg_normal;
    public static int startup_fg_large;
    public static int startup_fg_xlarge;
    public static int mark_bg;*/
    public static int icon;
    public static int platform_myspace_pulltorefresh_arrow;
    public static int browser_init_error;

    public static int browser_exitdialog_msg;
    public static int cancel;
    public static int browser_exitdialog_app_text;
    public static int confirm;

    public static Drawable windowBg;

    private static final String color = "color";
    private static final String drawable = "drawable";
    private static final String string = "string";

    public static boolean init(Context context) {
        String packg = context.getPackageName();
        Resources res = context.getResources();
        startup_bg_16_9 = res.getIdentifier("startup_bg_16_9", drawable, packg);
        startup_bg_3_2 = res.getIdentifier("startup_bg_3_2", drawable, packg);
        icon = res.getIdentifier("icon", drawable, packg);
        platform_myspace_pulltorefresh_arrow = res.getIdentifier("platform_myspace_pulltorefresh_arrow", drawable, packg);
        browser_init_error = res.getIdentifier("browser_init_error", string, packg);
        browser_exitdialog_msg = res.getIdentifier("browser_exitdialog_msg", string, packg);
        cancel = res.getIdentifier("cancel", string, packg);
        browser_exitdialog_app_text = res.getIdentifier("browser_exitdialog_app_text", string, packg);
        confirm = res.getIdentifier("confirm", string, packg);
        String release = Build.VERSION.RELEASE;
        windowBg = new ColorDrawable(0x00000000);
        if ("4.0.4".equals(release)) {
            windowBg = new ColorDrawable(0xFFFFFFFF);
        }
        if (startup_bg_16_9 == 0 ||
                startup_bg_3_2 == 0 ||
                icon == 0
                || platform_myspace_pulltorefresh_arrow == 0
                || browser_init_error == 0

                || browser_exitdialog_msg == 0
                || cancel == 0
                || browser_exitdialog_app_text == 0
                || confirm == 0) {
            return false;
        }
        return true;
    }
}
