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

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public final class YAxisAnimation extends Animation {

    private Camera mCamera;
    private final float mWidth;
    private final float mHeight;
    private int mRotationFlags;

    public YAxisAnimation(float w, float h, int flags) {
        mWidth = w;
        mHeight = h;
        mRotationFlags = flags;
    }

    public final void setRotationFlags(int flags) {
        mRotationFlags = flags;
    }

    protected final void applyTransformation(float paramFloat, Transformation transformation) {
        float f1 = 0.0F - 360.0F * paramFloat;
        float f2 = mWidth;
        float f3 = mHeight;
        final Camera camera = mCamera;
        Matrix matrix = transformation.getMatrix();
        camera.save();
        if ((0x1 & mRotationFlags) == 1)
            camera.rotateX(f1);
        if ((0x2 & mRotationFlags) == 2)
            camera.rotateY(f1);
        if ((0x4 & mRotationFlags) == 4)
            camera.rotateZ(f1);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-f2, -f3);
        matrix.postTranslate(f2, f3);
    }

    public final void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }
}