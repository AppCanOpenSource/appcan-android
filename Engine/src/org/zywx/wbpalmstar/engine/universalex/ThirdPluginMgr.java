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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.XmlResourceParser;

import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.ELinkedList;
import org.zywx.wbpalmstar.engine.EngineEventListener;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Map;

public class ThirdPluginMgr {

    private StringBuffer script;
    private Map<String, ThirdPluginObject> mThirdClass;
    private int PluginCount = 0;
    private String cachePath = null;
    private String dexJar = "dexfile/jar";
    private String dexLib = "dexfile/armeabi";
    private String optFile = "dexfile/out";

    public ThirdPluginMgr(XmlResourceParser plugins,
                          ELinkedList<EngineEventListener> mustInitObj, Context context) {
        script = new StringBuffer();
        mThirdClass = new Hashtable<String, ThirdPluginObject>();
        cachePath = context.getCacheDir().getAbsolutePath();
        initClass(plugins, mustInitObj, context);

    }

    public Map<String, ThirdPluginObject> getPlugins() {

        return mThirdClass;
    }

    public void clean() {
        mThirdClass.clear();
        mThirdClass = null;
        script = null;
    }

    private void initClass(XmlResourceParser plugins,
                           ELinkedList<EngineEventListener> listenerQueue, Context context) {
        String pluginNode = "plugin";
        String methodNode = "method";
        String propertyNode = "property";
        String uexNameAttr = "uexName";
        String classNameAttr = "className";
        String nameAttr = "name";
        String startupAttr = "startup";
        String globalAttr = "global";
        int eventType = -1;
        String jsName = "", javaName = "", startup = "";
        String globalStr = "";
        ThirdPluginObject scriptObj = null;

        try {
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    String strNode = plugins.getName();
                    if (strNode.equals(pluginNode)) {
                        jsName = plugins.getAttributeValue(null, uexNameAttr);
                        javaName = plugins.getAttributeValue(null,
                                classNameAttr);
                        startup = plugins.getAttributeValue(null, startupAttr);
                        if (null != javaName && javaName.trim().length() != 0) {
                            if (null != startup && "true".equals(startup)) {
                                Constructor<?> object = loadEngineEventClass(javaName);
                                handlerStartupAttr(object, listenerQueue);
                            } else {

                                Constructor<?> object = null;
                                object = loadUexDexClass(
                                        context.getClassLoader(), javaName);
                                if (null == object) {
                                    object = loadUexClass(javaName);
                                }
                                if (null != object) {
                                    scriptObj = new ThirdPluginObject(object);
                                    scriptObj.oneObjectBegin(jsName);
                                    scriptObj.jclass = javaName;

                                    globalStr = plugins.getAttributeValue(null, globalAttr);
                                    if (null != globalStr && "true".equals(globalStr)) {

                                        scriptObj.isGlobal = true;

                                    }
                                }

                            }


                        }


                    } else if (strNode.equals(methodNode)) {
                        String methodValue = plugins.getAttributeValue(null,
                                nameAttr);
                        if (null != scriptObj) {
                            scriptObj.addMethod(methodValue);
                        }
                    } else if (strNode.equals(propertyNode)) {
                        String propertyValue = plugins.getAttributeValue(null,
                                nameAttr);
                        if (null != scriptObj) {
                            scriptObj.addProperty(propertyValue);
                        }
                    }
                } else if (eventType == XmlResourceParser.END_TAG) {
                    String strNode = plugins.getName();
                    if (strNode.equals(pluginNode)) {
                        if (null != scriptObj) {
                            scriptObj.oneObjectOver(script);
                            mThirdClass.put(jsName, scriptObj);
                            scriptObj = null;
                        }
                    }
                }
                eventType = plugins.next();
            }
            EUExScript.F_UEX_SCRIPT += script.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(EUExUtil.getString("load_uex_object_error"));
        }
    }

    private void handlerStartupAttr(Constructor<?> object,
                                    ELinkedList<EngineEventListener> listenerQueue) {
        EngineEventListener objectIntance = null;
        try {
            objectIntance = (EngineEventListener) object.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != objectIntance) {
            listenerQueue.add(objectIntance);
        }
    }

    private Constructor<?> loadUexClass(String name) {
        Class<?> target = null;
        Constructor<?> targetStruct = null;
        if (name != null) {
            try {
                target = Class.forName(name);
                Class<?>[] paramTypes = {Context.class, EBrowserView.class};
                targetStruct = target.getConstructor(paramTypes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return targetStruct;
    }

    @SuppressLint("NewApi")
    private Constructor<?> loadUexDexClass(ClassLoader dexCl, String name) {

        Class<?> target = null;
        Constructor<?> targetStruct = null;
        if (name != null) {
            try {
                target = dexCl.loadClass(name);
                Class<?>[] paramTypes = {Context.class, EBrowserView.class};
                targetStruct = target.getConstructor(paramTypes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return targetStruct;
    }

    private Constructor<?> loadEngineEventClass(String name) {
        Class<?> target = null;
        Constructor<?> targetStruct = null;
        if (name != null) {
            try {
                target = Class.forName(name);
                Class<?>[] noneParam = {};
                targetStruct = target.getConstructor(noneParam);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return targetStruct;
    }

}
