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

import com.xwdz.download.DownloadConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author 黄兴伟 (xwdz9989@gamil.com)
 */
class DownloadThread implements Runnable {

    private static final String TAG = DownloadThread.class.getSimpleName();

    private static final int BUFF_SIZE = 1024 * 8;

    private final String mUrl;
    private final int mStartPos;
    private final int mEndPos;
    private final File mDestFile;
    private final DownloadListener mListener;
    private final int mThreadIndex;
    private final boolean isSingleDownload;
    private DownloadEntry.Status mStatus;

    private volatile boolean isPaused;
    private volatile boolean isCancelled;
    private volatile boolean isError;
    private volatile boolean isCompleted;

    private DownloadConfig mDownloadConfig;


    DownloadThread(String url, File destFile, int threadIndex, int startPos, int endPos, DownloadListener listener) {
        this.mUrl = url;
        this.mThreadIndex = threadIndex;
        this.mStartPos = startPos;
        this.mEndPos = endPos;
        this.mDestFile = destFile;
        this.mListener = listener;
        isSingleDownload = startPos == 0 && endPos == 0;
        mDownloadConfig = QuietDownloader.get().getConfigs();
    }

    @Override
    public void run() {
        //todo not impl retry
        realRun();
    }


    private void realRun() {
        mStatus = DownloadEntry.Status.DOWNLOADING;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(mUrl).openConnection();
            connection.setRequestMethod("GET");
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=" + mStartPos + "-" + mEndPos);
            }
            connection.setConnectTimeout(mDownloadConfig.getConnTimeMillis());
            connection.setReadTimeout(mDownloadConfig.getReadTimeoutMillis());
            connection.setRequestProperty("Connection", "close");


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
                    fos.flush();

                } finally {
                    if (fos != null) {
                        fos.close();
                    }

                    if (is != null) {
                        is.close();
                    }
                }
            } else {
                mStatus = DownloadEntry.Status.ERROR;
                mListener.onDownloadError(mThreadIndex, "server ERROR:" + responseCode);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (isPaused) {
                mStatus = DownloadEntry.Status.PAUSED;
                mListener.onDownloadPaused(mThreadIndex);
            } else if (isCancelled) {
                mStatus = DownloadEntry.Status.CANCELLED;
                mListener.onDownloadCancelled(mThreadIndex);
            } else {
                mStatus = DownloadEntry.Status.ERROR;
                mListener.onDownloadError(mThreadIndex, e.getMessage());
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            if (isPaused) {
                mStatus = DownloadEntry.Status.PAUSED;
                mListener.onDownloadPaused(mThreadIndex);
            } else if (isCancelled) {
                mStatus = DownloadEntry.Status.CANCELLED;
                mListener.onDownloadCancelled(mThreadIndex);
            } else if (isError) {
                mStatus = DownloadEntry.Status.ERROR;
                mListener.onDownloadError(mThreadIndex, "error");
            } else {
                mStatus = DownloadEntry.Status.COMPLETED;
                mListener.onDownloadCompleted(mThreadIndex);
            }
        }
    }



    public boolean isRunning() {
        return mStatus == DownloadEntry.Status.DOWNLOADING;
    }

    public void callPause() {
        isPaused = true;
    }

    public void callCancel() {
        isCancelled = true;
    }

    public void callCancelByError() {
        isError = true;
    }

    public void callCompleted() {
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
