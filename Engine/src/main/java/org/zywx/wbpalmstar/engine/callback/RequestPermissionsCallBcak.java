package org.zywx.wbpalmstar.engine.callback;

/**
 * Created by zhang on 2018/12/19.
 */

public interface RequestPermissionsCallBcak<T extends Object> {
    /**
     * 权限申请成功回调
     */
    void requestPermissionsSucesss(int requestCode);

    /**
     * 权限申请失败回调
     */
    void requestPermissionfailure(int errorCode, T requestCode);
}
