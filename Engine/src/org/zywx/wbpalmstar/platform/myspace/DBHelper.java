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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = "DBHelper";
    public static final int DB_VERSION = 2;
    public static final String DB_NAME = "appinfo.db";
    public static final String TABLE_INSTALL_INFO = "installinfo";
    public static final String TABLE_RECOMMEND_INFO = "recommendinfo";
    public static final String TABLE_DELAY_INSTALL = "delayinstall";
    public static final String TABLE_DELAY_START = "delaystart";
    public static final String TABLE_DELAY_UNINSTALL = "delayunistall";
    public static final String FILED_ID = "_id";
    public static final String FILED_USER_ID = "user_id";
    public static final String FILED_APP_ID = "app_id";
    public static final String FILED_SOFTWARE_ID = "software_id";
    public static final String FILED_MODE = "mode";
    public static final String FILED_APP_SIZE = "app_size";
    public static final String FILED_APP_NAME = "app_name";
    public static final String FILED_ICON_LOC = "icon_loc";
    public static final String FILED_DOWNLOAD_URL = "download_url";
    public static final String FILED_INSTALL_PATH = "install_path";
    public static final String FILED_IS_DOWNLOAD = "is_download";
    public static final String FILED_ICON_DATA = "icon_data";
    public static final String FILED_SESSION_KEY = "session_key";
    public static final String FILED_REPORT_TIME = "report_time";
    public static final String FILED_PLATFORM_ID = "platform_id";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 安装信息表结构
        String sql1 = "CREATE TABLE IF NOT EXISTS " + TABLE_INSTALL_INFO + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FILED_USER_ID + " TEXT," + FILED_APP_ID + " TEXT," + FILED_SOFTWARE_ID + " TEXT," + FILED_MODE
                + " INTEGER," + FILED_APP_SIZE + " TEXT," + FILED_APP_NAME + " TEXT," + FILED_ICON_LOC + " TEXT,"
                + FILED_DOWNLOAD_URL + " TEXT," + FILED_INSTALL_PATH + " TEXT," + FILED_IS_DOWNLOAD + " INTEGER)";
        // 推荐历史记录表结构
        String sql2 = "CREATE TABLE IF NOT EXISTS " + TABLE_RECOMMEND_INFO + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FILED_APP_ID + " TEXT," + FILED_SOFTWARE_ID + " TEXT," + FILED_MODE + " INTEGER," + FILED_APP_SIZE
                + " TEXT," + FILED_APP_NAME + " TEXT," + FILED_ICON_LOC + " TEXT," + FILED_DOWNLOAD_URL + " TEXT,"
                + FILED_ICON_DATA + " BLOB)";
        // 延迟安装上报信息表结构
        String sql3 = "CREATE TABLE IF NOT EXISTS " + TABLE_DELAY_INSTALL + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FILED_SESSION_KEY + " TEXT," + FILED_APP_ID + " TEXT," + FILED_SOFTWARE_ID + " TEXT,"
                + FILED_PLATFORM_ID + " TEXT," + FILED_REPORT_TIME + " TEXT)";
        // 延迟卸载上报表结构
        String sql4 = "CREATE TABLE IF NOT EXISTS " + TABLE_DELAY_UNINSTALL + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FILED_SESSION_KEY + " TEXT," + FILED_APP_ID + " TEXT," + FILED_SOFTWARE_ID + " TEXT,"
                + FILED_PLATFORM_ID + " TEXT," + FILED_REPORT_TIME + " TEXT)";
        //延迟启动上报表结构
        String sql5 = "CREATE TABLE IF NOT EXISTS " + TABLE_DELAY_START + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FILED_SESSION_KEY + " TEXT," + FILED_SOFTWARE_ID + " TEXT," + FILED_REPORT_TIME + " TEXT)";
        db.beginTransaction();
        Log.d(TAG, "DB onCreate success-------->");
        try {
            db.execSQL(sql1);
            db.execSQL(sql2);
            db.execSQL(sql3);
            db.execSQL(sql4);
            db.execSQL(sql5);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "create table fail......");
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS   " + TABLE_INSTALL_INFO);
            db.execSQL("DROP TABLE IF EXISTS  " + TABLE_RECOMMEND_INFO);
            db.execSQL("DROP TABLE IF EXISTS  " + TABLE_DELAY_INSTALL);
            db.execSQL("DROP TABLE IF EXISTS  " + TABLE_DELAY_START);
            db.execSQL("DROP TABLE IF EXISTS  " + TABLE_DELAY_UNINSTALL);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "delete table fail......");
        } finally {
            db.endTransaction();
        }
        onCreate(db);
    }

}
