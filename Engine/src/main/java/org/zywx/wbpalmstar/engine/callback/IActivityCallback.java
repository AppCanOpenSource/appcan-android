package org.zywx.wbpalmstar.engine.callback;

import android.content.Intent;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

/**
 * File Description: 与Activity的交互
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 1/15/21.
 */
public interface IActivityCallback {

    @Keep
    public void onActivityResult(int requestCode, int resultCode, Intent data);

    @Keep
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

}
