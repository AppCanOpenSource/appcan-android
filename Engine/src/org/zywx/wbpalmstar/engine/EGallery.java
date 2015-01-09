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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class EGallery extends ViewGroup {

	static final int F_TOUCH_STATE_REST 		= 0;
	static final int F_TOUCH_STATE_SCROLLING 	= 1;
	
	static final int SNAP_VELOCITY = 600;
	
	private int mTouchState;
	private int mTouchSlop;
	private float mLastMotionX;
	private int mCurChild;
	
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	public EGallery(Context context) {
		this(context, null);
	}
	
	public EGallery(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int childLeft = 0;
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (child.getVisibility() != View.GONE) {
					int childWidth = child.getMeasuredWidth();
					child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
					childLeft += childWidth;
				}
			}
		}
	}


    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);   
        int width = MeasureSpec.getSize(widthMeasureSpec);   
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);   
        if (widthMode != MeasureSpec.EXACTLY) {   
            throw new IllegalStateException("can only run at EXACTLY mode!"); 
        }   
  
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);   
        if (heightMode != MeasureSpec.EXACTLY) {   
            throw new IllegalStateException("can only run at EXACTLY mode!");
        }    
        int count = getChildCount();   
        for (int i = 0; i < count; i++) {   
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);   
        }    
        scrollTo(mCurChild * width, 0);         
    }  
    
    private void backToLeftOrRight() {
    	int width = getWidth();
    	int scrollX = getScrollX();
    	int destChild = (scrollX + width / 2) / width;
    	scrollToChild(destChild);
    }
    
    private void scrollToChild(int which) {
    	which = max(0, min(which, getChildCount() - 1));
    	int width = getWidth();
    	int scrollX = getScrollX();
    	if (scrollX != (which * width)) {
    		int delta = which * width - scrollX;
    		mScroller.startScroll(scrollX, 0, delta, 0, abs(delta) * 2);
    		mCurChild = which;
    		postInvalidate();
    	}
    }
    
    public void setDisplayedChild(int which) {
    	int size = getChildCount();
    	if(which >= size){
    		return;
    	}
    	which = max(0, min(which, getChildCount() - 1));
    
    	int duration = 300;
    	int count = which - mCurChild;
    	int c = abs(count);
    	duration = (int) ((duration * c) * 0.8);
    	int dist = count * getWidth();
    	int sx = getScrollX();
    	mScroller.startScroll(sx, 0, dist, 0, duration);
    	postInvalidate();
    	
    	mCurChild = which;
    	//scrollTo(dist, 0);
    }
    
    public int getDisplayedChild() {
    	
    	return mCurChild;
    }
    
    public View getCurrentView() {

    	return getChildAt(mCurChild);
    }
    
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			int cx = mScroller.getCurrX();
//			int cy = mScroller.getCurrY();
			int cy = 0;
			scrollTo(cx, cy);
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		int action = event.getAction();
		float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()){
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int)(mLastMotionX - x);
			mLastMotionX = x;
            scrollBy(deltaX, 0);
			break;
		case MotionEvent.ACTION_UP:  
            VelocityTracker velocityTracker = mVelocityTracker;   
            velocityTracker.computeCurrentVelocity(1000);   
            int velocityX = (int)velocityTracker.getXVelocity();   
            if (velocityX > SNAP_VELOCITY && mCurChild > 0) {   
                scrollToChild(mCurChild - 1);   
            } else if (velocityX < -SNAP_VELOCITY && mCurChild < getChildCount() - 1) {   
                scrollToChild(mCurChild + 1);   
            } else {   
                backToLeftOrRight();   
            }   
            if(mVelocityTracker != null) {   
                mVelocityTracker.recycle();   
                mVelocityTracker = null;   
            }    
            mTouchState = F_TOUCH_STATE_REST;   
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = F_TOUCH_STATE_REST;
			break;
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != F_TOUCH_STATE_REST)) {
			return true;
		}
		float x = ev.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mTouchState = mScroller.isFinished() ? F_TOUCH_STATE_REST : F_TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			int xDiff = (int)abs(mLastMotionX - x);
			if (xDiff > mTouchSlop) {
				mTouchState = F_TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = F_TOUCH_STATE_REST;
			break;
		}
		return mTouchState != F_TOUCH_STATE_REST;
	}
	
	private int max(int a, int b){
    	
    	return a > b ? a : b;
    }
    
    private int min(int a, int b) {
        return a < b ? a : b;
    }
    
    private int abs(int a) {
        return a >= 0 ? a : -a;
    }
    
    private float abs(float f) {
        int bits = Float.floatToIntBits(f);
        bits &= 0x7fffffff;
        return Float.intBitsToFloat(bits);
    }
}
