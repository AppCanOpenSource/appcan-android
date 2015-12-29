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
import android.animation.ObjectAnimator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

import org.zywx.wbpalmstar.base.BDebug;

public class EBrowserAnimation {

    public static final int BASE = 999;
    public static final int ANIM_ID_NONE = 0;
    public static final int ANIM_ID_FILL = -1;
    public static final int ANIM_ID_1 = 1;        //左入
    public static final int ANIM_ID_2 = 2;        //右入
    public static final int ANIM_ID_3 = 3;        //上入
    public static final int ANIM_ID_4 = 4;        //下入
    public static final int ANIM_ID_5 = 5;        //淡入淡出

    public static final int ANIM_ID_6 = 6;        //左百叶窗(暂不支持)
    public static final int ANIM_ID_7 = 7;        //又百叶窗(暂不支持)
    public static final int ANIM_ID_8 = 8;        //水波(暂不支持)

    public static final int ANIM_ID_9 = 9;            //左切入
    public static final int ANIM_ID_10 = 10;        //右切入
    public static final int ANIM_ID_11 = 11;        //上切入
    public static final int ANIM_ID_12 = 12;        //下切入

    public static final int ANIM_ID_13 = 13;        //左切出
    public static final int ANIM_ID_14 = 14;        //右切出
    public static final int ANIM_ID_15 = 15;        //上切出
    public static final int ANIM_ID_16 = 16;        //下切出

    static final int ANIM_ContraryID_1 = ANIM_ID_1 + BASE;
    static final int ANIM_ContraryID_2 = ANIM_ID_2 + BASE;
    static final int ANIM_ContraryID_3 = ANIM_ID_3 + BASE;
    static final int ANIM_ContraryID_4 = ANIM_ID_4 + BASE;
    static final int ANIM_ContraryID_5 = ANIM_ID_5 + BASE;

    static final int ANIM_ContraryID_6 = ANIM_ID_6 + BASE;
    static final int ANIM_ContraryID_7 = ANIM_ID_7 + BASE;
    static final int ANIM_ContraryID_8 = ANIM_ID_8 + BASE;

    static final int ANIM_ContraryID_9 = ANIM_ID_9 + BASE;
    static final int ANIM_ContraryID_10 = ANIM_ID_10 + BASE;
    static final int ANIM_ContraryID_11 = ANIM_ID_11 + BASE;
    static final int ANIM_ContraryID_12 = ANIM_ID_12 + BASE;

    static final int ANIM_ContraryID_13 = ANIM_ID_13 + BASE;
    static final int ANIM_ContraryID_14 = ANIM_ID_14 + BASE;
    static final int ANIM_ContraryID_15 = ANIM_ID_15 + BASE;
    static final int ANIM_ContraryID_16 = ANIM_ID_16 + BASE;

    static final int ANIM_ContraryID_NONE = ANIM_ID_NONE + BASE;

    public final static long defaultDuration = 250;
    public final static long defaultNoneDuration = 10;

    public static Animation getAnimBuyID(int animId, long duration) {
        Animation anim = null;
        if (duration <= 0) {
            duration = defaultDuration;
        }
        switch (animId) {
            case ANIM_ID_NONE:
                anim = new AlphaAnimation(1.0f, 1.0f);
                anim.setDuration(defaultNoneDuration);
                break;
            case ANIM_ID_1:// 左入
            case ANIM_ID_9:
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ID_2:// 右入
            case ANIM_ID_10:
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ID_3:// 上入
            case ANIM_ID_11:
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, -1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ID_4:// 下入
            case ANIM_ID_12:
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ID_5:// 淡入
                anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(duration);
                break;


            case ANIM_ContraryID_1:// 右出
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ContraryID_2:// 左出
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, -1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ContraryID_3:// 上出
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 1.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ContraryID_4:// 下出
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, -1.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ContraryID_5:// 淡出
                anim = new AlphaAnimation(1.0f, 0.0f);
                anim.setDuration(duration);
                break;


            case ANIM_ContraryID_13://左切出
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ContraryID_14://右切出
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, -1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
                anim.setDuration(duration);

                break;
            case ANIM_ContraryID_15://上切出
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 1.0f);
                anim.setDuration(duration);
                break;
            case ANIM_ContraryID_16://下切出
                anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, -1.0f);
                anim.setDuration(duration);
                break;


