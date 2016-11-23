package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

public class PushDeviceBindVO implements Serializable {

    private String deviceName;  //设备名字
    private String deviceVersion;  //设备版本
    private String deviceType;  //android或者ios
    private String deviceOwner;
    private String deviceToken;  //IOS标识
    private String softToken;    //Andorid标识
    private PushDeviceBindUserVO user;  //用户
    private String clientId;  //针对android设备连接标识
    private String channelId;  //渠道id
    private String versionId;  //版本id
    private boolean valid;  //设备可用标示（true/false）
    private String timeZone;  //时区
    private String org;  //部门
    private String tags;  //标签下的设备

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceOwner() {
        return deviceOwner;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getSoftToken() {
        return softToken;
    }

    public PushDeviceBindUserVO getUser() {
        return user;
    }

    public String getClientId() {
        return clientId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public boolean isValid() {
        return valid;
    }

    public String getTags() {
        return tags;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceOwner(String deviceOwner) {
        this.deviceOwner = deviceOwner;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setSoftToken(String softToken) {
        this.softToken = softToken;
    }

    public void setUser(PushDeviceBindUserVO user) {
        this.user = user;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
