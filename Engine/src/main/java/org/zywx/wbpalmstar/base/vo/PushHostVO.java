package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

public class PushHostVO implements Serializable {

    private String pushHost; //链接推送服务器的host地址
    private String bindUserHost; //绑定解绑的host地址
    private int status; //0设置成功;1设置失败

    public void setPushHost(String pushHost) {
        this.pushHost = pushHost;
    }

    public void setBindUserHost(String bindUserHost) {
        this.bindUserHost = bindUserHost;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPushHost() {
        return pushHost;
    }

    public String getBindUserHost() {
        return bindUserHost;
    }

    public int getStatus() {
        return status;
    }
}
