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
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EBrowserProgress extends RelativeLayout implements View.OnClickListener {


    private TextView mMessege;
    private ProgressBar mProgress;

    public EBrowserProgress(Context context) {
        super(context);
        mProgress = new ProgressBar(context);
        mProgress.setIndeterminate(true);
        mMessege = new TextView(context);
        RelativeLayout.LayoutParams parmPro = new LayoutParams(-2, -2);
        parmPro.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mProgress.setLayoutParams(parmPro);

        RelativeLayout.LayoutParams parmMsg = new LayoutParams(-2, -2);
        parmMsg.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mMessege.setLayoutParams(parmMsg);
        mMessege.setTextColor(0xFFFF0000);
        mMessege.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        addView(mProgress);
        addView(mMessege);
        setOnClickListener(this);
    }

    public void hiddenProgress() {

        mProgress.setVisibility(View.GONE);
    }

    public void showProgress() {

        mProgress.setVisibility(View.VISIBLE);
    }

    public void setProgress(int p) {

        mMessege.setText(p + "%");
    }

    @Override
    public void onClick(View v) {
        hiddenProgress();
        setVisibility(View.GONE);
    }
}
