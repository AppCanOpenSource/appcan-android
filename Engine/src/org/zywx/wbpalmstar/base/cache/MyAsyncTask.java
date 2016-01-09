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

package org.zywx.wbpalmstar.base.cache;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MyAsyncTask extends AsyncTask<Object, Integer, Object> {
    private static final int MSG_ACTION_PRE = 1;
    private static final int MSG_ACTION_COMPLETED = 2;
    private static final int MSG_ACTION_UPDATE_PROGRESS = 3;
    private static final int MSG_ACTION_CANCEL = 4;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ACTION_PRE:
                    handleOnPreLoad(MyAsyncTask.this);
                    break;
                case MSG_ACTION_UPDATE_PROGRESS:
                    handleOnUpdateProgress(MyAsyncTask.this, msg.arg1);
                    break;
                case MSG_ACTION_COMPLETED:
                    handleOnCompleted(MyAsyncTask.this, msg.obj);
                    break;
                case MSG_ACTION_CANCEL:
                    handleOnCanceled(MyAsyncTask.this);
                    break;
            }
        }

        ;
    };

    @Override
    protected void onPreExecute() {
        handler.sendEmptyMessage(MSG_ACTION_PRE);
    }

    @Override
    protected Object doInBackground(Object... params) {

        return null;
    }

    public void handleOnPreLoad(MyAsyncTask task) {

    }

    ;

    public void handleOnUpdateProgress(MyAsyncTask task, int percent) {

    }

    public void handleOnCanceled(MyAsyncTask task) {

    }

    ;

    public void handleOnCompleted(MyAsyncTask task, Object result) {

    }

    ;

    @Override
    protected void onProgressUpdate(Integer... values) {
        Message msg = handler.obtainMessage(MSG_ACTION_UPDATE_PROGRESS);
        msg.arg1 = values[0];
        handler.sendMessage(msg);
    }

    @Override
    protected void onPostExecute(Object result) {
        Message msg = handler.obtainMessage(MSG_ACTION_COMPLETED);
        msg.obj = result;
        handler.sendMessage(msg);
    }

    @Override
    protected void onCancelled() {
        handler.sendEmptyMessage(MSG_ACTION_CANCEL);
    }
}
