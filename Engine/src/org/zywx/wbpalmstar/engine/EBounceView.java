/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.engine;

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.universalex.EUExScript;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class EBounceView extends LinearLayout {

    private int mLastY;
    private Context mContext;
    private Scroller mScroller;
    public EBrowserView mBrwView;
    private EBounceViewHeader mTailView;
    private EBounceViewHeader mHeaderView;
    private int mRefrshHeight;
    private int mBounceViewHeight;
    private boolean mShowTopView;
    private boolean mShowBottomView;
    private boolean mBounce;
    private boolean mIsTop;
    private boolean mIsBottom;
    private boolean mTopLoading;
    private boolean mBottomLoading;
    private boolean mTopNotify;
    private boolean mBottomNotify;
    private boolean mTopAutoRefresh = false;
    private boolean isSupportSwipeCallback = false;//is need callback,set by API interface.
    private int mTopState;
    private int mBottomState;
    private int mTopBund;
    private int mBotomBund;
    private int mTouchSlop;

    public String mPullStr;
    public String mPullingStr;
    public String mReleaseStr;

    static final int F_VIEW_STATE_DRAG = 0;
    static final int F_VIEW_STATE_RELEASE = 1;

    static final int F_BOUNCEVIEW_STATE_PULL_RELOAD = 0;
    static final int F_BOUNCEVIEW_STATE_RELEASE_RELOAD = 1;
    static final int F_BOUNCEVIEW_STATE_LOADING = 2;

    public static final int F_SET_BOUNCEVIEW_TYPE_STR1 = 0;
    public static final int F_SET_BOUNCEVIEW_TYPE_STR2 = 1;
    public static final int F_SET_BOUNCEVIEW_TYPE_STR3 = 2;
    public static final int F_SET_BOUNCEVIEW_TYPE_STR4 = 3;
    public static final int F_SET_BOUNCEVIEW_TYPE_STR5 = 4;

    private float mDensity = ESystemInfo.getIntence().mDensity;

    public EBounceView(Context context) {
        super(context);
        mContext = context;
        mScroller = new Scroller(mContext);
        mRefrshHeight = ESystemInfo.getIntence().mDefaultBounceHeight;
        mBounceViewHeight = (ESystemInfo.getIntence().mHeightPixels / 3) * 2;
        setAnimationCacheEnabled(false);
        setAlwaysDrawnWithCacheEnabled(false);
        setOrientation(VERTICAL);
        mTopBund = -mRefrshHeight;
        mBotomBund = mRefrshHeight;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() / 2;
    }

    @Override
    public void addView(View child) {
        mHeaderView = new EBounceViewHeader(mContext, EViewEntry.F_BOUNCE_TYPE_TOP);
        LayoutParams hlp = new LinearLayout.LayoutParams(Compat.FILL, mBounceViewHeight);
        hlp.topMargin = -mBounceViewHeight;
        hlp.gravity = Gravity.CENTER;
        addView(mHeaderView, hlp);
        mHeaderView.setVisibility(GONE);

        mBrwView = (EBrowserView) child;
        LayoutParams wlp = new LinearLayout.LayoutParams(Compat.FILL, Compat.FILL);
        wlp.weight = 1.0f;
        addView(mBrwView, wlp);

        mTailView = new EBounceViewHeader(mContext, EViewEntry.F_BOUNCE_TYPE_BOTTOM);
        LayoutParams tlp = new LinearLayout.LayoutParams(Compat.FILL, mBounceViewHeight);
        tlp.bottomMargin = -mBounceViewHeight;
        tlp.gravity = Gravity.CENTER;
        addView(mTailView, tlp);
        mTailView.setVisibility(GONE);
    }

    private VelocityTracker mVelocityTracker;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getAction();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(e);
        if (!mBounce) {
            switch (action) {
                case MotionEvent.ACTION_UP:
                    handlerTracker();
                    break;
            }
            return false;
        }

        int y = (int) e.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int m = y - mLastY;
                boolean can = (m >= 0 ? m : -m) > mTouchSlop;
                if (m > 0 && !mBottomLoading && can && !mTopAutoRefresh) {
                    if (topCanBounce()) {
                        mIsTop = true;
                        if (mTopNotify) {
                            mBrwView.onBounceStateChange(EViewEntry.F_BOUNCE_TYPE_TOP, F_BOUNCEVIEW_STATE_PULL_RELOAD);
                        }
                        return true;
                    }
                } else if (m < 0 && !mTopLoading && can) {
                    if (bottomCanBounce()) {
                        mIsBottom = true;
                        if (mBottomNotify) {
                            mBrwView.onBounceStateChange(EViewEntry.F_BOUNCE_TYPE_BOTTOM, F_BOUNCEVIEW_STATE_PULL_RELOAD);
                        }
                        return true;
                    }
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                handlerTracker();
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int y = (int) event.getRawY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int m = y - mLastY;
                if (mIsTop) {
                    moveDown(m);
                } else if (mIsBottom) {
                    moveUp(m);
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                reverse();
                handlerTracker();
                break;
        }
        return true;
    }

    private boolean handlerTracker() {
        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000);
        int velocityX = (int) (velocityTracker.getXVelocity() / mDensity);
        int velocityY = (int) (velocityTracker.getYVelocity() / mDensity);
        endDrag();
        return onTracker(velocityX, velocityY);
    }

    private void endDrag() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private boolean onTracker(int velocityX, int velocityY) {
        boolean trigger = false;
        if (isSupportSwipeCallback) {
            final int absY = (velocityY < 0) ? -velocityY : velocityY;
            final int absX = (velocityX < 0) ? -velocityX : velocityX;
            int rate = ESystemInfo.getIntence().mSwipeRate;
            if ((velocityX > rate) && (absX > absY)) {
                mBrwView.loadUrl(EUExScript.F_UEX_SCRIPT_SWIPE_RIGHT);
                trigger = true;
            } else if ((velocityX < -rate) && (absX > absY)) {
                mBrwView.loadUrl(EUExScript.F_UEX_SCRIPT_SWIPE_LEFT);
                trigger = true;
            }
        }
        return trigger;
    }

    private void moveDown(int moveY) {
        int f1 = getScrollY();
        int f2 = 0;
        if (moveY > 0) {
            f2 = (int) (moveY * 0.5F);
        } else {
            f2 = (int) (moveY * 0.7F);
            int bound = f1 - f2;
            if (bound >= 0) {
                f2 = f2 + bound;
                scrollBy(0, -f2);
                return;
            }
        }
        scrollBy(0, -f2);
        if (f1 <= mTopBund && mTopState == F_VIEW_STATE_DRAG) {
            if (mShowTopView && mTopNotify) {
                mHeaderView.setArrowVisibility(VISIBLE);
                mHeaderView.setProgressBarVisibility(GONE);
                mHeaderView.showReleaseToReloadText();
                mHeaderView.rotateArrowImage(EBounceViewHeader.F_ROTATE_UP);
            }
            if (mTopLoading) {
                mTopLoading = false;
            }
            mTopState = F_VIEW_STATE_RELEASE;
        } else if (f1 > mTopBund && mTopState == F_VIEW_STATE_RELEASE) {
            if (mShowTopView && mTopNotify) {
                mHeaderView.setArrowVisibility(VISIBLE);
                mHeaderView.setProgressBarVisibility(GONE);
                mHeaderView.showPullToReloadText();
                mHeaderView.rotateArrowImage(EBounceViewHeader.F_ROTATE_DOWN);
            }
            mTopState = F_VIEW_STATE_DRAG;
        }
        return;
    }

    private void moveUp(int moveY) {
        int f1 = getScrollY();
        int f2 = 0;
        if (moveY < 0) {
            f2 = (int) (moveY * 0.5F);
        } else {
            f2 = (int) (moveY * 0.7F);
            int bound = f1 - f2;
            if (bound <= 0) {
                f2 = f2 + bound;
                scrollBy(0, -f2);
                return;
            }
        }
        scrollBy(0, -f2);
        if (f1 >= mBotomBund && mBottomState == F_VIEW_STATE_DRAG) {
            if (mShowBottomView && mBottomNotify) {
                mTailView.setArrowVisibility(VISIBLE);
                mTailView.setProgressBarVisibility(GONE);
                mTailView.showReleaseToReloadText();
                mTailView.rotateArrowImage(EBounceViewHeader.F_ROTATE_UP);
            }
            if (mBottomLoading) {
                mBottomLoading = false;
            }
            mBottomState = F_VIEW_STATE_RELEASE;
        } else if (f1 < mBotomBund && mBottomState == F_VIEW_STATE_RELEASE) {
            if (mShowBottomView && mBottomNotify) {
                mTailView.setArrowVisibility(VISIBLE);
                mTailView.setProgressBarVisibility(GONE);
                mTailView.showPullToReloadText();
                mTailView.rotateArrowImage(EBounceViewHeader.F_ROTATE_DOWN);
            }
            mBottomState = F_VIEW_STATE_DRAG;
        }
        return;
    }

    private void reverse() {
        int sy = getScrollY();
        if (mIsTop) {
            if (sy <= mTopBund && mTopNotify) {
                topRefreshing();
            } else {
                backToTop();
            }
        } else if (mIsBottom) {
            if (sy >= mBotomBund && mBottomNotify) {
                bottomRefreshing();
            } else {
                backToBottom();
            }
        }
    }

    public void topBounceViewRefresh() {
        if (!mTopLoading) {
            scrollTo(0, mTopBund);
            if (mShowTopView) {
                mHeaderView.setArrowVisibility(GONE);
                mHeaderView.setProgressBarVisibility(VISIBLE);
                mHeaderView.setTextVisibility(VISIBLE);
                mHeaderView.showLoadingText();
            }
            mTopAutoRefresh = true;
            mTopLoading = true;
            if (mTopNotify) {
                mBrwView.onBounceStateChange(EViewEntry.F_BOUNCE_TYPE_TOP, F_BOUNCEVIEW_STATE_LOADING);
            }
        }
    }

    private void topRefreshing() {
        if (mTopNotify) {
            mBrwView.onBounceStateChange(EViewEntry.F_BOUNCE_TYPE_TOP, F_BOUNCEVIEW_STATE_RELEASE_RELOAD);
        }
        mTopLoading = true;
        mTopState = F_VIEW_STATE_DRAG;
        int sy = getScrollY();
        int dist = mTopBund - sy;
        if (mShowTopView) {
            mHeaderView.setArrowVisibility(GONE);
            mHeaderView.setProgressBarVisibility(VISIBLE);
            mHeaderView.setTextVisibility(VISIBLE);
            mHeaderView.showLoadingText();
        }
        mScroller.startScroll(0, sy, 0, dist);
        postInvalidate();
        if (mTopNotify) {
            mBrwView.onBounceStateChange(EViewEntry.F_BOUNCE_TYPE_TOP, F_BOUNCEVIEW_STATE_LOADING);
        }
    }

    private void bottomRefreshing() {
        if (mBottomNotify) {
            mBrwView.onBounceStateChange(EViewEntry.F_BOUNCE_TYPE_BOTTOM, F_BOUNCEVIEW_STATE_RELEASE_RELOAD);
        }
        mBottomLoading = true;
        mBottomState = F_VIEW_STATE_DRAG;
        int sy = getScrollY();
        int dist = sy - mBotomBund;
        if (mShowBottomView) {
            mTailView.setArrowVisibility(GONE);
            mTailView.setProgressBarVisibility(VISIBLE);
            mTailView.setTextVisibility(VISIBLE);
            mTailView.showLoadingText();
        }
        mScroller.startScroll(0, -sy, 0, dist);
        postInvalidate();
        if (mBottomNotify) {
            mBrwView.onBounceStateChange(EViewEntry.F_BOUNCE_TYPE_BOTTOM, F_BOUNCEVIEW_STATE_LOADING);
        }
    }

    private void backToTop() {
        if (mTopAutoRefresh) {
            scrollTo(0, 0);
            mTopAutoRefresh = false;
        } else {
            int sy = getScrollY();
            int dist = 0 - sy;
            mScroller.startScroll(0, sy, 0, dist);
            postInvalidate();
        }
    }

    private void backToBottom() {
        int sy = getScrollY();
        int dist = sy;
        mScroller.startScroll(0, -sy, 0, dist);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int sy = mScroller.getCurrY();
            if (mIsTop) {
                scrollTo(0, sy);
            } else if (mIsBottom) {
                scrollTo(0, -sy);
            }
            postInvalidate();
            if (sy == 0) {
                mIsTop = false;
                mIsBottom = false;
                // mTopLoading = false;
                // mBottomLoading = false;
            }
        }
    }

    private void finishRefresh(int type) {
        switch (type) {
            case EViewEntry.F_BOUNCE_TYPE_TOP:
                if (!mTopLoading) {
                    return;
                }
                if (mShowTopView) {
                    mHeaderView.setArrowVisibility(VISIBLE);
                    mHeaderView.setProgressBarVisibility(GONE);
                    mHeaderView.showPullToReloadText();
                    mHeaderView.rotateArrowImage(EBounceViewHeader.F_ROTATE_DOWN);
                }
                backToTop();
                break;
            case EViewEntry.F_BOUNCE_TYPE_BOTTOM:
                if (!mBottomLoading) {
                    return;
                }
                if (mShowBottomView) {
                    mTailView.setArrowVisibility(VISIBLE);
                    mTailView.setProgressBarVisibility(GONE);
                    mTailView.showPullToReloadText();
                    mTailView.rotateArrowImage(EBounceViewHeader.F_ROTATE_DOWN);
                }
                backToBottom();
                break;
        }
        clear();
    }

    private void clear() {
        mTopLoading = false;
        mBottomLoading = false;
    }

    private boolean topCanBounce() {
        boolean b1 = mBrwView.getScrollY() == 0;
        if (b1) {
            return true;
        } else {
            return false;
        }
    }

    private boolean bottomCanBounce() {
        float nowScale = 1.0f;
        int versionA = Build.VERSION.SDK_INT;
        if (versionA <= 18) {
            nowScale = mBrwView.getScale();
        }
        int h1 = (int) (mBrwView.getContentHeight() * nowScale);
        int h2 = mBrwView.getScrollY() + mBrwView.getHeight();
        if (h1 <= h2 + 5) {
            return true;
        } else {
            return false;
        }
    }

    public void getBounce() {
        mBrwView.cbBounceState(mBounce ? 1 : 0);
    }

    public void setBounce(boolean flag) {
        mBounce = flag;
        mHeaderView.setVisibility(VISIBLE);
        mTailView.setVisibility(VISIBLE);
    }

    public void showBounceView(int inType, int inColor, int inFlag) {
        switch (inType) {
            case EViewEntry.F_BOUNCE_TYPE_TOP:
                mShowTopView = inFlag != 1 ? false : true;
                if (!mShowTopView) {
                    mHeaderView.componentsDisable();
                }
                mHeaderView.setBackgroundColor(inColor);
                break;
            case EViewEntry.F_BOUNCE_TYPE_BOTTOM:
                mShowBottomView = inFlag != 1 ? false : true;
                if (!mShowBottomView) {
                    mTailView.componentsDisable();
                }
                mTailView.setBackgroundColor(inColor);
                break;
        }
    }

    public void resetBounceView(int inType) {
        finishRefresh(inType);
    }

    public void release() {
        finishRefresh(EViewEntry.F_BOUNCE_TYPE_TOP);
        finishRefresh(EViewEntry.F_BOUNCE_TYPE_BOTTOM);
    }

    public void hiddenBounceView(int inType) {
        switch (inType) {
            case EViewEntry.F_BOUNCE_TYPE_TOP:
                mHeaderView.setVisibility(INVISIBLE);
                mTopNotify = false;
                break;
            case EViewEntry.F_BOUNCE_TYPE_BOTTOM:
                mTailView.setVisibility(INVISIBLE);
                mBottomNotify = false;
                break;
        }
    }

    public void notifyBounceEvent(int inType, int inStatus) {
        switch (inType) {
            case EViewEntry.F_BOUNCE_TYPE_TOP:
                mTopNotify = inStatus != 1 ? false : true;
                if (mTopNotify) {
                    mHeaderView.componentsEnable();
                }
                break;
            case EViewEntry.F_BOUNCE_TYPE_BOTTOM:
                mBottomNotify = inStatus != 1 ? false : true;
                if (mBottomNotify) {
                    mTailView.componentsEnable();
                }
                break;
        }
    }

    public void setBounceParms(int type, JSONObject json, String guestId) {

        String imagePath = null;
        String textColor = null;
        String levelText = null;
        String pullToReloadText = null;
        String releaseToReloadText = null;
        String loadingText = null;
        String loadingImagePath = null;
        imagePath = json.optString("imagePath");
        if (null != imagePath) {
            imagePath = BUtility.makeRealPath(imagePath,
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
        }
        textColor = json.optString("textColor");
        levelText = json.optString("levelText");
        pullToReloadText = json.optString("pullToReloadText");
        releaseToReloadText = json.optString("releaseToReloadText");
        loadingText = json.optString("loadingText");
        if (null != guestId && guestId.equals("donghang")) {
            loadingImagePath = json.optString("loadingImagePath");
            if (null != loadingImagePath) {
                loadingImagePath = BUtility.makeRealPath(loadingImagePath,
                        mBrwView.getCurrentWidget().m_widgetPath,
                        mBrwView.getCurrentWidget().m_wgtType);
            }
        }
        switch (type) {
            case EViewEntry.F_BOUNCE_TYPE_TOP:
                if (null != imagePath && 0 != imagePath.trim().length()) {
                    mHeaderView.setArrowhead(imagePath);
                }
                if (null != loadingImagePath && 0 != loadingImagePath.trim().length()) {
                    mHeaderView.setLoadingPic(loadingImagePath);
                }
                if ((null == textColor || 0 == textColor.trim().length())
                        && (null == pullToReloadText || 0 == pullToReloadText.trim().length())
                        && (null == releaseToReloadText || 0 == releaseToReloadText.trim().length())
                        && (null == loadingText || 0 == loadingText.trim().length())) {
                    mHeaderView.setContentEmpty(true);
                } else {
                    if (null != textColor && 0 != textColor.trim().length()) {
                        int color = mBrwView.parseColor(textColor);
                        mHeaderView.setTextColor(color);
                    }
                    if (null != levelText && 0 != levelText.trim().length()) {
                        mHeaderView.setLevelText(levelText);
                    }
                    if (null != releaseToReloadText && 0 != releaseToReloadText.trim().length()) {
                        mHeaderView.setReleaseToReloadText(releaseToReloadText);
                    }
                    if (null != loadingText && 0 != loadingText.trim().length()) {
                        mHeaderView.setLoadingText(loadingText);
                    }
                    if (null != pullToReloadText && 0 != pullToReloadText.trim().length()) {
                        mHeaderView.setPullToReloadText(pullToReloadText);
                    }
                }
                break;
            case EViewEntry.F_BOUNCE_TYPE_BOTTOM:
                if (null != imagePath && 0 != imagePath.trim().length()) {
                    mTailView.setArrowhead(imagePath);
                }
                if (null != loadingImagePath && 0 != loadingImagePath.trim().length()) {
                    mTailView.setLoadingPic(loadingImagePath);
                }
                if ((null == textColor || 0 == textColor.trim().length())
                        && (null == pullToReloadText || 0 == pullToReloadText.trim().length())
                        && (null == releaseToReloadText || 0 == releaseToReloadText.trim().length())
                        && (null == loadingText || 0 == loadingText.trim().length())) {
                    mTailView.setContentEmpty(true);
                } else {
                    if (null != textColor && 0 != textColor.trim().length()) {
                        int color = mBrwView.parseColor(textColor);
                        mTailView.setTextColor(color);
                    }
                    if (null != levelText && 0 != levelText.trim().length()) {
                        mTailView.setLevelText(levelText);
                    }
                    if (null != releaseToReloadText && 0 != releaseToReloadText.trim().length()) {
                        mTailView.setReleaseToReloadText(releaseToReloadText);
                    }
                    if (null != loadingText && 0 != loadingText.trim().length()) {
                        mTailView.setLoadingText(loadingText);
                    }
                    if (null != pullToReloadText && 0 != pullToReloadText.trim().length()) {
                        mTailView.setPullToReloadText(pullToReloadText);
                    }
                }
                break;
        }
    }

    /**
     * @param flag
     * @param bgColor
     * @param baseUrl
     * @param mEBrowserView
     * @author wanglei 
     * @createAt 20151124
     * @ps 与原EBrowserView的setBrwViewBackground类似，原方法只设置EBrowserView的背景
     *     当网页设置弹动效果后，上下滑动到边缘后，会露出EBrowserView下面的网页，故应给EBrowserView
     *     父对象EBounceView设置背景, EBrowserView背景设成透明。
     */
    public void setBounceViewBackground(boolean flag, String bgColor, String baseUrl,
            EBrowserView mEBrowserView) {
        if (flag) {
            if(bgColor.startsWith("#") || bgColor.startsWith("rgb")){
                int color = BUtility.parseColor(bgColor);
                setBackgroundColor(color);
            }else{
                String path = BUtility.makeRealPath(BUtility.makeUrl(mEBrowserView.getCurrentUrl(baseUrl),bgColor),
                        mEBrowserView.getCurrentWidget().m_widgetPath, mEBrowserView.getCurrentWidget().m_wgtType);
                Bitmap bitmap = BUtility.getLocalImg(mContext, path);
                Drawable d = null;
                if(bitmap != null){
                    d = new BitmapDrawable(mContext.getResources(), bitmap);
                }
                int version = Build.VERSION.SDK_INT;
                if(version < 16){
                    setBackgroundDrawable(d);
                    setBackgroundColor(Color.argb(0, 0, 0, 0));
                }else{
                    setBackground(d);
                    setBackgroundColor(Color.argb(0, 0, 0, 0));
                }
            }
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void setIsSupportSwipeCallback(boolean isSupport) {
        isSupportSwipeCallback = isSupport;
    }

}
