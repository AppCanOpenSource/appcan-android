package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;


/**
 * Created by yanlongtao on 2015/4/21 0021.
 */

public class TempActivity extends Activity {

    public static final String BROADCAST_ACTION = "com.appcan.close";
    private BroadcastReceiver mBroadcastReceiver;
    Handler mHandler = new Handler() {
    };

    private boolean isTemp = false;
    private long showTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout rootLayout = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(layoutParams);
        Bitmap bm = BUtility.getLoadingBitmap(this);
        if (bm != null) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageBitmap(bm);
            rootLayout.addView(imageView);
        }
        if (EBrowserActivity.develop) {
            TextView worn = new TextView(this);
            worn.setText(EUExUtil.getString("platform_only_test"));
            worn.setTextColor(0xffff0000);
            worn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            FrameLayout.LayoutParams wornPa = new FrameLayout.LayoutParams(
                    Compat.FILL, Compat.WRAP);
            wornPa.gravity = Gravity.TOP;
            wornPa.leftMargin = 10;
            wornPa.topMargin = 10;
            worn.setLayoutParams(wornPa);
            rootLayout.addView(worn);
        }
        setContentView(rootLayout);
        showTime = System.currentTimeMillis();
        try {
            Intent intent = getIntent();
            if (intent != null) {
                isTemp = intent.getBooleanExtra("isTemp", false);
            }
        } catch (Exception exception) {
        }
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!isTemp) {
//                    startActivity(new Intent(TempActivity.this, EBrowserActivity.class));
//                    overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_no_anim")
//                            , EUExUtil.getResAnimID("platform_myspace_no_anim"));
//                }
//            }
//        },800);
        mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, intentFilter);
        try {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.class.getField(
                            "FLAG_NEEDS_MENU_KEY").getInt(null));
        } catch (Exception e) {
        }
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        finish();
//        overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_fade_in_anim")
//                , EUExUtil.getResAnimID("platform_myspace_fade_out_anim"));
//    }


    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mBroadcastReceiver);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sp = getSharedPreferences(
                    BUtility.m_loadingImageSp, Context.MODE_PRIVATE);
            long lodingTime = sp.getLong(BUtility.m_loadingImageTime, 0);
            long time = System.currentTimeMillis() - showTime;
            //若前端同时调用了uexWidget.closeLoading()、uexWindow.setLoadingImagePath()，则启动图显示时间以最长的为准；
            if (lodingTime > time) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_fade_in_anim"),
                                EUExUtil.getResAnimID("platform_myspace_fade_out_anim"));
                    }
                }, lodingTime - time);
            } else {
                finish();
                overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_fade_in_anim"),
                        EUExUtil.getResAnimID("platform_myspace_fade_out_anim"));
            }

        }
    }
}
