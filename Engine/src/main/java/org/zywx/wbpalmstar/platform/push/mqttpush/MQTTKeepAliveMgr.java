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

import java.util.LinkedList;

public class MQTTKeepAliveMgr implements HeartKeepAliveMgr{
    
    /** *心跳步长 */
    private static short heartStep = 10;
    private LinkedList<HeartInfo> heartInfoList = new LinkedList<HeartInfo>();
    
    public MQTTKeepAliveMgr()
    {
        heartInfoList.add(new HeartInfo());
    }
    
    @Override
    public short calcHeartSucceed(final short curHeart)
    {
        short succeedHeart = HEART_MIN;
        if(isKeepAliveStable())
        {
            succeedHeart = heartInfoList.getFirst().keepAlive;
        }
        else
        {
            if(curHeart < HEART_MAX)
            {
                if(HEART_SUCCEED.equals(heartInfoList.getLast().pingRet))
                {
                    if(heartStep < hraetStepMax)
                    {
                        heartStep += heartStepStep;
                    }
                    succeedHeart = (short)Math.min(
                            (curHeart + heartStep), HEART_MAX);
                }
                else
                {
                    succeedHeart = (short) (curHeart + heartStep);
                    heartStep = 0;
                }
            }
            else
            {
                succeedHeart = curHeart;
            }
        }

        configHeartInfoList(HEART_SUCCEED, succeedHeart);
        return succeedHeart;
    }
    
    @Override
    public short calcHeartFailed(final short curHeart)
    {
        short failHeart = HEART_MIN;
        if(isKeepAliveStable())
        {
            failHeart = heartInfoList.getFirst().keepAlive;
        }
        else
        {
            if(curHeart > HEART_MIN)
            {
                if(HEART_FAILED.equals(heartInfoList.getLast().pingRet))
                {
                    if(heartStep < hraetStepMax)
                    {
                        heartStep += heartStepStep;
                    }
                    failHeart = (short)Math.max(
                            (curHeart - heartStep), HEART_MIN);
                }
                else
                {
                    failHeart = (short) (curHeart - heartStep);
                    heartStep = 0;
                }
            }
            else
            {
                failHeart = curHeart;
            }
        }
        
        configHeartInfoList(HEART_FAILED, failHeart);
        
        return failHeart;
    }
    
    private void configHeartInfoList(String ret, short keepAlive)
    {
        HeartInfo heartInfo = new HeartInfo();
        heartInfo.pingRet = ret;
        heartInfo.keepAlive = keepAlive;
        heartInfoList.addLast(heartInfo);
        if(heartInfoList.size() > 3)
        {
            heartInfoList.removeFirst();
        }
    }
    
    private boolean isKeepAliveStable()
    {
        return (heartInfoList.size() == 3
                && (heartInfoList.getFirst().keepAlive
                == heartInfoList.getLast().keepAlive));
    }
}
