package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

public class BaseTempActivity extends Activity {
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerFinishReceiver();
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
            overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_no_anim")
                    , EUExUtil.getResAnimID("platform_myspace_no_anim"));
        }
    }

    private void registerFinishReceiver() {
        mBroadcastReceiver = new FinishSelfReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LoadingActivity.FINISH_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, intentFilter);
    }
}
