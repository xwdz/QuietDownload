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
import com.xwdz.download.DownloadConfig;
import com.xwdz.download.notify.DataUpdatedWatcher;
import com.xwdz.download.utils.Constants;
import com.xwdz.download.utils.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * @author 黄兴伟 (xwdz9989@gamil.com)
 */
public class QuietDownloader {

    private static final String TAG = QuietDownloader.class.getSimpleName();

    private static class Holder {
        private static final QuietDownloader INSTANCE = new QuietDownloader();
    }

    public static QuietDownloader getImpl() {
        return Holder.INSTANCE;
    }

    public static void initializeDownloader(Context context) {
        getImpl().initialize(new DownloadConfig(context));
    }

    public static void initializeDownloader(DownloadConfig downloadConfig) {
        getImpl().initialize(downloadConfig);
    }


    private static boolean sInitialize;

    //
    private DownloadConfig    mDownloadConfig;
    private long              mLastOperatedTime = 0;
    private DownloaderHandler mDownloadHandler;

    private QuietDownloader() {
    }

    private void initialize(DownloadConfig downloadConfig) {
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
    private boolean checkIfExecutable() {
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
    public void download(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }

        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_ADD, downloadEntry);
    }

    /**
     * 暂停一个任务
     */
    public void pause(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }
        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_PAUSE, downloadEntry);
    }

    /**
     * 恢复一个任务
     */
    public void resume(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }


        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_RESUME, downloadEntry);
    }

    /**
     * 取消一个任务
     */
    public void cancel(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }
        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_CANCEL, downloadEntry);
    }

    /**
     * 暂停队列所有任务
     */
    public void pauseAll() {
        if (!checkIfExecutable()) {
            return;
        }
        mDownloadHandler.handler(Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL, null);
    }

    /**
     * 恢复队列所有任务
     */
    public void recoverAll() {
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
    public void addObserver(DataUpdatedWatcher watcher) {
        mDownloadHandler.addObserver(watcher);
    }

    /**
     * 删除一个数据接收器
     *
     * @see DataUpdatedWatcher
     */
    public void removeObserver(DataUpdatedWatcher watcher) {
        mDownloadHandler.deleteObserver(watcher);
    }

    /**
     * @return 获取操作数据库Dao对象
     */
    public Dao<DownloadEntry, String> getDBDao() throws SQLException {
        return mDownloadHandler.getDao();
    }


    /**
     * 删除一个任务从数据库中
     */
    public void deleteById(String id) {
        mDownloadHandler.deleteById(id);
    }

    /**
     * 待删除的文件名称,从下载文件夹中
     *
     * @param name 文件名
     */
    public void deleteFileByName(String name) {
        File file = mDownloadConfig.getDownloadFile(name);
        if (file.exists())
            file.delete();
    }


    /**
     * 查询当前队列中是否有该 DownloadEntry
     */
    public DownloadEntry queryById(String id) {
        return mDownloadHandler.queryById(id);
    }

    /**
     * 从数据库中查询所有下载任务
     */
    public List<DownloadEntry> queryAll() {
        return mDownloadHandler.queryAll();
    }


    public DownloadConfig getConfigs() {
        return mDownloadConfig;
    }
}