            case ANIM_ID_6:
            case ANIM_ID_7:
            case ANIM_ID_8:
            case ANIM_ContraryID_6:
            case ANIM_ContraryID_7:
            case ANIM_ContraryID_8:

            case ANIM_ContraryID_9:
            case ANIM_ContraryID_10:
            case ANIM_ContraryID_11:
            case ANIM_ContraryID_12:

            case ANIM_ID_13:
            case ANIM_ID_14:
            case ANIM_ID_15:
            case ANIM_ID_16:
                anim = new AlphaAnimation(1.0f, 1.0f);
                anim.setDuration(duration);
                break;

            case ANIM_ContraryID_NONE:
                anim = new AlphaAnimation(1.0f, 1.0f);
                anim.setDuration(defaultNoneDuration);
                break;
            default:
                anim = new AlphaAnimation(1.0f, 1.0f);
                anim.setDuration(defaultNoneDuration);
                break;
        }
//		AccelerateInterpolator inter = new AccelerateInterpolator();
//		anim.setInterpolator(inter);
        return anim;
    }

    public static Animation[] getAnimPair(int animationID, long time) {
        Animation[] pair = new Animation[2];
        pair[0] = getAnimBuyID(animationID, time);
        pair[1] = getAnimBuyID(animationID + BASE, time);
        return pair;
    }

    public static int contrary(int inID) {
        int ret = ANIM_ID_NONE;
        switch (inID) {
            case ANIM_ID_5:
                ret = ANIM_ID_5;
                break;
            case ANIM_ID_9:
                ret = ANIM_ID_14;
                break;
            case ANIM_ID_10:
                ret = ANIM_ID_13;
                break;
            case ANIM_ID_11:
                ret = ANIM_ID_16;
                break;
            case ANIM_ID_12:
                ret = ANIM_ID_15;
                break;
            case ANIM_ID_6:
            case ANIM_ID_7:
            case ANIM_ID_8:
                ret = ANIM_ID_NONE;
                break;
            default:
                int a = inID % 2;
                ret = a == 1 ? (inID + 1) : (inID - 1);
                break;
        }
        return ret;
    }

    public static boolean isNoneAnim(int id) {

        return ANIM_ID_NONE == id;
    }

    public static boolean isFillAnim(int id) {

        return ANIM_ID_FILL == id;
    }

    class Animation3D extends Animation {
        private final float m_fromDegree;
        private final float m_toDegree;
        private final float m_centerX;
        private final float m_centerY;
        private final float m_depthZ;//翻转程度
        private final boolean m_reverse;//是否翻转
        private Camera m_camera;

        public Animation3D(float fromDegree, float toDegree, float centerX, float centerY, float depthZ, boolean reverse) {
            m_fromDegree = fromDegree;
            m_toDegree = toDegree;
            m_centerX = centerX;
            m_centerY = centerY;
            m_depthZ = depthZ;
            m_reverse = reverse;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            m_camera = new Camera();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final float fromDegrees = m_fromDegree;
            float degrees = fromDegrees + ((m_toDegree - fromDegrees) * interpolatedTime);
            final float centerX = m_centerX;
            final float centerY = m_centerY;
            final Camera camera = m_camera;
            final Matrix matrix = t.getMatrix();
            camera.save();
            if (m_reverse) {
                camera.translate(0.0f, 0.0f, m_depthZ * interpolatedTime);
            } else {
                camera.translate(0.0f, 0.0f, m_depthZ * (1.0f - interpolatedTime));
            }
            camera.rotateY(degrees);
            camera.getMatrix(matrix);
            camera.restore();
            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
        }
    }

    public static void animFromRight(View target, float width, long duration, long delayTime, final AnimatorListener
            callback) {
        BDebug.i("width", width);
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", width, 0);
        animator.setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.setStartDelay(delayTime);
        animator.start();
    }

    public interface AnimatorListener {
        void onAnimationEnd();
    }

}
