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

import com.j256.ormlite.dao.Dao;
import com.xwdz.download.DownloadConfig;
import com.xwdz.download.notify.DataUpdatedWatcher;
import com.xwdz.download.utils.Constants;
import com.xwdz.download.utils.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * @author xingwei.huang (xwdz9989@gmail.com)
 * @since v1.0.7
 */
final class _Loader {

    private static final String TAG = _Loader.class.getSimpleName();

    private static class Holder {
        private static final _Loader INSTANCE = new _Loader();
    }

    public static _Loader getImpl() {
        return Holder.INSTANCE;
    }

    private static boolean sInitialize;

    //
    private DownloadConfig    mDownloadConfig;
    private long              mLastOperatedTime = 0;
    private DownloaderHandler mDownloadHandler;

    private _Loader() { }

    void initialize(DownloadConfig downloadConfig) {
        if (!sInitialize) {
            DownloadDBManager.getImpl().initDBHelper(downloadConfig.getAppContext());
            mDownloadConfig = downloadConfig;
            mDownloadHandler = new DownloaderHandler(mDownloadConfig);
            sInitialize = true;
        }
    }

    /**
     * @return 检查事件间隔时间
     */
    boolean checkIfExecutable() {
        long    tmp               = System.currentTimeMillis();
        boolean isMinTimeInterval = tmp - mLastOperatedTime > mDownloadConfig.getMinOperateInterval();
        if (isMinTimeInterval && sInitialize) {
            mLastOperatedTime = tmp;
            return true;
        } else {
            Logger.e(TAG, "isMinTimeInterval:" + isMinTimeInterval + " sInitialize:" + sInitialize);
        }
        return false;
    }

    /**
     * 开始下载一个任务
     */
    void download(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }

        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_ADD, downloadEntry);
    }

    /**
     * 暂停一个任务
     */
    void pause(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }
        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_PAUSE, downloadEntry);
    }

    /**
     * 恢复一个任务
     */
    void resume(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }


        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_RESUME, downloadEntry);
    }

    /**
     * 取消一个任务
     */
    void cancel(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }
        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_CANCEL, downloadEntry);
    }

    /**
     * 暂停队列所有任务
     */
    void pauseAll() {
        if (!checkIfExecutable()) {
            return;
        }
        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL, null);
    }

    /**
     * 恢复队列所有任务
     */
    void recoverAll() {
        if (!checkIfExecutable()) {
            return;
        }

        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL, null);
    }

    /**
     * 添加一个数据接收器
     *
     * @see DataUpdatedWatcher
     */
    void addObserver(DataUpdatedWatcher watcher) {
        mDownloadHandler.addObserver(watcher);
    }

    /**
     * 删除一个数据接收器
     *
     * @see DataUpdatedWatcher
     */
    void removeObserver(DataUpdatedWatcher watcher) {
        mDownloadHandler.deleteObserver(watcher);
    }

    /**
     * @return 获取操作数据库Dao对象
     */
    Dao<DownloadEntry, String> getDBDao() throws SQLException {
        return mDownloadHandler.getDao();
    }


    /**
     * 删除一个任务从数据库中
     */
    void deleteById(String id) {
        mDownloadHandler.deleteById(id);
    }

    /**
     * 待删除的文件名称,从下载文件夹中
     *
     * @param name 文件名
     */
    void deleteFileByName(String name) {
        File file = mDownloadConfig.getDownloadFile(name);
        if (file.exists())
            file.delete();
    }


    /**
     * 查询当前队列中是否有该 DownloadEntry
     */
    DownloadEntry queryById(String id) {
        return mDownloadHandler.queryById(id);
    }

    /**
     * 从数据库中查询所有下载任务
     */
    List<DownloadEntry> queryAll() {
        return mDownloadHandler.queryAll();
    }


    DownloadConfig getConfigs() {
        return mDownloadConfig;
    }


}
