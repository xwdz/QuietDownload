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

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author huangxingwei (xwdz9989@gmall.com)
 */
@DatabaseTable(tableName = "downloadentry")
public class DownloadEntry implements Serializable, Cloneable {

    @DatabaseField(id = true)
    public String id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String url;
    @DatabaseField
    public int currentLength;
    @DatabaseField
    public int totalLength;
    @DatabaseField
    public Status status = Status.IDLE;
    @DatabaseField
    public boolean isSupportRange = false;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer, Integer> ranges;
    @DatabaseField
    public String filePath;

    public DownloadEntry(){

    }

    /**
     * @param url  下载地址
     * @param name 文件名称（带后缀）
     */
    public DownloadEntry(String url, String name) {
        this.url = url;
        this.id = url;
        this.name = name;
        this.filePath = QuietDownloader.getImpl().getConfigs().getDownloadFile(name).getAbsolutePath();
    }

    public void reset() {
        currentLength = 0;
        ranges = null;
        File file = QuietDownloader.getImpl().getConfigs().getDownloadFile(url);
        if (file.exists()) {
            file.delete();
        }
    }


    public DownloadEntry newEntry(DownloadEntry entry) {
        DownloadEntry downloadEntry = new DownloadEntry(entry.url, entry.name);
        downloadEntry.status = entry.status;
        downloadEntry.currentLength = entry.currentLength;
        downloadEntry.totalLength = entry.totalLength;
        downloadEntry.isSupportRange = entry.isSupportRange;
        downloadEntry.ranges = entry.ranges;
        return downloadEntry;
    }


    public enum Status {
        //空闲
        IDLE,
        // 等待
        WAITING,
        // 连接 获取下载信息
        CONNECTING,
        // 连接成功 即获取到下载文件大小等
        CONNECT_SUCCESSFUL,
        // 开始下载
        DOWNLOADING,
        // 暂停
        PAUSED,
        // 取消
        CANCELLED,
        // 完成
        COMPLETED,
        // 错误
        ERROR
    }

    @Override
    public String toString() {
        return "DownloadEntry{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", currentLength=" + currentLength +
                ", totalLength=" + totalLength +
                ", status=" + status +
                ", isSupportRange=" + isSupportRange +
                ", filePath='" + filePath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return o.hashCode() == this.hashCode();
    }


    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + id.hashCode();
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
