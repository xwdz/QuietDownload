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

/**
 * @author xwdz(xwdz9989@gmail.com)
 */
public class Constants {


    public static final String KEY_DOWNLOAD_ENTRY = "key_download_entry";
    public static final String KEY_APP_ENTRY = "key_app_entry";

    public static final String KEY_DOWNLOAD_ACTION = "key_download_action";
    public static final int KEY_DOWNLOAD_ACTION_ADD = 1;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE = 2;
    public static final int KEY_DOWNLOAD_ACTION_RESUME = 3;
    public static final int KEY_DOWNLOAD_ACTION_CANCEL = 4;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE_ALL = 5;

    public static final int KEY_DOWNLOAD_ACTION_RECOVER_ALL = 6;
    public static final int CONNECT_TIME = 10 * 1000;
    public static final int READ_TIME = 10 * 1000;
}
