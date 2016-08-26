package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.WebViewSdkCompat;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.InputStream;
import java.lang.ref.WeakReference;


/**
 * Created by yanlongtao on 2015/4/21 0021.
 */

public class LoadingActivity extends Activity {

    public static final String FINISH_BROADCAST_ACTION = "com.appcan.close";

    public static final String KEY_INTENT_WIDGET_DATA = "widget_data";

    public static final String KEY_INTENT_ROOT_PAGE_DATA = "root_page_data";

    private static final int MSG_GET_WIDGET_DATA=100;

    private FrameLayout mRootLayout;
    private WWidgetData mWidgetData;
    private BroadcastReceiver mBroadcastReceiver;

    private Handler mHandler;

    private boolean isTemp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler=new LoadingHandler(this);
        handleIntent();
        initRootView();
        addLoadingImage(mRootLayout);
        setContentView(mRootLayout);
        registerFinishReceiver();
        addDevelopInfo();
        hideMenu();
        if (!isTemp) {
            getWidgetData();
         }
    }

    /**
     * 延时启动Activity，有些机型的Launcher App动画没有播放完，直接启动会闪
     */
    private void handleWidgetData() {
        WebViewSdkCompat.initInLoadingActivity(LoadingActivity.this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isTemp) {
                    startActivityWithData(EBrowserActivity.class);
                }
            }
        }, 700);

    }

    private void startActivityWithData(Class<?> cls) {
        Intent intent = new Intent(LoadingActivity.this, cls);
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        if (mWidgetData != null) {
            intent.putExtra(KEY_INTENT_WIDGET_DATA, mWidgetData);
        }
        startActivity(intent);
        overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_no_anim")
                , EUExUtil.getResAnimID("platform_myspace_no_anim"));

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

    private void addLoadingImage(ViewGroup parent) {
        InputStream inputStream = getResources().openRawResource(EUExUtil.getResDrawableID("startup_bg_16_9"));
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap bm = BUtility.createBitmapWithStream(inputStream,
                dm.widthPixels, dm.heightPixels);
        if (bm != null) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageBitmap(bm);
            parent.addView(imageView);
        }
    }

    private void addDevelopInfo() {
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

    private void getWidgetData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WDataManager wDataManager = new WDataManager(LoadingActivity.this.getApplicationContext());
                mWidgetData = wDataManager.getWidgetData();
                if (mWidgetData != null && mWidgetData.m_indexUrl != null) {
                    BUtility.initWidgetOneFile(LoadingActivity.this.getApplicationContext(), mWidgetData.m_appId);
                }
                mHandler.sendEmptyMessage(MSG_GET_WIDGET_DATA);
            }
        }).start();
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
                    if (mActivity.get()!=null){
                        mActivity.get().handleWidgetData();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
