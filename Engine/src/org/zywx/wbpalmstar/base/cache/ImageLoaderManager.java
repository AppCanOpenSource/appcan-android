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

package org.zywx.wbpalmstar.base.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import org.zywx.wbpalmstar.base.BDebug;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片异步加载与缓存管理
 * 
 *
 * 
 */
public class ImageLoaderManager {
	private static final String TAG = "ImageCacheManager";
	public static final String MAP_KEY_BITMAP = "bitmap";
	public static final String MAP_KEY_TASK = "task";
	public static final int ACTION_LOAD_COMPLETED = 1;
	public static final int ACTION_LOAD_START = 2;
	public static final int ACTION_LOAD_COUNT = 3;
	public static final int ACTION_LOAD_FINISH = 4;
	public static final int DEFAULT_CONCURRENT_THREAD_SIZE = 3;
	public static final int SOFT_REFERENCE_MAX_SIZE = 40;
	// LruCache可控回收力度而不内存溢出
	public LruCache<String, Bitmap> memoryCache;
	private LinkedList<ImageLoadTask> taskList = new LinkedList<ImageLoadTask>();
	private ImageLoadStatusListener countListener;
	private static ImageLoaderManager loaderManager;
	private ExecutorService executorService;

	/**
	 * 
	 * @param maxConcurrentThreads
	 *            最大并发线程数
     */
	private ImageLoaderManager(int maxConcurrentThreads, int memoryCacheSize) {
		memoryCache = new LruCache<String, Bitmap>(memoryCacheSize) {
			/**
			 * 当调用lruCache.get(String key)方法返回值为null时,会调用此create(String
			 * key)方法为此key创建一个Value
			 */
			@Override
			protected Bitmap create(String key) {
				return super.create(key);
			}

			// 计算bitmap在内存中占用字节数-->height*width*4(bytes)
			/**
			 * 计算此Value在内存中占用的字节大小， 以此来衡量是否达到了LurCache的最大容量。
			 * 为在恰当时机释放之前的key-value提供依据
			 */
			@Override
			protected int sizeOf(String key, Bitmap value) {
				if (value == null) {
					return super.sizeOf(key, value);
				} else {
					final int bytes = value.getRowBytes() * value.getHeight();
					return bytes;
				}
			}

			/**
			 * 超过LruCache最大容量，移除硬引用，为了回收利用，放入软引用表里面
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
			}

		};
		executorService = Executors.newFixedThreadPool(maxConcurrentThreads);
	}

	public synchronized static ImageLoaderManager getInstance(int maxConcurrentThreads, int memoryCacheSize) {
		if (loaderManager == null) {
			loaderManager = new ImageLoaderManager(maxConcurrentThreads, memoryCacheSize);
		}
		return loaderManager;
	}

	public static ImageLoaderManager getDefaultInstance() {
		return loaderManager;
	}

	public static ImageLoaderManager initImageLoaderManager(Context context) {
		if (context == null) {
			throw new NullPointerException("context can't be null!");
		}
		ImageLoaderManager imageLoaderManager = ImageLoaderManager.getDefaultInstance();
		if (imageLoaderManager == null) {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			final int memoryClass = activityManager.getMemoryClass();
			final int memoryCacheSize = memoryClass / 8 * 1024 * 1024;
			imageLoaderManager = ImageLoaderManager.getInstance(3, memoryCacheSize);
		}
		DiskCache.initDiskCache(context);
		return imageLoaderManager;
	}

	public Bitmap getCacheBitmap(String url) {
		if (url == null) {
			return null;
		}
		return memoryCache.get(url);// 查找硬引用
	}

	public void setOnCountListener(ImageLoadStatusListener listener) {
		this.countListener = listener;
	}

	public void asyncLoad(ImageLoadTask task) {
		synchronized (taskList) {
			final int findIndex = taskList.indexOf(task);
			if (findIndex != -1) {// find task in taskList
				final ImageLoadTask item = taskList.get(findIndex);
				// in order to prior display newest task result for user,
				// move ready task to taskList rear
				if (item.getStatus() == ImageLoadTask.STATUS_READY) {
					taskList.remove(findIndex);
					taskList.addLast(item);
				}
			} else {// not found,add to taskLisk last
				if (taskList.size() == 0) {
					handler.sendEmptyMessage(ACTION_LOAD_START);
				}
				taskList.addLast(task);
				executorService.execute(new TaskWorker());
			}
		}
	}

	public void addDelayTask(ImageLoadTask task) {
		synchronized (taskList) {
			int index = taskList.lastIndexOf(task);
			if (index == -1) {
				taskList.addFirst(task);
				executorService.execute(new TaskWorker());
			}
		}
	}

	public void clear() {
		memoryCache.evictAll();
		removeAllTask();
	}

	public void removeAllTask() {
		synchronized (taskList) {
			taskList.clear();
		}
	}

	private class TaskWorker implements Runnable {

		private ImageLoadTask seekReadyTask() {
			synchronized (taskList) {
				for (int i = taskList.size() - 1; i >= 0; i--) {
					final ImageLoadTask loadTask = taskList.get(i);
					if (loadTask.getStatus() == ImageLoadTask.STATUS_READY) {
						loadTask.setStatus(ImageLoadTask.STATUS_STARTED);// 改变其任务状态
						return loadTask;
					}
				}
			}
			return null;
		}

		private Bitmap loadFromSource(ImageLoadTask loadTask) {
			Bitmap bitmap = loadTask.startExecute();
			if (bitmap != null) {// 从源取得bitmap成功，将bitmap写入内存文件缓存起来
				DiskCache.writeDiskCache(loadTask.getKey(), bitmap);
			}
			return bitmap;
		}

		public void run() {
			/* 设置任务线程为后台线程，获得更少的执行机会，减少对UI渲染的影响 */
			int threadPriority = Process.getThreadPriority(Process.myTid());
			if (threadPriority != Process.THREAD_PRIORITY_LOWEST) {
				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
			}
			final ImageLoadTask loadTask = seekReadyTask();
			if (loadTask == null) {
				handler.sendEmptyMessage(ACTION_LOAD_FINISH);
				return;
			}
			try {
				// 从磁盘去取
				Bitmap bitmap = DiskCache.readCache(loadTask.getKey());
				if (bitmap == null) {// 尚未缓存，从源去取
					bitmap = loadFromSource(loadTask);
				}
				if (bitmap != null) {// 取得图片成功
					// 将bitmap放入LruCache缓存列表
					memoryCache.put(loadTask.filePath, bitmap);
				}
				final Bitmap finalBitmap = bitmap;
				if (loadTask.getCallBack() != null) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							loadTask.performCallback(finalBitmap);
						}
					});
				}
			} catch (OutOfMemoryError e) {
				memoryCache.evictAll();
				System.gc();
				e.printStackTrace();
				BDebug.e(TAG, "OutOfMemoryError!!!!!!!!!!!!!!!!!!!!:" + e.getMessage());
			} finally {
				if (loadTask != null) {
					synchronized (taskList) {
						taskList.remove(loadTask);
					}
				}
			}
		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ACTION_LOAD_START:
				if (countListener != null) {
					countListener.onImageLoadStart(ImageLoaderManager.this);
				}
				break;
			case ACTION_LOAD_FINISH:
				if (countListener != null) {
					countListener.onImageLoadFinish(ImageLoaderManager.this);
				}
				break;
			}
		}

	};

	public static interface ImageLoadStatusListener {

		void onImageLoadStart(ImageLoaderManager manager);

		void onImageLoadFinish(ImageLoaderManager manager);
	}

}
