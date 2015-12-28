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


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExEventListener;
import org.zywx.wbpalmstar.engine.universalex.EUExWidget.SpaceClickListener;
import org.zywx.wbpalmstar.platform.myspace.DragableView;
import org.zywx.wbpalmstar.platform.myspace.DragableView.OnGSBallFallListener;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.util.ArrayList;
import java.util.List;

public class EBrowserAround implements android.view.View.OnClickListener {

    private View mScreenSplash;
    private DragableView mHover;
    private int mSpaceFlag;
    private boolean mTimeFlag;
    private boolean mInSubWidget;
    private boolean mSpaceShow;
    private Context mContext;
    private List<EUExEventListener> mEventListeners;
    private SpaceClickListener listener;

    private EBrowserWidgetPool mBrwWidPool;
    private boolean mIsSpaceEnable = false;

    public EBrowserAround(View layout) {
        mScreenSplash = layout;
        mContext = layout.getContext();
        mEventListeners = new ArrayList<EUExEventListener>();
    }

    public void init(EBrowserWidgetPool brwWidPool) {

        mBrwWidPool = brwWidPool;
    }

    public void onResume() {
        for (EUExEventListener lis : mEventListeners) {
            lis.onEvent(EUExBase.F_UEX_EVENT_TYPE_APP_ON_RESUME);
        }
    }

    public void onPause() {
        for (EUExEventListener lis : mEventListeners) {
            lis.onEvent(EUExBase.F_UEX_EVENT_TYPE_APP_ON_PAUSE);
        }
    }

    public boolean onExit() {
        boolean beHandler = false;
        for (EUExEventListener lis : mEventListeners) {
            boolean one = lis.onEvent(EUExBase.F_UEX_EVENT_TYPE_APP_EXIT);
            if (one) {
                beHandler = true;
                break;
            }
        }
        return beHandler;
    }

    private void onReady() {
        for (EUExEventListener lis : mEventListeners) {
            lis.onEvent(EUExBase.F_UEX_EVENT_TYPE_APP_ON_READY);
        }
    }

    public void registerAppEventListener(EUExEventListener listener) {
        if (null != listener && !mEventListeners.contains(listener)) {
            mEventListeners.add(listener);
        }
    }

    public void unRegisterAppEventListener(EUExEventListener listener) {
        if (null != listener) {
            mEventListeners.remove(listener);
        }
    }

    public void hiddenSplashScreen(int flag) {
//		Animation nin = new AlphaAnimation(1.0f, 0.0f);
//		nin.setDuration(300);
//		nin.setAnimationListener(new AnimationListener() {
//			public void onAnimationStart(Animation animation) {}
//			public void onAnimationRepeat(Animation animation) {}
//			public void onAnimationEnd(Animation animation) {
//				mScreenSplash.setVisibility(View.GONE);
//				ViewGroup parent = (ViewGroup) mScreenSplash.getParent();
//				if(null != parent){
//					parent.removeView(mScreenSplash);
//					mScreenSplash = null;
//				}
//				onReady();
//			}
//		});
//		mScreenSplash.startAnimation(nin);

        mScreenSplash.setVisibility(View.GONE);
        ViewGroup parent = (ViewGroup) mScreenSplash.getParent();
        if (null != parent) {
            parent.removeView(mScreenSplash);
            mScreenSplash = null;
        }
        onReady();
        if (ESystemInfo.getIntence().mIsDevelop || !checkSpaceFlag(WWidgetData.F_SPACESTATUS_CLOSE)) {
            checkHover();
            mHover.setVisibility(View.VISIBLE);
        }
        setTimeFlag(false);
    }

    public void setTimeFlag(boolean flag) {
        if (mTimeFlag != flag) {
            mTimeFlag = flag;
        }
    }

    public boolean checkTimeFlag() {

        return mTimeFlag;
    }

    public void removeViewImmediate() {
        if (null != mHover) {
            ((Activity) mContext).getWindowManager().removeViewImmediate(mHover);
        }
    }

    public void showHover(boolean isInSubWidget) {
        if (ESystemInfo.getIntence().mIsDevelop) {
            checkHover();
            mHover.setVisibility(View.VISIBLE);
        } else {
            if (checkSpaceFlag(WWidgetData.F_SPACESTATUS_CLOSE))
                return;
            checkHover();
            mHover.setVisibility(View.VISIBLE);
        }
        mInSubWidget = isInSubWidget;
        mSpaceShow = false;
    }

    public void hiddenHover(boolean isInSubWidget) {
        if (ESystemInfo.getIntence().mIsDevelop || !mHover.isShown()) {
            return;
        }
        mHover.setVisibility(View.GONE);
        mInSubWidget = isInSubWidget;
    }

    public void setSpaceFlag(int flag) {

        mSpaceFlag = flag;
    }

    private boolean checkSpaceFlag(int value) {

        return value == mSpaceFlag;
    }

    private void checkHover() {
        if (null == mHover) {
            WindowManager wmgr = ((Activity) mContext).getWindowManager();
            mHover = new DragableView(wmgr, mContext);
            mHover.setClickable(true);
            mHover.setVisibility(View.GONE);
            mHover.setGSBallFallListener(new OnGSBallFallListener() {
                @Override
                public void onGSBallFalled() {
                    mBrwWidPool.goMySpace();
                }
            });
            mHover.setOnClickListener(this);
            wmgr.addView(mHover, mHover.getLayoutParams());
        }
    }

    public void clean() {
        mContext = null;
        mHover = null;
        mScreenSplash = null;
        mEventListeners.clear();
    }

    @Override
    public void onClick(View view) {
        if (mIsSpaceEnable && view == mHover) {
            listener.onSpaceClick();
            return;
        }

        EBrowserActivity ctx = (EBrowserActivity) mContext;
        if (ctx.customViewShown()) {
            ctx.hideCustomView();
            return;
        }
        if (ESystemInfo.getIntence().mIsDevelop || mInSubWidget) {
            mBrwWidPool.finishWidget(null, null, true);
            return;
        }
        if (mSpaceShow) {
            return;
        }
        mBrwWidPool.goMySpace();
        mSpaceShow = true;
    }

    public void setSpaceEnable(SpaceClickListener listener) {
        mIsSpaceEnable = true;
        this.listener = listener;
        checkHover();
        mHover.setVisibility(View.VISIBLE);
    }
}
