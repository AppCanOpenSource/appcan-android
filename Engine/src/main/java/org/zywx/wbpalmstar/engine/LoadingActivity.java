package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.zywx.wbpalmstar.base.util.ConfigXmlUtil;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.lang.ref.WeakReference;


/**
 * Created by yanlongtao on 2015/4/21 0021.
 *
 * LoadingActivity 里面不要做AppCan 配置相关的操作，
 *
 * 原因：历史问题，推送，暴露给三方的入口等都是EBrowserActivity
 *
 */

public class LoadingActivity extends Activity {

    public static final String FINISH_BROADCAST_ACTION = "com.appcan.close";

    public static final String KEY_INTENT_ROOT_PAGE_DATA = "root_page_data";

    private static final int MSG_GET_WIDGET_DATA=100;

    private FrameLayout mRootLayout;
    private BroadcastReceiver mBroadcastReceiver;

    private Handler mHandler;

    private boolean isTemp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler=new LoadingHandler(this);
        handleIntent();
        initRootView();
        ConfigXmlUtil.setStatusBarColorWithAddView(this, Color.TRANSPARENT);
        setContentView(mRootLayout);
        registerFinishReceiver();
        addDevelopInfo();
        hideMenu();
        if (!isTemp) {
            startEngine();
        }
    }

    private void handleIntent() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                isTemp = intent.getBooleanExtra("isTemp", false);
            }
        } catch (Exception exception) {
        }
    }

    private void initRootView() {
        mRootLayout = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mRootLayout.setLayoutParams(layoutParams);
    }

    private void addDevelopInfo() {
        if (EBrowserActivity.develop) {
            TextView worn = new TextView(this);
            worn.setText(getResources().getIdentifier("platform_only_test","string",getPackageName()));
            worn.setTextColor(0xffff0000);
            worn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            FrameLayout.LayoutParams wornPa = new FrameLayout.LayoutParams(
                    Compat.FILL, Compat.WRAP);
            wornPa.gravity = Gravity.TOP;
            wornPa.leftMargin = 10;
            wornPa.topMargin = 60;
            worn.setLayoutParams(wornPa);
            mRootLayout.addView(worn);
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mBroadcastReceiver);
    }

    private class FinishSelfReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
            overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_fade_in_anim")
                    , EUExUtil.getResAnimID("platform_myspace_fade_out_anim"));
        }
    }

    private void registerFinishReceiver() {
        mBroadcastReceiver = new FinishSelfReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FINISH_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, intentFilter);
    }

    /**
     * 解决三星S6显示空菜单问题
     */
    private void hideMenu() {
        try {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.class.getField(
                            "FLAG_NEEDS_MENU_KEY").getInt(null));
        } catch (Exception e) {
        }
    }

    private void startEngine() {
        AppCan.getInstance().start(LoadingActivity.this,AppCan.getInstance().getRootWidgetData(),getIntent().getExtras());
    }

    private static class LoadingHandler extends Handler{

        private final WeakReference<LoadingActivity> mActivity;

        LoadingHandler(LoadingActivity activity){
            mActivity=new WeakReference<LoadingActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_GET_WIDGET_DATA:

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
