package org.zywx.wbpalmstar.base.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * 记录Activity的生命周期变化，从而判断App整体的前后台状态
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2018/9/17.
 */
public class ActivityActionRecorder {

    public interface AppBackgroundStatusListener{

        void onEnterBackground();

        void onEnterForground();
    }

    private static ActivityActionRecorder sActivityActionRecorder;
    private int mActiveActivityCount = 0;
    private AppBackgroundStatusListener mAppBackgroundStatusListener;

    public static ActivityActionRecorder getInstance() {
        if (sActivityActionRecorder == null){
            sActivityActionRecorder = new ActivityActionRecorder();
        }
        return sActivityActionRecorder;
    }

    /**
     * 归零
     */
    private void clearCount(){
        mActiveActivityCount = 0;
    }

    public void registerTriggerListener(AppBackgroundStatusListener listener){
        mAppBackgroundStatusListener = listener;
    }

    /**
     * 必须在Application的onCreate调用，即所有的Activity都未启动之前这个时机，否则计数将不准确
     *
     * @param application
     */
    public void initRecorder(Application application){
        clearCount();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (mAppBackgroundStatusListener!=null
                        && isAllActivityInBackground()){
                    //计数增加之前，如果都是后台状态，说明这一次是从后台到了前台，需要回调。
                    mAppBackgroundStatusListener.onEnterForground();
                }
                mActiveActivityCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActiveActivityCount--;
                if (mAppBackgroundStatusListener!=null
                        && isAllActivityInBackground()){
                    mAppBackgroundStatusListener.onEnterBackground();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public boolean isAllActivityInBackground(){
        return mActiveActivityCount == 0;
    }

}
