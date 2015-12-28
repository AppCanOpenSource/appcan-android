/*
 * Copyright (c) 2015.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

/**
 * Created by ylt on 15/12/24.
 */
public class CreateContainerVO implements Serializable {

    private float x;
    private float y;
    private float w;
    private float h;
    private String id;
    private long animTime = 300;
    private long animDelayTime = 100;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getAnimTime() {
        return animTime;
    }

    public void setAnimTime(long animTime) {
        this.animTime = animTime;
    }

    public long getAnimDelayTime() {
        return animDelayTime;
    }

    public void setAnimDelayTime(long animDelayTime) {
        this.animDelayTime = animDelayTime;
    }
}
