package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

/**
 * Listen to network changes and refresh the upload DNS under different network environments in time
 * 监听网络变化，及时刷新不同网络环境下的上传DNS
 */
public class TVCNetWorkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "TVC-NetWorkMonitor";

    @Override
    public void onReceive(Context context, Intent intent) {
        TVCLog.i(TAG, "TVCNetWorkStateReceiver onReceive");
        boolean networkChange = false;
        // Check if the API is less than 23,
        // because the getNetworkInfo(int networkType) method is deprecated after API 23
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // Get WIFI connection information
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            // Get mobile data connection information
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
                // Some specific models have info as null, to avoid crash and ensure DNS is effective,
                // a forced refresh is performed
                networkChange = true;
            }
            // Use the following method for network monitoring when the API is greater than 23
        } else {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // Get information about all network connections
            Network[] networks = connMgr.getAllNetworks();
            if (networks.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < networks.length; i++) {
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                    if (networkInfo != null) {
                        sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
                        if (networkInfo.isConnected()) {
                            networkChange = true;
                        }
                    } else {
                        // Some specific models have info as null, to avoid crash and ensure DNS is effective,
                        // a forced refresh is performed
                        networkChange = true;
                    }
                }
            } else {
                // Some specific models have info as null, to avoid crash and ensure DNS is effective,
                // a forced refresh is performed
                networkChange = true;
            }
        }

        if (networkChange) {
            TVCLog.i(TAG, "networkChanged");
            TXUGCPublishOptCenter.getInstance().reFresh(context.getApplicationContext(), null);
        }
    }
}
