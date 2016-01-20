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

public class EViewEntry {

    public final static String F_PLUGIN_VIEW_TAG = "plugin";

    public final static int F_ADD_LOCATION_TOP = 0;
    public final static int F_ADD_LOCATION_MID = 1;
    public final static int F_ADD_LOCATION_BOTTOM = 2;

    public static final int F_BOUNCE_TYPE_TOP = 0;
    public static final int F_BOUNCE_TYPE_BOTTOM = 1;

    public static final int F_BOUNCE_TASK_SHOW_BOUNCE_VIEW = 0;
    public static final int F_BOUNCE_TASK_HIDDEN_BOUNCE_VIEW = 1;
    public static final int F_BOUNCE_TASK_RESET_BOUNCE_VIEW = 2;
    public static final int F_BOUNCE_TASK_SET_BOUNCE_VIEW = 3;
    public static final int F_BOUNCE_TASK_NOTIFY_BOUNCE_VIEW = 4;
    public static final int F_BOUNCE_TASK_SET_BOUNCE_PARMS = 5;
    public static final int F_BOUNCE_TASK_GET_BOUNCE_VIEW = 6;
    public static final int F_BOUNCE_TASK_TOP_BOUNCE_VIEW_REFRESH = 7;

    public long time;
    public long interval;
    public int type;
    public int width;
    public int height;
    public int x;
    public int y;
    public int flag;
    public int location;
    public int duration;
    public int color;
    public String url;
    public String msg;
    public Object obj;
    public Object obj1;
    public String arg1;
    public String arg2;
    public boolean bArg1;
    public boolean bArg2;

    //addview use
    public EViewEntry(int inType, String inUrl, int inDTime, int inHeight, int inWidth,
                      int inInterval, int inFlag) {
        type = inType;
        url = inUrl;
        time = inDTime;
        height = inHeight;
        width = inWidth;
        interval = inInterval;
        flag = inFlag;
    }

    //toast use
    public EViewEntry(int inType, int inLocation, String inMsg,
                      int inDuration) {
        type = inType;
        location = inLocation;
        duration = inDuration;
        msg = inMsg;
    }

    public EViewEntry() {

    }

    public boolean checkFlag(int inFlag) {
        return ((flag & inFlag) != 0) ? true : false;
    }
}
