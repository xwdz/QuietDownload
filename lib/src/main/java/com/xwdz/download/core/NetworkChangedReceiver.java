package com.xwdz.download.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xwdz.download.utils.Logger;
import com.xwdz.download.utils.NetworkUtils;

/**
 * @author xingwei.huang (xwdz9989@gmail.com)
 * @since v1.0.0
 */
public class NetworkChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int netWorkStates = NetworkUtils.getNetWorkStates(context);

        switch (netWorkStates) {
            case NetworkUtils.NET_NONE:
                //断网了
                break;
            case NetworkUtils.NET_MOBILE:
                //打开了移动网络
                Logger.d("close wifi");
                break;
            case NetworkUtils.NET_WIFI:
                //打开了WIFI
                Logger.d("open wifi");
                break;

            default:
                break;
        }
    }
}
