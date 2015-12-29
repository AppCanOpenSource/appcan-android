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
import org.zywx.wbpalmstar.engine.ESystemInfo;
import org.zywx.wbpalmstar.platform.myspace.GSenseView.OnBallFallIntoCallback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public class DragableView extends View implements SensorEventListener {
    public static final String TAG = "DragableView";
    private DisplayMetrics dm;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private ResoureFinder finder;
    float currentX;
    float currentY;
    private SensorManager sensorManager;
    private Sensor sensor;
    private GSenseView gSenseView;
    private OnGSBallFallListener listener;
    private boolean isAddedSenseView = false;
    private int cpuMHZ;

    public DragableView(WindowManager windowManager, Context context) {
        super(context);
        this.windowManager = windowManager;
        init();
    }

    public DragableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setGSBallFallListener(OnGSBallFallListener fallListener) {
        listener = fallListener;
    }

    public WindowManager.LayoutParams getLayoutParams() {
        return params;
    }

    private void init() {
        finder = ResoureFinder.getInstance(getContext());
        cpuMHZ = ESystemInfo.getIntence().cpuMHZ;
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gSenseView = new GSenseView(getContext());
        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        dm = getContext().getResources().getDisplayMetrics();
        Drawable drawable = finder.getDrawable("browser_right_hover");
        setBackgroundDrawable(drawable);
        params.height = drawable.getIntrinsicHeight();
        params.width = drawable.getIntrinsicWidth();
        params.alpha = 1.0f;
        params.format = PixelFormat.TRANSPARENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = (int) (dm.widthPixels - 5 * dm.density - params.width);
        params.y = (int) (dm.heightPixels - 50 * dm.density - params.height);
        params.windowAnimations = finder.getStyleId("Anim_platform_myspace_fade");
    }

    float startX;
    float startY;
    boolean dragable = false;

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (cpuMHZ > 800) {
            if (visibility == View.VISIBLE) {
                // sensorManager.registerListener(this, sensor,
                // SensorManager.SENSOR_DELAY_UI);
                setClickable(true);
            } else {
                // sensorManager.unregisterListener(this, sensor);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (gSenseView != null && isAddedSenseView) {
            windowManager.removeViewImmediate(gSenseView);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 触摸点相对于屏幕左上角坐标
        boolean isHandle = false;
        currentX = event.getRawX();
        currentY = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!dragable) {
                    final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                    if (Math.abs(event.getX() - startX) > touchSlop || Math.abs(event.getY() - startY) > touchSlop) {
                        dragable = true;
                        updatePosition();
                        isHandle = true;
                    }
                }
                if (dragable) {
                    updatePosition();
                    isHandle = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (dragable) {
                    updatePosition();
                    isHandle = true;
                }
                dragable = false;
                startX = startY = 0;
                break;
        }
        return isHandle ? isHandle : super.onTouchEvent(event);
    }

    // 更新浮动窗口位置参数
    private void updatePosition() {
        // View的当前位置
        params.x = (int) (currentX - startX);
        params.y = (int) (currentY - startY);
        windowManager.updateViewLayout(this, params);
    }

    private float x, y, z, last_x, last_y, last_z;
    private long lastUpdate;
    private static final int SHAKE_THRESHOLD = 2000;

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = System.currentTimeMillis();
        // 每100毫秒检测一次
        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            x = event.values[SensorManager.DATA_X];
            y = event.values[SensorManager.DATA_Y];
            z = event.values[SensorManager.DATA_Z];
            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
            if (speed > SHAKE_THRESHOLD) {
                // 检测到摇晃后执行的代码
                setVisibility(View.GONE);
                setClickable(false);
                sensorManager.unregisterListener(this, sensor);
                if (!isAddedSenseView) {
                    windowManager.addView(gSenseView, gSenseView.getLayoutParams());
                    isAddedSenseView = true;
                    gSenseView.startSense();
                    gSenseView.addBallFallIntoCallback(new OnBallFallIntoCallback() {
                        @Override
                        public void onFallInto() {
                            windowManager.removeViewImmediate(gSenseView);
                            isAddedSenseView = false;
                            if (listener != null) {
                                listener.onGSBallFalled();
                            }
                        }
                    });

                }

            }
            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static interface OnGSBallFallListener {
        void onGSBallFalled();
    }
}
