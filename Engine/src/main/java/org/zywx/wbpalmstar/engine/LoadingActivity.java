package org.zywx.wbpalmstar.engine;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.listener.OnAppCanInitStatusChanged;
import org.zywx.wbpalmstar.base.util.ConfigXmlUtil;
import org.zywx.wbpalmstar.base.util.PermissionUtils;
import org.zywx.wbpalmstar.engine.callback.RequestPermissionsCallBcak;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

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

    private static final String TAG = "LoadingActivity";
    
    public static final String FINISH_BROADCAST_ACTION = "com.appcan.close";

    public static final String KEY_INTENT_ROOT_PAGE_DATA = "root_page_data";

    private static final int MSG_GET_WIDGET_DATA = 100;
    private static final int REQUEST_CODE_ASK_CALL_PHONE = 1;
    private static final int REQUEST_CODE_START_SPLASH = 2;

    private static final String SP_NAME_APPCAN_LOADING = "appcan_loading";
    private static final String SP_KEY_APPCAN_LOADING_SHOWN_VERSION = "shownVersion";
    private static final String SP_KEY_APPCAN_LOADING_SHOWN_VERSION_DEFAULT = "defaultVersion";

    private FrameLayout mRootLayout;
    private BroadcastReceiver mBroadcastReceiver;

    private Handler mHandler;

    private boolean isTemp = false;

    // 申请两个权限，手机状态权限和文件读写权限
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<String>();
    /**
     * 用于防止再次onResume的时候重复执行逻辑
     */
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

    /**
     * 动态添加启动图，CENTER_CROP模式，防止变形
     */
    private void addLoadingImage(ViewGroup parent) {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(EUExUtil.getResDrawableID("startup_bg_16_9"));
        parent.addView(imageView);
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
        addLoadingImage(mRootLayout);
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

    /**
     * 判断如果path不为空，并且version与上次显示过的version不同
     *
     * @return
     */
    private boolean shouldShowSplashPage(){
        WWidgetData widgetData = AppCan.getInstance().getRootWidgetData();
        String splashPageUrl = widgetData.splashDialogPagePath;
        String splashPageVersion = widgetData.splashDialogPageVersion;
        String lastVersion = getShownCustomSplashPageVersion();
        BDebug.i(TAG, "shouldShowSplashPage: lastVersion=" + lastVersion + " splashPageVersion=" + splashPageVersion + " splashPageUrl=" + splashPageUrl);
        return !TextUtils.isEmpty(splashPageUrl) // path不为空表示配置了splashPage
                &&(TextUtils.isEmpty(lastVersion)||!lastVersion.equals(splashPageVersion)); // lastVersion为空则表示首次启动，需要展示；或者是lastVersion与本次不一致，表示版本号变了（不比大小，只比变化），也需要展示。
    }

    /**
     * 获取是否已经显示过自定义首屏页面
     */
    private String getShownCustomSplashPageVersion(){
        SharedPreferences sp = getSharedPreferences(SP_NAME_APPCAN_LOADING, Context.MODE_PRIVATE);
        String shownVersion = sp.getString(SP_KEY_APPCAN_LOADING_SHOWN_VERSION, "");
        return shownVersion;
    }

    /**
     * 保存状态：是否已经显示过自定义的首屏页面（保存版本号）
     *
     * @param showVersion
     */
    private void setShownCustomSplashPageVersion(String showVersion){
        // 这个地方的只要存值，就不要存空，下一次取出判断的时候，就可以判断出来是否是首次了。
        if (TextUtils.isEmpty(showVersion)){
            showVersion = SP_KEY_APPCAN_LOADING_SHOWN_VERSION_DEFAULT;
        }
        SharedPreferences sp = getSharedPreferences(SP_NAME_APPCAN_LOADING, Context.MODE_PRIVATE);
        sp.edit().putString(SP_KEY_APPCAN_LOADING_SHOWN_VERSION, showVersion).apply();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFrist && shouldShowSplashPage()) {
            Intent intent = new Intent(this, LaunchNoticeWebViewActivity.class);
            startActivityForResult(intent, REQUEST_CODE_START_SPLASH);
            BDebug.i(TAG, "start LaunchNoticePage");
        }else{
            startEngin();
            BDebug.i(TAG, "escape LaunchNoticePage");
        }
        isFrist = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_START_SPLASH && resultCode == Activity.RESULT_OK){
            String status = data.getExtras().getString(LaunchNoticeWebViewActivity.INTENT_KEY_STATUS, "");
            if (status.equals(OnAppCanInitStatusChanged.STATUS.CONTINUE)){
                // 继续
                WWidgetData widgetData = AppCan.getInstance().getRootWidgetData();
                String splashPageVersion = widgetData.splashDialogPageVersion;
                setShownCustomSplashPageVersion(splashPageVersion);
                startEngin();
            }else{
                // 退出
                finish();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
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
