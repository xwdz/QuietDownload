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
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xwdz.download.core.QuietDownloader;
import com.xwdz.download.db.DownloadEntry;
import com.xwdz.download.notify.DataUpdatedWatcher;

import java.util.ArrayList;

/**
 * @author 黄兴伟 (xwd9989@gamil.com)
 * @since 2018/9/4
 */
public class ListActivity extends AppCompatActivity {

    private QuietDownloader mQuietDownloader;
    private ArrayList<DownloadEntry> mDownloadEntries = new ArrayList<>();
    private DataUpdatedWatcher watcher = new DataUpdatedWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            int index = mDownloadEntries.indexOf(data);
            if (index != -1) {
                mDownloadEntries.remove(index);
                mDownloadEntries.add(index, data);
                adapter.notifyDataSetChanged();
            }
        }
    };
    private ListView mDownloadLsv;
    private DownloadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuietDownloader = QuietDownloader.getImpl();
        setContentView(R.layout.activity_list);
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150723/de6fd89a346e304f66535b6d97907563/com.sina.weibo_2057.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150706/f67f98084d6c788a0f4593f588ea9dfc/com.taobao.taobao_121.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150720/789cd3f2facef6b27004d9f813599463/com.mfw.roadbook_147.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150810/10805820b9fbe1eeda52be289c682651/com.qihoo.vpnmaster_3019020.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150730/580642ffcae5fe8ca311c53bad35bcf2/com.taobao.trip_3001032.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150807/42ac3ad85a189125701e69ccff36ad7a/com.eg.android.AlipayGphone_78.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150813/9e775b5afb66feb960941cd8879af0b8/com.sankuai.meituan_291.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150706/5a9bec48b764a892df801424278a4285/com.mt.mtxx.mtxx_434.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150707/2ef5e16e0b8b3135aa714ad9b56b9a3d/com.happyelements.AndroidAnimal_25.apk"));
        mDownloadEntries.add(new DownloadEntry("http://shouji.360tpcdn.com/150716/aea8ca0e6617b0989d3dcce0bb9877d5/com.cmge.xianjian.a360_30.apk"));
        DownloadEntry entry = null;
        DownloadEntry realEntry = null;
        for (int i = 0; i < mDownloadEntries.size(); i++) {
            entry = mDownloadEntries.get(i);
            realEntry = mQuietDownloader.queryDownloadEntry(entry.id);
            if (realEntry != null) {
                mDownloadEntries.remove(i);
                mDownloadEntries.add(i, realEntry);
            }
        }
        mDownloadLsv = (ListView) findViewById(R.id.mDownloadLsv);
        adapter = new DownloadAdapter();
        mDownloadLsv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mQuietDownloader.addObserver(watcher);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mQuietDownloader.removeObserver(watcher);
    }

    class DownloadAdapter extends BaseAdapter {

        private ViewHolder holder;

        @Override
        public int getCount() {
            return mDownloadEntries != null ? mDownloadEntries.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mDownloadEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getTag() == null) {
                convertView = LayoutInflater.from(ListActivity.this).inflate(R.layout.activity_list_item, parent,false);
                holder = new ViewHolder();
                holder.mDownloadBtn = (Button) convertView.findViewById(R.id.downloadBtn);
                holder.mDownloadLabel = (TextView) convertView.findViewById(R.id.downloadLabel);
                holder.mPause = convertView.findViewById(R.id.pause);
                holder.mProgressBar = convertView.findViewById(R.id.progressBar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final DownloadEntry entry = mDownloadEntries.get(position);
            float length = entry.currentLength * 1.0f / entry.totalLength;
            int percent = (int) (length * 100);
            holder.mProgressBar.setProgress(percent);

            holder.mDownloadLabel.setText(entry.name + " is " + entry.status + " "
                    + Formatter.formatShortFileSize(getApplicationContext(), entry.currentLength)
                    + "/" + Formatter.formatShortFileSize(getApplicationContext(), entry.totalLength));
            holder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (entry.status == DownloadEntry.DownloadStatus.IDLE
                            || entry.status == DownloadEntry.DownloadStatus.CANCELLED
                            || entry.status == DownloadEntry.DownloadStatus.PAUSED) {
                        mQuietDownloader.download(entry);
                    }
                }
            });

            holder.mPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (entry.status == DownloadEntry.DownloadStatus.DOWNLOADING) {
                        mQuietDownloader.pause(entry);
                    }
                }
            });

            holder.mDownloadLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppEntry appEntry = new AppEntry();
                    appEntry.name = entry.name;
                    appEntry.url = entry.url;
                    AppDetailActivity.start(ListActivity.this, appEntry);
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        TextView mDownloadLabel;
        Button mDownloadBtn;
        Button mPause;
        ProgressBar mProgressBar;

    }
}
