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
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.FrameLayout;


public class EBrowserMainFrame extends FrameLayout {

    private EBrowser mBrw;
    private View mCustomPlayer;
    private CustomViewCallback mCustomCallback;


    public EBrowserMainFrame(Context context) {
        super(context);
        setBackgroundColor(0);
    }

    public void init(EBrowser eInBrw) {

        mBrw = eInBrw;
    }

    public void showCustomView(View view, CustomViewCallback callback) {
        if (null != mCustomPlayer) {
            removeView(mCustomPlayer);
            mCustomCallback.onCustomViewHidden();
        }
        addView(view);
        mCustomPlayer = view;
        mCustomCallback = callback;
    }

    public void hideCustomView() {
        removeView(mCustomPlayer);
        mCustomCallback.onCustomViewHidden();
        mCustomPlayer = null;
        mCustomCallback = null;
    }

    public boolean customViewShown() {

        return mCustomPlayer != null;
    }

    public EBrowser getBrowser() {

        return mBrw;
    }
}
