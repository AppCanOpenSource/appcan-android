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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebStorage.QuotaUpdater;
import android.widget.FrameLayout;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

public class CBrowserMainFrame7 extends CBrowserMainFrame {

    final long MAX_QUOTA = 104857600L;

    /**
     * android version < 2.1 use
     *
     * @param context
     */
    public CBrowserMainFrame7(Context context) {
        super(context);
    }

//	private ValueCallback<Uri> mFile;

    public void onHideCustomView() {
        ((EBrowserActivity) mContext).hideCustomView();
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        String packg = mContext.getPackageName();
        Resources res = mContext.getResources();
        int id = res.getIdentifier("plugin_file_video", "drawable", packg);
        Bitmap map = BitmapFactory.decodeResource(res, id);
        return map;
    }

    @Override
    public View getVideoLoadingProgressView() {
        EBrowserToast progress = new EBrowserToast(mContext);
        progress.setMsg(mContext.getString(EUExUtil.getResStringID("platform_myspace_loading")));
        progress.setInLargeModul();
        progress.showProgress();
        return progress;
    }

    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        FrameLayout container = new FrameLayout(mContext);
        container.setBackgroundColor(0xff000000);
        FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        container.setLayoutParams(parm);
        container.setClickable(true);
        container.addView(view);
        //	((EBrowserActivity)mContext).requestWindowFeature()
        ((EBrowserActivity) mContext).showCustomView(container, callback);

    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        FullscreenHolder container = new FullscreenHolder(mContext);
        FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        container.setLayoutParams(parm);
        container.addView(view);
        ((EBrowserActivity) mContext).showCustomView(container, callback);

    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota,
                                        QuotaUpdater quotaUpdater) {
        if (estimatedSize < MAX_QUOTA) {
            long newQuota = estimatedSize;
            quotaUpdater.updateQuota(newQuota * 2);
        } else {
            quotaUpdater.updateQuota(currentQuota);
        }
    }

    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(0xFF000000);
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }

    }

//	public void openFileChooser(ValueCallback<Uri> uploadFile) {
//		if(null != mFile){
//			return;
//		}
//		mFile = uploadFile;
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("*/*");
//        ((Activity)m_eContext).startActivityForResult(Intent.createChooser(intent, ""), EBrowser.F_ACT_REQ_CODE_UEX_NATIVE_FILE_EXPLORER);
//	}
//	
//	public void openFileCallBack(Uri uri){
//		mFile.onReceiveValue(uri);
//		mFile = null;
//	}

}
