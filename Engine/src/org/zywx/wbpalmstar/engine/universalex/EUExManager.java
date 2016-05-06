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

package org.zywx.wbpalmstar.engine.universalex;

import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.ELinkedList;
import org.zywx.wbpalmstar.widgetone.WidgetOneApplication;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EUExManager {
    private final static String RETURN_RESULT_FORMAT = "{\"code\": %d, \"result\": %s}";
    private Context mContext;
    private ELinkedList<EUExBase> mThirdPlugins;

    public EUExManager(Context context) {
        mContext = context;
        mThirdPlugins = new ELinkedList<EUExBase>();
    }

    public void addJavascriptInterface(EBrowserView brwView) {
        EUExWidgetOne widgetOne = new EUExWidgetOne(mContext, brwView);
        widgetOne.setUexName(EUExWidgetOne.tag);
        EUExWindow window = new EUExWindow(mContext, brwView);
        window.setUexName(EUExWindow.tag);
        EUExWidget widget = new EUExWidget(mContext, brwView);
        widget.setUexName(EUExWidget.tag);
        EUExAppCenter appCenter = new EUExAppCenter(mContext, brwView);
        appCenter.setUexName(EUExAppCenter.tag);
//		EUExDataAnalysis dataAnalysis = new EUExDataAnalysis(mContext, brwView);
//		dataAnalysis.setUexName(EUExDataAnalysis.tag);
//		brwView.addJavascriptInterface(widgetOne, EUExWidgetOne.tag);
//		brwView.addJavascriptInterface(window, EUExWindow.tag);
//		brwView.addJavascriptInterface(widget, EUExWidget.tag);
//		brwView.addJavascriptInterface(appCenter, EUExAppCenter.tag);
        if (Build.VERSION.SDK_INT >= 11) {
            brwView.removeJavascriptInterface("searchBoxJavaBridge_");
            brwView.removeJavascriptInterface("accessibility");
            brwView.removeJavascriptInterface("accessibilityTraversal");
        }
//		brwView.addJavascriptInterface(dataAnalysis, EUExDataAnalysis.tag);
        mThirdPlugins.add(widgetOne);
        mThirdPlugins.add(window);
        mThirdPlugins.add(widget);
        mThirdPlugins.add(appCenter);
//		mThirdPlugins.add(dataAnalysis);
        // third-party plugin
        Map<String, ThirdPluginObject> thirdPlugins = getPlugins();
//		String symbol = "_";
        Set<Map.Entry<String, ThirdPluginObject>> pluginSet = thirdPlugins.entrySet();
        for (Map.Entry<String, ThirdPluginObject> entry : pluginSet) {
            String uName = entry.getKey();
            ThirdPluginObject scriptObj = entry.getValue();
            EUExBase objectIntance = null;
            try {

                if (scriptObj.isGlobal && scriptObj.pluginObj != null) {

                    objectIntance = scriptObj.pluginObj;

                } else {
                    Constructor<?> init = scriptObj.jobject;
                    objectIntance = (EUExBase) init.newInstance(mContext, brwView);
                }

            } catch (Exception e) {
                BDebug.e(e.toString());
            }
            if (null != objectIntance) {
//				String uexName = uName + symbol;
                objectIntance.setUexName(uName);
//				brwView.addJavascriptInterface(objectIntance, uexName);

                if (scriptObj.isGlobal) {
                    scriptObj.pluginObj = objectIntance;
                } else {
                    mThirdPlugins.add(objectIntance);
                }

            }
        }
    }

    public Map<String, ThirdPluginObject> getPlugins() {
        WidgetOneApplication app = (WidgetOneApplication) mContext.getApplicationContext();
        ThirdPluginMgr tpm = app.getThirdPlugins();
        return tpm.getPlugins();
    }

    public String callMethod(final EUExBase plugin, final String methodName, final String[] params) {
        if (plugin.mDestroyed) {
            BDebug.e("plugin", plugin.getUexName(), " has been destroyed");
            return null;
        }
        try {
            Method targetMethod = plugin.getClass().getMethod(methodName,
                    String[].class);
            return getReturn(200,targetMethod.invoke(plugin, (Object) params));
        } catch (NoSuchMethodException e) {
            BDebug.e(methodName, " NoSuchMethodException");
            return getReturn(201,"NoSuchMethodException:"+e.getMessage());
        } catch (IllegalAccessException e) {
            BDebug.e(plugin.getUexName(), methodName, e.toString());
            return getReturn(202,"IllegalAccessException:"+e.getMessage());
        } catch (InvocationTargetException e) {
            BDebug.e(plugin.getUexName(),methodName, " InvocationTargetException");
            if (BDebug.DEBUG) {
                e.printStackTrace();
            }
            return getReturn(203,"InvocationTargetException:"+e.getMessage());
        }
    }

    public static String getReturn (int stateCode, Object result) {
        String insertRes;
        if (result == null) {
            insertRes = "null";
        } else if (result instanceof String) {
            result = ((String) result).replace("\"", "\\\"");
            insertRes = "\"" + result + "\"";
        } else if (!(result instanceof Integer)
                && !(result instanceof Long)
                && !(result instanceof Boolean)
                && !(result instanceof Float)
                && !(result instanceof Double)
                && !(result instanceof JSONObject)) {    // 非数字或者非字符串的构造对象类型都要序列化后再拼接
            insertRes = DataHelper.gson.toJson(result);
        } else {  //数字直接转化
            insertRes = String.valueOf(result);
        }
        return String.format(Locale.getDefault(),RETURN_RESULT_FORMAT, stateCode, insertRes);
    }

    public void notifyReset() {
        for (EUExBase uex : mThirdPlugins) {
            uex.reset();
        }
    }

    public void notifyDocChange() {
        for (EUExBase uex : mThirdPlugins) {
            uex.clean();
        }
    }

    public void notifyStop() {
        notifyDocChange();
        for (EUExBase uex : mThirdPlugins) {
            uex.stop();
        }
    }

    public void notifyDestroy(WebView view) {
        notifyDocChange();
        for (EUExBase uex : mThirdPlugins) {
            if (Build.VERSION.SDK_INT >= 11) {
                String uexName = uex.getUexName();
                view.removeJavascriptInterface(uexName);
            }
            uex.destroy();
        }
        mThirdPlugins.clear();
        mThirdPlugins = null;
        mContext = null;
    }

    public ELinkedList<EUExBase> getThirdPlugins() {
        return mThirdPlugins;
    }
}
