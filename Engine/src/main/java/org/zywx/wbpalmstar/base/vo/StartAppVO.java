/*
 * Copyright (c) 2016.  The AppCan Open Source Project.
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

package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

/**
 * Created by ylt on 16/1/29.
 */
public class StartAppVO implements Serializable {

    private static final long serialVersionUID = -644923159386862574L;

    private String data;
    private String isNewTask;// 是否通过NEW_TASK启动第三方Activity的开关

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getIsNewTask() {
        return isNewTask;
    }

    public void setIsNewTask(String isNewTask) {
        this.isNewTask = isNewTask;
    }
}
