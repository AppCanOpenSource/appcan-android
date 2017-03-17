package org.zywx.wbpalmstar.base;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 *
 */

public class killSelfService extends Service {

    public static final String KEY_STOP_DELAY="key_stop_delay";
    public static final String KEY_STOP_PACKAGE_NAME="key_stop_package_name";
    private Handler handler;
    private String PackageName;
    public killSelfService() {
        handler=new Handler();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        long delayTime =intent.getLongExtra(KEY_STOP_DELAY, 1000);
        PackageName=intent.getStringExtra(KEY_STOP_PACKAGE_NAME);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(PackageName);
                startActivity(LaunchIntent);
                killSelfService.this.stopSelf();
            }
        }, delayTime);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
