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

package org.zywx.wbpalmstar.base.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.zywx.wbpalmstar.base.BConstant;

/**
 * Created by ylt on 16/2/26.
 */
public class SpManager {

    public static final String SP_NAME="appcan_data";

    private static SpManager instance=null;

    private SharedPreferences mSharedPreferences;

    public SpManager(Context context) {
        mSharedPreferences=context.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
    }

    public static SpManager getInstance(){
        if (instance==null){
            instance=new SpManager(BConstant.app);
        }
        return instance;
    }


    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public boolean putString(String key, String value){
        return mSharedPreferences.edit().putString(key,value).commit();
    }

    public String getString(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    public boolean remove(String key) {
        return mSharedPreferences.edit().remove(key).commit();
    }

}
