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

import com.xwdz.download.db.DownloadEntry;
import com.xwdz.download.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author xwdz(xwdz9989@gmail.com)
 */
public class DownloadThread implements Runnable {

    private static final String TAG = DownloadThread.class.getSimpleName();

    private static final int BUFF_SIZE = 4096;

    private final String mUrl;
    private final int mStartPos;
    private final int mEndPos;
    private final File mDestFile;
    private final DownloadListener mListener;
    private final int mThreadIndex;
    private final boolean isSingleDownload;
    private volatile boolean isPaused;

    private DownloadEntry.DownloadStatus mStatus;
    private volatile boolean isCancelled;
    private volatile boolean isError;
    private volatile boolean isCompleted;

    private int mRetryCountIndex = 0;

    public DownloadThread(String url, File destFile, int threadIndex, int startPos, int endPos, DownloadListener listener) {
        this.mUrl = url;
        this.mThreadIndex = threadIndex;
        this.mStartPos = startPos;
        this.mEndPos = endPos;
        this.mDestFile = destFile;
        this.mListener = listener;
        isSingleDownload = startPos == 0 && endPos == 0;
    }

    @Override
    public void run() {
        //todo not impl retry
        realRun();
//        final int retryCount = QuietConfig.getConfig().getMaxRetryCount();
//        for (int i = 0; i < retryCount; i++) {
//            if (mRetryCountIndex == retryCount && !isError) {
//                Logger.d(TAG, "current retryCount:" + mRetryCountIndex + " setRetryCount:" + retryCount);
//                break;
//            }
//
//            if (!isCompleted) {
//                boolean isError = realRun();
//                if (isError) {
//                    mRetryCountIndex++;
//                    Logger.d(TAG, "retry count(" + mRetryCountIndex + ")" + "max retryCount:" + retryCount);
//                    realRun();
//                } else {
//                    break;
//                }
//            }
//        }
    }


    private boolean realRun() {
        mStatus = DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(mUrl).openConnection();
            connection.setRequestMethod("GET");
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=" + mStartPos + "-" + mEndPos);
            }
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            RandomAccessFile raf = null;
            FileOutputStream fos = null;
            InputStream is = null;
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                try {
                    raf = new RandomAccessFile(mDestFile, "rw");
                    raf.seek(mStartPos);
                    is = connection.getInputStream();
                    byte[] buffer = new byte[BUFF_SIZE];
                    int len = -1;
                    while ((len = is.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        mListener.onProgressChanged(mThreadIndex, len);
                        if (isPaused || isCancelled || isError) {
                            break;
                        }
                    }
                    return true;
                } finally {
                    if (raf != null) {
                        raf.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                }
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    fos = new FileOutputStream(mDestFile);
                    is = connection.getInputStream();
                    byte[] buffer = new byte[BUFF_SIZE];
                    int len = -1;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        mListener.onProgressChanged(mThreadIndex, len);
                        if (isPaused || isCancelled || isError) {
                            break;
                        }
                    }
                    return true;
                } finally {
                    if (fos != null) {
                        fos.close();
                    }

                    if (is != null) {
                        is.close();
                    }
                }
            } else {
                mStatus = DownloadEntry.DownloadStatus.error;
                mListener.onDownloadError(mThreadIndex, "server error:" + responseCode);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                mListener.onDownloadPaused(mThreadIndex);
            } else if (isCancelled) {
                mStatus = DownloadEntry.DownloadStatus.cancelled;
                mListener.onDownloadCancelled(mThreadIndex);
            } else {
                mStatus = DownloadEntry.DownloadStatus.error;
                mListener.onDownloadError(mThreadIndex, e.getMessage());
            }

            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                mListener.onDownloadPaused(mThreadIndex);
            } else if (isCancelled) {
                mStatus = DownloadEntry.DownloadStatus.cancelled;
                mListener.onDownloadCancelled(mThreadIndex);
            } else if (isError) {
                mStatus = DownloadEntry.DownloadStatus.error;
                mListener.onDownloadError(mThreadIndex, "cancel manually by error");
            } else {
                mStatus = DownloadEntry.DownloadStatus.completed;
                mListener.onDownloadCompleted(mThreadIndex);
            }
        }
        return false;
    }


    public boolean isRunning() {
        return mStatus == DownloadEntry.DownloadStatus.downloading;
    }

    public void pause() {
        isPaused = true;
    }

    public void cancel() {
        isCancelled = true;
    }

    public void cancelByError() {
        isError = true;
    }

    public void completed() {
        isCompleted = true;
    }

    interface DownloadListener {
        void onProgressChanged(int index, int progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index, String message);

        void onDownloadPaused(int index);

        void onDownloadCancelled(int index);
    }
}
