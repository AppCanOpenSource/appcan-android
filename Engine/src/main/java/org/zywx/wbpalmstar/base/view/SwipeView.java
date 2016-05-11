package org.zywx.wbpalmstar.base.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

@SuppressLint("NewApi")
public class SwipeView extends FrameLayout {

    public static final String TAG = SwipeView.class.getSimpleName();

    public static boolean sNavFlag = false;//设置全部Window是否可以滑动

    /**
     * 是否可以滑动关闭页面
     */
    protected boolean mSwipeEnabled = true;

    /**
     * 是否可以在页面任意位置右滑关闭页面，如果是false则从左边滑才可以关闭。
     */
    protected boolean mSwipeAnyWhere = false;

    boolean mCanSwipe = false;
    /**
     * 超过了touchslop仍然没有达到没有条件，则忽略以后的动作
     */
    boolean mIgnoreSwipe = false;
    int mSideWidthInDP = 16;
    int mSideWidth = 72;
    int mScreenWidth = 1080;
    VelocityTracker mVelocityTracker;

    float mDownX;
    float mDownY;
    float mLastX;
    float mCurrentX;
    float mCurrentY;

    int mTouchSlopDP = 30;
    int mTouchSlop = 60;

    boolean mAbleToSwipe = true;//控制每个Window是否可以滑动（root Window不能滑动）

    private OnViewClosedListener mOnViewClosedListener = null;

    public SwipeView(Context context) {
        super(context);
        init();

    }

    public SwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public void setSwipeAnyWhere(boolean swipeAnyWhere) {
        this.mSwipeAnyWhere = swipeAnyWhere;
    }

    public boolean isSwipeAnyWhere() {
        return mSwipeAnyWhere;
    }

    public void setSwipeEnabled(boolean swipeEnabled) {
        this.mSwipeEnabled = swipeEnabled;
    }

    public boolean isSwipeEnabled() {
        return mSwipeEnabled;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Activity.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private boolean swipeFinished = false;


    public void init() {
        mTouchSlop = (int) (mTouchSlopDP * getContext().getResources().getDisplayMetrics().density);
        mSideWidth = (int) (mSideWidthInDP * getContext().getResources().getDisplayMetrics().density);
        mScreenWidth = getScreenWidth(getContext());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mAbleToSwipe && sNavFlag && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mSwipeEnabled && !mCanSwipe && !mIgnoreSwipe) {
                if (mSwipeAnyWhere) {
                    switch (ev.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mDownX = ev.getRawX();
                            mDownY = ev.getRawY();
                            mCurrentX = mDownX;
                            mCurrentY = mDownY;
                            mLastX = mDownX;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float dx = ev.getRawX() - mDownX;
                            float dy = ev.getRawY() - mDownY;
                            if (dx * dx + dy * dy > mTouchSlop * mTouchSlop) {
                                if (dy == 0f || Math.abs(dx / dy) > 1) {
                                    mDownX = ev.getRawX();
                                    mDownY = ev.getRawY();
                                    mCurrentX = mDownX;
                                    mCurrentY = mDownY;
                                    mLastX = mDownX;
                                    mCanSwipe = true;
                                    mVelocityTracker = VelocityTracker.obtain();
                                    return true;
                                } else {
                                    mIgnoreSwipe = true;
                                }
                            }
                            break;
                    }
                } else if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getRawX() < mSideWidth) {
                    mCanSwipe = true;
                    mVelocityTracker = VelocityTracker.obtain();
                    return true;
                }
            }
            if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                mIgnoreSwipe = false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mCanSwipe || super.onInterceptTouchEvent(ev);
    }

    boolean hasIgnoreFirstMove;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCanSwipe) {
            mVelocityTracker.addMovement(event);
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    mCurrentX = mDownX;
                    mCurrentY = mDownY;
                    mLastX = mDownX;
                    break;
                case MotionEvent.ACTION_MOVE:
                    mCurrentX = event.getRawX();
                    mCurrentY = event.getRawY();
                    float dx = mCurrentX - mLastX;
                    if (dx != 0f && !hasIgnoreFirstMove) {
                        hasIgnoreFirstMove = true;
                        dx = dx / dx;
                    }
                    if (getContentX() + dx < 0) {
                        setContentX(0);
                    } else {
                        Log.i(TAG, "dx: " + dx);
                        setContentX(mCurrentX);
                    }
                    mLastX = mCurrentX;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mVelocityTracker.computeCurrentVelocity(10000);
                    mVelocityTracker.computeCurrentVelocity(1000, 20000);
                    mCanSwipe = false;
                    hasIgnoreFirstMove = false;
                    int mv = mScreenWidth / 200 * 1000;
                    if (Math.abs(mVelocityTracker.getXVelocity()) > mv) {
                        animateFromVelocity(mVelocityTracker.getXVelocity());
                    } else {
                        if (getContentX() > mScreenWidth / 2) {
                            animateFinish(false);
                        } else {
                            animateBack(false);
                        }
                    }
                    mVelocityTracker.recycle();
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    ObjectAnimator animator;

    public void cancelPotentialAnimation() {
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
        }
    }

    public void setContentX(float x) {
        int ix = (int) x;
        Log.i(TAG, "ContentX: " + ix);
        this.setX(ix);
        invalidate();
    }

    public float getContentX() {
        return this.getX();
    }


    /**
     * 弹回，不关闭，因为left是0，所以setX和setTranslationX效果是一样的
     *
     * @param withVel 使用计算出来的时间
     */
    private void animateBack(boolean withVel) {
        cancelPotentialAnimation();
        animator = ObjectAnimator.ofFloat(this, "contentX", getContentX(), 0);
        int tmpDuration = withVel ? ((int) (duration * getContentX() / mScreenWidth)) : duration;
        if (tmpDuration < 100) {
            tmpDuration = 100;
        }
        animator.setDuration(tmpDuration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private void animateFinish(boolean withVel) {
        cancelPotentialAnimation();
        animator = ObjectAnimator.ofFloat(this, "contentX", getContentX(), mScreenWidth);
        int tmpDuration = withVel ? ((int) (duration * (mScreenWidth - getContentX()) / mScreenWidth)) : duration;
        if (tmpDuration < 100) {
            tmpDuration = 100;
        }
        animator.setDuration(tmpDuration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                swipeFinished = true;
                if (mOnViewClosedListener != null) {
                    mOnViewClosedListener.onViewClosed();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();
    }

    private final int duration = 200;

    private void animateFromVelocity(float v) {
        if (v > 0) {
            if (getContentX() < mScreenWidth / 2 && v * duration / 1000 + getContentX() < mScreenWidth / 2) {
                animateBack(false);
            } else {
                animateFinish(true);
            }
        } else {
            if (getContentX() > mScreenWidth / 2 && v * duration / 1000 + getContentX() > mScreenWidth / 2) {
                animateFinish(false);
            } else {
                animateBack(true);
            }
        }

    }

    public void setOnViewClosedListener(OnViewClosedListener onViewClosedListener) {
        mOnViewClosedListener = onViewClosedListener;
    }

    public interface OnViewClosedListener {
        void onViewClosed();
    }

    public void setAbleToSwipe(boolean ableToSwipe) {
        this.mAbleToSwipe = ableToSwipe;
    }

}
