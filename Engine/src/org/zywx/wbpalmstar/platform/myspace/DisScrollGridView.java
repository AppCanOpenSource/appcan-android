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

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.GridView;
import android.widget.ListAdapter;

public class DisScrollGridView extends GridView {

    public DisScrollGridView(Context context) {
        super(context);
    }

    public DisScrollGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DisScrollGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private int srcPaddingTop = -1;
    private int srcPaddingBottom = -1;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (srcPaddingTop == -1 || srcPaddingBottom == -1) {
            srcPaddingTop = getPaddingTop();
            srcPaddingBottom = getPaddingBottom();
        }
        ListAdapter adapter = getAdapter();
        if (adapter == null || adapter.getCount() == 0) {
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            this.setPadding(getPaddingLeft(), getPaddingLeft() + (int) (40 * dm.density), getPaddingRight(),
                    getPaddingRight() + (int) (40 * dm.density));
        } else {
            this.setPadding(getPaddingLeft(), srcPaddingTop, getPaddingRight(), srcPaddingBottom);
        }
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
