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

import com.xwdz.download.DownloadConfig;
import com.xwdz.download.utils.LOG;
import com.xwdz.download.utils.TickTack;

import java.io.File;
import java.util.HashMap;

/**
 * @author 黄兴伟 (xwdz9989@gamil.com)
 */
public class DownloadTaskManager implements ConnectThread.ConnectListener, DownloadThread.DownloadListener {

    private static final String TAG = DownloadTaskManager.class.getSimpleName();

    private final DownloadEntry mDownloadEntry;
    private final Handler mHandler;
    private volatile boolean isPaused;
    private volatile boolean isCancelled;
    private ConnectThread mConnectThread;
    private DownloadThread[] mDownloadThreads;
    private DownloadEntry.DownloadStatus[] mDownloadStatus;
    private File mDestFile;
    private DownloadConfig mDownloadConfig;

    DownloadTaskManager(DownloadEntry downloadEntry, Handler handler) {
        this.mDownloadConfig = QuietDownloader.getImpl().getConfigs();
        this.mDownloadEntry = downloadEntry;
        this.mHandler = handler;
        this.mDestFile = mDownloadConfig.getDownloadFile(downloadEntry.name);

    }

    void pause() {
        LOG.w(TAG, "callPause task!");
        isPaused = true;
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    if (mDownloadEntry.isSupportRange) {
                        mDownloadThreads[i].callPause();
                    } else {
                        mDownloadThreads[i].callPause();
                    }
                }
            }
        }
    }

    void cancel() {
        LOG.e(TAG, "callCancel task!");
        isCancelled = true;
        if (mDownloadThreads != null && mDownloadThreads.length > 0) {
            for (int i = 0; i < mDownloadThreads.length; i++) {
                if (mDownloadThreads[i] != null && mDownloadThreads[i].isRunning()) {
                    mDownloadThreads[i].callCancel();
                }
            }
        }
    }

    public void start() {
        if (mDownloadEntry.totalLength > 0) {
            LOG.e(TAG, "no need to check if support range and totalLength");
            startDownload();
        } else {
            mDownloadEntry.status = (DownloadEntry.DownloadStatus.CONNECTING);
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_CONNECTING);

            mConnectThread = new ConnectThread(mDownloadEntry.url, this);
            QuietExecutors.execute(mConnectThread);
        }
    }

    private void startDownload() {
        LOG.w(TAG, "startDownload: isSupportRange-" + mDownloadEntry.isSupportRange);
        if (mDownloadEntry.isSupportRange) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void startMultiDownload() {
        LOG.w(TAG, "startMultiDownload");
        mDownloadEntry.status = (DownloadEntry.DownloadStatus.DOWNLOADING);
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_DOWNLOADING);
        int block = mDownloadEntry.totalLength / mDownloadConfig.getMaxDownloadThreads();
        int startPos = 0;
        int endPos = 0;
        if (mDownloadEntry.ranges == null) {
            mDownloadEntry.ranges = new HashMap<>();
            for (int i = 0; i < mDownloadConfig.getMaxDownloadThreads(); i++) {
                mDownloadEntry.ranges.put(i, 0);
            }
        }
        mDownloadThreads = new DownloadThread[mDownloadConfig.getMaxDownloadThreads()];
        mDownloadStatus = new DownloadEntry.DownloadStatus[mDownloadConfig.getMaxDownloadThreads()];
        for (int i = 0; i < mDownloadConfig.getMaxDownloadThreads(); i++) {
            startPos = i * block + mDownloadEntry.ranges.get(i);
            if (i == mDownloadConfig.getMaxDownloadThreads() - 1) {
                endPos = mDownloadEntry.totalLength - 1;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(mDownloadEntry.url, mDestFile, i, startPos, endPos, this);
                mDownloadStatus[i] = DownloadEntry.DownloadStatus.DOWNLOADING;
                QuietExecutors.execute(mDownloadThreads[i]);
            } else {
                mDownloadStatus[i] = DownloadEntry.DownloadStatus.COMPLETED;
            }
        }
    }

    private void startSingleDownload() {
        LOG.w(TAG, "startSingleDownload");
        mDownloadEntry.status = DownloadEntry.DownloadStatus.DOWNLOADING;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_DOWNLOADING);
        mDownloadStatus = new DownloadEntry.DownloadStatus[1];
        mDownloadStatus[0] = mDownloadEntry.status;
        mDownloadThreads = new DownloadThread[1];
        mDownloadThreads[0] = new DownloadThread(mDownloadEntry.url, mDestFile, 0, 0, 0, this);
        QuietExecutors.execute(mDownloadThreads[0]);
    }

    private void notifyUpdate(DownloadEntry downloadEntry, int what) {
        LOG.w(TAG, "notifyUpdate:" + what + ":" + downloadEntry.currentLength);
        if (mHandler.hasMessages(what)) {
            mHandler.removeMessages(what);
        }
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = downloadEntry;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        mDownloadEntry.isSupportRange = isSupportRange;
        mDownloadEntry.totalLength = totalLength;
        mDownloadEntry.status = DownloadEntry.DownloadStatus.CONNECT_SUCCESSFUL;
        notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_CONNECT_SUCCESSFUL);

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
        if (TickTack.getInstance().needToNotify()) {
            notifyUpdate(mDownloadEntry, DownloadService.NOTIFY_UPDATING);
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        mDownloadStatus[index] = DownloadEntry.DownloadStatus.COMPLETED;

        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadEntry.DownloadStatus.COMPLETED) {
                mDownloadThreads[i].callCompleted();
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
        LOG.e(TAG, "onDownloadError:" + message);
        mDownloadStatus[index] = DownloadEntry.DownloadStatus.ERROR;

        for (int j = 0; j < mDownloadStatus.length; j++) {
            if (mDownloadStatus[j] != DownloadEntry.DownloadStatus.COMPLETED && mDownloadStatus[j] != DownloadEntry.DownloadStatus.ERROR) {
                mDownloadThreads[j].callCancelByError();
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
