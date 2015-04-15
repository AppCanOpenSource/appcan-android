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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;



public class AbsoluteLayout extends ViewGroup {
    public AbsoluteLayout(Context context) {
        super(context);
    }

    public AbsoluteLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsoluteLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	int count = getChildCount();
        int maxHeight = 0;
        int maxWidth = 0;
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childRight;
                int childBottom;
                AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) child.getLayoutParams();
                childRight = lp.x + child.getMeasuredWidth();
                childBottom = lp.y + child.getMeasuredHeight();
                maxWidth = Math.max(maxWidth, childRight);
                maxHeight = Math.max(maxHeight, childBottom);
            }
        }
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec), resolveSize(maxHeight, heightMeasureSpec));
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) child.getLayoutParams();
                int childLeft = getPaddingLeft() + lp.x;
                int childTop = getPaddingTop() + lp.y;
                child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(),
                        childTop + child.getMeasuredHeight());

            }
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof AbsoluteLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int x;
        public int y;
        public LayoutParams(int width, int height, int x, int y) {
            super(width, height);
            this.x = x;
            this.y = y;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public String debug(String output) {
            return output + "Absolute.LayoutParams={width="
                    + sizeToString(width) + ", height=" + sizeToString(height)
                    + " x=" + x + " y=" + y + "}";
        }
        
        protected static String sizeToString(int size) {
            if (size == WRAP_CONTENT) {
                return "wrap-content";
            }
            if (size == FILL_PARENT) {
                return "fill-parent";
            }
            return String.valueOf(size);
        }
    }
}


