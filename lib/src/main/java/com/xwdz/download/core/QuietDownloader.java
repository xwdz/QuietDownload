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

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.xwdz.download.DownloadConfig;
import com.xwdz.download.notify.DataUpdatedWatcher;

import java.sql.SQLException;
import java.util.List;

/**
 * @author 黄兴伟 (xwdz9989@gamil.com)
 */
public class QuietDownloader {

    private static final String TAG = QuietDownloader.class.getSimpleName();


    public static void initializeDownloader(Context context) {
        _Loader.getImpl().initialize(new DownloadConfig(context));
    }

    public static void initializeDownloader(DownloadConfig downloadConfig) {
        _Loader.getImpl().initialize(downloadConfig);
    }


    /**
     * 开始下载一个任务
     */
    public static void download(DownloadEntry downloadEntry) {
        _Loader.getImpl().download(downloadEntry);
    }

    /**
     * 暂停一个任务
     */
    public static void pause(DownloadEntry downloadEntry) {
        _Loader.getImpl().pause(downloadEntry);
    }

    /**
     * 恢复一个任务
     */
    public static void resume(DownloadEntry downloadEntry) {
        _Loader.getImpl().resume(downloadEntry);
    }

    /**
     * 取消一个任务
     */
    public static void cancel(DownloadEntry downloadEntry) {
        _Loader.getImpl().cancel(downloadEntry);
    }

    /**
     * 暂停队列所有任务
     */
    public static void pauseAll() {
        _Loader.getImpl().pauseAll();
    }

    /**
     * 恢复队列所有任务
     */
    public static void recoverAll() {
        _Loader.getImpl().recoverAll();

    }

    /**
     * 添加一个数据接收器
     *
     * @see DataUpdatedWatcher
     */
    public static void addObserver(DataUpdatedWatcher watcher) {
        _Loader.getImpl().addObserver(watcher);
    }

    /**
     * 删除一个数据接收器
     *
     * @see DataUpdatedWatcher
     */
    public static void removeObserver(DataUpdatedWatcher watcher) {
        _Loader.getImpl().removeObserver(watcher);
    }

    /**
     * @return 获取操作数据库Dao对象
     */
    public static Dao<DownloadEntry, String> getDBDao() throws SQLException {
        return _Loader.getImpl().getDBDao();
    }


    /**
     * 删除一个任务从数据库中
     */
    public static void deleteById(String id) {
        _Loader.getImpl().deleteById(id);
    }

    /**
     * 待删除的文件名称,从下载文件夹中
     *
     * @param name 文件名
     */
    public static void deleteFileByName(String name) {
        _Loader.getImpl().deleteFileByName(name);
    }


    /**
     * 查询当前队列中是否有该 DownloadEntry
     */
    public static DownloadEntry queryById(String id) {
        return _Loader.getImpl().queryById(id);
    }

    /**
     * 从数据库中查询所有下载任务
     */
    public static List<DownloadEntry> queryAll() {
        return _Loader.getImpl().queryAll();
    }


    public static DownloadConfig getConfigs() {
        return _Loader.getImpl().getConfigs();
    }
}
