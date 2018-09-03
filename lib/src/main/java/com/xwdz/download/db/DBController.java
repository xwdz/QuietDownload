/*
 * Copyright 2018 xwdz
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xwdz.download.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.xwdz.download.utils.Logger;

import java.sql.SQLException;
import java.util.ArrayList;


/**
 * @author xwdz(xwdz9989@gmail.com)
 */
public class DBController {

    private static final String TAG = DBController.class.getSimpleName();

    private static DBController instance;
    private DownloadDBHelper mDBHelper;

    private DBController(Context context) {
        mDBHelper = new DownloadDBHelper(context);
    }

    public static DBController getInstance(Context context) {
        if (instance == null) {
            instance = new DBController(context);
        }
        return instance;
    }

    public synchronized void newOrUpdate(DownloadEntry downloadEntry) {
        try {
            Dao<DownloadEntry, String> dao = mDBHelper.getDao(DownloadEntry.class);
            dao.createOrUpdate(downloadEntry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<DownloadEntry> queryAll() {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBHelper.getDao(DownloadEntry.class);
            return (ArrayList<DownloadEntry>) dao.query(dao.queryBuilder().prepare());
        } catch (SQLException e) {
            Logger.e(TAG,e.getMessage());
            return null;
        }
    }

    public synchronized DownloadEntry queryById(String id) {
        try {
            Dao<DownloadEntry, String> dao = mDBHelper.getDao(DownloadEntry.class);
            return dao.queryForId(id);
        } catch (SQLException e) {
            Logger.e(TAG,e.getMessage());
            return null;
        }
    }

    public void deleteById(String id) {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBHelper.getDao(DownloadEntry.class);
            dao.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
