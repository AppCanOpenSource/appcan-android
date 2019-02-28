package org.zywx.wbpalmstar.engine;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.util.ConfigXmlUtil;
import org.zywx.wbpalmstar.base.util.PermissionUtils;
import org.zywx.wbpalmstar.engine.callback.RequestPermissionsCallBcak;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static org.zywx.wbpalmstar.base.util.PermissionUtils.REQUESTFLAGDENIED;
import static org.zywx.wbpalmstar.base.util.PermissionUtils.REQUESTFLAGDENIEDNOASK;


/**
 * Created by yanlongtao on 2015/4/21 0021.
 * <p>
 * LoadingActivity 里面不要做AppCan 配置相关的操作，
 * <p>
 * 原因：历史问题，推送，暴露给三方的入口等都是EBrowserActivity
 */

public class LoadingActivity extends Activity implements RequestPermissionsCallBcak {

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
            Manifest.permission.ACCESS_COARSE_LOCATION,
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

    /**
     * 权限申请过执行的操作
     *
     * @param requestCode
     */
    @Override
    public void requestPermissionsSucesss(int requestCode) {
        startEngin();
    }

    /**
     * 权限申请失败回调
     *
     * @param errorCode
     * @param requestCode
     */
    @Override
    public void requestPermissionfailure(int errorCode, Object requestCode) {
        //普通拒绝重新申请权限
        if (errorCode == REQUESTFLAGDENIED) {
            if (requestCode instanceof List) {
                String[] requestAgin = (String[]) ((List) requestCode).toArray(new String[((List) requestCode).size()]);
                PermissionUtils.requestPermissions(this, requestAgin, REQUEST_CODE_ASK_CALL_PHONE, this);
            }

        } else if (errorCode == REQUESTFLAGDENIEDNOASK) {   //已经勾选不在提示申请权限
//            showPerssionDialog();
            Toast.makeText(this,"请到设置页面开启相关权限！",Toast.LENGTH_LONG).show();
        }
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
        //多个权限申请操作
        PermissionUtils.requestPermissions(this, perssions, REQUEST_CODE_ASK_CALL_PHONE, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE_ASK_CALL_PHONE == requestCode) {
            PermissionUtils.onRequestPermissionsResults(this, requestCode, permissions, grantResults, this);
        }
    }

    private void showPerssionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("请到设置中心开启相关权限!")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gotoMiuiPermission();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }
    /**
     * 跳转到miui的权限管理页面
     */
    private void gotoMiuiPermission() {
        try { // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", this.getPackageName());
            this.startActivity(localIntent);
        } catch (Exception e) {
            try { // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", this.getPackageName());
                this.startActivity(localIntent);
            } catch (Exception e1) { // 否则跳转到应用详情
                e1.printStackTrace();
            }
        }
    }

}
