package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.io.InputStream;


/**
 * Created by yanlongtao on 2015/4/21 0021.
 */

public class TempActivity extends Activity {

    public static final String BROADCAST_ACTION = "com.appcan.close";
    private BroadcastReceiver mBroadcastReceiver;
    Handler mHandler=new Handler() {
    };

    private boolean isTemp=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InputStream inputStream=getResources().openRawResource(EUExUtil.getResDrawableID("startup_bg_16_9"));
        ImageView imageView=new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        setContentView(imageView);
        Intent intent=getIntent();
        if (intent!=null){
            isTemp=intent.getBooleanExtra("isTemp",false);
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
        registerReceiver(mBroadcastReceiver, intentFilter);
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
