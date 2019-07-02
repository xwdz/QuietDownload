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

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * @author 黄兴伟 (xwdz9989@gamil.com)
 */
public class DownloadConfig {

    public static boolean isDebug = true;

    /**
     * 最大同时下载任务数
     **/
    private int     mMaxDownloadTasks         = 3;
    /**
     * 最大下载线程数
     **/
    private int     mMaxDownloadThreads       = 3;
    /**
     * 下载文件夹
     **/
    private File    mDownloadDir              = null;
    /**
     * 事件间隔
     **/
    private int     mMinOperateInterval       = 800;
    /**
     * 自动恢复下载
     **/
    private boolean mRecoverDownloadWhenStart = true;
    /**
     * 是否自动回复下载
     */
    private boolean mAutoRecovery             = false;
    /**
     * 是否打开重试
     */
    private boolean mOpenRetry                = false;

    /**
     * 重试次数
     */
    private int  mMaxRetryCount       = 3;
    /**
     * 每次重试时间间隔
     */
    private long mRetryIntervalMillis = 2500;

    private long mMaxFileLength = 50 * 1024 * 1024;

    private Context mContext;

    public DownloadConfig(Context context) {
        mContext = context;
        mDownloadDir = getCacheDir(context, "quietDownloader");
        checkDownloadFileExists(mDownloadDir);
    }

    public int getMaxDownloadTasks() {
        return mMaxDownloadTasks;
    }


    public boolean isAutoRecovery() {
        return mAutoRecovery;
    }

    public void setAutoRecovery(boolean autoRecovery) {
        mAutoRecovery = autoRecovery;
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
        checkDownloadFileExists(mDownloadDir);
        return this;
    }


    public long getMaxFileLength() {
        return mMaxFileLength;
    }

    public void setMaxFileLength(long maxFileLength) {
        mMaxFileLength = maxFileLength;
    }

    private void checkDownloadFileExists(File file) {
        mDownloadDir = file;
        if (!mDownloadDir.exists()) {
            mDownloadDir.mkdir();
        }
    }

    public DownloadConfig setDebug(boolean debug) {
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

    public Context getAppContext() {
        return mContext.getApplicationContext();
    }

    public void setContext(Context context) {
        mContext = context;
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

    public boolean isOpenRetry() {
        return mOpenRetry;
    }

    public void setOpenRetry(boolean openRetry) {
        mOpenRetry = openRetry;
    }

    public File getDownloadFile(String name) {
        return new File(mDownloadDir, name);
    }

    private int mConnTimeMillis    = 30 * 1000;
    private int mReadTimeoutMillis = 30 * 1000;

    public int getConnTimeMillis() {
        return mConnTimeMillis;
    }

    public DownloadConfig setConnTimeMillis(int connTimeMillis) {
        mConnTimeMillis = connTimeMillis;
        return this;
    }

    public int getReadTimeoutMillis() {
        return mReadTimeoutMillis;
    }

    public DownloadConfig setReadTimeoutMillis(int readTimeoutMillis) {
        mReadTimeoutMillis = readTimeoutMillis;
        return this;
    }

    public long getRetryIntervalMillis() {
        return mRetryIntervalMillis;
    }

    public void setRetryIntervalMillis(long retryIntervalMillis) {
        mRetryIntervalMillis = retryIntervalMillis;
    }

    private File getCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
}
