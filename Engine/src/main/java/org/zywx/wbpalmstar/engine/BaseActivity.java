package org.zywx.wbpalmstar.engine;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

/**
 * Created by ylt on 16/7/22.
 */

public class BaseActivity extends FragmentActivity implements Handler.Callback{


    private Handler mHandler;

    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler=new Handler(this);
    }

    /**
     * 添加遮罩，等待webView加载完毕后关闭
     */
    protected void startMaskActivity() {
        Intent intent = new Intent(this, TempActivity.class);
        intent.putExtra("isTemp", true);
        startActivity(intent);
        overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_no_anim")
                , EUExUtil.getResAnimID("platform_myspace_no_anim"));
    }

    /**
     * 添加自定义遮罩，等待webView加载完毕后关闭
     */
    protected void startCustomMaskActivity(Intent intent) {
        if (intent.hasExtra(AppCan.INTENT_APPCAN_SDK_CUSTOM_MASK_CLASSNAME)){
            ComponentName componentName = new ComponentName(getPackageName(),
                    intent.getStringExtra(AppCan.INTENT_APPCAN_SDK_CUSTOM_MASK_CLASSNAME));
            intent.setComponent(componentName);
            intent.removeExtra(AppCan.INTENT_APPCAN_SDK_CUSTOM_MASK_CLASSNAME);
            startActivity(intent);
            overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_no_anim")
                    , EUExUtil.getResAnimID("platform_myspace_no_anim"));
        }
    }

    /**
     * 发送广播关闭遮罩Activity
     * @param delayTime 延时发送广播，单位毫秒
     */
    protected void sendFinishLoadingBroadcast(long delayTime) {
        BDebug.d("send broadcast delayTime: ",delayTime);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LocalBroadcastManager broadcastManager = LocalBroadcastManager
                        .getInstance(BaseActivity.this);
                Intent intent = new Intent(LoadingActivity.FINISH_BROADCAST_ACTION);
                broadcastManager.sendBroadcast(intent);
            }
        },delayTime);

    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    public void showToast(String text){
        if (mToast==null) {
            mToast=Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }else{
            mToast.setText(text);
        }
        mToast.show();
    }





}
