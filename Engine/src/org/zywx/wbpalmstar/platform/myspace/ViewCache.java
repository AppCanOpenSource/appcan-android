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


import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewCache {
    public ImageView iconImageView;
    public ProgressBar downloadProgressBar;
    public TextView nameTextView;

    /**
     * 清除ViewCache数据状态
     */
    public void clearStatus() {
        iconImageView.setBackgroundDrawable(null);
        iconImageView.setImageDrawable(null);
        downloadProgressBar.setVisibility(View.GONE);
        downloadProgressBar.setProgress(0);
        nameTextView.setText("");
    }
}
