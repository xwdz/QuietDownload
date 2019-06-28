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

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.example.lib.R;
import com.xwdz.download.DownloadConfig;
import com.xwdz.download.utils.Constants;
import com.xwdz.download.utils.LOG;
import com.xwdz.download.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author 黄兴伟 (xwdz9989@gamil.com)
 */
public class DownloadService extends Service {

    private static final String TAG = DownloadService.class.getSimpleName();

    public static final int NOTIFY_DOWNLOADING         = 1;
    public static final int NOTIFY_UPDATING            = 2;
    public static final int NOTIFY_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFY_COMPLETED           = 4;
    public static final int NOTIFY_CONNECTING          = 5;
    public static final int NOTIFY_ERROR               = 6;
    public static final int NOTIFY_CONNECT_SUCCESSFUL  = 7;

    private ConcurrentHashMap<String, DownloadTaskManager> mDownloadingTasks = new ConcurrentHashMap<>();
    private LinkedBlockingDeque<DownloadEntry>             mWaitingQueue     = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<DownloadEntry>             mWifiQueue        = new LinkedBlockingDeque<>();
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


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LOG.d(TAG, "downloader service created. ");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Notification.Builder notificationBuilder = new Notification.Builder(this, "xwd_download");
//            notificationBuilder.setOngoing(true);
//            startForeground(101, notificationBuilder.build());
//        }

        mDataChanger = DataChanger.getImpl();
        mDownloadConfig = QuietDownloader.getImpl().getConfigs();

        initDownload();
    }

    private void initDownload() {
        ArrayList<DownloadEntry> downloadEntrys = mDataChanger.queryAll();
        if (downloadEntrys != null) {
            for (DownloadEntry downloadEntry : downloadEntrys) {

                if (downloadEntry.status == DownloadEntry.Status.PAUSED) {
                    // todo 暂停的自动赋值恢复进度
                    LOG.d(TAG, "auto recover");
//                    startDownload(downloadEntry);
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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            LOG.d(TAG, "onStartCommand receiver intent:" + intent);
            DownloadEntry downloadEntry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            if (downloadEntry == null) {
                LOG.w(TAG, "onStartCommand receiver downloadEntry is null");
                return START_STICKY;
            }

            if (mDataChanger.contains(downloadEntry.id)) {
                downloadEntry = mDataChanger.queryById(downloadEntry.id);
            }
            int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
            doAction(action, downloadEntry);
        } else {
            LOG.d(TAG, "not receiver intent");
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
            downloadEntry.status = DownloadEntry.Status.PAUSED;
            mDataChanger.postNotifyStatus(downloadEntry);
        }

        for (Map.Entry<String, DownloadTaskManager> entry : mDownloadingTasks.entrySet()) {
            entry.getValue().pause();
        }

        mDownloadingTasks.clear();
    }

    private void addDownload(DownloadEntry downloadEntry) {
        if (mDownloadingTasks.size() >= mDownloadConfig.getMaxDownloadTasks()) {
            mWaitingQueue.offer(downloadEntry);
            downloadEntry.status = DownloadEntry.Status.WAITING;
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
            downloadEntry.status = DownloadEntry.Status.CANCELLED;
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
            downloadEntry.status = DownloadEntry.Status.PAUSED;
            mDataChanger.postNotifyStatus(downloadEntry);
        }
    }

    private void startDownload(DownloadEntry downloadEntry) {
        DownloadTaskManager task = new DownloadTaskManager(downloadEntry, mHandler);
        task.start();
        mDownloadingTasks.put(downloadEntry.id, task);
    }

}
