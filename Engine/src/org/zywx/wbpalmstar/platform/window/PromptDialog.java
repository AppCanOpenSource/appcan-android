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

package org.zywx.wbpalmstar.platform.window;

import android.app.AlertDialog;
import android.content.Context;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.zywx.wbpalmstar.base.ResoureFinder;

public class PromptDialog extends AlertDialog {

    private TextView tvDesc;
    private EditText etInput;

    private ResoureFinder finder;

    public PromptDialog(Context context) {
        super(context);
        finder = ResoureFinder.getInstance(context);
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final RelativeLayout layout = (RelativeLayout) inflater.inflate(
                finder.getLayoutId("platform_window_dialog_prompt_layout"), null);
        tvDesc = (TextView) layout.findViewById(finder.getId("tv_dialog_input_desc"));
        etInput = (EditText) layout.findViewById(finder.getId("et_dialog_input_text"));
        etInput.setSelectAllOnFocus(true);
        setView(layout);
    }

    public String getInput() {
        return etInput.getText().toString();
    }

    public void setInputText(String text) {
        if (text != null && text.length() > 0) {
            etInput.setText(text);
        }
    }

    public void setHint(String hint){
        etInput.setHint(hint);
    }

    public void setInputDesc(String desc) {
        if (desc != null && desc.length() > 0) {
            tvDesc.setText(desc);
        }
    }

    public IBinder getWindowToken() {

        return etInput.getWindowToken();
    }

    public static PromptDialog show(Context context, String title, String desc, String defalutValue,
                                    String hint,
                                    String confirmLabel, OnClickListener confirmListener, String cancelLabel, OnClickListener cancelListener) {
        final PromptDialog dialog = new PromptDialog(context);
        dialog.setCancelable(false);
        if (title != null) {
            dialog.setTitle(title);
        }
        if (desc != null) {
            dialog.setInputDesc(desc);
        }
        if (!TextUtils.isEmpty(defalutValue)) {
            dialog.setInputText(defalutValue);
        }
        if (!TextUtils.isEmpty(hint)){
            dialog.setHint(hint);
        }
        if (confirmLabel != null) {
            dialog.setButton(confirmLabel, confirmListener);
        }
        if (cancelLabel != null) {
            dialog.setButton3(cancelLabel, cancelListener);
        }
        dialog.show();
        return dialog;
    }

}
