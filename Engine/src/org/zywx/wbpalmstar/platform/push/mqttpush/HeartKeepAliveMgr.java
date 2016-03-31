/*
 *  Copyright (C) 2016 The AppCan Open Source Project.
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

package org.zywx.wbpalmstar.platform.push.mqttpush;

public interface HeartKeepAliveMgr {
    public final static String HEART_SUCCEED = "heartSucceed";
    public final static String HEART_FAILED = "heartFailed";
    public final static short HEART_MAX = 300;
    public final static short HEART_MIN = 10;
    public final static short hraetStepMax = 40;
    /** *心跳步长增减的步长（心跳增减的步长是动态调整的） */
    public final static short heartStepStep = 10;
    
    public short calcHeartSucceed(final short curHeart);
    public short calcHeartFailed(final short curHeart);
    

    public static class HeartInfo
    {
        String pingRet = HEART_FAILED;
        short keepAlive = HEART_MIN;
    }
}
