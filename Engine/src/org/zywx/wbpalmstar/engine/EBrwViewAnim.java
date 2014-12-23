/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.engine;

import java.util.ArrayList;
import org.zywx.wbpalmstar.engine.universalex.EUExScript;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class EBrwViewAnim {

	public static final int BrwViewAnimaCurveEaseInOut 	= 1;
	public static final int BrwViewAnimCurveEaseIn 		= 2;
	public static final int BrwViewAnimCurveEaseOut 	= 3;
	public static final int BrwViewAnimCurveLinear 		= 4;

	public ArrayList<Animation> animMatrix;
	
	public int final_x;
	public int final_y;
	public int final_width;
	public int final_heigh;
	
	public int curve;
	public long delay;
	public long duration;
	public int repeatCount;
	public boolean autoReverse;

	private boolean begin;
	private boolean willReverse;
	
	public EBrwViewAnim(){
		curve = -1;
		animMatrix = new ArrayList<Animation>();
	}
	
	public void beginAnimition(EBrowserView target){
		if(begin){
			return;
		}
		reset();
		View parent = (View)target.getParent();
		if(null == parent){
			return;
		}
		FrameLayout.LayoutParams parm = (LayoutParams) parent.getLayoutParams();
		final_x = parm.leftMargin;
		final_y = parm.topMargin;
		final_width = parm.width;
		final_heigh = parm.height;
	}
	
	public void setAnimitionDelay(EBrowserView target, long del){
		if(begin){
			return;
		}
		delay = del;
	}
	
	public void setAnimitionDuration(EBrowserView target, long dur){
		if(begin){
			return;
		}
		duration = dur;
	}
	
	public void setAnimitionCurve(EBrowserView target, int cur){
		if(begin){
			return;
		}
		curve = cur;
	}
	
	public void setAnimitionRepeatCount(EBrowserView target, int count){
		if(begin){
			return;
		}
		repeatCount = count;
	}
	
	public void setAnimitionAutoReverse(EBrowserView target, boolean flag){
		if(begin){
			return;
		}
		willReverse = flag;
	}
	
	public void makeTranslation(EBrowserView target, float tx, float ty, float tz){
		if(begin){
			return;
		}
		final_x += tx;
		final_y += ty;
		TranslateAnimation tranAnim = new TranslateAnimation(0.0f, tx, 0.0f, ty);
		animMatrix.add(tranAnim);
	}
	
	public void makeScale(EBrowserView target, float tx, float ty, float tz){
		if(begin){
			return;
		}
//		int width = final_width;
//		int height = final_heigh;
//		final_width = (int)(width * tx);
//		final_heigh = (int)(height * ty);
//		final_x -= ((tx * width) - (width)) / 2;
//		final_y -= ((ty * height) - (height)) / 2;
//		ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, tx, 1.0f, ty, 
//				Animation.RELATIVE_TO_SELF, 0.5f, 
//				Animation.RELATIVE_TO_SELF, 0.5f);
//		animMatrix.add(scaleAnim);
	}
	
	public void makeRotate(EBrowserView target, float fdegree, float pivotX, float pivotY, float pivotZ){
		if(begin){
			return;
		}
//		boolean rotate = pivotZ != 0 ? true : false;
//		if(rotate){
//			RotateAnimation rotateAnim =new RotateAnimation ( 0, fdegree, 
//					Animation.RELATIVE_TO_SELF, 0.5f, 
//					Animation.RELATIVE_TO_SELF, 0.5f );
//			animMatrix.add(rotateAnim);
//		}
		 
	}
	
	public void makeAlpha(EBrowserView target, float fc){
		if(begin){
			return;
		}
//		AlphaAnimation alphaAnim = new AlphaAnimation(0, fc);
//		animMatrix.add(alphaAnim);
	}
	
	public void reset(){
		final_x = 0;
		final_y = 0;
		final_width = 0;
		final_heigh = 0;
		delay = 0;
		duration = 0;
		repeatCount = 0;
		curve = -1;
		autoReverse = false;
		willReverse = false;
		begin = false;
		animMatrix.clear();
	}
	
	public void commitAnimition(final EBrowserView Obj){
		if(begin){
			return;
		}
		begin = true;
		int len = animMatrix.size();
		if(0 == len){
			return;
		}
		Interpolator inter = new LinearInterpolator();
		switch (curve) {
		case BrwViewAnimaCurveEaseInOut://加速->减速
			inter = new AccelerateDecelerateInterpolator();
			break;
		case BrwViewAnimCurveEaseIn://加速
			inter = new AccelerateInterpolator();
			break;
		case BrwViewAnimCurveEaseOut://减速
			inter = new DecelerateInterpolator();
			break;
		case BrwViewAnimCurveLinear://线性平滑
			break;
		}
		final EBounceView target = (EBounceView)Obj.getParent();
		if(null == target){
			return;
		}
		final AnimationSet animationSet = new AnimationSet(true);
		for(int i = 0; i < len; ++i){
			Animation anim = animMatrix.get(i);
			anim.setRepeatCount(repeatCount);
			animationSet.addAnimation(anim);
		}
		animationSet.setInterpolator(inter);
		if(delay > 0){
			animationSet.setStartOffset(delay);
		}
		animationSet.setDuration(duration);
		if(!willReverse){
			animationSet.setFillEnabled(true);
			animationSet.setFillAfter(true);
		}
		animationSet.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationEnd(Animation animation) {
				if(!willReverse){
					FrameLayout.LayoutParams parms = (LayoutParams) target.getLayoutParams();
					int temX = final_x;
					int temY = final_y;
					int temW = final_width;
					int temH = final_heigh;
					parms.leftMargin = temX;
					parms.topMargin = temY;
					parms.width = temW;
					parms.height = temH;
					target.setLayoutParams(parms);
				}
				Obj.loadUrl(EUExScript.F_UEX_SCRIPT_ANIMATIONEND);
				animationSet.setAnimationListener(null);
				target.clearAnimation();
				reset();
			}
		});
		animationSet.setStartTime(Animation.START_ON_FIRST_FRAME);
		target.startAnimation(animationSet);
	}
}
