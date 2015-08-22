package org.zywx.wbpalmstar.base;

import org.zywx.wbpalmstar.base.cache.DiskCache;

import android.app.Application;
import android.widget.ImageView;

import com.ace.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.ace.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.ace.universalimageloader.core.DisplayImageOptions;
import com.ace.universalimageloader.core.ImageLoader;
import com.ace.universalimageloader.core.ImageLoaderConfiguration;
import com.ace.universalimageloader.core.assist.QueueProcessingType;
import com.ace.universalimageloader.core.download.BaseImageDownloader;

/**
 * Created by ylt on 2015/4/28.
 */
public class ACEImageLoader {

    private static ACEImageLoader aceImageLoader;

    private ACEImageLoader(){
        if (BConstant.app==null){
            return;
        }
        DiskCache.initDiskCache(BConstant.app);
        ImageLoaderConfiguration  config=new ImageLoaderConfiguration
                .Builder(BConstant.app)
                .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCache(new UnlimitedDiskCache(DiskCache.cacheFolder))//自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(BConstant.app, 5 * 1000, 30 * 1000)) // connectTimeout (5 s),
                // readTimeout (30 s)超时时间
                .writeDebugLogs() // Remove for release app
                .build();//开始构建
        ImageLoader.getInstance().init(config);
    }

    public synchronized static ACEImageLoader getInstance(){
        if (aceImageLoader==null){
            aceImageLoader=new ACEImageLoader();
        }
        return aceImageLoader;
    }

    public <T extends ImageView> void displayImage(T imageView,String imgUrl){
        String realImgUrl=null;
        if (imgUrl.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
            String assetFileName =BUtility.F_Widget_RES_path
                    + imgUrl.substring(BUtility.F_Widget_RES_SCHEMA.length());
            realImgUrl="assets://"+assetFileName;
        } else if (imgUrl.startsWith(BUtility.F_FILE_SCHEMA)) {
            realImgUrl=imgUrl;
        } else if (imgUrl.startsWith(BUtility.F_Widget_RES_path)) {
            realImgUrl="assets://"+imgUrl;
        } else if (imgUrl.startsWith("/")) {
            realImgUrl=BUtility.F_FILE_SCHEMA+imgUrl;
        } else if (imgUrl.startsWith("http://")) {
            realImgUrl=imgUrl;
        }
        ImageLoader.getInstance().displayImage(realImgUrl,imageView);
    }

}
