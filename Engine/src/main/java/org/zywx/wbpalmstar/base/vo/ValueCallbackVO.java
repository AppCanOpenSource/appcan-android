package org.zywx.wbpalmstar.base.vo;

import android.net.Uri;

import org.zywx.wbpalmstar.base.WebViewSdkCompat;

/**
 * File Description: WebView选择文件的回调相关
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 1/14/21.
 */
public class ValueCallbackVO {

    private WebViewSdkCompat.ValueCallback<Uri> valueCallback;
    private WebViewSdkCompat.ValueCallback<Uri[]> valueCallbackForApi21;
    private String cameraImgSaveUrl;

    public ValueCallbackVO() {
    }

    public ValueCallbackVO(WebViewSdkCompat.ValueCallback<Uri[]> valueCallbackForApi21) {
        this.valueCallbackForApi21 = valueCallbackForApi21;
    }

    public ValueCallbackVO(WebViewSdkCompat.ValueCallback<Uri[]> valueCallbackForApi21, String cameraImgSaveUrl) {
        this.valueCallbackForApi21 = valueCallbackForApi21;
        this.cameraImgSaveUrl = cameraImgSaveUrl;
    }

    public WebViewSdkCompat.ValueCallback<Uri> getValueCallback() {
        return valueCallback;
    }

    public void setValueCallback(WebViewSdkCompat.ValueCallback<Uri> valueCallback) {
        this.valueCallback = valueCallback;
    }

    public WebViewSdkCompat.ValueCallback<Uri[]> getValueCallbackForApi21() {
        return valueCallbackForApi21;
    }

    public void setValueCallbackForApi21(WebViewSdkCompat.ValueCallback<Uri[]> valueCallbackForApi21) {
        this.valueCallbackForApi21 = valueCallbackForApi21;
    }

    public String getCameraImgSaveUrl() {
        return cameraImgSaveUrl;
    }

    public void setCameraImgSaveUrl(String cameraImgSaveUrl) {
        this.cameraImgSaveUrl = cameraImgSaveUrl;
    }
}
