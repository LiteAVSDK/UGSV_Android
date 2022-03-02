package com.tencent.qcloud.ugckit.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NetworkUtil {
    @Nullable
    private        NetchangeReceiver mNetchangeReceiver = null;
    private        Context           mContext;
    private static NetworkUtil       sInstance;
    private        NetchangeListener mListener;

    private NetworkUtil(Context context) {
        mContext = context.getApplicationContext();
    }

    public static NetworkUtil getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NetworkUtil(context);
        }
        return sInstance;
    }

    /*
     * 获取网络类型
     */
    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 注册网络变化监听器
     */
    public void registerNetChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        if (null == mNetchangeReceiver) {
            mNetchangeReceiver = new NetchangeReceiver();
        }
        mContext.registerReceiver(mNetchangeReceiver, intentFilter);
    }


    /**
     * 取消注册网络变化监听器
     */
    public void unregisterNetChangeReceiver() {
        if (mContext != null && mNetchangeReceiver != null) {
            mContext.unregisterReceiver(mNetchangeReceiver);
        }
    }

    /**
     * 网络状态监听器，当网络断开时，提示用户
     */
    public class NetchangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    if (mListener != null) {
                        mListener.onNetworkAvailable();
                    }
                }
            }
        }
    }

    public void setNetchangeListener(NetchangeListener listener) {
        mListener = listener;
    }

    public interface NetchangeListener {
        void onNetworkAvailable();
    }
}
