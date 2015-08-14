package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.io.InputStream;


/**
 * Created by yanlongtao on 2015/4/21 0021.
 */

public class LoadingActivity extends Activity {

    public static final String BROADCAST_ACTION = "com.appcan.close";
    private BroadcastReceiver mBroadcastReceiver;
    Handler mHandler=new Handler() {
    };

    private boolean isTemp=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        if (intent!=null) {
            isTemp = intent.getBooleanExtra("isTemp", false);
        }
        FrameLayout rootLayout=new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(layoutParams);
        InputStream inputStream=getResources().openRawResource(EUExUtil.getResDrawableID("startup_bg_16_9"));
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Bitmap bm = BUtility.createBitmapWithStream(inputStream,
				dm.widthPixels, dm.heightPixels);
		if (bm != null) {
			ImageView imageView = new ImageView(this);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setImageBitmap(bm);
			rootLayout.addView(imageView);
		}
        setContentView(rootLayout);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isTemp) {
                    startActivity(new Intent(LoadingActivity.this, EBrowserActivity.class));
                    overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_no_anim")
                            , EUExUtil.getResAnimID("platform_myspace_no_anim"));
                }
            }
        },700);

        mBroadcastReceiver = new MyBroadcastReceiver();
              IntentFilter intentFilter = new IntentFilter();
               intentFilter.addAction(BROADCAST_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);
        if (EBrowserActivity.develop) {
            TextView worn = new TextView(this);
            worn.setText("测试版本仅用于开发测试");
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
    }

    @Override
    public Resources.Theme getTheme() {
        return super.getTheme();
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
        unregisterReceiver(mBroadcastReceiver);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
            overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_fade_in_anim")
                , EUExUtil.getResAnimID("platform_myspace_fade_out_anim"));
        }
    }
}
