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

package com.example.quinn.downloadertest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xwdz.download.core.DownloadEntry;
import com.xwdz.download.core.QuietDownloader;
import com.xwdz.download.notify.DataUpdatedWatcher;
import com.xwdz.download.utils.Logger;

import java.util.ArrayList;

/**
 * @author 黄兴伟 (xwd9989@gamil.com)
 * @since 2018/9/4
 */
public class OneActivity extends AppCompatActivity {

    private ArrayList<DownloadEntry> mDownloadEntries = new ArrayList<>();
    private DataUpdatedWatcher       watcher          = new DataUpdatedWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            Logger.d("TAG", "data:" + data.toString());
            int index = mDownloadEntries.indexOf(data);
            if (index != -1) {
                mDownloadEntries.remove(index);
                mDownloadEntries.add(index, data);
            }
        }
    };


    long a = 1669615688;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mDownloadEntries.add(new DownloadEntry("http://test.static.lvyuetravel.com/cms/material/20191211/c5beec37-d0da-49b6-92f8-573a8b3ba001.png", "我是一张图片.jpg"));
        mDownloadEntries.add(new DownloadEntry("http://test.static.lvyuetravel.com/cms/material/20191211/c5beec37-d0da-49b6-92f8-573a8b3ba001.png", "img2"));
        mDownloadEntries.add(new DownloadEntry("http://test.static.lvyuetravel.com/cms/material/20191211/c5beec37-d0da-49b6-92f8-573a8b3ba001.png", "img3"));


        for (DownloadEntry downloadEntry : mDownloadEntries) {
            QuietDownloader.download(downloadEntry);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        QuietDownloader.addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        QuietDownloader.removeObserver(watcher);
    }
}
