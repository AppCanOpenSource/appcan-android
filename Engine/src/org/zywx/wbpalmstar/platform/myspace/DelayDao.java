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
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayInstallInfo;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayStartInfo;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayUninstallInfo;

import java.util.ArrayList;

public class DelayDao {
    public static final String TAG = "DelayDao";
    private DBHelper dbHelper;

    public DelayDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 添加延迟安装信息
     *
     * @param delayInstallInfo
     * @return
     */
    public boolean addDelayInstallInfo(DelayInstallInfo delayInstallInfo) {
        SQLiteDatabase db = null;
        boolean isInserted = false;
        long rowId = 0;
        try {
            db = dbHelper.getWritableDatabase();
            final ContentValues cv = new ContentValues();
            cv.put(DBHelper.FILED_SESSION_KEY, delayInstallInfo.sessionKey);
            cv.put(DBHelper.FILED_APP_ID, delayInstallInfo.mainAppId);
            cv.put(DBHelper.FILED_SOFTWARE_ID, delayInstallInfo.softwareId);
            cv.put(DBHelper.FILED_PLATFORM_ID, delayInstallInfo.platformId);
            cv.put(DBHelper.FILED_REPORT_TIME, delayInstallInfo.reportTime);
            rowId = db.insert(DBHelper.TABLE_DELAY_INSTALL, null, cv);
            if (rowId > 0) {
                isInserted = true;
            }
        } catch (SQLException e) {
            BDebug.e(TAG, "addDelayInstallInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "addDelayInstallInfo-->rowId:" + rowId);
        }
        return isInserted;
    }

    /**
     * 取得所有延迟安装信息
     *
     * @return
     */
    public ArrayList<DelayInstallInfo> getAllDelayInstallInfo() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<DelayInstallInfo> arrayList = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "SELECT * FROM " + DBHelper.TABLE_DELAY_INSTALL;
            cursor = db.rawQuery(sql, new String[]{});
            if (cursor != null) {
                arrayList = new ArrayList<DelayInstallInfo>();
                while (cursor.moveToNext()) {
                    final DelayInstallInfo info = new DelayInstallInfo();
                    info.sessionKey = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_SESSION_KEY));
                    info.mainAppId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_ID));
                    info.softwareId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_SOFTWARE_ID));
                    info.platformId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_PLATFORM_ID));
                    info.reportTime = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_REPORT_TIME));
                    arrayList.add(info);
                }
            }
        } catch (SQLException e) {
            BDebug.e(TAG, "getAllDelayInstallInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "getAllDelayInstallInfo() size:" + (arrayList != null ? arrayList.size() : null));
        }
        return arrayList;
    }

    /**
     * 删除所有延迟安装信息
     */
    public void removeAllDelayInstallInfo() {
        SQLiteDatabase db = null;
        int rows = 0;
        try {
            db = dbHelper.getWritableDatabase();
            rows = db.delete(DBHelper.TABLE_DELAY_INSTALL, null, new String[]{});

        } catch (SQLException e) {
            BDebug.e(TAG, "removeAllDelayInstallInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "removeAllDelayInstallInfo()-->rows:" + rows);
        }
    }

    /**
     * 添加延迟卸载信息
     *
     * @param delayInstallInfo
     * @return
     */
    public boolean addDelayUninstallInfo(DelayUninstallInfo delayUninstallInfo) {
        SQLiteDatabase db = null;
        boolean isInserted = false;
        long rowId = 0;
        try {
            db = dbHelper.getWritableDatabase();
            final ContentValues cv = new ContentValues();
            cv.put(DBHelper.FILED_SESSION_KEY, delayUninstallInfo.sessionKey);
            cv.put(DBHelper.FILED_APP_ID, delayUninstallInfo.mainAppId);
            cv.put(DBHelper.FILED_SOFTWARE_ID, delayUninstallInfo.softwareId);
            cv.put(DBHelper.FILED_PLATFORM_ID, delayUninstallInfo.platformId);
            cv.put(DBHelper.FILED_REPORT_TIME, delayUninstallInfo.reportTime);
            rowId = db.insert(DBHelper.TABLE_DELAY_UNINSTALL, null, cv);
            if (rowId > 0) {
                isInserted = true;
            }
        } catch (SQLException e) {
            BDebug.e(TAG, "addDelayUninstallInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "addDelayUninstallInfo rowId:" + rowId);
        }
        return isInserted;
    }

    /**
     * 取得所有延迟卸载信息
     *
     * @return
     */
    public ArrayList<DelayUninstallInfo> getAllDelayUninstallInfo() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<DelayUninstallInfo> arrayList = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "SELECT * FROM " + DBHelper.TABLE_DELAY_UNINSTALL;
            cursor = db.rawQuery(sql, new String[]{});
            if (cursor != null) {
                arrayList = new ArrayList<DelayUninstallInfo>();
                while (cursor.moveToNext()) {
                    final DelayUninstallInfo info = new DelayUninstallInfo();
                    info.sessionKey = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_SESSION_KEY));
                    info.mainAppId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_APP_ID));
                    info.softwareId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_SOFTWARE_ID));
                    info.platformId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_PLATFORM_ID));
                    info.reportTime = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_REPORT_TIME));
                    arrayList.add(info);
                }
            }
        } catch (SQLException e) {
            BDebug.e(TAG, "getAllDelayUninstallInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "getAllDelayUninstallInfo() size:" + (arrayList != null ? arrayList.size() : null));
        }
        return arrayList;
    }

    /**
     * 删除所有延迟卸载信息
     */
    public void removeAllDelayUninstallInfo() {
        SQLiteDatabase db = null;
        int rows = 0;
        try {
            db = dbHelper.getWritableDatabase();
            rows = db.delete(DBHelper.TABLE_DELAY_UNINSTALL, null, new String[]{});
        } catch (SQLException e) {
            BDebug.e(TAG, "removeAllDelayUninstallInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "removeAllDelayUninstallInfo()-->rows:" + rows);
        }
    }

    /**
     * 添加延迟卸载信息
     *
     * @param delayInstallInfo
     * @return
     */
    public boolean addDelayStartInfo(DelayStartInfo delayStartInfo) {
        SQLiteDatabase db = null;
        boolean isInserted = false;
        long rowId = 0;
        try {
            db = dbHelper.getWritableDatabase();
            final ContentValues cv = new ContentValues();
            cv.put(DBHelper.FILED_SESSION_KEY, delayStartInfo.sessionKey);
            cv.put(DBHelper.FILED_SOFTWARE_ID, delayStartInfo.softwareId);
            cv.put(DBHelper.FILED_REPORT_TIME, delayStartInfo.reportTime);
            rowId = db.insert(DBHelper.TABLE_DELAY_START, null, cv);
            if (rowId > 0) {
                isInserted = true;
            }
        } catch (SQLException e) {
            BDebug.e(TAG, "addDelayStartInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "addDelayStartInfo()-->rowId:" + rowId);
        }
        return isInserted;
    }

    /**
     * 取得所有延迟启动信息
     *
     * @return
     */
    public ArrayList<DelayStartInfo> getAllDelayStartInfo() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<DelayStartInfo> arrayList = null;
        try {
            db = dbHelper.getReadableDatabase();
            String sql = "SELECT * FROM " + DBHelper.TABLE_DELAY_START;
            cursor = db.rawQuery(sql, new String[]{});
            if (cursor != null) {
                arrayList = new ArrayList<DelayStartInfo>();
                while (cursor.moveToNext()) {
                    final DelayStartInfo info = new DelayStartInfo();
                    info.sessionKey = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_SESSION_KEY));
                    info.softwareId = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_SOFTWARE_ID));
                    info.reportTime = cursor.getString(cursor.getColumnIndex(DBHelper.FILED_REPORT_TIME));
                    arrayList.add(info);
                }
            }
        } catch (SQLException e) {
            BDebug.e(TAG, "getAllDelayStartInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "getAllDelayStartInfo() size:" + (arrayList != null ? arrayList.size() : null));
        }
        return arrayList;
    }

    /**
     * 删除所有延迟卸载信息
     */
    public void removeAllDelayStartInfo() {
        SQLiteDatabase db = null;
        int rows = 0;
        try {
            db = dbHelper.getWritableDatabase();
            rows = db.delete(DBHelper.TABLE_DELAY_START, null, new String[]{});

        } catch (SQLException e) {
            BDebug.e(TAG, "removeAllDelayStartInfo() ERROR:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
            BDebug.d(TAG, "removeAllDelayStartInfo()---> rows:" + rows);
        }
    }
}
