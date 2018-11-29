package org.zywx.wbpalmstar.engine;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.util.ConfigXmlUtil;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by yanlongtao on 2015/4/21 0021.
 * <p>
 * LoadingActivity 里面不要做AppCan 配置相关的操作，
 * <p>
 * 原因：历史问题，推送，暴露给三方的入口等都是EBrowserActivity
 */

public class LoadingActivity extends Activity  {

    public static final String FINISH_BROADCAST_ACTION = "com.appcan.close";

    public static final String KEY_INTENT_ROOT_PAGE_DATA = "root_page_data";

    private static final int MSG_GET_WIDGET_DATA = 100;
    private static final int REQUEST_CODE_ASK_CALL_PHONE = 1;

    private FrameLayout mRootLayout;
    private BroadcastReceiver mBroadcastReceiver;

    private Handler mHandler;

    private boolean isTemp = false;

    //申请两个权限，录音和文件读写
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    String permissions[] = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<String>();
    private boolean isFrist = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new LoadingHandler(this);
        handleIntent();
        initRootView();
        ConfigXmlUtil.setStatusBarColorWithAddView(this, Color.TRANSPARENT);
        setContentView(mRootLayout);
        registerFinishReceiver();
        addDevelopInfo();
        hideMenu();
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
            worn.setText(getResources().getIdentifier("platform_only_test", "string", getPackageName()));
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
    protected void onResume() {
        super.onResume();

        if (isFrist) {
            requsetPerssions(permissions);
            isFrist = false;
        }
    }

    private void startEngin() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isTemp) {
                    startEngine();
                }
            }
        }, 100);
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
        AppCan.getInstance().start(LoadingActivity.this, AppCan.getInstance().getRootWidgetData(), getIntent().getExtras());
    }

    private static class LoadingHandler extends Handler {

        private final WeakReference<LoadingActivity> mActivity;

        LoadingHandler(LoadingActivity activity) {
            mActivity = new WeakReference<LoadingActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_WIDGET_DATA:

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    public void requsetPerssions(final String[] perssions) {
        //系统运行环境小于6.0不需要权限申请
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startEngin();
            return;
        }
        mPermissionList.clear();//清空没有通过的权限
        for (String perssion : perssions) {
            if (ContextCompat.checkSelfPermission(this, perssion) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(perssion);
            }
        }
        if (mPermissionList.size() > 0) {
            //有未申请的权限，需要动态去申请

//            for (String  permission : permissions) {
                ActivityCompat.requestPermissions(this, perssions, REQUEST_CODE_ASK_CALL_PHONE);
//            }

        } else {
            //说明权限都已经通过，
            startEngin();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = true;//有权限没有通过
        if (REQUEST_CODE_ASK_CALL_PHONE == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = false;
                    //在用户已经拒绝授权的情况下，如果shouldShowRequestPermissionRationale返回false则
                    // 可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        //解释原因，并且引导用户至设置页手动授权
                        //引导用户至设置页手动授权
                        Toast.makeText(this,"为了不影响正常使用,请到设置界面开启权限" ,Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        },2000);

                    } else {
                        //权限请求失败，但未选中“不再提示”选项
                        showPerssionDialog("为了能够正常使用App,请开启权限!",permissions);
                    }
                    break;
                }

            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                startEngin();
            }

        }
    }

    private void showPerssionDialog(String message, final String[] permission) {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(LoadingActivity.this, permission, REQUEST_CODE_ASK_CALL_PHONE);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }
}
