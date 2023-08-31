package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

/**
 * 监听网络变化，及时刷新不同网络环境下的上传DNS
 */
public class TVCNetWorkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "TVC-NetWorkMonitor";

    @Override
    public void onReceive(Context context, Intent intent) {
        TVCLog.i(TAG, "TVCNetWorkStateReceiver onReceive");
        boolean networkChange = false;
        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取ConnectivityManager对象对应的NetworkInfo对象
            //获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo != null && dataNetworkInfo != null) {
                if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                    networkChange = true;
                } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                    networkChange = true;
                } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                    networkChange = true;
                }
            } else {
                networkChange = true; // 某些特定机型存在 info 为 null的情况，避免 crash，且保证 DNS 有效，进行一次强制刷新。
            }
            //API大于23时使用下面的方式进行网络监听
        } else {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();

            if (networks != null && networks.length > 0) {
                //用于存放网络连接信息
                StringBuilder sb = new StringBuilder();
                //通过循环将网络信息逐个取出来
                for (int i = 0; i < networks.length; i++) {
                    //获取ConnectivityManager对象对应的NetworkInfo对象
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                    if (networkInfo != null) {
                        sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
                        if (networkInfo.isConnected()) {
                            networkChange = true;
                        }
                    } else {
                        networkChange = true; // 某些特定机型存在 info 为 null的情况，避免 crash，且保证 DNS 有效，进行一次强制刷新。
                    }
                }
            } else {
                networkChange = true; // 某些特定机型存在 info 为 null的情况，避免 crash，且保证 DNS 有效，进行一次强制刷新。
            }
        }

        if (networkChange) {
            TXUGCPublishOptCenter.getInstance().reFresh(context.getApplicationContext(), null);
        }
    }
}
