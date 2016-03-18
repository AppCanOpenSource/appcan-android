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

package org.zywx.wbpalmstar.base;

import android.widget.ImageView;

import com.ace.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.ace.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.ace.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.ace.universalimageloader.core.DisplayImageOptions;
import com.ace.universalimageloader.core.ImageLoader;
import com.ace.universalimageloader.core.ImageLoaderConfiguration;
import com.ace.universalimageloader.core.assist.QueueProcessingType;
import com.ace.universalimageloader.core.download.BaseImageDownloader;

import org.zywx.wbpalmstar.base.cache.DiskCache;

/**
 * Created by ylt on 2015/4/28.
 */
public class ACEImageLoader {

    private static ACEImageLoader aceImageLoader;

    private ACEImageLoader() {
        if (BConstant.app == null) {
            return;
        }
        DiskCache.initDiskCache(BConstant.app);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(BConstant.app)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .memoryCache(new LRULimitedMemoryCache(10 * 1024 * 1024))
                .memoryCacheSize(10 * 1024 * 1024)
                .diskCache(new UnlimitedDiskCache(DiskCache.cacheFolder))//自定义缓存路径
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(200 * 1024 * 1024)
                .diskCacheFileCount(100)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(BConstant.app, 5 * 1000, 30 * 1000)) // connectTimeout (5 s),
                // readTimeout (30 s)超时时间
                .writeDebugLogs() // Remove for release app
                .build();//开始构建
        ImageLoader.getInstance().init(config);
    }

    public synchronized static ACEImageLoader getInstance() {
        if (aceImageLoader == null) {
            aceImageLoader = new ACEImageLoader();
        }
        return aceImageLoader;
    }

    public <T extends ImageView> void displayImage(T imageView, String imgUrl) {
        String realImgUrl = null;
        if (imgUrl.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
            String assetFileName = BUtility.F_Widget_RES_path
                    + imgUrl.substring(BUtility.F_Widget_RES_SCHEMA.length());
            realImgUrl = "assets://" + assetFileName;
        } else if (imgUrl.startsWith(BUtility.F_FILE_SCHEMA)) {
            realImgUrl = imgUrl;
        } else if (imgUrl.startsWith(BUtility.F_Widget_RES_path)) {
            realImgUrl = "assets://" + imgUrl;
        } else if (imgUrl.startsWith("/")) {
            realImgUrl = BUtility.F_FILE_SCHEMA + imgUrl;
        } else {
            realImgUrl = imgUrl;
        }
        displayImageWithOptions(realImgUrl,imageView,true);
    }

    public <T extends ImageView> void displayImageWithOptions(String imgUrl, T imageView,boolean cacheOnDisk)
    {
        if (cacheOnDisk){
            DisplayImageOptions options;
            options=new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .build();
            ImageLoader.getInstance().displayImage(imgUrl, imageView,options);
        }else{
            ImageLoader.getInstance().displayImage(imgUrl, imageView);
        }

    }



}
