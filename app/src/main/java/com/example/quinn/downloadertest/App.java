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

import android.app.Application;

import com.xwdz.download.core.QuietDownloader;

/**
 * @author 黄兴伟 (xwd9989@gamil.com)
 * @since 2018/9/3
 */
public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();


        //
//        DownloadConfig downloadConfig = new DownloadConfig(this);
//        downloadConfig.setMaxDownloadTasks();
//        downloadConfig.setMaxDownloadThreads();
//        downloadConfig.setDownloadDir()
//        QuietDownloader.getImpl().setDownloadConfig(downloadConfig);

        QuietDownloader.getImpl().initializeConfig(this);

    }

}
