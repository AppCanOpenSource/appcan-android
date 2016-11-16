/*
 * Copyright (c) 2015.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.zywx.wbpalmstar.engine.universalex;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 历史遗留问题，需要保证混淆后的类名是c，此类名不能更改
 */
public class c extends Handler {

    public c(Looper loop) {
        super(loop);
    }

    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) return;
        EUExBase base = (EUExBase) msg.obj;
        base.onHandleMessage(msg);
    }
}
