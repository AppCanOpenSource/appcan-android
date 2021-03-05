package org.zywx.wbpalmstar.engine.universalex;

import android.content.Intent;
import android.support.annotation.Keep;

import org.zywx.wbpalmstar.engine.callback.IActivityCallback;

/**
 * File Description: 定义了插件初始化的Context所必须的方法功能。
 *                   引擎中实现此接口的是EBrowserActivity
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 3/5/21.
 */
@Keep
public interface IACEContext {

    void registerAppEventListener(EUExEventListener listener);

    void unRegisterAppEventListener(EUExEventListener listener);

    void registerActivityForResult(IActivityCallback callback);

    void startActivityForResult(IActivityCallback callback, Intent intent,
                                int requestCode);

    void acRequestPermissionsMore(final String[] permissions, IActivityCallback callback, String message, final int requestCode);

    void acRequestPermissions(final String permission, IActivityCallback callback, String message, final int requestCode);

    void exitBrowser();
}
