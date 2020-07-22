package org.zywx.wbpalmstar.engine.external;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/* 全屏模式下，即使将activity的windowSoftInputMode的属性设置为：adjustResize，
在键盘显示时它未将Activity的Screen向上推动，所以你Activity的view的根树的尺寸是没有变化的。
在这种情况下，你也就无法得知键盘的尺寸，对根view的作相应的推移。全屏下的键盘无法Resize的问题从2.1就已经存在了，直到现在google还未给予解决。 引用自：https://www.jianshu.com/p/d3c96ccf4950 */
/**
 * File Description: 用于解决沉浸式状态栏开启后键盘压缩模式不生效的问题
 * <p>
 * Created by zhangyipeng with Email: sandy1108@163.com at Date: 2020/7/22.
 */
public class AndroidBug5497Workaround {

    public static void assistActivity(View content) {
        new AndroidBug5497Workaround(content);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private ViewGroup.LayoutParams frameLayoutParams;

    private AndroidBug5497Workaround(View content) {
        if (content != null) {
            mChildOfContent = content;
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    possiblyResizeChildOfContent();
                }
            });
            frameLayoutParams = mChildOfContent.getLayoutParams();
        }
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            //如果两次高度不一致
            //将计算的可视高度设置成视图的高度
            frameLayoutParams.height = usableHeightNow;
            mChildOfContent.requestLayout();//请求重新布局
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        //计算视图可视高度
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom);
    }
}
