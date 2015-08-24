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

import android.webkit.JavascriptInterface;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.ELinkedList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by ylt on 15/8/21.
 */
public class EUExDispatcher {


    public static final String JS_OBJECT_NAME="uexDispatcher";

    public EUExManager mEuExManager;

    public EUExDispatcher(EUExManager manager){
        this.mEuExManager=manager;
    }


    @JavascriptInterface
    public void dispatch(String pluginName,String methodName,String[] params){
        ELinkedList<EUExBase> plugins=mEuExManager.getThirdPlugins();
        for (EUExBase plugin:plugins) {
            if (plugin.getUexName().equals(pluginName)){
                callMethod(plugin,methodName,params);
                return;
            }
        }
        BDebug.i("plugin",pluginName,"not exist...");
    }

    private void callMethod(EUExBase plugin,String methodName,String[] params){
        if (plugin.mDestroyed){
            BDebug.e("plugin",plugin.getUexName()," has been destroyed");
            return;
        }
        try {
            Method targetMethod=plugin.getClass().getMethod(methodName, String[].class);
            targetMethod.invoke(plugin, (Object) params);
        } catch (NoSuchMethodException e) {
            BDebug.e(methodName," NoSuchMethodException");
        }catch (IllegalAccessException e) {
            BDebug.e(e.toString());
        } catch (InvocationTargetException e) {
            BDebug.e(e.toString());
        }
    }

}
