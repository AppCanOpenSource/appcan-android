package org.zywx.wbpalmstar.engine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.uex.R;

/**
 * Created by yanlongtao on 2015/4/21 0021.
 */
public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
        overridePendingTransition(EUExUtil.getResAnimID("platform_myspace_fade_in_anim")
                ,EUExUtil.getResAnimID("platform_myspace_fade_out_anim"));
    }


}
