package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

public class DownloadCallbackInfoVO implements Serializable {

    private String url;  //下载文件的url
    private String userAgent;  //请求下载时的UserAgent标识
    private String contentDisposition;  //包含文件名信息
    private String mimetype;  //文件的媒体类型
    private long contentLength;  //传输长度
    private String windowName;  // 返回非主窗口的窗口名，若发生在主窗口，此字段为空，仅在回调给主窗口时返回，即在downloadCallback为1时

    public String getUrl() {
        return url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public String getMimetype() {
        return mimetype;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getWindowName() {
        return windowName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void setWindowName(String windowName) {
        this.windowName = windowName;
    }
}
