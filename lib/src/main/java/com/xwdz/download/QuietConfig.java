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

import com.xwdz.download.core.EventIntercept;

import java.io.File;
import java.util.ArrayList;

/**
 * @author xwdz(xwdz9989@gmail.com)
 */
public class QuietConfig {

    public boolean isDebug = true;

    private int mMaxDownloadTasks = 3;
    private int mMaxDownloadThreads = 3;
    private File mDownloadDir = null;
    private int mMinOperateInterval = 1000;
    private boolean mRecoverDownloadWhenStart = true;

    private final ArrayList<EventIntercept> mEventIntercepts = new ArrayList<>();


    //  todo: no imp
    // todo 处理 jar 包重复问题
    private int mMaxRetryCount = 2;


    private QuietConfig() {
    }

    private static class HolderClass {
        private static final QuietConfig INSTANCE = new QuietConfig();
    }

    public synchronized static QuietConfig getImpl() {
        return HolderClass.INSTANCE;
    }

    public QuietConfig initDownloadFile(Context context) {
        mDownloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + context.getPackageName());
        if (!mDownloadDir.exists()) {
            mDownloadDir.mkdir();
        }
        return this;
    }

    public int getMaxDownloadTasks() {
        return mMaxDownloadTasks;
    }

    public QuietConfig setMaxDownloadTasks(int maxDownloadTasks) {
        this.mMaxDownloadTasks = maxDownloadTasks;
        return this;
    }

    public int getMaxDownloadThreads() {
        return mMaxDownloadThreads;
    }

    public QuietConfig setMaxDownloadThreads(int maxDownloadThreads) {
        this.mMaxDownloadThreads = maxDownloadThreads;
        return this;
    }

    public File getDownloadDir() {
        return mDownloadDir;
    }

    public QuietConfig setDownloadDir(File downloadDir) {
        this.mDownloadDir = downloadDir;
        return this;
    }

    public QuietConfig setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }

    public int getMinOperateInterval() {
        return mMinOperateInterval;
    }

    public QuietConfig setMinOperateInterval(int minOperateInterval) {
        this.mMinOperateInterval = minOperateInterval;
        return this;
    }

    public boolean isRecoverDownloadWhenStart() {
        return mRecoverDownloadWhenStart;
    }

    public QuietConfig setRecoverDownloadWhenStart(boolean recoverDownloadWhenStart) {
        this.mRecoverDownloadWhenStart = recoverDownloadWhenStart;
        return this;
    }

    public int getMaxRetryCount() {
        return mMaxRetryCount;
    }

    public QuietConfig setMaxRetryCount(int maxRetryCount) {
        this.mMaxRetryCount = maxRetryCount;
        return this;
    }

    public File getDownloadFile(String name) {
        return new File(mDownloadDir, name);
    }

    public void setEventIntercepts(ArrayList<EventIntercept> list) {
        mEventIntercepts.addAll(list);
    }

    public void setEventIntercepts(EventIntercept intercept) {
        mEventIntercepts.add(intercept);
    }

    public ArrayList<EventIntercept> getEventIntercepts() {
        return mEventIntercepts;
    }


    private HandlerNetworkListener mHandlerNetworkListener;

    public HandlerNetworkListener getHandlerNetworkListener() {
        return mHandlerNetworkListener;
    }

    /**
     * @see HandlerNetworkListener 处理网络情况
     */
    public QuietConfig setHandlerNetworkListener(HandlerNetworkListener handlerNetworkListenerListener) {
        this.mHandlerNetworkListener = handlerNetworkListenerListener;
        return this;
    }


    public interface HandlerNetworkListener {
        /**
         * 处理网络状况接口
         *
         * @return true:  消费该事件终止运行下载任务
         * false: 正常执行下载任务
         */
        boolean onHandlerNetworkStatus();
    }

}
