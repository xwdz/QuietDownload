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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xwdz.download.core.QuietDownloader;
import com.xwdz.download.core.DownloadEntry;
import com.xwdz.download.notify.DataUpdatedWatcher;

public class AppDetailActivity extends AppCompatActivity {

    private QuietDownloader mDownloadManager;
    private DownloadEntry entry;
    private AppEntry mAppEntry;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private DataUpdatedWatcher watcher = new DataUpdatedWatcher() {

        @Override
        public void notifyUpdate(DownloadEntry data) {
            if (data.id.equals(entry.id)) {
                entry = data;
                initializeData();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        mTextView = findViewById(R.id.text);
        mProgressBar = findViewById(R.id.progressBar);

        mAppEntry = (AppEntry) getIntent().getSerializableExtra("url");
        mDownloadManager = QuietDownloader.getImpl();
        entry = mDownloadManager.queryById(mAppEntry.url) == null ? mDownloadManager.queryById(mAppEntry.url) : mAppEntry.generateDownloadEntry();

        initializeData();
    }

    private void initializeData() {
        mTextView.setText(entry.name + " " + entry.status + "\n"
                + Formatter.formatShortFileSize(getApplicationContext(), entry.currentLength)
                + "/" + Formatter.formatShortFileSize(getApplicationContext(), entry.totalLength));
        float length = entry.currentLength * 1.0f / entry.totalLength;
        int percent = (int) (length * 100);
        mProgressBar.setProgress(percent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDownloadManager.addObserver(watcher);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDownloadManager.removeObserver(watcher);
    }


    public static void start(Context context, AppEntry appEntry) {
        Intent starter = new Intent(context, AppDetailActivity.class);
        starter.putExtra("url", appEntry);
        context.startActivity(starter);
    }
}
