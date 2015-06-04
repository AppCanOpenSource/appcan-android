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


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;

public class CBrowserMainFrame extends WebChromeClient {

    public Context mContext;

	/**
	 *android version < 2.1 use 
	 */
	public CBrowserMainFrame(Context context){
        this.mContext=context;
	}

	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		EBrowserView target = (EBrowserView)view;
		EBrowserWindow bWindow = target.getBrowserWindow();
		bWindow.setGlobalProgress(newProgress);
		if(100 == newProgress){
			bWindow.hiddenProgress();
		}
	}

	@Override
	public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
		if (!((EBrowserActivity)view.getContext()).isVisable()){
			result.confirm();
		}
		AlertDialog.Builder dia = new AlertDialog.Builder(view.getContext());
		dia.setTitle("提示消息");
		dia.setMessage(message);
		dia.setCancelable(false);
		dia.setPositiveButton("确定", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				result.confirm();
			}
		});
		dia.create();
		dia.show();
		return true;
	}

	@Override
	public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
		if (!((EBrowserActivity)view.getContext()).isVisable()){
			result.cancel();
			return true;
		} 
		AlertDialog.Builder dia = new AlertDialog.Builder(view.getContext());
		 dia.setMessage(message);
		 dia.setTitle("确认消息");
		 dia.setCancelable(false);
		 dia.setPositiveButton("确定", 
         	new DialogInterface.OnClickListener() {
             	public void onClick(DialogInterface dialog, int which) {
             		result.confirm();
                 }
             });
		 dia.setNegativeButton("取消", 
                 	new DialogInterface.OnClickListener() {
             	public void onClick(DialogInterface dialog, int which) {
             		result.cancel();
                 }
             });
		 dia.create();
		 dia.show();
         return true;
	}

	@Override
	public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
		if (!((EBrowserActivity)view.getContext()).isVisable()){
			result.cancel();
			return true;
		}
		AlertDialog.Builder dia = new AlertDialog.Builder(view.getContext());
		dia.setTitle(null);
		dia.setMessage(message);
		final EditText input = new EditText(view.getContext());
		if (defaultValue != null) {
			input.setText(defaultValue);
		}
		input.setSelectAllOnFocus(true);
		dia.setView(input);
		dia.setCancelable(false);
		dia.setPositiveButton("确定", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					result.confirm(input.getText().toString());
			}
		});
		dia.setNegativeButton("取消", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					result.cancel();
			}
		});
		dia.create();
		dia.show();
		return true;
	}


    // For Android 3.0-
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        ((EBrowserActivity)mContext).setmUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        ((EBrowserActivity)mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        ((EBrowserActivity)mContext).setmUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        ((EBrowserActivity)mContext).startActivityForResult(Intent.createChooser(i, "File Browser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

    // For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        ((EBrowserActivity)mContext).setmUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        ((EBrowserActivity)mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"),
                EBrowserActivity.FILECHOOSER_RESULTCODE);
    }

}
