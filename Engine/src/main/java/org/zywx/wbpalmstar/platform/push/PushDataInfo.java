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

package org.zywx.wbpalmstar.platform.push;

import java.io.Serializable;

public class PushDataInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String pushDataString;
    private int contentAvailable; //静默方式;透传（静默）：1，通知：0
    private String appId;
    private String taskId;
    private String title;
    private String alert;
    private int badge;
    private String[] remindType; //声音：sound，震动：shake，呼吸灯：breathe 
    private String iconUrl;
    private String fontColor;
    private String behavior;
    private String tenantId;

    public PushDataInfo() {
    }

    public int getContentAvailable() {
        return contentAvailable;
    }

    public void setContentAvailable(int contentAvailable) {
        this.contentAvailable = contentAvailable;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getPushDataString() {
        return pushDataString;
    }

    public void setPushDataString(String pushDataString) {
        this.pushDataString = pushDataString;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public String[] getRemindType() {
        return remindType;
    }

    public void setRemindType(String[] remindType) {
        this.remindType = remindType;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}