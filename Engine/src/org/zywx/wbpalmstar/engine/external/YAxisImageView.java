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

package org.zywx.wbpalmstar.engine.external;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class YAxisImageView extends ImageView {
	
	private YAxisAnimation mAnimation = null;
	private boolean mRunning = false;
	private int mRotationFlags = 2;

	public YAxisImageView(Context paramContext) {
		super(paramContext);
	}

	public YAxisImageView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public YAxisImageView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	private void start() {
		int width = getWidth();
		if ((!mRunning) && (width > 0) && (getVisibility() == 0)) {
			mRunning = true;
			if (mAnimation == null) {
				mAnimation = new YAxisAnimation(width / 2.0F, getHeight() / 2.0F, mRotationFlags);
				mAnimation.setDuration(1000L);
				mAnimation.setInterpolator(new LinearInterpolator());
				mAnimation.setRepeatCount(-1);
				mAnimation.setRepeatMode(1);
			}
			startAnimation(mAnimation);
		}
	}

	private void stop() {
		if (mRunning) {
			mRunning = false;
			setAnimation(null);
			mAnimation = null;
			clearAnimation();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		start();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stop();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w > 0){
			start();
		}
	}
	
	@Override
	protected void onVisibilityChanged(View v, int visibility) {
		super.onVisibilityChanged(v, visibility);
		if ((visibility == INVISIBLE) || (visibility == GONE)){
			stop();
		}else{
			start();
		}
	}

	public void setRotationFlags(int paramInt) {
		mRotationFlags = paramInt;
		if (mAnimation != null){
			mAnimation.setRotationFlags(mRotationFlags);
		}
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if ((visibility == INVISIBLE) || (visibility == GONE)){
			stop();
		}else{
			start();
		}
	}
}