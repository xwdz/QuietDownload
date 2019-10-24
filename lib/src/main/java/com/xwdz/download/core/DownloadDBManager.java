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

package com.xwdz.download.core;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.xwdz.download.utils.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huangxingwei (xwdz9989@gamil.com)
 * @since 2018/9/20
 */
class DownloadDBManager {

    private static final String TAG = DownloadDBManager.class.getSimpleName();

    private static DownloadDBManager mInstance;

    private DownloadDBHelper mDBHelper;

    private DownloadDBManager() {
    }

    public static DownloadDBManager getImpl() {
        if (mInstance == null) {
            mInstance = new DownloadDBManager();
        }
        return mInstance;
    }

    public void initDBHelper(Context context) {
        mDBHelper = new DownloadDBHelper(context.getApplicationContext());
    }

    synchronized void newOrUpdate(DownloadEntry downloadEntry) {
        try {
            Dao<DownloadEntry, String> dao = mDBHelper.getDao(DownloadEntry.class);
            dao.createOrUpdate(downloadEntry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized List<DownloadEntry> queryAll() {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBHelper.getDao(DownloadEntry.class);
            return dao.query(dao.queryBuilder().prepare());
        } catch (SQLException e) {
            Logger.e(TAG, e.getMessage());
            return new ArrayList<>();
        }
    }

    synchronized DownloadEntry queryById(String id) {
        try {
            Dao<DownloadEntry, String> dao = mDBHelper.getDao(DownloadEntry.class);
            return dao.queryForId(id);
        } catch (SQLException e) {
            Logger.e(TAG, e.getMessage());
            return null;
        }
    }

    public Dao<DownloadEntry, String> getDao() throws SQLException {
        return mDBHelper.getDao(DownloadEntry.class);
    }

    synchronized int deleteById(String id) {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBHelper.getDao(DownloadEntry.class);
            return dao.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
