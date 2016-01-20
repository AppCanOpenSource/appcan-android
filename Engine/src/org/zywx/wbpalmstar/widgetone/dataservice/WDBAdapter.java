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

package org.zywx.wbpalmstar.widgetone.dataservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WDBAdapter {
    public static final String F_DB_NAME = "wbpalmstar.db";
    public static final String F_WIDGET_TABLE_NAME = "widget";
    // public static final String F_SPACE_TABLE_NAME = "space";
    // public static final String F_WIDGETONE_TABLE_NAME = "widgetone";
    public static final int F_DB_VERSION = 12;

    public static final String F_COLUMN_ID = "_id";
    public static final String F_COLUMN_WIDGETONEID = "widgetOneId";
    public static final String F_COLUMN_WIDGETID = "widgetId";
    public static final String F_COLUMN_APPID = "appId";
    public static final String F_COLUMN_NAME = "name";
    public static final String F_COLUMN_VER = "ver";
    public static final String F_COLUMN_CHANNELCODE = "channelCode";
    public static final String F_COLUMN_IMEI = "imei";
    public static final String F_COLUMN_MD5CODE = "md5Code";
    public static final String F_COLUMN_FILEPATH = "filePath";
    // public static final String F_COLUMN_HTTPURL = "httpUrl";
    public static final String F_COLUMN_INDEXURL = "indexUrl";
    // public static final String F_COLUMN_FILESIZE = "fileSize";
    // public static final String F_COLUMN_DOWNLOADSIZE = "downloadSize";
    public static final String F_COLUMN_ICON = "icon";
    // public static final String F_COLUMN_STATE = "state";
    // public static final String F_COLUMN_PERMISSION = "permission";
    public static final String F_COLUMN_OBFUSCATION = "obfuscation";
    public static final String F_COLUMN_LOGSERVERIP = "logserverip";
    public static final String F_COLUMN_WGTTYPE = "wgtType";
    public static final String F_COLUMN_updateurl = "updateurl";
    public static final String F_COLUMN_SHOWSPACE = "spaceStatus";
    public static final String F_COLUMN_DESCRIPTION = "description";
    public static final String F_COLUMN_EMAIL = "email";
    public static final String F_COLUMN_AUTHOR = "author";
    public static final String F_COLUMN_LICENSE = "license";
    public static final String F_COLUMN_ORIENTATION = "orientation";
    public static final String F_COLUMN_OPAQUE = "opaque";
    public static final String F_COLUMN_BGCOLOR = "bgColor";

    public static final String F_WIDGET_CREATE_TABLE = "CREATE TABLE "
            + F_WIDGET_TABLE_NAME + " (" + F_COLUMN_ID
            + " INTEGER PRIMARY KEY," + F_COLUMN_WIDGETONEID + " TEXT,"
            + F_COLUMN_WIDGETID + " TEXT," + F_COLUMN_APPID + " TEXT,"
            + F_COLUMN_NAME + " TEXT," + F_COLUMN_VER + " TEXT,"
            + F_COLUMN_CHANNELCODE + " TEXT," + F_COLUMN_IMEI + " TEXT,"
            + F_COLUMN_MD5CODE + " TEXT," + F_COLUMN_FILEPATH + " TEXT,"
            + F_COLUMN_INDEXURL + " TEXT," + F_COLUMN_ICON + " TEXT,"
            + F_COLUMN_OBFUSCATION + " INTEGER," + F_COLUMN_LOGSERVERIP
            + " TEXT," + F_COLUMN_WGTTYPE + " INTEGER," + F_COLUMN_updateurl
            + " TEXT," + F_COLUMN_SHOWSPACE + " INTEGER,"
            + F_COLUMN_DESCRIPTION + " TEXT," + F_COLUMN_EMAIL + " TEXT,"
            + F_COLUMN_AUTHOR + " TEXT," + F_COLUMN_LICENSE + " TEXT,"
            + F_COLUMN_ORIENTATION + " INTEGER," + F_COLUMN_OPAQUE + " TEXT,"
            + F_COLUMN_BGCOLOR + " TEXT)";
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public WDBAdapter(Context ctx) {
        try {
            DBHelper = new DatabaseHelper(ctx);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, F_DB_NAME, null, F_DB_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("DROP TABLE IF EXISTS " + F_WIDGET_TABLE_NAME);

            db.execSQL(F_WIDGET_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + F_WIDGET_TABLE_NAME);
            onCreate(db);
        }
    }

    // ---打开数据库---

    public WDBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    // ---关闭数据库---

    public void close() {
        DBHelper.close();
    }

    /**
     * 插入
     */
    public long insert(ContentValues cv, String tableName) {
        if (db == null || tableName == null || cv == null) {
            return -1;
        }
        return db.insert(tableName, null, cv);

    }

    public int deleteTable(String tableName) {
        if (db == null) {
            return -1;
        }
        return db.delete(tableName, null, null);
    }

    /**
     * 修改
     */
    public void update(String sql) {
        db.execSQL(sql);
    }

    /**
     * 删除
     */
    public void delete(int id, String tableName) {
        db.delete(tableName, "_id=" + id, null);

    }

    public int deleteByAppID(String tableName, String appId) {
        return db.delete(tableName, F_COLUMN_APPID + "='" + appId + "'", null);
    }

    public void deleteByType(int type) {
        db.delete(F_WIDGET_TABLE_NAME, F_COLUMN_WGTTYPE + "=" + type, null);
    }

    /**
     * 查询
     */
    public Cursor select(String sql) {
        return db.rawQuery(sql, null);

    }

}
