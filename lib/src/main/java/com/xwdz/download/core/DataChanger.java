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
import java.util.List;
import java.util.Map;
import java.util.Observable;


/**
 * @author xwdz (xwdz9989 @gmail.com)
 */
class DataChanger extends Observable {


    private static class HolderClass {
        private static final DataChanger INSTANCE = new DataChanger();
    }

    static DataChanger getImpl() {
        return HolderClass.INSTANCE;
    }

    private final Object LOCK = new Object();

    private LinkedHashMap<String, DownloadEntry> mOperatedEntries;


    private DataChanger() {
        mOperatedEntries = new LinkedHashMap<>();
    }



    void postNotifyStatus(DownloadEntry downloadEntry) {

        synchronized (LOCK) {
            mOperatedEntries.put(downloadEntry.id, downloadEntry);
        }

        DownloadDBManager.getImpl().newOrUpdate(downloadEntry);

        setChanged();
        notifyObservers(downloadEntry);
    }

    ArrayList<DownloadEntry> queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> recoverableEntries = null;
        for (Map.Entry<String, DownloadEntry> entry : mOperatedEntries.entrySet()) {
            if (entry.getValue().status == DownloadEntry.Status.PAUSED) {
                if (recoverableEntries == null) {
                    recoverableEntries = new ArrayList<>();
                }
                recoverableEntries.add(entry.getValue());
            }
        }
        return recoverableEntries;
    }

    DownloadEntry queryById(String id) {
        final DownloadEntry downloadEntry = mOperatedEntries.get(id);
        if (downloadEntry == null) {
            return DownloadDBManager.getImpl().queryById(id);
        } else {
            return downloadEntry;
        }
    }


    void addToOperatedEntryMap(String key, DownloadEntry value) {
        synchronized (LOCK) {
            mOperatedEntries.put(key, value);
        }
    }

    boolean contains(String id) {
        return mOperatedEntries.containsValue(id);
    }

    void deleteById(String id) {

        synchronized (LOCK) {
            mOperatedEntries.remove(id);
        }

        DownloadDBManager.getImpl().deleteById(id);
    }

    List<DownloadEntry> queryAll() {
        if (mOperatedEntries == null || mOperatedEntries.isEmpty()) {
            return DownloadDBManager.getImpl().queryAll();
        }
        return new ArrayList<>(mOperatedEntries.values());

    }

    void newOrUpdate(DownloadEntry downloadEntry) {
        DownloadDBManager.getImpl().newOrUpdate(downloadEntry);
    }


    private static void checkContext(Context context) {
        if (context == null) {
            throw new NullPointerException("not initDownloadFile,please call intContext()");
        }
    }
}
