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


    public static final String display_back;
    public static final String display_confirm;
    public static final String display_cancel;
    public static final String display_prompt;
    public static final String display_exitdialog_app_text;
    public static final String display_exitdialog_msg;
    public static final String display_dialog_error;
    public static final String display_init_error;

    public static final String display_network_error;
    public static final String display_network_msg;
    public static final String display_error_exit;
    public static final String display_error_contiue;

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

    static {
        Locale language = Locale.getDefault();
        if (language.equals(Locale.CHINA)
                || language.equals(Locale.CHINESE)
                || language.equals(Locale.TAIWAN)
                || language.equals(Locale.TRADITIONAL_CHINESE)
                || language.equals(Locale.SIMPLIFIED_CHINESE)
                || language.equals(Locale.PRC)) {

            display_back = "返回";
            display_confirm = "确定";
            display_cancel = "取消";
            display_prompt = "提示";
            display_exitdialog_app_text = "确定要退出程序吗？";
            display_exitdialog_msg = "退出提示";
            display_dialog_error = "错误提示";
            display_init_error = "程序不完整，缺少必须的资源";

            display_network_error = "无可用网络";
            display_network_msg = "您的应用只能访问离线资源";
            display_error_exit = "退出应用";
            display_error_contiue = "进入应用";
        } else {
            display_back = "Back";
            display_confirm = "Ok";
            display_cancel = "Cancel";
            display_prompt = "Prompt";
            display_exitdialog_app_text = "exit application?";
            display_exitdialog_msg = "Exit Application";
            display_dialog_error = "Error";
            display_init_error = "Application broken!";

            display_network_error = "network error";
            display_network_msg = "Your Application Can Only Access The Offline Resources!";
            display_error_exit = "Exit";
            display_error_contiue = "Continue";
        }
    }
}
