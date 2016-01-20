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

public class EWgtResultInfo {

    /**
     * widget结束后回调上一个widget的js函数
     */
    private String m_callBack;

    private String m_resultInfo;
    /**
     * 上一个widget传入本widget的内容
     */
    private String m_openerInfo;
    private String m_finishInfo;
    /**
     * 打开本widget时的动画ID
     */
    private int m_animiId;
    /**
     * 打开本widget时的动画持续时间
     */
    private long m_duration;

    public EWgtResultInfo(String inCallBack, String inOpenerInfo) {
        m_callBack = inCallBack;
        m_openerInfo = inOpenerInfo;
    }

    public void setResultInfo(String inResult) {
        m_resultInfo = inResult;
    }

    public String getResultInfo() {
        return m_resultInfo;
    }

    public void setCallBack(String inCallBack) {
        m_callBack = inCallBack;
    }

    public String getCallBack() {
        return m_callBack;
    }

    public void setOpenerInfo(String inOpener) {
        m_openerInfo = inOpener;
    }

    public String getOpener() {

        return m_openerInfo;
    }

    public void setFinish(String inInfo) {
        m_finishInfo = inInfo;
    }

    public String getFinish() {

        return m_finishInfo;
    }

    public void setAnimiId(int inAnimiId) {
        m_animiId = inAnimiId;
    }

    public int getAnimiId() {

        return m_animiId;
    }

    public int getContraryAnimiId() {

        return EBrowserAnimation.contrary(m_animiId);
    }

    public long getDuration() {
        return m_duration;
    }

    public void setDuration(long duration) {
        m_duration = duration;
    }
}