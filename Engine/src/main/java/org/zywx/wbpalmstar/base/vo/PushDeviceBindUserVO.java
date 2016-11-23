package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

public class PushDeviceBindUserVO implements Serializable {

    private String userId;  //用户ID
    private String username;  //用户名
    private String tags;
    private String sessionStatus;  //0/1

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getTags() {
        return tags;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
}
