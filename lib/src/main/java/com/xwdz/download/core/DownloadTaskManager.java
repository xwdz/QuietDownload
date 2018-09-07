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


import com.xwdz.download.QuietConfig;
import com.xwdz.download.db.DownloadEntry;
import com.xwdz.download.utils.TickTack;
import com.xwdz.download.utils.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;


/**
 * @author xwdz(xwdz9989@gmail.com)
 */
public class DownloadTaskManager implements ConnectThread.ConnectListener, DownloadThread.DownloadListener {


    private static final String TAG = DownloadTaskManager.class.getSimpleName();

    private final DownloadEntry mDownloadEntry;
    private final Handler mHandler;
    private final ExecutorService mExecutor;
    private volatile boolean isPaused;
    private volatile boolean isCancelled;
    private ConnectThread mConnectThread;
    private DownloadThread[] mDownloadThreads;
    private DownloadEntry.DownloadStatus[] mDownloadStatus;
    private File destFile;


    public DownloadTaskManager(DownloadEntry downloadEntry, Handler mHandler, ExecutorService mExecutor) {
        this.mDownloadEntry = downloadEntry;
        this.mHandler = mHandler;
        this.mExecutor = mExecutor;
        this.destFile = QuietConfig.getImpl().getDownloadFile(downloadEntry.url);
    }

