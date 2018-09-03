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


import com.xwdz.download.utils.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author xwdz(xwdz9989@gmail.com)
 */
public class ConnectThread implements Runnable {

    private final String mUrl;
    private final ConnectListener mListener;
    private volatile boolean isRunning;

    public ConnectThread(String url, ConnectListener listener) {
        this.mUrl = url;
        this.mListener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(mUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            boolean isSupportRange = false;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String ranges = connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges)) {
                    isSupportRange = true;
                }
                mListener.onConnected(isSupportRange, contentLength);
            } else {
                mListener.onConnectError("server error:" + responseCode);
            }
            isRunning = false;
        } catch (IOException e) {
            isRunning = false;
            mListener.onConnectError(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
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

