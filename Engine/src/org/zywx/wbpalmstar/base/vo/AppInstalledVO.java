package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

public class AppInstalledVO implements Serializable{
    private static final long serialVersionUID = 5678596521871255439L;
    private String appData;

    public String getAppData() {
        return appData;
    }

    public void setAppData(String appData) {
        this.appData = appData;
    }
}
