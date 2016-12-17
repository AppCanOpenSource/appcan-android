package org.zywx.wbpalmstar.base.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

    /**
     * 设置状态栏颜色
     */
    public static void setStatusBarColor(Activity activity, int color){
        if (color==-1){
            return;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){//4.4 全透明状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            Window window = activity.getWindow();
            if (color!= Color.TRANSPARENT) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    /**
     * 设置状态栏颜色
     */
    public static void setStatusBarColorWithAddView(Activity activity, int color) {
        if (color==-1){
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            View view = createStatusBarView(activity, color);
            if (view != null) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
                viewGroup.addView(view);

                View contentView =  ((ViewGroup) activity.getWindow().getDecorView()
                        .findViewById(Window.ID_ANDROID_CONTENT)).getChildAt(0);
                if(contentView instanceof ViewGroup){
                    ViewGroup content = (ViewGroup)contentView;
                    //这个是为了内容不会伸到zh
                    content.setFitsSystemWindows(true);
                    content.setClipToPadding(true);
                }
            }
        }
    }

    /**
     * 获取系统状态栏的高度
     * @param activity
     * @return
     */
    private static int getStatusBarHeight(Activity activity) {
        int viewHeight = 0;
        if (activity != null) {
            int resourseId =
                    activity.getResources().getIdentifier("status_bar_height", "dimen", "android");

            viewHeight = activity.getResources().getDimensionPixelSize(resourseId);
        }
        return viewHeight;
    }


    /**
     * 创建一个view填充状态栏
     */
    private static View createStatusBarView(Activity activity, int color) {

        int viewHeight = getStatusBarHeight(activity);
        if (viewHeight == 0) {
            return null;
        } else {
            View view = new View(activity);
            ViewGroup.LayoutParams layoutParams =
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewHeight);
            view.setLayoutParams(layoutParams);
            view.setBackgroundColor(color);
            return view;
        }
    }
}
