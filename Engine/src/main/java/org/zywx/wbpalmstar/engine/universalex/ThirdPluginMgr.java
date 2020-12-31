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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;

import org.xmlpull.v1.XmlPullParser;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.ELinkedList;
import org.zywx.wbpalmstar.engine.EngineEventListener;
import org.zywx.wbpalmstar.widgetone.Smith;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class ThirdPluginMgr {

	private static final String TAG = "ThirdPluginMgr";

	private final static String pluginNode = "plugin";
	private final static String methodNode = "method";
	private final static String propertyNode = "property";
	private final static String uexNameAttr = "uexName";
	private final static String classNameAttr = "className";
	private final static String nameAttr = "name";
	private final static String startupAttr = "startup";
	private final static String globalAttr = "global";

	private static final String F_SP_NAME_PLUGIN_LOADING = "plugins_loading";
	private static final String F_SP_KEY_NAME_PLUGIN_COPY_FINISHED = "isPluginCopyFinished";
	private static final String F_SP_KEY_NAME_PLUGIN_COPY_LAST_PKG_VERSION = "lastCopyPkgVersion";
	private static final String dexApk = "apkfile";
	private static final String dexJar = "dexfile/jar";
	private static final String CPU_ABI_ARM_LEGACY = "armeabi";
	private static final String CPU_ABI_ARM_V7A = "armeabi-v7a";
	private static final String CPU_ABI_ARM64_V8 = "arm64-v8a";
	private static final String SO_LIB_PARENT = "dexfile/libs/";
	private static final String SO_LIB_ARM_LEGACY = SO_LIB_PARENT + CPU_ABI_ARM_LEGACY;
	private static final String SO_LIB_ARM_V7A = SO_LIB_PARENT + CPU_ABI_ARM_V7A;
	private static final String SO_LIB_ARM64_V8= SO_LIB_PARENT + CPU_ABI_ARM64_V8;
	private static String dexLib = SO_LIB_ARM_LEGACY;
	private static final String optFile = "dexfile/out";

	private Context mContext;

	private DexClassLoader mParentClassLoader;
	private AssetManager mAssetManager;
	private Resources mResources;

    private StringBuffer script;
    private Map<String, ThirdPluginObject> mThirdClass;
	private LinkedList<String> javaNames;
    private int PluginCount = 0;
	private String libsParentDir;

	private String[] pluginJars = null;

	public ThirdPluginMgr(Context context) {
        mThirdClass = new Hashtable<String, ThirdPluginObject>();
		javaNames = new LinkedList<String>();
        libsParentDir = context.getFilesDir().getAbsolutePath();
        BDebug.i(TAG, "CPU_ABI: " + Build.CPU_ABI);
        BDebug.i(TAG, "CPU_ABI2: " + Build.CPU_ABI2);
        if (Build.CPU_ABI.equalsIgnoreCase(CPU_ABI_ARM_V7A)){
        	// v7a的统一使用32位的v7a架构so
			dexLib = SO_LIB_ARM_V7A;
		}else{
        	// 其他架构统统使用64位的v8架构so（以后的新架构如果增加在引擎中，这里再修改代码）
        	dexLib = SO_LIB_ARM64_V8;
		}
		mContext = context;
    }

    public Map<String, ThirdPluginObject> getPlugins() {

        return mThirdClass;
    }

    public void clean() {
        mThirdClass.clear();
        mThirdClass = null;
        script = null;
    }

	public void loadInitAllDexPluginClass() {
		copyLib();
		copyJar();
		initClassLoader();
	}

	private void copyLib() {

		InputStream in = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		String libPath = libsParentDir + File.separator + dexLib;
		File dirFile = new File(libPath);
		if (dirFile != null)
			dirFile.delete();
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		try {
			String[] libs = mContext.getAssets().list(dexLib);
			if (null != libs && libs.length > 0) {
				for (int i = 0; i < libs.length; i++) {
					in = mContext.getAssets().open(
							dexLib + File.separator + libs[i]);
					File file = new File(libPath + File.separator + libs[i]);

					if (!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					bis = new BufferedInputStream(in);
					fos = new FileOutputStream(file);

					byte[] b = new byte[1024];
					int len = 0;
					while ((len = bis.read(b)) != -1) {
						fos.write(b, 0, len);
					}
					fos.flush();
					in.close();
					bis.close();

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (in != null && bis != null && fos != null) {
					in.close();
					bis.close();
					fos.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void copyJar() {
		InputStream in = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		String jarPath = libsParentDir + File.separator + dexJar;
		File dirFile = new File(jarPath);
		pluginJars = null;
		if (dirFile != null)
			dirFile.delete();
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		try {
			pluginJars = mContext.getAssets().list(dexJar);

			if (pluginJars != null && pluginJars.length > 0) {

				for (int i = 0; i < pluginJars.length; i++) {
					in = mContext.getAssets().open(
							dexJar + File.separator + pluginJars[i]);
					File file = new File(jarPath + File.separator
							+ pluginJars[i]);

					if (!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					bis = new BufferedInputStream(in);
					fos = new FileOutputStream(file);

					byte[] b = new byte[1024];
					int len = 0;
					while ((len = bis.read(b)) != -1) {
						fos.write(b, 0, len);
					}
					fos.flush();
					in.close();
					bis.close();

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			if (in != null && bis != null && fos != null) {
				try {
					in.close();
					bis.close();
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

	public ClassLoader getClassLoader() {
		return mParentClassLoader;
	}

	public AssetManager getAssets() {
		return mAssetManager;
	}

	public Resources getResources() {
		return mResources;
	}

	/**
	 * 判断是否已经加载了重复的插件
	 * 
	 * @param jsName 插件名称
	 * @param javaName 插件入口类名称
	 * @return
	 */
    private boolean isDuplicatedPlugin(String jsName, String javaName) {
        return (jsName == null || javaName == null) ? false : (mThirdClass
                .containsKey(jsName) || javaNames.contains(javaName));
    }

	/**
	 * 根据plugin.xml中的配置，加载插件
	 * 
	 * @param plugins
	 * @param listenerQueue
	 * @param classLoader
	 *            如果不需要指定classLoader，传null
	 */
	public void initClass(XmlPullParser plugins,
			ELinkedList<EngineEventListener> listenerQueue,
			ClassLoader classLoader) {

        int eventType = -1;
        String jsName = "", javaName = "", startup = "";
        String globalStr = "";
        ThirdPluginObject scriptObj = null;
		if (classLoader == null) {
			// 传空则使用默认的classLoader
			classLoader = mContext.getClassLoader();
		}
        try {
			script = new StringBuffer();
			while ((eventType = plugins.next()) != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    String strNode = plugins.getName();
                    if (strNode.equals(pluginNode)) {
                        jsName = plugins.getAttributeValue(null, uexNameAttr);
                        javaName = plugins.getAttributeValue(null,
                                classNameAttr);
                        startup = plugins.getAttributeValue(null, startupAttr);
						if (null != javaName && javaName.trim().length() != 0
								&& !isDuplicatedPlugin(jsName, javaName)) {
                            if (null != startup && "true".equals(startup)) {
                                Constructor<?> object = loadEngineEventClass(javaName);
                                handlerStartupAttr(object, listenerQueue);
                            } else {

                                Constructor<?> object = null;
								object = loadUexDexClass(classLoader, javaName);
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
						if (null != scriptObj
								&& !isDuplicatedPlugin(jsName, javaName)) {
                            scriptObj.addMethod(methodValue);
                        }
                    } else if (strNode.equals(propertyNode)) {
                        String propertyValue = plugins.getAttributeValue(null,
                                nameAttr);
						if (null != scriptObj
								&& !isDuplicatedPlugin(jsName, javaName)) {
                            scriptObj.addProperty(propertyValue);
                        }
                    }
                } else if (eventType == XmlResourceParser.END_TAG) {
                    String strNode = plugins.getName();
					if (strNode.equals(pluginNode)) {
						if (null != scriptObj
								&& !isDuplicatedPlugin(jsName, javaName)) {
                            scriptObj.oneObjectOver(script);
                            mThirdClass.put(jsName, scriptObj);
							javaNames.add(javaName);
							BDebug.i("DL", jsName + " plugin loaded.");
                            scriptObj = null;
							PluginCount++;
                        }
                    }
                }

            }
			BDebug.i("DL", PluginCount + " plugins total loaded.");
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

	private boolean CopyAssets(Context context, String assetDir, String dir) {
		boolean isSuccess = false;
		String[] files;
		try {
			files = context.getResources().getAssets().list(assetDir);
		} catch (IOException e1) {
			isSuccess = false;
			return isSuccess;
		}
		File mWorkingPath = new File(dir);
		// if this directory does not exists, make one.
		if (!mWorkingPath.exists()) {
			if (!mWorkingPath.mkdirs()) {
				BDebug.e("--CopyAssetsPlugins--", "cannot create directory.");
			}
		}

		for (int i = 0; i < files.length; i++) {
			try {
				String fileName = files[i];
				// we make sure file name not contains '.' to be a folder.
				if (!fileName.contains(".")) {
					if (0 == assetDir.length()) {
						CopyAssets(context, fileName, dir + fileName + "/");
					} else {
						CopyAssets(context, assetDir + "/" + fileName, dir
								+ "/" + fileName + "/");
					}
					continue;
				}
				File outFile = new File(mWorkingPath, fileName);
				if (outFile.exists())
					outFile.delete();
				InputStream in = null;
				if (0 != assetDir.length())
					in = context.getAssets().open(assetDir + "/" + fileName);
				else
					in = context.getAssets().open(fileName);
				OutputStream out = new FileOutputStream(outFile);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				in.close();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		isSuccess = true;
		return isSuccess;
	}

	// 因为之前的方法无法替换子进程的classloader，故改成以下的方式。由于每一个进程初始化的时候都会初始化一次他的application，而且默认的classloader是和application的classloader一样的
	// 故在application初始化的时候，替换掉application的classloader。之前只有在主进程中才替换掉application的loader，所以子进程还是无法加载动态插件
	private void initClassLoader() {
		try {
			pluginJars = mContext.getAssets().list(dexJar);

			if (pluginJars != null && pluginJars.length > 0) {

				// create the dexPath

				int PluginCount = pluginJars.length;
				String dexPath = libsParentDir + File.separator + dexJar;
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < PluginCount; i++) {
					sb.append(dexPath).append(File.separator)
							.append(pluginJars[i]).append(File.pathSeparator);
				}
				dexPath = sb.toString();

				// create the optPath

				String optPath = libsParentDir + File.separator + optFile;
				File dirFile = new File(optPath);
				if (!dirFile.exists()) {
					dirFile.mkdirs();
				}
				String libPath = libsParentDir + File.separator + dexLib;

				// create the dexclassloader
				DexClassLoader dexCl = new DexClassLoader(dexPath, optPath,
						libPath, mContext.getClassLoader());

				replaceCurrentClassLoader(dexCl);

			}

			/*
			 * Field mMainThread =
			 * Activity.class.getDeclaredField("mMainThread");
			 * mMainThread.setAccessible(true); Object mainThread =
			 * mMainThread.get((EBrowserActivity) context); Class threadClass =
			 * mainThread.getClass(); Field mPackages =
			 * threadClass.getDeclaredField("mPackages");
			 * mPackages.setAccessible(true); WeakReference<?> ref; Map<String,
			 * ?> map = (Map<String, ?>) mPackages.get(mainThread); ref =
			 * (WeakReference<?>) map.get(context.getPackageName()); Object apk
			 * = ref.get(); Class apkClass = apk.getClass();
			 * 
			 * Field mClassLoader = apkClass.getDeclaredField("mClassLoader");
			 * mClassLoader.setAccessible(true); mClassLoader.set(apk, dexCl);
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * use reflection tech replace the current classloader
	 * 
	 * @param dexCl
	 * @throws Exception
	 */
	private void replaceCurrentClassLoader(DexClassLoader dexCl)
			throws Exception {

		Context mBase = new Smith<Context>(mContext, "mBase").get();

		Object mPackageInfo = new Smith<Object>(mBase, "mPackageInfo").get();

		Smith<ClassLoader> sClassLoader = new Smith<ClassLoader>(mPackageInfo,
				"mClassLoader");
		sClassLoader.set(dexCl);
	}

}
