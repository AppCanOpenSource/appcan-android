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

import android.os.AsyncTask;

public abstract class DownloadTask extends AsyncTask<Object, Integer, Boolean> {

    private static final int MSG_ACTION_PRE_DOWNLOAD = 1;
    private static final int MSG_ACTION_COMPLETED_DOWNLOAD = 2;
    private static final int MSG_ACTION_UPDATE_PROGRESS = 3;
    private static final int MSG_ACTION_CANCEL_DOWNLOAD = 4;
    private DownloadTaskCallback callback;

    public DownloadTask() {

    }

    @Override
    protected void onPostExecute(Boolean result) {

    }

    public static interface DownloadTaskCallback {
        void onPreDownload(DownloadTask task);

        void onUpdateProgress(DownloadTask task, int percent);

        void onCancelDownload(DownloadTask task);

        void onCompletedDowload(DownloadTask task);
    }

    public static class DefaultDownloadTaskCallback implements
            DownloadTaskCallback {

        @Override
        public void onCancelDownload(DownloadTask task) {

        }

        @Override
        public void onCompletedDowload(DownloadTask task) {

        }

        @Override
        public void onPreDownload(DownloadTask task) {

        }

        @Override
        public void onUpdateProgress(DownloadTask task, int percent) {

        }

    }

}
