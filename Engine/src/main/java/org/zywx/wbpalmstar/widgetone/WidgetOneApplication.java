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

package org.zywx.wbpalmstar.widgetone;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;

import org.zywx.wbpalmstar.engine.AppCan;

public class WidgetOneApplication extends Application {

    private boolean init=false;//是否init完成

    @Override
    public void onCreate() {
        super.onCreate();
//         init=true;
    }

    @Override
    public AssetManager getAssets() {
        if (!init){
            return super.getAssets();
        }
        AssetManager assetManager = AppCan.getInstance().getThirdPlugins()== null ? super.getAssets()
                : AppCan.getInstance().getThirdPlugins().getAssets();
        return assetManager == null ? super.getAssets() : assetManager;
    }

    @Override
    public Resources getResources() {
        if (!init){
            return super.getResources();
        }
        Resources resources = AppCan.getInstance().getThirdPlugins() == null ? super.getResources()
                : AppCan.getInstance().getThirdPlugins().getResources();
        return resources == null ? super.getResources() : resources;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (!init){
            return super.getClassLoader();
        }
        ClassLoader classLoader = AppCan.getInstance().getThirdPlugins() == null ? super
                .getClassLoader() : AppCan.getInstance().getThirdPlugins().getClassLoader();
        return classLoader == null ? super.getClassLoader() : classLoader;
    }

}
