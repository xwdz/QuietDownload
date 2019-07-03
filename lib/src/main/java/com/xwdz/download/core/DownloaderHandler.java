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

import android.os.Handler;
import android.os.Message;

import com.j256.ormlite.dao.Dao;
import com.xwdz.download.DownloadConfig;
import com.xwdz.download.notify.DataUpdatedWatcher;
import com.xwdz.download.utils.Constants;
import com.xwdz.download.utils.Logger;
import com.xwdz.download.utils.NetworkUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author 黄兴伟 (xwdz9989@gamil.com)
 */
public class DownloaderHandler {

    private static final String TAG = DownloaderHandler.class.getSimpleName();

    public static final int NOTIFY_DOWNLOADING         = 1;
    public static final int NOTIFY_UPDATING            = 2;
    public static final int NOTIFY_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFY_COMPLETED           = 4;
    public static final int NOTIFY_CONNECTING          = 5;
    public static final int NOTIFY_ERROR               = 6;
    public static final int NOTIFY_CONNECT_SUCCESSFUL  = 7;

    private ConcurrentHashMap<String, DownloadTaskManager> mDownloadingTasks = new ConcurrentHashMap<>();
    private LinkedBlockingDeque<DownloadEntry>             mWaitingQueue     = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<DownloadEntry>             mWaitWifiQueue    = new LinkedBlockingDeque<>();
    private DataChanger                                    mDataChanger;
    private DownloadConfig                                 mDownloadConfig;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case NOTIFY_PAUSED_OR_CANCELLED:
                case NOTIFY_COMPLETED:
                case NOTIFY_ERROR:
                    checkNext((DownloadEntry) msg.obj);
                    break;
            }
            mDataChanger.postNotifyStatus((DownloadEntry) msg.obj);
            return true;
        }
    });

    private void checkNext(DownloadEntry obj) {
        mDownloadingTasks.remove(obj.id);
        DownloadEntry newDownloadEntry = mWaitingQueue.poll();
        if (newDownloadEntry != null) {
            startDownload(newDownloadEntry);
        }
    }

    ////////////////////////////////////////////////////

    DownloaderHandler(DownloadConfig config) {
        Logger.d(TAG, "downloader handler created. ");
        mDownloadConfig = config;
        mDataChanger = DataChanger.getImpl();
        initDownload();
    }

    private void initDownload() {
        List<DownloadEntry> downloadEntrys = mDataChanger.queryAll();
        if (downloadEntrys != null) {
            for (DownloadEntry downloadEntry : downloadEntrys) {

                if (downloadEntry.status == DownloadEntry.Status.PAUSED) {
                    if (mDownloadConfig.isAutoRecovery()) {
                        startDownload(downloadEntry);
                    }
                }

                if (downloadEntry.status == DownloadEntry.Status.DOWNLOADING
                        || downloadEntry.status == DownloadEntry.Status.WAITING) {
                    if (mDownloadConfig.isRecoverDownloadWhenStart()) {
                        if (downloadEntry.isSupportRange) {
                            downloadEntry.status = DownloadEntry.Status.PAUSED;
                        } else {
                            downloadEntry.status = DownloadEntry.Status.IDLE;
                            downloadEntry.reset();
                        }
                        addDownload(downloadEntry);
                    } else {
                        if (downloadEntry.isSupportRange) {
                            downloadEntry.status = DownloadEntry.Status.PAUSED;
                        } else {
                            downloadEntry.status = DownloadEntry.Status.IDLE;
                            downloadEntry.reset();
                        }
                        mDataChanger.newOrUpdate(downloadEntry);
                    }
                }
                mDataChanger.addToOperatedEntryMap(downloadEntry.id, downloadEntry);
            }
        }
    }


    void handler(int action, DownloadEntry downloadEntry) {
        switch (action) {
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                addDownload(downloadEntry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownload(downloadEntry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                resumeDownload(downloadEntry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownload(downloadEntry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pauseAll();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll();
                break;
        }
    }

    void recoverAll() {
        ArrayList<DownloadEntry> mRecoverableEntries = mDataChanger.queryAllRecoverableEntries();
        if (mRecoverableEntries != null) {
            for (DownloadEntry downloadEntry : mRecoverableEntries) {
                addDownload(downloadEntry);
            }
        }
    }

    void pauseAll() {
        while (mWaitingQueue.iterator().hasNext()) {
            DownloadEntry downloadEntry = mWaitingQueue.poll();
            downloadEntry.status = DownloadEntry.Status.PAUSED;
            mDataChanger.postNotifyStatus(downloadEntry);
        }

        for (Map.Entry<String, DownloadTaskManager> entry : mDownloadingTasks.entrySet()) {
            entry.getValue().pause();
        }

        mDownloadingTasks.clear();
    }

    void addDownload(DownloadEntry downloadEntry) {
        if (mDownloadingTasks.size() >= mDownloadConfig.getMaxDownloadTasks()) {
            mWaitingQueue.offer(downloadEntry);
            downloadEntry.status = DownloadEntry.Status.WAITING;
            mDataChanger.postNotifyStatus(downloadEntry);
        } else {
            startDownload(downloadEntry);
        }
    }

    void cancelDownload(DownloadEntry downloadEntry) {
        DownloadTaskManager task = mDownloadingTasks.remove(downloadEntry.id);
        if (task != null) {
            task.cancel();
        } else {
            mWaitingQueue.remove(downloadEntry);
            downloadEntry.status = DownloadEntry.Status.CANCELLED;
            mDataChanger.postNotifyStatus(downloadEntry);
        }
    }

    void resumeDownload(DownloadEntry downloadEntry) {
        addDownload(downloadEntry);
    }

    void pauseDownload(DownloadEntry downloadEntry) {
        DownloadTaskManager task = mDownloadingTasks.remove(downloadEntry.id);
        if (task != null) {
            task.pause();
        } else {
            mWaitingQueue.remove(downloadEntry);
            downloadEntry.status = DownloadEntry.Status.PAUSED;
            mDataChanger.postNotifyStatus(downloadEntry);
        }
    }

    void startDownload(DownloadEntry downloadEntry) {
        if (mDownloadConfig.isAssignNetwork()) {
            if (!NetworkUtils.isWIFIAvailable(mDownloadConfig.getAppContext())) {
                Logger.d(TAG,"current network not wifi! add to Wifi Queue");
                mWaitWifiQueue.offer(downloadEntry);
            }
        } else {
            DownloadTaskManager task = new DownloadTaskManager(downloadEntry, mHandler);
            task.start();
            mDownloadingTasks.put(downloadEntry.id, task);
        }
    }

    void deleteById(String id) {
        mDataChanger.deleteById(id);
    }

    DownloadEntry queryById(String id) {
        return mDataChanger.queryById(id);
    }

    List<DownloadEntry> queryAll() {
        return mDataChanger.queryAll();
    }

    Dao<DownloadEntry, String> getDao() throws SQLException {
        return DownloadDBManager.getImpl().getDao();
    }

    void deleteObserver(DataUpdatedWatcher watcher) {
        mDataChanger.deleteObserver(watcher);
    }

    void addObserver(DataUpdatedWatcher watcher) {
        mDataChanger.addObserver(watcher);
    }

}
