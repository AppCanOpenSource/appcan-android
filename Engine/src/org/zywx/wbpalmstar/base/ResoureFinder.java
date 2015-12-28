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

package org.zywx.wbpalmstar.base;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class ResoureFinder {


    private static ResoureFinder resoureFinder = new ResoureFinder();


    private ResoureFinder() {

    }

    private ResoureFinder(Context context) {

    }

    /**
     * 通过传入context获得ResoureFinder实例以后调用方法可以不用传入context
     *
     * @param context
     * @return
     */
    public static ResoureFinder getInstance(Context context) {
        if (resoureFinder == null) {
            resoureFinder = new ResoureFinder(context);
        }
        return resoureFinder;
    }

    /**
     * 通过此方法获得ResoureFinder实例以后调用方法需要传入context
     *
     * @return
     */
    public static ResoureFinder getInstance() {
        return resoureFinder;
    }

    /**
     * get animation resource id according animation resource name
     *
     * @param context
     * @param animName
     * @return
     */
    public int getAnimId(Context context, String animName) {
        return EUExUtil.getResAnimID(animName);
    }

    public int getAnimId(String animName) {
        return EUExUtil.getResAnimID(animName);
    }

    /**
     * get animation object according animation resource name
     *
     * @param context
     * @param animName
     * @return
     */
    public Animation getAnimation(Context context, String animName) {
        final int animId = getAnimId(context, animName);
        if (animId == 0) {
            return null;
        } else {
            return AnimationUtils.loadAnimation(context, animId);
        }
    }

    public Animation getAnimation(String animName) {
        final int animId = getAnimId(EUExUtil.mContext, animName);
        if (animId == 0) {
            return null;
        } else {
            return AnimationUtils.loadAnimation(EUExUtil.mContext, animId);
        }
    }

    /**
     * get attribute resource id according attr resource name
     *
     * @param context
     * @param attrName
     * @return
     */
    public int getAttrId(Context context, String attrName) {
        return EUExUtil.getResAttrID(attrName);
    }

    public int getAttrId(String attrName) {
        return EUExUtil.getResAttrID(attrName);
    }

    /**
     * get color resource id according color resource name
     *
     * @param context
     * @param colorName
     * @return
     */
    public int getColorId(Context context, String colorName) {
        return EUExUtil.getResColorID(colorName);
    }

    public int getColorId(String colorName) {
        return EUExUtil.getResColorID(colorName);
    }

    /**
     * get color value according color resource name
     *
     * @param context
     * @param colorName
     * @return
     */
    public int getColor(Context context, String colorName) {
        final int colorId = getColorId(context, colorName);
        if (colorId == 0) {
            return 0;
        } else {
            return context.getResources().getColor(colorId);
        }
    }

    public int getColor(String colorName) {
        final int colorId = getColorId(EUExUtil.mContext, colorName);
        if (colorId == 0) {
            return 0;
        } else {
            return EUExUtil.resources.getColor(colorId);
        }
    }

    /**
     * get drawable resource id according drawable resource name
     *
     * @param context
     * @param drawableName
     * @return
     */
    public int getDrawableId(Context context, String drawableName) {
        return EUExUtil.getResDrawableID(drawableName);
    }

    public int getDrawableId(String drawableName) {
        return EUExUtil.getResDrawableID(drawableName);
    }

    /**
     * get drawable object according drawable resource name
     *
     * @param context
     * @param drawableName
     * @return
     */
    public Drawable getDrawable(Context context, String drawableName) {
        final int drawableId = getDrawableId(context, drawableName);
        if (drawableId == 0) {
            return null;
        } else {
            return context.getResources().getDrawable(drawableId);
        }
    }

    public Drawable getDrawable(String drawableName) {
        final int drawableId = getDrawableId(EUExUtil.mContext, drawableName);
        if (drawableId == 0) {
            return null;
        } else {
            return EUExUtil.resources.getDrawable(drawableId);
        }
    }

    /**
     * get view id according id's name
     *
     * @param context
     * @param idName
     * @return
     */
    public int getId(Context context, String idName) {
        return EUExUtil.getResIdID(idName);
    }

    public int getId(String idName) {
        return EUExUtil.getResIdID(idName);
    }

    /**
     * get layout resource id according layout resource name
     *
     * @param context
     * @param layoutName
     * @return
     */
    public int getLayoutId(Context context, String layoutName) {
        return EUExUtil.getResLayoutID(layoutName);
    }

    public int getLayoutId(String layoutName) {
        return EUExUtil.getResLayoutID(layoutName);
    }

    /**
     * get raw resoure id according raw resource name
     *
     * @param context
     * @param rawName
     * @return
     */
    public int getRawId(Context context, String rawName) {
        return EUExUtil.getResRawID(rawName);
    }

    public int getRawId(String rawName) {
        return EUExUtil.getResRawID(rawName);
    }

    /**
     * get String resource id according string resource name
     *
     * @param context
     * @param stringName
     * @return
     */
    public int getStringId(Context context, String stringName) {
        return EUExUtil.getResStringID(stringName);
    }

    public int getStringId(String stringName) {
        return EUExUtil.getResStringID(stringName);
    }

    /**
     * get String value according string resource name
     *
     * @param context
     * @param stringName
     * @return
     */
    public String getString(Context context, String stringName) {
        final int stringId = getStringId(context, stringName);
        if (stringId == 0) {
            return "";
        } else {
            return context.getResources().getString(stringId);
        }
    }

    public String getString(String stringName) {
        final int stringId = getStringId(EUExUtil.mContext, stringName);
        if (stringId == 0) {
            return "";
        } else {
            return EUExUtil.resources.getString(stringId);
        }
    }

    /**
     * get style resource id according style resource name
     *
     * @param context
     * @param styleName
     * @return
     */
    public int getStyleId(Context context, String styleName) {
        return EUExUtil.getResStyleID(styleName);
    }

    public int getStyleId(String styleName) {
        return EUExUtil.getResStyleID(styleName);
    }
}
