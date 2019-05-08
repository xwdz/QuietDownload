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

package com.xwdz.download.utils;

import android.util.Log;

import com.xwdz.download.core.QuietDownloader;

/**
 * @author xwdz(xwdz9989 @ gmail.com)
 */
public class LOG {

    public static final String TAG = "xwdz_downloader";
    private static final boolean DEBUG = QuietDownloader.get().getConfigs().isDebug;

    public static void d(String msg) {
        if (DEBUG)
            Log.d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (DEBUG)
            Log.d(TAG, "[" + tag + "] " + msg);
    }


    public static void e(String tag, String msg) {
        if (DEBUG)
            Log.e(TAG, "[" + tag + "] " + msg);
    }

    public static void w(String tag, String msg) {
        if (DEBUG)
            Log.w(TAG, "[" + tag + "] " + msg);
    }
}
