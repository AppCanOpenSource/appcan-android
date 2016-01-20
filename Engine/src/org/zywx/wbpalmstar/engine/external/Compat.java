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

package org.zywx.wbpalmstar.engine.external;

import android.os.Build;
import android.view.ViewGroup;

public class Compat {

    static int sdkInt = Build.VERSION.SDK_INT;

    public static int FILL = sdkInt >= 8 ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.FILL_PARENT;
    public static int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

}
