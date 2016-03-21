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

import org.zywx.wbpalmstar.engine.universalex.EUExWindow;

public class EDialogTask {

    public static final int F_TYPE_ALERT = 0;
    public static final int F_TYPE_CONFIRM = 1;
    public static final int F_TYPE_PROMPT = 2;

    public String title;
    public String msg;
    public String defaultValue;
    public String[] buttonLables;
    public int type;
    public EUExWindow mUexWind;
    public String hint;

    public EDialogTask() {

    }

    public EDialogTask(EUExWindow inUexWind, String inTitle, String inMessage, String inDefaultValue, String[] inButtonLables) {
        mUexWind = inUexWind;
        title = inTitle;
        msg = inMessage;
        defaultValue = inDefaultValue;
        buttonLables = inButtonLables;
    }

    public void exc() {
        switch (type) {
            case F_TYPE_ALERT:
                mUexWind.private_alert(title, msg, defaultValue);
                break;
            case F_TYPE_CONFIRM:
                mUexWind.private_confirm(title, msg, buttonLables);
                break;
            case F_TYPE_PROMPT:
                mUexWind.private_prompt(title, msg, defaultValue, buttonLables,hint);
                break;
        }
        mUexWind = null;
    }
}
