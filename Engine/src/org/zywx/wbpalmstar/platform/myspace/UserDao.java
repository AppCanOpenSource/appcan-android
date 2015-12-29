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
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DownloadData;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.InstallInfo;

import java.util.ArrayList;

public class UserDao {

    public static final String TAG = "UserDao";
    private DBHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * insert installinfo into database
     *
     * @param userId
     * @param installInfo
     */
    public void installApp(String userId, InstallInfo installInfo) {
        if (userId == null || installInfo == null) {
            return;
        }
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            String sql = "insert into " + DBHelper.TABLE_INSTALL_INFO + "(" + DBHelper.FILED_USER_ID + ","
                    + DBHelper.FILED_APP_ID + "," + DBHelper.FILED_SOFTWARE_ID + "," + DBHelper.FILED_MODE + ","
                    + DBHelper.FILED_APP_SIZE + "," + DBHelper.FILED_APP_NAME + "," + DBHelper.FILED_ICON_LOC + ","
                    + DBHelper.FILED_DOWNLOAD_URL + "," + DBHelper.FILED_INSTALL_PATH + ","
                    + DBHelper.FILED_IS_DOWNLOAD + ")values(?,?,?,?,?,?,?,?,?,?)";
            DownloadData info = installInfo.getDownloadInfo();
            db.execSQL(sql, new Object[]{userId, info.appId, info.softwareId, info.mode, info.appSize, info.appName,
                    info.iconLoc, info.downloadUrl, installInfo.installPath,
                    installInfo.isDownload ? InstallInfo.TRUE : InstallInfo.FALSE});
            BDebug.i(TAG, "db installApp " + installInfo.getDownloadInfo().appName + "  success!");
        } catch (Exception e) {
            BDebug.e(TAG, "insert install info error!....");
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * delete installinfo from database, by userId and appId
     *
     * @param userId
     * @param appId
     * @return
     */
    public boolean uninstallApp(String userId, String appId) {
        SQLiteDatabase db = null;
        boolean isSuc = false;
        try {
            db = dbHelper.getWritableDatabase();
            int rows = db.delete(DBHelper.TABLE_INSTALL_INFO, DBHelper.FILED_APP_ID + "=? and "
                    + DBHelper.FILED_USER_ID + "=?", new String[]{appId, userId});
            if (rows > 0) {
                isSuc = true;
            }
            BDebug.d(TAG, "uninstallApp-->rows: " + rows + " userId:" + userId + " appId=" + appId);
        } catch (SQLException e) {
            BDebug.e(TAG, "delete install info fail...");
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return isSuc;
    }

    /**
     * 根据userId和appId查询应用的InstallInfo
     *
     * @param userId
     * @param appId
     * @return 应用的InstallInfo
     */
    public InstallInfo getInstallInfoByUserIdAndAppId(String userId, String appId) {
        if (userId == null || appId == null) {
            return null;
        }
        Cursor cursor = null;
        SQLiteDatabase db = null;
        InstallInfo installInfo = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "SELECT * FROM " + DBHelper.TABLE_INSTALL_INFO + " WHERE " + DBHelper.FILED_APP_ID + "=? and "
                    + DBHelper.FILED_USER_ID + "=?";
            cursor = db.rawQuery(sql, new String[]{appId, userId});
            if (cursor != null && cursor.moveToNext()) {
                installInfo = new InstallInfo();
                installInfo.getDownloadInfo().appId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_ID));
                installInfo.getDownloadInfo().softwareId = cursor.getString(cursor
                        .getColumnIndex(DBHelper.FILED_SOFTWARE_ID));
                installInfo.getDownloadInfo().mode = cursor.getInt(cursor.getColumnIndex(DBHelper.FILED_MODE));
                installInfo.getDownloadInfo().appSize = cursor
                        .getString(cursor.getColumnIndex(DBHelper.FILED_APP_SIZE));
                installInfo.getDownloadInfo().appName = cursor
                        .getString(cursor.getColumnIndex(DBHelper.FILED_APP_NAME));
                installInfo.getDownloadInfo().iconLoc = cursor
                        .getString(cursor.getColumnIndex(DBHelper.FILED_ICON_LOC));
                installInfo.getDownloadInfo().downloadUrl = cursor.getString(cursor
                        .getColumnIndex(DBHelper.FILED_DOWNLOAD_URL));
                installInfo.installPath = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_INSTALL_PATH));
                installInfo.isDownload = cursor.getInt(cursor.getColumnIndex(DBHelper.FILED_IS_DOWNLOAD)) == InstallInfo.TRUE ? true
                        : false;
                BDebug.d(TAG, "getInstallInfoByUserIdAndAppId(): " + installInfo);
            }
        } catch (SQLException e) {
            installInfo = null;
            BDebug.e(TAG, "getInstallInfoByUserIdAndAppId() error:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return installInfo;
    }


    public ArrayList<InstallInfo> getInstallInfosByUserId(String userId) {
        if (userId == null) {
            return null;
        }
        ArrayList<InstallInfo> arrayList = null;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            String sql = "select * from " + DBHelper.TABLE_INSTALL_INFO + " where " + DBHelper.FILED_USER_ID + "=?";
            Cursor cursor = db.rawQuery(sql, new String[]{userId});
            arrayList = new ArrayList<InstallInfo>();
            while (cursor.moveToNext()) {
                InstallInfo info = new InstallInfo();
                info.getDownloadInfo().appId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_ID));
                info.getDownloadInfo().softwareId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_SOFTWARE_ID));
                info.getDownloadInfo().mode = cursor.getInt(cursor.getColumnIndex(DBHelper.FILED_MODE));
                info.getDownloadInfo().appSize = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_SIZE));
                info.getDownloadInfo().appName = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_NAME));
                info.getDownloadInfo().iconLoc = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_ICON_LOC));
                info.getDownloadInfo().downloadUrl = cursor.getString(cursor
                        .getColumnIndex(DBHelper.FILED_DOWNLOAD_URL));
                info.installPath = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_INSTALL_PATH));
                info.isDownload = cursor.getInt(cursor.getColumnIndex(DBHelper.FILED_IS_DOWNLOAD)) == 1 ? true : false;
                arrayList.add(info);
            }
        } catch (SQLException e) {
            BDebug.e(TAG, "get all  install info fail...");
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return arrayList;
    }

}
