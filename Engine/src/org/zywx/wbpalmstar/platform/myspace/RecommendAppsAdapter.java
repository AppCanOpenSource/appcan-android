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

package org.zywx.wbpalmstar.platform.myspace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.base.cache.BytesArrayFactory;
import org.zywx.wbpalmstar.base.cache.BytesArrayFactory.BytesArray;
import org.zywx.wbpalmstar.base.cache.ImageLoadTask;
import org.zywx.wbpalmstar.base.cache.ImageLoadTask.ImageLoadTaskCallback;
import org.zywx.wbpalmstar.base.cache.ImageLoaderManager;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DownloadData;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class RecommendAppsAdapter extends BaseAdapter {
    public static final String TAG = "RecommendAppsAdapter";
    private ArrayList<DownloadData> appList;
    private LayoutInflater inflater;
    private GridView gridView;
    private Drawable defaultItemBg;
    private int destSize;
    private RecommendDao recommendDao;
    private DisplayMetrics dm;
    private ResoureFinder finder;
    private ImageLoaderManager loaderManager;

    public RecommendAppsAdapter(ArrayList<DownloadData> infos, Context context, GridView gridView) {
        if (infos == null || context == null || gridView == null) {
            throw new NullPointerException("Parmas can not be null.......");
        }
        finder = ResoureFinder.getInstance(context);
        appList = infos;
        inflater = LayoutInflater.from(context);
        loaderManager = ImageLoaderManager.initImageLoaderManager(context);
        this.gridView = gridView;
        recommendDao = new RecommendDao(context);
        defaultItemBg = finder.getDrawable("platform_myspace_grid_item_default_bg");
        dm = context.getResources().getDisplayMetrics();
        destSize = (int) (48 * dm.density);
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    public void reload(ArrayList<DownloadData> infos) {
        this.appList.clear();
        this.appList = infos;
        notifyDataSetChanged();
    }

    @Override
    public DownloadData getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(DownloadData downloadInfo) {
        appList.add(downloadInfo);
        notifyDataSetChanged();
    }

    public boolean addItemAtPostion(DownloadData downloadInfo, int postion) {
        if (postion < 0 || postion > getCount() - 1) {
            return false;
        }
        appList.add(postion, downloadInfo);
        return true;
    }

    public void removeItemAtPostion(int postion) {
        if (postion < 0 || postion > getCount() - 1) {
            return;
        }
        appList.remove(postion);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewCache viewCache = null;
        if (convertView == null) {
            convertView = inflater.inflate(finder.getLayoutId("platform_myspace_myapp_item"), null);
            viewCache = new ViewCache();
            viewCache.iconImageView = (ImageView) convertView.findViewById(finder.getId("platform_myspace_app_icon"));
            viewCache.nameTextView = (TextView) convertView.findViewById(finder.getId("platform_myspace_app_name"));
            convertView.setTag(viewCache);
        } else {
            viewCache = (ViewCache) convertView.getTag();
        }
        final DownloadData downloadData = appList.get(position);
        viewCache.iconImageView.setTag(downloadData.iconLoc);
        viewCache.nameTextView.setText(downloadData.appName);
        Bitmap bitmap = loaderManager.getCacheBitmap(downloadData.iconLoc);
        if (bitmap == null) {
            viewCache.iconImageView.setBackgroundDrawable(defaultItemBg);
            loaderManager.asyncLoad(new RecommendImageLoadTask(downloadData, downloadData.iconLoc)
                    .addCallback(new ImageLoadTaskCallback() {
                        @Override
                        public void onImageLoaded(ImageLoadTask task, Bitmap bitmap) {
                            View tagedView = gridView.findViewWithTag(task.filePath);
                            if (tagedView != null && bitmap != null) {
                                ((ImageView) tagedView).setBackgroundDrawable(new BitmapDrawable(bitmap));
                            }
                        }
                    }));
        } else {
            viewCache.iconImageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
        }
        return convertView;
    }

    private class RecommendImageLoadTask extends ImageLoadTask {

        private static final long serialVersionUID = 2063780269913439830L;
        private DownloadData downData;

        public RecommendImageLoadTask(DownloadData info, String filePath) {
            super(filePath);
            this.downData = info;
        }

        @Override
        protected Bitmap doInBackground() {
            byte[] data = recommendDao.getIconDataBySoftwareId(downData.softwareId);
            if (data != null) {
                return BitmapFactory.decodeByteArray(data, 0, data.length);
            }
            Bitmap bitmap = null;
            if (URLUtil.isNetworkUrl(filePath)) {
                bitmap = decodeHttpBitmap(filePath);
            } else if (filePath.startsWith("/") || filePath.startsWith("file://")) {
                bitmap = decodeLocalBitmap(filePath);
            } else if (filePath.startsWith("res://drawable")) {
                bitmap = decodeResourceBitmap(filePath);
            }
            Bitmap dest = null;
            if (bitmap != null) {
                Bitmap scaleBitmap = BUtility.imageScale(bitmap, destSize, destSize);
                dest = CommonUtility.GetRoundedCornerBitmap(scaleBitmap, 8 * dm.density);
                scaleBitmap.recycle();
                bitmap.recycle();
            }
            return dest;
        }

        @Override
        protected BytesArray transBitmapToBytesArray(Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            int size = bitmap.getWidth() * bitmap.getHeight() * 4;
            BytesArray bytesArray = BytesArrayFactory.getDefaultInstance().requestBytesArray(size);
            bitmap.compress(bitmap.hasAlpha() ? CompressFormat.PNG : CompressFormat.JPEG, 100, bytesArray);
            final String softwareId = downData.softwareId;
            if (recommendDao.getIconDataBySoftwareId(softwareId) == null) {
                byte[] data = bytesArray.toByteArray();
                recommendDao.updateCachePathBySoftwareId(downData.softwareId, data);
            }
            return bytesArray;
        }

        private Bitmap decodeHttpBitmap(String path) {
            Bitmap bitmap = null;
            if (URLUtil.isNetworkUrl(path)) {
                byte[] data = CommonUtility.downloadImage(path);
                if (data != null) {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            }
            return bitmap;
        }

        private Bitmap decodeLocalBitmap(String path) {
            Bitmap bitmap = null;
            if (path.startsWith("/") || path.startsWith("file://")) {
                String imgPath = path.replace("file://", "");
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(imgPath);
                    bitmap = BitmapFactory.decodeFileDescriptor(fis.getFD());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return bitmap;
        }

        private Bitmap decodeResourceBitmap(String path) {
            Bitmap bitmap = null;
            if (path.startsWith("res://drawable")) {
                bitmap = BitmapFactory.decodeResource(inflater.getContext().getResources(),
                        finder.getDrawableId("platform_myspace_grid_item_add_bg"));
            }
            return bitmap;
        }
    }

}
