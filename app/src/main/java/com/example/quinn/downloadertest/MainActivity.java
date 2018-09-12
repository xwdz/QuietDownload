package com.example.quinn.downloadertest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import com.xwdz.download.core.QuietDownloader;
import com.xwdz.download.db.DownloadEntry;
import com.xwdz.download.notify.DataUpdatedWatcher;

public class MainActivity extends AppCompatActivity {


    private static final String URL = "http://shouji.360tpcdn.com/150723/de6fd89a346e304f66535b6d97907563/com.sina.weibo_2057.apk";
    private static final String URL2 = "https://dldir1.qq.com/weixin/android/weixin672android1340.apk";
    private static final String URL3 = "http://download.fixdown.com/soft/douyin_fixdown.apk";


    private DownloadEntry mDownloadEntry;


    private final DataUpdatedWatcher mDataUpdatedWatcher = new DataUpdatedWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            initializeData(data);
        }
    };


    private QuietDownloader mDownloader = QuietDownloader.getImpl();

    private void initializeData(DownloadEntry entry) {
        if (entry.url.equals(URL)) {
            mTextView.setText(entry.status + "\n"
                    + Formatter.formatShortFileSize(getApplicationContext(), entry.currentLength)
                    + "/" + Formatter.formatShortFileSize(getApplicationContext(), entry.totalLength));
        } else if (entry.url.equals(URL2)) {
            mTextView2.setText(entry.status + "\n"
                    + Formatter.formatShortFileSize(getApplicationContext(), entry.currentLength)
                    + "/" + Formatter.formatShortFileSize(getApplicationContext(), entry.totalLength));

        } else {
            mTextView3.setText(entry.status + "\n"
                    + Formatter.formatShortFileSize(getApplicationContext(), entry.currentLength)
                    + "/" + Formatter.formatShortFileSize(getApplicationContext(), entry.totalLength));
        }
    }

    private TextView mTextView;
    private TextView mTextView2;
    private TextView mTextView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);
        mTextView2 = findViewById(R.id.text2);
        mTextView3 = findViewById(R.id.text3);

        mDownloadEntry = new DownloadEntry(URL);

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloader.download(mDownloadEntry);
            }
        });

        mTextView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloader.download(new DownloadEntry(URL2));
            }
        });

        mTextView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloader.download(new DownloadEntry(URL3));
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mDownloader.addObserver(mDataUpdatedWatcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDownloader.removeObserver(mDataUpdatedWatcher);
    }
}
