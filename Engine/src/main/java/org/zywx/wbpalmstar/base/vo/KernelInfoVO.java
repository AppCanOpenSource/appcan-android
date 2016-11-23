package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

public class KernelInfoVO implements Serializable {

    private String kernelType;  //WebView内核类型
    private String kernelVersion;  //WebView内核版本

    public String getKernelType() {
        return kernelType;
    }

    public void setKernelType(String type) {
        this.kernelType = type;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String version) {
        this.kernelVersion = version;
    }
}
