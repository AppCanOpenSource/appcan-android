package org.zywx.wbpalmstar.base.util;

import android.app.Activity;
import android.view.WindowManager;

import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

/**
 * 设置config.xml 里面的配置信息
 * <p/>
 * Created by ylt on 16/6/14.
 */
public class ConfigXmlUtil {

    /**
     * 设置全屏
     */
    public static void setFullScreen(Activity activity) {
        if (WWidgetData.sFullScreen) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

}
