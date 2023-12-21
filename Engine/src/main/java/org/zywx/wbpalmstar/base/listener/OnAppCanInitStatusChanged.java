package org.zywx.wbpalmstar.base.listener;

/**
 * File Description: 初始化状态变更监听（用于控制初始化过程中的暂停和继续等事件）
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 1/26/21.
 */
public interface OnAppCanInitStatusChanged {

    class STATUS {
        public final static String CONTINUE = "continue";
        public final static String EXIT = "exit";
    }

    /**
     * 状态变更
     */
    void onReceivedStatus(String status);

}
