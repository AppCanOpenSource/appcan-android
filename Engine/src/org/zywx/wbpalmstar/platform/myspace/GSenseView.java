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

package org.zywx.wbpalmstar.platform.myspace;

import org.zywx.wbpalmstar.base.ResoureFinder;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class GSenseView extends View implements SensorEventListener {

    public static final String TAG = "GSenseView";
    private static final int GSENSE_MIN_VELOCITY = 300;
    private Bitmap bitmapBall;
    private Bitmap bitmapHole;
    private int bitmapW;
    private int bitmapH;
    public static float left;
    public static float top;
    private float radius;
    public static int viewWith;
    public static int viewHeight;
    private int maxLeft;
    private int maxTop;
    private float dissAreaLeft;
    private float dissAreaTop;
    private float dissAreaRight;
    private float dissAreaBottom;
    private Paint paint = new Paint();
    private Activity activity;
    public int windowHeight;
    public int windowWidth;
    private Vibrator mVibrator;
    private SensorManager sm;
    private long mLastTime;
    public static float ballXVelocity;
    public static float ballYVelocity;
    private WindowManager.LayoutParams params;
    private OnBallFallIntoCallback sensorCallback;
    private SoundPool soundPool;
    private int soundId;

    private ResoureFinder finder;

    public GSenseView(Context context) {
        super(context);
        init();
    }

    public GSenseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void addBallFallIntoCallback(OnBallFallIntoCallback callback) {
        this.sensorCallback = callback;
    }

    private void init() {
        finder = ResoureFinder.getInstance(getContext());
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(getContext(), finder.getRawId("collision"), 1);
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        bitmapBall = BitmapFactory.decodeResource(getResources(), finder.getDrawableId("platform_myspace_ball"));
        bitmapHole = BitmapFactory.decodeResource(getResources(), finder.getDrawableId("platform_myspace_hole"));
        bitmapW = bitmapBall.getWidth();
        bitmapH = bitmapBall.getHeight();
        radius = bitmapW / 2;
        dissAreaLeft = 200 * dm.density;
        dissAreaTop = 50 * dm.density;
        dissAreaRight = dissAreaLeft + bitmapHole.getWidth();
        dissAreaBottom = dissAreaTop + bitmapHole.getHeight();
        activity = (Activity) getContext();
        Rect rectgle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        windowWidth = rectgle.width();
        windowHeight = rectgle.height();
        mVibrator = (Vibrator) activity.getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        sm = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        setBackgroundDrawable(finder.getDrawable("platform_myspace_gsense_bg_shape"));
        params = new WindowManager.LayoutParams();
        params.height = windowHeight;
        params.width = windowWidth;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION | WindowManager.LayoutParams.FIRST_SUB_WINDOW;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;// 模态，不能获得焦点，背景失焦
        params.alpha = 1.0f;
        params.format = PixelFormat.TRANSPARENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        params.x = dm.widthPixels - windowWidth;
        params.y = dm.heightPixels - windowHeight;
        params.windowAnimations = finder.getStyleId("Anim_platform_myspace_fade");
    }

    public LayoutParams getLayoutParams() {
        return params;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWith = w;
        viewHeight = h;
        maxLeft = viewWith - bitmapW;
        maxTop = viewHeight - bitmapH;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (left < 0.0f) {
            if (Math.abs(ballXVelocity) > GSENSE_MIN_VELOCITY) {
                soundPool.play(soundId, 1.0f, 1.0f, 0, 1, 1.0f);
            }
            left = 0.0f;
            ballXVelocity = -(ballXVelocity / 1.4f);
        } else if (left > maxLeft) {
            if (Math.abs(ballXVelocity) > GSENSE_MIN_VELOCITY) {
                soundPool.play(soundId, 1.0f, 1.0f, 0, 1, 1.0f);
            }
            left = maxLeft;
            ballXVelocity = -(ballXVelocity / 1.4f);
        }
        if (top < 0.0f) {
            if (Math.abs(ballYVelocity) > GSENSE_MIN_VELOCITY) {
                soundPool.play(soundId, 1.0f, 1.0f, 0, 1, 1.0f);
            }
            top = 0.0f;
            ballYVelocity = -(ballYVelocity / 1.4f);
        } else if (top > maxTop) {
            if (Math.abs(ballYVelocity) > GSENSE_MIN_VELOCITY) {
                soundPool.play(soundId, 1.0f, 1.0f, 0, 1, 1.0f);
            }
            top = maxTop;
            ballYVelocity = -(ballYVelocity / 1.4f);
        }
        canvas.drawBitmap(bitmapHole, dissAreaLeft, dissAreaTop, paint);
        canvas.drawBitmap(bitmapBall, left, top, paint);
        float ballX = left + radius;
        float ballY = top + radius;
        float xRange = (dissAreaRight - dissAreaLeft) / 3f;
        float yRange = (dissAreaBottom - dissAreaTop) / 3f;
        if (ballX >= dissAreaLeft + xRange && ballX <= dissAreaRight - xRange && ballY >= dissAreaTop + yRange
                && ballY <= dissAreaBottom - yRange) {
            mVibrator.vibrate(500);
            performSenseAction();
        }
    }

    public void startSense() {
        mLastTime = 0;
        ballXVelocity = 0.0f;
        ballYVelocity = 0.0f;
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    public void stopSense() {
        sm.unregisterListener(this);
        mLastTime = 0;
        ballXVelocity = 0.0f;
        ballYVelocity = 0.0f;
    }

    private float startX;
    private float startY;
    private int touchSlop;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - startX) > touchSlop || Math.abs(event.getY() - startY) > touchSlop) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if ((Math.abs(event.getX() - startX) < touchSlop && Math.abs(event.getY() - startY) < touchSlop)) {
                    performSenseAction();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void performSenseAction() {
        if (sensorCallback != null) {
            stopSense();
            sensorCallback.onFallInto();
            left = 0.0f;
            top = 0.0f;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long now = System.currentTimeMillis();
        {
            long diff = 0;
            if (mLastTime != 0)
                diff = now - mLastTime;
            if (Math.abs(event.values[SensorManager.DATA_X]) > 1.5f) { //
                if (Math.abs(event.values[SensorManager.DATA_X]) < 10.0f)
                    diff = 1;
                if (mLastTime != 0)
                    ballXVelocity = ballXVelocity + (-event.values[SensorManager.DATA_X] * diff * 20);
            } else {
                ballXVelocity = ballXVelocity + (event.values[SensorManager.DATA_X]); //
            }

            if (Math.abs(event.values[SensorManager.DATA_Y]) > 1.5f) { //
                if (Math.abs(event.values[SensorManager.DATA_Y]) < 10.0f)
                    diff = 1;
                if (mLastTime != 0)
                    ballYVelocity = ballYVelocity + (event.values[SensorManager.DATA_Y] * diff * 10);
            } else {
                ballYVelocity = ballYVelocity + (-event.values[SensorManager.DATA_Y]);
            }
            mLastTime = now;
            left += ballXVelocity / 200;
            top += ballYVelocity / 200;
            invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void startVibrate() {
        mVibrator.vibrate(new long[]{100, 100, 100, 1000}, -1);
    }

    public void cancelVibrate() {
        mVibrator.cancel();
    }

    public static interface OnBallFallIntoCallback {
        void onFallInto();
    }

}
