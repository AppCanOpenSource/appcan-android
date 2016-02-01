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

import android.os.Handler;
import android.os.Message;


public abstract class EAdViewTimer {

    private long mTime;
    private long mInterval;

    private static final int F_AD_VIEW_MSG_SHOW = 0;
    private static final int F_AD_VIEW_MSG_CLOSE = 1;

    public EAdViewTimer(long time, long interval) {
        mTime = time;
        mInterval = interval;
    }

    public final void cancel() {
        onClose();
        mHandler.removeMessages(F_AD_VIEW_MSG_SHOW);
        mHandler.removeMessages(F_AD_VIEW_MSG_CLOSE);
    }

    public final void showAlway() {
        onShow();
        mHandler.removeMessages(F_AD_VIEW_MSG_SHOW);
        mHandler.removeMessages(F_AD_VIEW_MSG_CLOSE);
    }

    public synchronized final EAdViewTimer start() {
        mHandler.sendMessage(mHandler.obtainMessage(F_AD_VIEW_MSG_SHOW));

        return this;
    }

    public synchronized final void reStart(long time, long interval) {
        mTime = time;
        mInterval = interval;
        mHandler.removeMessages(F_AD_VIEW_MSG_SHOW);
        mHandler.removeMessages(F_AD_VIEW_MSG_CLOSE);
        mHandler.sendMessage(mHandler.obtainMessage(F_AD_VIEW_MSG_SHOW));
    }

    public abstract void onShow();

    public abstract void onClose();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (EAdViewTimer.this) {
                switch (msg.what) {
                    case F_AD_VIEW_MSG_SHOW:
                        onShow();
                        sendMessageDelayed(obtainMessage(F_AD_VIEW_MSG_CLOSE), mTime);
                        break;
                    case F_AD_VIEW_MSG_CLOSE:
                        onClose();
                        sendMessageDelayed(obtainMessage(F_AD_VIEW_MSG_SHOW), mInterval);
                        break;
                }
            }
        }
    };
}
