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

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.ArrayMap;

import com.xwdz.download.QuietConfig;
import com.xwdz.download.db.DBController;
import com.xwdz.download.db.DownloadEntry;
import com.xwdz.download.utils.Constants;
import com.xwdz.download.utils.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author huangxingwei(xwdz9989@gmail.com)
 */
public class DownloadService extends Service {

    private static final String TAG = DownloadService.class.getSimpleName();


    public static final int NOTIFY_DOWNLOADING = 1;
    public static final int NOTIFY_UPDATING = 2;
    public static final int NOTIFY_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFY_COMPLETED = 4;
    public static final int NOTIFY_CONNECTING = 5;
    public static final int NOTIFY_ERROR = 6;
    public static final int NOTIFY_CONNECT_SUCCESSFUL = 7;

    @SuppressLint("NewApi")
    private ArrayMap<String, DownloadTaskManager> mDownloadingTasks = new ArrayMap<>();
    private ExecutorService mExecutors;
    private LinkedBlockingDeque<DownloadEntry> mWaitingQueue = new LinkedBlockingDeque<>();
    private DataChanger mDataChanger;

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

    private DBController mDBController;

    private void checkNext(DownloadEntry obj) {
        mDownloadingTasks.remove(obj.id);
        DownloadEntry newDownloadEntry = mWaitingQueue.poll();
        if (newDownloadEntry != null) {
            startDownload(newDownloadEntry);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(TAG, "downloader service Create ");
        mExecutors = Executors.newCachedThreadPool();
        mDataChanger = DataChanger.getImpl();
        mDataChanger.initContext(this);
        mDBController = DBController.getInstance(getApplicationContext());
        initDownload();
    }

    private void initDownload() {
        ArrayList<DownloadEntry> downloadEntrys = mDBController.queryAll();
        if (downloadEntrys != null) {
            for (DownloadEntry downloadEntry : downloadEntrys) {
                if (downloadEntry.status == DownloadEntry.DownloadStatus.DOWNLOADING || downloadEntry.status == DownloadEntry.DownloadStatus.WAITING) {
                    if (QuietConfig.getImpl().isRecoverDownloadWhenStart()) {
                        if (downloadEntry.isSupportRange) {
                            downloadEntry.status = DownloadEntry.DownloadStatus.PAUSED;
                        } else {
                            downloadEntry.status = DownloadEntry.DownloadStatus.IDLE;
                            downloadEntry.reset();
                        }
                        addDownload(downloadEntry);
                    } else {
                        if (downloadEntry.isSupportRange) {
                            downloadEntry.status = DownloadEntry.DownloadStatus.PAUSED;
                        } else {
                            downloadEntry.status = DownloadEntry.DownloadStatus.IDLE;
                            downloadEntry.reset();
                        }
                        mDBController.newOrUpdate(downloadEntry);
                    }
                }
                mDataChanger.addToOperatedEntryMap(downloadEntry.id, downloadEntry);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Logger.d(TAG, "onStartCommand receiver intent:" + intent);
            DownloadEntry downloadEntry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            if (downloadEntry == null) {
                Logger.e(TAG, "onStartCommand receiver downloadEntry is null");
                return START_STICKY;
            }

            if (mDataChanger.containsDownloadEntry(downloadEntry.id)) {
                downloadEntry = mDataChanger.queryDownloadEntryById(downloadEntry.id);
            }
            int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
            doAction(action, downloadEntry);
        } else {
            Logger.d(TAG, "not receiver intent");
        }
        return START_STICKY;
    }

    private void doAction(int action, DownloadEntry downloadEntry) {
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

    private void recoverAll() {
        ArrayList<DownloadEntry> mRecoverableEntries = mDataChanger.queryAllRecoverableEntries();
        if (mRecoverableEntries != null) {
            for (DownloadEntry downloadEntry : mRecoverableEntries) {
                addDownload(downloadEntry);
            }
        }
    }

    private void pauseAll() {
        while (mWaitingQueue.iterator().hasNext()) {
            DownloadEntry downloadEntry = mWaitingQueue.poll();
            downloadEntry.status = DownloadEntry.DownloadStatus.PAUSED;
            mDataChanger.postNotifyStatus(downloadEntry);
        }

        for (Map.Entry<String, DownloadTaskManager> entry : mDownloadingTasks.entrySet()) {
            entry.getValue().pause();
        }

        mDownloadingTasks.clear();
    }

    private void addDownload(DownloadEntry downloadEntry) {
        if (mDownloadingTasks.size() >= QuietConfig.getImpl().getMaxDownloadTasks()) {
            mWaitingQueue.offer(downloadEntry);
            downloadEntry.status = DownloadEntry.DownloadStatus.WAITING;
            mDataChanger.postNotifyStatus(downloadEntry);
        } else {
            startDownload(downloadEntry);
        }
    }

    private void cancelDownload(DownloadEntry downloadEntry) {
        DownloadTaskManager task = mDownloadingTasks.remove(downloadEntry.id);
        if (task != null) {
            task.cancel();
        } else {
            mWaitingQueue.remove(downloadEntry);
            downloadEntry.status = DownloadEntry.DownloadStatus.CANCELLED;
            mDataChanger.postNotifyStatus(downloadEntry);
        }
    }

    private void resumeDownload(DownloadEntry downloadEntry) {
        addDownload(downloadEntry);
    }

    private void pauseDownload(DownloadEntry downloadEntry) {
        DownloadTaskManager task = mDownloadingTasks.remove(downloadEntry.id);
        if (task != null) {
            task.pause();
        } else {
            mWaitingQueue.remove(downloadEntry);
            downloadEntry.status = DownloadEntry.DownloadStatus.PAUSED;
            mDataChanger.postNotifyStatus(downloadEntry);
        }
    }

    private void startDownload(DownloadEntry downloadEntry) {
        ArrayList<EventIntercept> eventIntercepts = QuietConfig.getImpl().getEventIntercepts();
        if (!eventIntercepts.isEmpty()) {
            for (EventIntercept eventIntercept : eventIntercepts) {
                boolean result = eventIntercept.onIntnercept(downloadEntry);
                if (result) {
                    return;
                }
            }
        }
        DownloadTaskManager task = new DownloadTaskManager(downloadEntry, mHandler, mExecutors);
        task.start();
        mDownloadingTasks.put(downloadEntry.id, task);
    }

}
