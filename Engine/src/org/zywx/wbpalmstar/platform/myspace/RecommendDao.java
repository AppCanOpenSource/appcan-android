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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DownloadData;

import java.util.ArrayList;

public class RecommendDao {
    private DBHelper dbHelper;

    public static final String TAG = "RecommendDao";

    public RecommendDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 移除所有推荐列表
     */
    public synchronized void removeAllRecommendApps() {
        SQLiteDatabase db = null;
        int rows = 0;
        try {
            db = dbHelper.getWritableDatabase();
            rows = db.delete(DBHelper.TABLE_RECOMMEND_INFO, null, new String[]{});
        } catch (SQLException e) {
            BDebug.e(TAG, "removeAllRecommendApps() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "removeAllRecommendApps() rows::" + rows);
        }
    }

    /**
     * 根据softwareId移除推荐应用信息
     *
     * @param softwareId
     */
    public synchronized void removeRecommendAppBySoftwareId(String softwareId) {
        if (softwareId == null) {
            return;
        }
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete(DBHelper.TABLE_RECOMMEND_INFO, DBHelper.FILED_SOFTWARE_ID + "=?", new String[]{softwareId});
        } catch (SQLException e) {
            e.printStackTrace();
            BDebug.e(TAG, "removeRecommendAppBySoftwareId() ERROR:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "removeRecommendAppBySoftwareId() softId:" + softwareId);
        }
    }

    public synchronized byte[] getIconDataBySoftwareId(String softwareId) {
        if (softwareId == null) {
            return null;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        byte[] data = null;
        try {
            db = dbHelper.getReadableDatabase();
            final String sql = "SELECT * FROM " + DBHelper.TABLE_RECOMMEND_INFO + " WHERE "
                    + DBHelper.FILED_SOFTWARE_ID + "=?";
            cursor = db.rawQuery(sql, new String[]{softwareId});
            if (cursor.moveToNext()) {
                data = cursor.getBlob(cursor.getColumnIndex(DBHelper.FILED_ICON_DATA));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            BDebug.e(TAG, "getIconDataBySoftwareId() ERROR:" + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "updateCachePathBySoftwareId() softId:" + softwareId + "  data:" + data);
        }
        return data;
    }

    public synchronized void syncRecommendApps(ArrayList<DownloadData> serverApps) {
        if (serverApps == null || serverApps.size() == 0) {
            return;
        }
        // 获得所有本地的推荐列表
        ArrayList<DownloadData> localApps = getAllRecommendApps();
        for (DownloadData serverApp : serverApps) {
            if (localApps.contains(serverApp)) {// App已存在，更新数据即可
                updateRecommendInfo(serverApp);
                localApps.remove(serverApp);
            } else {//App不存在，添加
                saveRecommendApp(serverApp);
            }
        }
        //删除之前的推荐记录
        for (DownloadData item : localApps) {
            removeRecommendAppBySoftwareId(item.softwareId);
        }
    }

    public synchronized boolean updateRecommendInfo(DownloadData downloadData) {
        if (downloadData == null || downloadData.softwareId == null) {
            return false;
        }
        SQLiteDatabase db = null;
        boolean isUpdated = false;
        int rows = 0;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues(6);
            cv.put(DBHelper.FILED_APP_ID, downloadData.appId);
            cv.put(DBHelper.FILED_MODE, downloadData.mode);
            cv.put(DBHelper.FILED_APP_SIZE, downloadData.appSize);
            cv.put(DBHelper.FILED_APP_NAME, downloadData.appName);
            cv.put(DBHelper.FILED_ICON_LOC, downloadData.iconLoc);
            cv.put(DBHelper.FILED_DOWNLOAD_URL, downloadData.downloadUrl);
            rows = db.update(DBHelper.TABLE_RECOMMEND_INFO, cv, DBHelper.FILED_SOFTWARE_ID + "=?",
                    new String[]{downloadData.softwareId});
            if (rows > 0) {
                isUpdated = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            BDebug.e(TAG, "updateRecommendInfo() ERROR:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "updateRecommendInfo() softId:" + downloadData.softwareId + "  rows:" + rows);
        }
        return isUpdated;
    }

    public synchronized boolean updateCachePathBySoftwareId(String softwareId, byte[] cacheData) {
        if (softwareId == null) {
            return false;
        }
        SQLiteDatabase db = null;
        boolean isUpdated = false;
        int rows = 0;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues(1);
            cv.put(DBHelper.FILED_ICON_DATA, cacheData);
            rows = db.update(DBHelper.TABLE_RECOMMEND_INFO, cv, DBHelper.FILED_SOFTWARE_ID + "=?",
                    new String[]{softwareId});
            if (rows > 0) {
                isUpdated = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            BDebug.e(TAG, "updateCachePathBySoftwareId() ERROR:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "updateCachePathBySoftwareId() softId:" + softwareId + "  rows:" + rows);
        }
        return isUpdated;
    }

    public synchronized void saveRecommendApp(DownloadData downloadData) {
        if (downloadData == null) {
            throw new IllegalArgumentException("RecommendInfo can not be null...");
        }
        SQLiteDatabase db = null;
        try {
            String sql = "insert into " + DBHelper.TABLE_RECOMMEND_INFO + "(" + DBHelper.FILED_APP_ID + ","
                    + DBHelper.FILED_SOFTWARE_ID + "," + DBHelper.FILED_MODE + "," + DBHelper.FILED_APP_SIZE + ","
                    + DBHelper.FILED_APP_NAME + "," + DBHelper.FILED_ICON_LOC + "," + DBHelper.FILED_DOWNLOAD_URL
                    + ")values(?,?,?,?,?,?,?)";
            db = dbHelper.getWritableDatabase();
            db.execSQL(sql, new Object[]{downloadData.appId, downloadData.softwareId, downloadData.mode,
                    downloadData.appSize, downloadData.appName, downloadData.iconLoc, downloadData.downloadUrl});
        } catch (SQLException e) {
            e.printStackTrace();
            BDebug.e(TAG, "save recommend info fail...");
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public synchronized void saveRecommendAppList(ArrayList<DownloadData> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        for (DownloadData info : list) {
            saveRecommendApp(info);
        }
    }

    public synchronized ArrayList<DownloadData> getAllRecommendApps() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<DownloadData> arrayList = null;
        try {
            db = dbHelper.getReadableDatabase();
            final String sql = "SELECT * FROM " + DBHelper.TABLE_RECOMMEND_INFO;
            cursor = db.rawQuery(sql, new String[]{});
            if (cursor != null) {
                arrayList = new ArrayList<DownloadData>();
                while (cursor.moveToNext()) {
                    DownloadData downloadData = new DownloadData();
                    downloadData.appId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_ID));
                    downloadData.softwareId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_SOFTWARE_ID));
                    downloadData.mode = cursor.getInt(cursor.getColumnIndex(DBHelper.FILED_MODE));
                    downloadData.appSize = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_SIZE));
                    downloadData.appName = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_NAME));
                    downloadData.iconLoc = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_ICON_LOC));
                    downloadData.downloadUrl = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_DOWNLOAD_URL));
                    arrayList.add(downloadData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            BDebug.e(TAG, "getAllRecommendApps() ERROR:" + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return arrayList;
    }

}