    public void pause() {
        Logger.e(TAG, "download PAUSED");
        isPaused = true;
        //todo  connect Thread cancel...
//        if (mConnectThread != null && mConnectThread.isRunning()) {
//            mConnectThread.cancel();
//        }
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    if (mDownloadEntry.isSupportRange) {
                        mDownloadThreads[i].pause();
                    } else {
                        mDownloadThreads[i].cancel();
                    }
                }
            }
        }
    }

    public void cancel() {
        Logger.e(TAG, "download CANCELLED");
        isCancelled = true;
        //todo  connect Thread cancel...

        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    mDownloadThreads[i].cancel();
                }
            }
        }
    }

    public void start() {
        if (mDownloadEntry.totalLength > 0) {
            Logger.e(TAG, "no need to check if support range and totalLength");
            startDownload();
        } else {
            mDownloadEntry.status = (DownloadEntry.DownloadStatus.CONNECTING);
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_CONNECTING);
            mConnectThread = new ConnectThread(mDownloadEntry.url, this);
            mExecutor.execute(mConnectThread);
        }
    }

    private void startDownload() {
        Logger.e(TAG, "download: isSupportRange-" + mDownloadEntry.isSupportRange);
        if (mDownloadEntry.isSupportRange) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void startMultiDownload() {
        Logger.e(TAG, "startMultiDownload");
        mDownloadEntry.status = (DownloadEntry.DownloadStatus.DOWNLOADING);
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_DOWNLOADING);
        int block = mDownloadEntry.totalLength / QuietConfig.getImpl().getMaxDownloadThreads();
        int startPos = 0;
        int endPos = 0;
        if (mDownloadEntry.ranges == null) {
            mDownloadEntry.ranges = new HashMap<>();
            for (int i = 0; i < QuietConfig.getImpl().getMaxDownloadThreads(); i++) {
                mDownloadEntry.ranges.put(i, 0);
            }
        }
        mDownloadThreads = new DownloadThread[QuietConfig.getImpl().getMaxDownloadThreads()];
        mDownloadStatus = new DownloadEntry.DownloadStatus[QuietConfig.getImpl().getMaxDownloadThreads()];
        for (int i = 0; i < QuietConfig.getImpl().getMaxDownloadThreads(); i++) {
            startPos = i * block + mDownloadEntry.ranges.get(i);
            if (i == QuietConfig.getImpl().getMaxDownloadThreads() - 1) {
                endPos = mDownloadEntry.totalLength - 1;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(mDownloadEntry.url, destFile, i, startPos, endPos, this);
                mDownloadStatus[i] = DownloadEntry.DownloadStatus.DOWNLOADING;
                mExecutor.execute(mDownloadThreads[i]);
            } else {
                mDownloadStatus[i] = DownloadEntry.DownloadStatus.COMPLETED;
            }
        }
    }

    private void startSingleDownload() {
        Logger.e(TAG, "startSingleDownload");
        mDownloadEntry.status = DownloadEntry.DownloadStatus.DOWNLOADING;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_DOWNLOADING);
        mDownloadStatus = new DownloadEntry.DownloadStatus[1];
        mDownloadStatus[0] = mDownloadEntry.status;
        mDownloadThreads = new DownloadThread[1];
        mDownloadThreads[0] = new DownloadThread(mDownloadEntry.url, destFile, 0, 0, 0, this);
        mExecutor.execute(mDownloadThreads[0]);
    }

    private void notifyUpdate(DownloadEntry downloadEntry, int what) {
        Logger.e(TAG, "notifyUpdate:" + what + ":" + downloadEntry.currentLength);
        if (mHandler.hasMessages(what)) {
            mHandler.removeMessages(what);
        }
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = downloadEntry;
        mHandler.sendMessage(msg);
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        mDownloadEntry.isSupportRange = isSupportRange;
        mDownloadEntry.totalLength = totalLength;

        startDownload();
    }

    @Override
    public void onConnectError(String message) {
        if (isPaused || isCancelled) {
            mDownloadEntry.status = isPaused ? DownloadEntry.DownloadStatus.PAUSED : DownloadEntry.DownloadStatus.CANCELLED;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
        } else {
            mDownloadEntry.status = DownloadEntry.DownloadStatus.ERROR;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_ERROR);
        }
    }

    @Override
    public synchronized void onProgressChanged(int index, int progress) {
        if (mDownloadEntry.isSupportRange) {
            int range = mDownloadEntry.ranges.get(index) + progress;
            mDownloadEntry.ranges.put(index, range);
        }
        mDownloadEntry.currentLength += progress;
//        long stamp = System.currentTimeMillis();
//        if (stamp - mLastStamp > 1000) {
//            mLastStamp = stamp;
//        }
        if (TickTack.getInstance().needToNotify()) {
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_UPDATING);
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        mDownloadStatus[index] = DownloadEntry.DownloadStatus.COMPLETED;

        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadEntry.DownloadStatus.COMPLETED) {
                mDownloadThreads[i].completed();
                return;
            }
        }

        if (mDownloadEntry.totalLength > 0 && mDownloadEntry.currentLength != mDownloadEntry.totalLength) {
            mDownloadEntry.status = DownloadEntry.DownloadStatus.ERROR;
            resetDownload();
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_ERROR);
        } else {
            mDownloadEntry.status = DownloadEntry.DownloadStatus.COMPLETED;
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_COMPLETED);
        }
    }

    private void resetDownload() {
        mDownloadEntry.reset();
    }

    @Override
    public synchronized void onDownloadError(int index, String message) {
        Logger.e(TAG, "onDownloadError:" + message);
        mDownloadStatus[index] = DownloadEntry.DownloadStatus.ERROR;

//        TODO download retry operation
        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadEntry.DownloadStatus.COMPLETED && mDownloadStatus[i] != DownloadEntry.DownloadStatus.ERROR) {
                mDownloadThreads[i].cancelByError();
                return;
            }
        }

        mDownloadEntry.status = DownloadEntry.DownloadStatus.ERROR;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_ERROR);
    }

    @Override
    public synchronized void onDownloadPaused(int index) {
        mDownloadStatus[index] = DownloadEntry.DownloadStatus.PAUSED;

        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadEntry.DownloadStatus.COMPLETED && mDownloadStatus[i] != DownloadEntry.DownloadStatus.PAUSED) {
                return;
            }
        }

        mDownloadEntry.status = DownloadEntry.DownloadStatus.PAUSED;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }

    @Override
    public synchronized void onDownloadCancelled(int index) {
        mDownloadStatus[index] = DownloadEntry.DownloadStatus.CANCELLED;
        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadEntry.DownloadStatus.COMPLETED && mDownloadStatus[i] != DownloadEntry.DownloadStatus.CANCELLED) {
                return;
            }
        }
        mDownloadEntry.status = DownloadEntry.DownloadStatus.CANCELLED;
        resetDownload();
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }
}
