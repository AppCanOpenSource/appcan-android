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


import org.zywx.wbpalmstar.engine.external.YAxisImageView;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EBounceViewHeader extends RelativeLayout {

    public static final int F_ROTATE_DOWN = 0;
    public static final int F_ROTATE_UP = 1;

    static final int F_CONTENT_ID = 0x000110;
    static final int F_WAP_ID = 0x000111;

    private boolean mDonghang;
    private boolean mContentEmpty;
    private RelativeLayout wap;
    private TextView mContent;
    private TextView mLevelContent;
    private ProgressBar mProgress;
    private YAxisImageView mYAxisProgress;
    private ImageView mArrowImage;
    private RotateAnimation mAnimationDown;
    private RotateAnimation mAnimationUp;
    private int textColor = 0xFF717171;
    private String levelText;
    private String pullToReloadText = EUExUtil.getString("platform_myspace_pull_to_refresh");
    private String releaseToReloadText = EUExUtil.getString("platform_myspace_release_to_refresh");
    private String loadingText = EUExUtil.getString("platform_myspace_loading");

    public EBounceViewHeader(Context context, int type) {
        super(context);
        setWillNotDraw(true);
        setBackgroundColor(0);
        setFocusable(false);
        ESystemInfo intence = ESystemInfo.getIntence();
        int height = intence.mDefaultBounceHeight;
        RelativeLayout wapper = new RelativeLayout(context);
        wapper.setWillNotDraw(true);
        wapper.setBackgroundColor(0);
        wapper.setFocusable(false);
        RelativeLayout.LayoutParams wParm = new LayoutParams(-1, height);
        if (type == EViewEntry.F_BOUNCE_TYPE_TOP) {
            wParm.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        } else {
            wParm.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        }
        wapper.setLayoutParams(wParm);
        addView(wapper);

        wap = new RelativeLayout(context);
        wap.setId(F_WAP_ID);
        RelativeLayout.LayoutParams wm = new LayoutParams(-2, height);
        wm.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        wm.leftMargin = 30;
        wap.setLayoutParams(wm);

        mContent = new TextView(context);
        mContent.setId(F_CONTENT_ID);
        RelativeLayout.LayoutParams parmMsg = new LayoutParams(-2, -2);
        parmMsg.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mContent.setLayoutParams(parmMsg);
        mContent.setTextColor(textColor);
        mContent.setText(pullToReloadText);
        mContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float) (intence.mDefaultNatvieFontSize));
        mContent.setVisibility(GONE);
        wap.addView(mContent);

        mLevelContent = new TextView(context);
        RelativeLayout.LayoutParams parml = new LayoutParams(-2, -2);
        parml.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        parml.addRule(RelativeLayout.BELOW, F_CONTENT_ID);
        mLevelContent.setLayoutParams(parml);
        mLevelContent.setTextColor(textColor);
        mLevelContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float) (intence.mDefaultNatvieFontSize * 0.6));
        mLevelContent.setVisibility(GONE);
        wap.addView(mLevelContent);

        wapper.addView(wap);

        mProgress = new ProgressBar(context);
        mProgress.setIndeterminate(true);
        int use = height - 12;
        RelativeLayout.LayoutParams parmPro = new LayoutParams(use, use);
        parmPro.addRule(RelativeLayout.LEFT_OF, F_WAP_ID);
        parmPro.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mProgress.setLayoutParams(parmPro);
        mProgress.setVisibility(GONE);
        wapper.addView(mProgress);

        mYAxisProgress = new YAxisImageView(context);
        int useY = height - 12;
        RelativeLayout.LayoutParams parmProY = new LayoutParams(useY, useY);
        parmProY.addRule(RelativeLayout.LEFT_OF, F_WAP_ID);
        parmProY.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mYAxisProgress.setLayoutParams(parmProY);
        mYAxisProgress.setVisibility(GONE);
        wapper.addView(mYAxisProgress);

        mArrowImage = new ImageView(context);
        int useA = height - 12;
        RelativeLayout.LayoutParams parmImage = new LayoutParams(useA, useA);
        parmImage.addRule(RelativeLayout.LEFT_OF, F_WAP_ID);
        parmImage.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mArrowImage.setLayoutParams(parmImage);
        Drawable icon = context.getResources().getDrawable(EResources.platform_myspace_pulltorefresh_arrow);
        mArrowImage.setImageDrawable(icon);
        mArrowImage.setVisibility(GONE);
        wapper.addView(mArrowImage);

        mAnimationUp = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mAnimationUp.setInterpolator(new AccelerateInterpolator());
        mAnimationUp.setDuration(250);
        mAnimationUp.setFillAfter(true);

        mAnimationDown = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mAnimationDown.setInterpolator(new AccelerateInterpolator());
        mAnimationDown.setDuration(250);
        mAnimationDown.setFillAfter(true);
    }

    public void setDonghang(boolean flag) {
        mDonghang = flag;
    }

    public void setContentEmpty(boolean empty) {
        mContentEmpty = empty;
        if (mContentEmpty) {
            wap.setVisibility(GONE);
            mYAxisProgress.setRotationFlags(4);
        }
    }

    public void changeText(String text) {
        mContent.setText(text);
    }

    public void setArrowVisibility(int v) {
        mArrowImage.setVisibility(v);
        if (v == GONE) {
            mArrowImage.clearAnimation();
        }
    }

    public void rotateArrowImage(int type) {
        mArrowImage.clearAnimation();
        switch (type) {
            case F_ROTATE_DOWN:
                mArrowImage.startAnimation(mAnimationDown);
                break;
            case F_ROTATE_UP:
                mArrowImage.startAnimation(mAnimationUp);
                break;
        }
    }

    public void setProgressBarVisibility(int v) {
        if (mDonghang) {
            mYAxisProgress.setVisibility(v);
            if (v == GONE) {
                mYAxisProgress.clearAnimation();
            }
        } else {
            mProgress.setVisibility(v);
        }
    }

    public void setTextVisibility(int v) {
        if (mContentEmpty) {
            wap.setVisibility(GONE);
        } else {
            mContent.setVisibility(v);
        }
    }

    public void componentsEnable() {
        mArrowImage.setVisibility(VISIBLE);
        if (mContentEmpty) {
            wap.setVisibility(GONE);
        } else {
            mContent.setVisibility(VISIBLE);
        }
    }

    public void componentsDisable() {
        mArrowImage.setVisibility(GONE);
        mContent.setVisibility(GONE);
        mYAxisProgress.setVisibility(GONE);
        mProgress.setVisibility(GONE);
        mYAxisProgress.clearAnimation();
    }

    public void setImage(Bitmap pic) {
        mArrowImage.setImageBitmap(pic);
    }

    public void setTextColor(int color) {
        mContent.setTextColor(color);
    }

    public void setLevelText(String value) {
        levelText = value;
        RelativeLayout.LayoutParams parmMsg = new LayoutParams(-2, -2);
        parmMsg.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        mContent.setLayoutParams(parmMsg);
        mContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float) (ESystemInfo.getIntence().mDefaultNatvieFontSize));
        mLevelContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float) (ESystemInfo.getIntence().mDefaultNatvieFontSize * 0.6));
        mLevelContent.setText(levelText);
        mLevelContent.setVisibility(VISIBLE);
    }

    public void setPullToReloadText(String value) {
        pullToReloadText = value;
        showPullToReloadText();
    }

    public void setReleaseToReloadText(String value) {
        releaseToReloadText = value;
        showReleaseToReloadText();
    }

    public void setLoadingText(String value) {
        loadingText = value;
        showLoadingText();
    }

    public void setArrowhead(String path) {
        Bitmap pic = ((EBrowserActivity) getContext()).getImage(path);
        if (null != pic) {
//			BitmapDrawable bd = new BitmapDrawable(pic);
//			mArrowImage.setBackgroundDrawable(bd);
            mArrowImage.setImageBitmap(pic);
        }
    }

    public void setLoadingPic(String path) {
        Bitmap pic = ((EBrowserActivity) getContext()).getImage(path);
        if (null != pic) {
            setDonghang(true);
            mYAxisProgress.setImageBitmap(pic);
//			mProgress.setVisibility(GONE);
//			mYAxisProgress.setVisibility(VISIBLE);
        } else {
            setDonghang(false);
//			mProgress.setVisibility(VISIBLE);
//			mYAxisProgress.setVisibility(GONE);
            mYAxisProgress.clearAnimation();
        }
    }

    public void showPullToReloadText() {
        if (mContentEmpty) {
            wap.setVisibility(GONE);
        } else {
            mContent.setText(pullToReloadText);
        }
    }

    public void showReleaseToReloadText() {
        if (mContentEmpty) {
            wap.setVisibility(GONE);
        } else {
            mContent.setText(releaseToReloadText);
        }
    }

    public void showLoadingText() {
        if (mContentEmpty) {
            wap.setVisibility(GONE);
        } else {
            mContent.setText(loadingText);
        }
    }
}
