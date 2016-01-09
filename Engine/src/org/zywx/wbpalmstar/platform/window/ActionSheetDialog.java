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

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import org.zywx.wbpalmstar.base.ResoureFinder;

import java.util.ArrayList;

public class ActionSheetDialog extends Dialog implements OnClickListener, OnItemClickListener {

    private LayoutInflater inflater;
    private TextView tvTitle;
    private ListView lvBtnList;
    private Button btnCancel;
    private ListAdapter lisAdapter;
    private boolean mCloseByBackKey;
    private ActionSheetDialogItemClickListener listener;
    public static final int MESSAGE_ACTION_CLICKED_POSTION = 100;
    public static final int MESSAGE_ACTION_CLICKED_CANCEL = 101;
    public static final int INDEX_DELETE = 0;
    public static final int ANIM_TIME = 300;
    private ResoureFinder finder;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_ACTION_CLICKED_POSTION:
                    if (listener != null) {
                        listener.onItemClicked(ActionSheetDialog.this, msg.arg1);
                    }
                    break;
                case MESSAGE_ACTION_CLICKED_CANCEL:
                    if (listener != null) {
                        listener.onCanceled(ActionSheetDialog.this);
                    }
                    break;
            }
        }
    };

    public ActionSheetDialog(Context context) {
        super(context, ResoureFinder.getInstance(context).getStyleId("Style_platform_dialog"));
        finder = ResoureFinder.getInstance(context);
        inflater = LayoutInflater.from(context);
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.windowAnimations = finder.getStyleId("Anim_platform_window_actionsheet_dialog");
        // 对话框与底部对齐，横向填满
        params.gravity = Gravity.BOTTOM | Gravity.FILL_HORIZONTAL;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.5f;
        setContentView(finder.getLayoutId("platform_window_action_sheet_dialog_layout"));
        tvTitle = (TextView) findViewById(finder.getId("dialog_title"));
        btnCancel = (Button) findViewById(finder.getId("dialog_cancel_button"));
        btnCancel.setOnClickListener(this);
        lvBtnList = (ListView) findViewById(finder.getId("dialog_button_list"));
        lvBtnList.setOnItemClickListener(this);
    }

    @Override
    protected void onStop() {
        if (mCloseByBackKey) {
            handler.sendEmptyMessageDelayed(MESSAGE_ACTION_CLICKED_CANCEL, ANIM_TIME);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mCloseByBackKey = true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void setupData(String[] labels) {
        if (labels == null) {
            return;
        }
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0, size = labels.length; i < size; i++) {
            list.add(labels[i]);
        }
        lisAdapter = new ListAdapter(list);
        lvBtnList.setAdapter(lisAdapter);
    }

    @Override
    public void setTitle(CharSequence title) {
        tvTitle.setText(title);
    }

    public void setButtonLabel(String label) {
        btnCancel.setText(label);
    }

    @Override
    public void show() {
        super.show();
    }

    public void setOnDialogItemClickedListener(ActionSheetDialogItemClickListener cb) {
        this.listener = cb;
    }

    @Override
    public void onClick(View v) {
        this.dismiss();
        handler.sendEmptyMessageDelayed(MESSAGE_ACTION_CLICKED_CANCEL, ANIM_TIME);
    }

    private void sendPostionMessage(int postion) {
        Message message = handler.obtainMessage(MESSAGE_ACTION_CLICKED_POSTION);
        message.arg1 = postion;
        handler.sendMessageDelayed(message, ANIM_TIME);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.dismiss();
        sendPostionMessage(position);
    }

    private class ListAdapter extends BaseAdapter {

        private ArrayList<String> list;

        public ListAdapter(ArrayList<String> texts) {
            list = texts;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            RelativeLayout layout = (RelativeLayout) inflater.inflate(
                    finder.getLayoutId("platform_window_actionsheet_list_item"), null);
            TextView button = (TextView) layout.findViewById(finder.getId("actionsheet_list_menu_item_btn"));
            button.setText(list.get(position));
            return layout;
        }

    }

    public static ActionSheetDialog show(Context context, String[] lables, String title, String cancelLabel,
                                         ActionSheetDialogItemClickListener listener) {
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(context);
        actionSheetDialog.setTitle(title);
        actionSheetDialog.setButtonLabel(cancelLabel);
        actionSheetDialog.setupData(lables);
        actionSheetDialog.setOnDialogItemClickedListener(listener);
        actionSheetDialog.show();
        return actionSheetDialog;
    }

    public static interface ActionSheetDialogItemClickListener {
        void onItemClicked(ActionSheetDialog dialog, int postion);

        void onCanceled(ActionSheetDialog dialog);
    }

}
