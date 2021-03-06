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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

public class NetworkUtils {

    private final static String TAG = "NetworkUtils";

    /**
     * 网络类型定义
     */
    public static final int NET_NONE    = -1;
    public static final int NET_UNKNOWN = 0;
    public static final int NET_WIFI    = 1;
    public static final int NET_2G      = 2;
    public static final int NET_3G      = 3;
    public static final int NET_4G      = 4;
    public static final int NET_MOBILE  = 5;

    /**
     * 判断WIFI是否连接
     *
     * @param context 上下文
     * @return true 连接；false 未连接
     */
    public static boolean isWIFIAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            return ((null != info) && info.isAvailable()
                    && (info.getType() == ConnectivityManager.TYPE_WIFI));
        } catch (Throwable t) {
            Logger.e(TAG, "check WIFI available failed(Throwable): " + t.getMessage());
        }

        return false;
    }

    public static final int getNetWorkStates(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo         activeNetworkInfo   = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            return NET_NONE;//没网
        }

        int type = activeNetworkInfo.getType();
        switch (type) {
            case ConnectivityManager.TYPE_MOBILE:
                return NET_MOBILE;//移动数据
            case ConnectivityManager.TYPE_WIFI:
                return NET_WIFI;//WIFI
            default:
                break;
        }
        return NET_NONE;
    }


    /**
     * 判断数据网络是否连接
     *
     * @param context 上下文
     * @return true 连接；false 未连接
     */
    public static boolean isMobileAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            return ((null != info) && info.isAvailable()
                    && (info.getType() == ConnectivityManager.TYPE_MOBILE));
        } catch (Throwable t) {
            Logger.e(TAG, "check mobile available failed(Throwable): " + t.getMessage());
        }

        return false;
    }

    /**
     * 获取网络类型
     *
     * @param context 上下文
     * @return 当前网络类型
     */
    public static int getNetworkType(Context context) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null != connMgr) {
                NetworkInfo info = connMgr.getActiveNetworkInfo();

                if (null == info) {
                    return NET_NONE;
                } else {
                    int type = info.getType();

                    if (type == ConnectivityManager.TYPE_WIFI) {
                        return NET_WIFI;
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        return typeOf(info.getSubtype());
                    } else {
                        return NET_UNKNOWN;
                    }
                }
            }
        } catch (Throwable t) {
            Logger.e(TAG, "getImpl network type failed(" + t.getClass().getSimpleName()
                    + "): " + t.getMessage());
        }

        return NET_UNKNOWN;
    }

    /**
     * 将系统网络类型转为自定义
     *
     * @param networkType 系统网络类型
     * @return 自定义网络类型
     */
    private static int typeOf(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NET_2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NET_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NET_4G;
            default:
                return NET_UNKNOWN;
        }
    }
}
