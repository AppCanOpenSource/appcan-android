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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout.LayoutParams;

import org.zywx.wbpalmstar.engine.universalex.EUExScript;

import java.util.ArrayList;

public class EBrwViewAnim {

    public static final int BrwViewAnimaCurveEaseInOut = 1;
    public static final int BrwViewAnimCurveEaseIn = 2;
    public static final int BrwViewAnimCurveEaseOut = 3;
    public static final int BrwViewAnimCurveLinear = 4;

    public ArrayList<Animator> animMatrix;

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

    private float mStartX;
    private float mStartY;
    private float mStartZ;

    private float mStartScaleX;
    private float mStartScaleY;

    private float mStartRotationX;
    private float mStartRotationY;

    private float mStartAlpha;

    public EBrwViewAnim() {
        curve = -1;
        animMatrix = new ArrayList<Animator>();
    }

    public void beginAnimition(EBrowserView target) {
        if (begin) {
            return;
        }
        reset();
        View parent = (View) target.getParent();
        if (null == parent) {
            return;
        }
        LayoutParams parm = (LayoutParams) parent.getLayoutParams();
        final_x = parm.leftMargin;
        final_y = parm.topMargin;
        final_width = parm.width;
        final_heigh = parm.height;
        mStartAlpha = target.getAlpha();
        mStartX = target.getTranslationX();
        mStartY = target.getTranslationY();
        if (Build.VERSION.SDK_INT >= 21) {
            mStartZ = target.getTranslationZ();
        }
        mStartRotationX = target.getRotationX();
        mStartRotationY = target.getRotationY();
        mStartScaleX = target.getScaleX();
        mStartScaleY = target.getScaleY();
    }

    public void setAnimitionDelay(EBrowserView target, long del) {
        if (begin) {
            return;
        }
        delay = del;
    }

    public void setAnimitionDuration(EBrowserView target, long dur) {
        if (begin) {
            return;
        }
        duration = dur;
    }

    public void setAnimitionCurve(EBrowserView target, int cur) {
        if (begin) {
            return;
        }
        curve = cur;
    }

    public void setAnimitionRepeatCount(EBrowserView target, int count) {
        if (begin) {
            return;
        }
        repeatCount = count;
    }

    public void setAnimitionAutoReverse(EBrowserView target, boolean flag) {
        if (begin) {
            return;
        }
        willReverse = flag;
    }

    public void makeTranslation(EBrowserView target, float tx, float ty, float tz) {
        if (begin) {
            return;
        }
        final_x += tx;
        final_y += ty;
        if (tx != 0) {
            float transX = target.getTranslationX();
            ObjectAnimator transAnimatorX = ObjectAnimator.ofFloat(target, "TranslationX", transX, transX + tx);
            setAnimatorRepeat(transAnimatorX);
            animMatrix.add(transAnimatorX);
        }
        if (ty != 0) {
            float transY = target.getTranslationY();
            ObjectAnimator transAnimatorY = ObjectAnimator.ofFloat(target, "TranslationY", transY, transY + ty);
            setAnimatorRepeat(transAnimatorY);
            animMatrix.add(transAnimatorY);
        }
        if (tz != 0) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                float transZ = 0;
                transZ = target.getTranslationZ();
                ObjectAnimator transAnimatorZ = ObjectAnimator.ofFloat(target, "TranslationZ", transZ, transZ + tz);
                setAnimatorRepeat(transAnimatorZ);
                animMatrix.add(transAnimatorZ);
            }

        }

    }

    public void makeScale(EBrowserView target, float tx, float ty, float tz) {
        if (begin) {
            return;
        }
        int width = final_width;
        int height = final_heigh;
        final_width = (int) (width * tx);
        final_heigh = (int) (height * ty);
        final_x -= ((tx * width) - (width)) / 2;
        final_y -= ((ty * height) - (height)) / 2;
        if (tx != 0) {
            float scaleX = target.getScaleX();
            ObjectAnimator scaleAnimatorX = ObjectAnimator.ofFloat(target, "scaleX", scaleX, tx);
            setAnimatorRepeat(scaleAnimatorX);
            animMatrix.add(scaleAnimatorX);
        }
        if (ty != 0) {
            float scaleY = target.getScaleY();
            ObjectAnimator scaleAnimatorY = ObjectAnimator.ofFloat(target, "scaleY", scaleY, ty);
            setAnimatorRepeat(scaleAnimatorY);
            animMatrix.add(scaleAnimatorY);
        }

    }

    public void makeRotate(EBrowserView target, float fdegree, float pivotX, float pivotY, float pivotZ) {
        if (begin) {
            return;
        }
        if (pivotX == 1) {
            float rotationX = target.getRotationX();
            ObjectAnimator rotationAnimatorX = ObjectAnimator.ofFloat(target, "rotationX", rotationX, fdegree);
            setAnimatorRepeat(rotationAnimatorX);
            animMatrix.add(rotationAnimatorX);
        }
        if (pivotY == 1) {
            float rotationY = target.getRotationY();
            ObjectAnimator rotationAnimatorY = ObjectAnimator.ofFloat(target, "rotationY", rotationY, fdegree);
            setAnimatorRepeat(rotationAnimatorY);
            animMatrix.add(rotationAnimatorY);
        }
        if (pivotZ == 1) {

        }
    }

    public void makeAlpha(EBrowserView target, float fc) {
        if (begin) {
            return;
        }
        float alpha = target.getAlpha();
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(target, "alpha", alpha, fc);
        setAnimatorRepeat(alphaAnimator);
        animMatrix.add(alphaAnimator);
    }

    public void reset() {
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

    public void commitAnimition(final EBrowserView Obj) {
        if (begin) {
            return;
        }
        begin = true;
        int len = animMatrix.size();
        if (0 == len) {
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
        final EBounceView target = (EBounceView) Obj.getParent();
        if (null == target) {
            return;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animMatrix);
        animatorSet.setDuration(duration);
        if (animMatrix.size() > 0) {
            animMatrix.get(0).addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (willReverse) {
                        revertView(Obj);
                    }
                    Obj.loadUrl(EUExScript.F_UEX_SCRIPT_ANIMATIONEND);
                    reset();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        if (delay > 0) {
            animatorSet.setStartDelay(delay);
        }
        animatorSet.setInterpolator(inter);
        animatorSet.setTarget(target);
        animatorSet.start();
    }

    private void revertView(EBrowserView target) {
        target.setAlpha(mStartAlpha);
        target.setTranslationX(mStartX);
        target.setTranslationY(mStartY);
        target.setScaleX(mStartScaleX);
        target.setScaleY(mStartScaleY);
        target.setRotationX(mStartRotationX);
        target.setRotationY(mStartRotationY);
        if (Build.VERSION.SDK_INT >= 21) {
            target.setTranslationZ(mStartZ);
        }
    }

    private void setAnimatorRepeat(ObjectAnimator animator) {
        animator.setRepeatCount(repeatCount);
    }
}
