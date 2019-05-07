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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;


/**
 * @author xwdz(xwdz9989@gmail.com)
 */
class DataChanger extends Observable {

    private static class HolderClass {
        private static final DataChanger INSTANCE = new DataChanger();
    }

    public static DataChanger getImpl() {
        return HolderClass.INSTANCE;
    }


    private Context mContext;
    private LinkedHashMap<String, DownloadEntry> mOperatedEntries;


    private DataChanger() {
        mOperatedEntries = new LinkedHashMap<>();
    }

    public void initContext(Context context) {
        this.mContext = context;
    }


    public void postNotifyStatus(DownloadEntry downloadEntry) {
        checkContext(mContext);

        mOperatedEntries.put(downloadEntry.id, downloadEntry);

        DownloadDBManager.getImpl().newOrUpdate(downloadEntry);

        setChanged();
        notifyObservers(downloadEntry);
    }

    public ArrayList<DownloadEntry> queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> mRecoverableEntries = null;
        for (Map.Entry<String, DownloadEntry> entry : mOperatedEntries.entrySet()) {
            if (entry.getValue().status == DownloadEntry.DownloadStatus.PAUSED) {
                if (mRecoverableEntries == null) {
                    mRecoverableEntries = new ArrayList<>();
                }
                mRecoverableEntries.add(entry.getValue());
            }
        }
        return mRecoverableEntries;
    }

    public DownloadEntry queryDownloadEntryById(String id) {
        return mOperatedEntries.get(id);
    }


    public void addToOperatedEntryMap(String key, DownloadEntry value) {
        mOperatedEntries.put(key, value);
    }

    public DownloadEntry queryDownloadEntryForQueue(String id) {
        return mOperatedEntries.get(id);
    }

    public boolean containsDownloadEntry(String id) {
        return mOperatedEntries.containsValue(id);
    }

    public void deleteDownloadEntry(String id) {
        checkContext(mContext);

        mOperatedEntries.remove(id);
        DownloadDBManager.getImpl().deleteById(id);
    }


    private static void checkContext(Context context) {
        if (context == null) {
            throw new NullPointerException("not initDownloadFile,please call intContext()");
        }
    }
}
