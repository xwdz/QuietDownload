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

package com.xwdz.download;

import android.os.Environment;


import com.xwdz.download.utils.FileUtils;

import java.io.File;

/**
 * @author xwdz(xwdz9989@gmail.com)
 */
public class DownloadConfig {

    public  static boolean isDebug = true;

    private static DownloadConfig mConfig;

    private int mMaxDownloadTasks = 3;
    private int mMaxDownloadThreads = 3;
    private File mDownloadDir = null;
    private int mMinOperateInterval = 1000;
    private boolean mRecoverDownloadWhenStart = false;


    //  FIXME: no implement
    private int mMaxRetryCount = 2;

    private DownloadConfig() {
        mDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public synchronized static DownloadConfig getConfig() {
        if (mConfig == null) {
            mConfig = new DownloadConfig();
        }
        return mConfig;
    }

    public int getMaxDownloadTasks() {
        return mMaxDownloadTasks;
    }

    public DownloadConfig setMaxDownloadTasks(int maxDownloadTasks) {
        this.mMaxDownloadTasks = maxDownloadTasks;
        return this;
    }

    public int getMaxDownloadThreads() {
        return mMaxDownloadThreads;
    }

    public DownloadConfig setMaxDownloadThreads(int maxDownloadThreads) {
        this.mMaxDownloadThreads = maxDownloadThreads;
        return this;
    }

    public File getDownloadDir() {
        return mDownloadDir;
    }

    public DownloadConfig setDownloadDir(File downloadDir) {
        this.mDownloadDir = downloadDir;
        return this;
    }

    public DownloadConfig setDebug(boolean debug){
        isDebug = debug;
        return this;
    }

    public int getMinOperateInterval() {
        return mMinOperateInterval;
    }

    public DownloadConfig setMinOperateInterval(int minOperateInterval) {
        this.mMinOperateInterval = minOperateInterval;
        return this;
    }

    public boolean isRecoverDownloadWhenStart() {
        return mRecoverDownloadWhenStart;
    }

    public DownloadConfig setRecoverDownloadWhenStart(boolean recoverDownloadWhenStart) {
        this.mRecoverDownloadWhenStart = recoverDownloadWhenStart;
        return this;
    }

    public int getMaxRetryCount() {
        return mMaxRetryCount;
    }

    public DownloadConfig setMaxRetryCount(int maxRetryCount) {
        this.mMaxRetryCount = maxRetryCount;
        return this;
    }

    public File getDownloadFile(String url) {
        return new File(mDownloadDir, FileUtils.getMd5FileName(url));
    }
}
