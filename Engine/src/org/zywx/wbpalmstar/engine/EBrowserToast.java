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
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EBrowserToast extends RelativeLayout {

    public static final int TOAST_LOCATION_LEFT_TOP = 1;
    public static final int TOAST_LOCATION_TOP = 2;
    public static final int TOAST_LOCATION_RIGHT_TOP = 3;
    public static final int TOAST_LOCATION_LEFT = 4;
    public static final int TOAST_LOCATION_MIDDLE = 5;
    public static final int TOAST_LOCATION_RIGHT = 6;
    public static final int TOAST_LOCATION_BOTTOM_LEFT = 7;
    public static final int TOAST_LOCATION_BOTTOM = 8;
    public static final int TOAST_LOCATION_RIGHT_BOTTOM = 9;


    private TextView m_msg;
    private ProgressBar m_progress;

    public EBrowserToast(Context context) {
        super(context);
        GradientDrawable grade = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x99000000, 0x99000000});
        grade.setCornerRadius(6);
        setBackgroundDrawable(grade);
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                5, ESystemInfo.getIntence().mDisplayMetrics);
        setPadding(pad, pad, pad, pad);
        m_progress = new ProgressBar(context);
        m_progress.setId(0x1101);
        m_progress.setIndeterminate(true);
        m_msg = new TextView(context);
        m_msg.setId(0x1102);
        int use = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                20, ESystemInfo.getIntence().mDisplayMetrics);
        RelativeLayout.LayoutParams parmPro = new LayoutParams(use, use);
        parmPro.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        parmPro.addRule(RelativeLayout.CENTER_VERTICAL);
        m_progress.setLayoutParams(parmPro);
        m_progress.setMinimumHeight(10);

        RelativeLayout.LayoutParams parmMsg = new LayoutParams(-2, -2);
        parmMsg.addRule(RelativeLayout.RIGHT_OF, 0x1101);
        parmMsg.addRule(RelativeLayout.CENTER_VERTICAL);
        m_msg.setLayoutParams(parmMsg);
        m_msg.setTextColor(0xFFFFFFFF);
        m_msg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        addView(m_progress);
        addView(m_msg);
    }

    public void showProgress() {
        m_progress.setVisibility(VISIBLE);
    }

    public void hiddenProgress() {
        m_progress.setVisibility(GONE);
    }

    public void setMsg(String msg) {
        m_msg.setText(msg);
    }

    public void setInLargeModul() {
        int use = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                30, ESystemInfo.getIntence().mDisplayMetrics);
        RelativeLayout.LayoutParams parmPro = new LayoutParams(use, use);
        parmPro.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        parmPro.addRule(RelativeLayout.CENTER_VERTICAL);
        m_progress.setLayoutParams(parmPro);

        m_msg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
    }

    public void setInSmallModul() {
        int use = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                20, ESystemInfo.getIntence().mDisplayMetrics);
        RelativeLayout.LayoutParams parmPro = new LayoutParams(use, use);
        parmPro.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        parmPro.addRule(RelativeLayout.CENTER_VERTICAL);
        m_progress.setLayoutParams(parmPro);

        m_msg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
    }
}
