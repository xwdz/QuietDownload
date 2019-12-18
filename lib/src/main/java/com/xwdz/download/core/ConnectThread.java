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
import com.xwdz.download.utils.Logger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xwdz (xwdz9989@gmail.com)
 */
public class ConnectThread implements Runnable {

    private static final String TAG = ConnectThread.class.getSimpleName();

    private final    String          mUrl;
    private final    ConnectListener mListener;
    private volatile boolean         isRunning;
    private volatile boolean         isError;
    private final    AtomicInteger   mRetryCount = new AtomicInteger();
    private          DownloadConfig  mDownloadConfig;

    public ConnectThread(DownloadConfig downloadConfig, String url, ConnectListener listener) {
        this.mDownloadConfig = downloadConfig;
        this.mUrl = url;
        this.mListener = listener;
    }

    @Override
    public void run() {
        Logger.w(TAG, "isRetry: [" + (mRetryCount.get() >= 1) + "]");
        isRunning = true;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(mUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Connection", "close");
            connection.setConnectTimeout(QuietDownloader.getConfigs().getConnTimeMillis());
            connection.setReadTimeout(QuietDownloader.getConfigs().getReadTimeoutMillis());
            int     responseCode   = connection.getResponseCode();
            int     contentLength  = connection.getContentLength();
            boolean isSupportRange = false;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                }
                mListener.onConnected(isSupportRange, contentLength);
            } else {
                mListener.onConnectError("server ERROR:" + responseCode);
            }
            isRunning = false;
        } catch (Throwable e) {
            isError = true;
            isRunning = false;
            mListener.onConnectError(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (isError) {
                if (mRetryCount.getAndIncrement() < mDownloadConfig.getMaxRetryCount()) {
                    Logger.w(TAG, "RetryCount:" + mRetryCount.get());
                    try {
                        Thread.sleep(mDownloadConfig.getRetryIntervalMillis());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    run();
                }
            }

        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    interface ConnectListener {
        void onConnected(boolean isSupportRange, int totalLength);

        void onConnectError(String message);
    }
}

