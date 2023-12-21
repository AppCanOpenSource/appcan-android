package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.listener.OnAppCanInitStatusChanged;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

/**
 * File Description: 提示用的带WebView的透明Activity
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 1/4/21.
 */
public class LaunchNoticeWebViewActivity extends Activity implements OnAppCanInitStatusChanged {

    private static final String TAG = "LaunchNoticeWebViewActi";

    public static final String INTENT_KEY_STATUS = "status";

    private EBrowserView mBrwView;
    private String mSplashPageUrl;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(EUExUtil.getResLayoutID("platform_activity_layout_launch_notice_webview"));
        mBrwView = new EBrowserView(this, 0, null);
        mBrwView.init();
        mBrwView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout rootLayout = findViewById(EUExUtil.getResIdID("root_layout"));
        rootLayout.addView(mBrwView);
        WWidgetData widgetData = AppCan.getInstance().getRootWidgetData();
        boolean debugEnable = widgetData.m_appdebug == 1;
        if (debugEnable){
            EBrowserView.setWebContentsDebuggingEnabled(true);
        }
        String splashPageUrl = widgetData.splashDialogPagePath;
//        splashPageUrl = "file:///android_asset/widget/" + splashPageUrl;
        mSplashPageUrl = splashPageUrl;
        BDebug.i(TAG, "splashPageUrl: " + splashPageUrl);
    }

    /**
     * 启动自定义首屏提示页面
     */
    private void startSplashPageUrl(){
        if (!TextUtils.isEmpty(mSplashPageUrl)){
            mBrwView.loadUrl(mSplashPageUrl);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSplashPageUrl();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            BDebug.i(TAG, "KEYCODE_BACK");
            // 拦截返回键，弹出提示语，而不是直接返回键关闭页面。
            Toast.makeText(this, EUExUtil.getResStringID("ac_engine_splash_ask_to_agree"), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 状态变更
     */
    @Override
    public void onReceivedStatus(String status) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(status)){
            bundle.putString(INTENT_KEY_STATUS, status);
            intent.putExtras(bundle);
            setResult(Activity.RESULT_OK, intent);
        }else{
            // empty op
            setResult(Activity.RESULT_OK, intent);
        }
        // 接收到状态后设置结果然后关闭
        finish();
    }
}
