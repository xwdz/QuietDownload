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
import android.content.Intent;
import android.os.Build;

import com.j256.ormlite.dao.Dao;
import com.xwdz.download.DownloadConfig;
import com.xwdz.download.notify.DataUpdatedWatcher;
import com.xwdz.download.utils.Constants;
import com.xwdz.download.utils.LOG;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author 黄兴伟 (xwdz9989@gamil.com)
 */
public class QuietDownloader {

    private static final String TAG = QuietDownloader.class.getSimpleName();

    private static class Holder {
        private static final QuietDownloader INSTANCE = new QuietDownloader();
    }

    public static QuietDownloader get() {
        return Holder.INSTANCE;
    }

    private QuietDownloader() {
        mDataChanger = DataChanger.getImpl();
    }


    private static boolean mInit;

    private Context mContext;
    private long mLastOperatedTime = 0;
    private DataChanger mDataChanger;
    private DownloadConfig mDownloadConfig;


    public void setDownloadConfig(DownloadConfig downloadConfig) {
        this.mDownloadConfig = downloadConfig;
    }

    public void bindContext(Context context) {
        this.mContext = context.getApplicationContext();
        this.mDownloadConfig = new DownloadConfig(mContext);
        this.mDataChanger.initContext(mContext);
        DownloadDBManager.getImpl().initDBHelper(mContext);
    }


    public void startService() {
        mInit = true;
        Intent intent = new Intent(mContext, DownloadService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            mContext.startForegroundService(intent);
        } else {
            mContext.startService(intent);
        }
    }

    /**
     * @return 检查事件间隔时间
     */
    private boolean checkIfExecutable() {
        long tmp = System.currentTimeMillis();
        boolean isMinTimeInterval = tmp - mLastOperatedTime > mDownloadConfig.getMinOperateInterval();
        if (isMinTimeInterval && mInit) {
            mLastOperatedTime = tmp;
            return true;
        } else {
            LOG.e(TAG, "isMinTimeInterval:" + isMinTimeInterval + " mInit:" + mInit);
        }
        return false;
    }

    /**
     * 开始下载一个任务
     */
    public void startDownload(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }

        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD);
        mContext.startService(intent);
    }

    /**
     * 暂停一个任务
     */
    public void pause(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE);
        mContext.startService(intent);
    }

    /**
     * 恢复一个任务
     */
    public void resume(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }

        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RESUME);
        mContext.startService(intent);
    }

    /**
     * 取消一个任务
     */
    public void cancel(DownloadEntry downloadEntry) {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_CANCEL);
        mContext.startService(intent);
    }

    /**
     * 暂停队列所有任务
     */
    public void pauseAll() {
        if (!checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL);
        mContext.startService(intent);
    }

    /**
     * 恢复队列所有任务
     */
    public void recoverAll() {
        if (!checkIfExecutable()) {
            return;
        }

        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL);
        mContext.startService(intent);
    }

    /**
     * 添加一个数据接收器
     *
     * @see DataUpdatedWatcher
     */
    public void addObserver(DataUpdatedWatcher watcher) {
        mDataChanger.addObserver(watcher);
    }

    /**
     * 删除一个数据接收器
     *
     * @see DataUpdatedWatcher
     */
    public void removeObserver(DataUpdatedWatcher watcher) {
        mDataChanger.deleteObserver(watcher);
    }

    /**
     * @return 获取操作数据库Dao对象
     */
    public Dao<DownloadEntry, String> getDBDao() throws SQLException {
        return DownloadDBManager.getImpl().getDao();
    }


    /**
     * 删除一个任务从数据库中
     */
    public void deleteById(String id) {
        mDataChanger.deleteDownloadEntry(id);
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
        return mDataChanger.queryById(id);
    }

    /**
     * 从数据库中查询所有下载任务
     */
    public ArrayList<DownloadEntry> queryAll() {
        return mDataChanger.queryAll();
    }


    public DownloadConfig getConfigs() {
        return mDownloadConfig;
    }
}
