package com.xwdz.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author xingwei.huang (xwdz9989@gmail.com)
 * @since v1.0.6
 */
public class DownloaderSQLite extends SQLiteOpenHelper {

    private static final String DB_NAME = "quiet_downloader_m.db";
    private static final int DB_VERSION = 1;

//
//    private static final String CREATE_WHITES_SQL = "CREATE TABLE IF NOT EXISTS " + T_Whites.T_NAME + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
//            + T_Whites.COLUMN_PACKAGE_NAME + " TEXT, "
//            + T_Whites.COLUMN_LAST_UPDATE_TIME + " TEXT);";
//
//    private static final String CREATE_AD_PKGS_SQL = "CREATE TABLE IF NOT EXISTS " + T_AdStatus.T_NAME + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
//            + T_AdStatus.COLUMN_PACKAGE_NAME + " TEXT, "
//            + T_AdStatus.COLUMN_LAST_SHOW_AD_TIME + " TEXT, "
//            + T_AdStatus.COLUMN_SHOW_AD_COUNT + " INTEGER);";
//
//
//    private static final String CREATE_AD_INSTALL_SQL = "CREATE TABLE IF NOT EXISTS " + T_AppInstall.T_NAME + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
//            + T_AppInstall.COLUMN_INSTALL_APP_TIME + " TEXT, "
//            + T_AppInstall.COLUMN_PACKAGE_NAME + " TEXT);";
//
//    private SQLiteDatabase mDB;
//
//
    public DownloaderSQLite(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
//        getDB();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(CREATE_WHITES_SQL);
//        db.execSQL(CREATE_AD_PKGS_SQL);
//        db.execSQL(CREATE_AD_INSTALL_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + T_Whites.T_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + T_AdStatus.T_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + T_AppInstall.T_NAME);
//        onCreate(db);
    }

//    public SQLiteDatabase getDB() {
//        try {
//            if (mDB == null) {
//                mDB = getWritableDatabase();
//            }
//        } catch (SQLiteException e) {
//        }
//
//        return mDB;
//    }

}
