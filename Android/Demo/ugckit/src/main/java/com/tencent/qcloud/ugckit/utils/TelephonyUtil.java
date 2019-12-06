package com.tencent.qcloud.ugckit.utils;

import android.app.Service;
import android.support.annotation.NonNull;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.tencent.qcloud.ugckit.UGCKitImpl;

import java.lang.ref.WeakReference;

/**
 * 电话监听管理
 */
public class TelephonyUtil {
    private TXPhoneStateListener mPhoneListener;
    private OnTelephoneListener mOnStopListener;

    @NonNull
    private static TelephonyUtil instance = new TelephonyUtil();

    private TelephonyUtil() {
    }

    @NonNull
    public static TelephonyUtil getInstance() {
        return instance;
    }

    public void initPhoneListener() {
        //设置电话监听
        if (mPhoneListener == null) {
            mPhoneListener = new TXPhoneStateListener(this);
            TelephonyManager tm = (TelephonyManager) UGCKitImpl.getAppContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public void uninitPhoneListener() {
        if (mPhoneListener != null) {
            TelephonyManager tm = (TelephonyManager) UGCKitImpl.getAppContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TelephonyUtil> mWekRef;

        public TXPhoneStateListener(TelephonyUtil context) {
            mWekRef = new WeakReference<TelephonyUtil>(context);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TelephonyUtil kit = mWekRef.get();
            if (kit == null) return;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:  //电话等待接听
                    if (mOnStopListener != null) {
                        mOnStopListener.onRinging();
                    }
                case TelephonyManager.CALL_STATE_OFFHOOK:  //电话接听
                    if (mOnStopListener != null) {
                        mOnStopListener.onOffhook();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE://电话挂机
                    if (mOnStopListener != null) {
                        mOnStopListener.onIdle();
                    }
                    break;
            }
        }
    }

    public void setOnTelephoneListener(OnTelephoneListener listener) {
        mOnStopListener = listener;
    }

    public interface OnTelephoneListener {
        void onRinging();

        void onOffhook();

        void onIdle();
    }
}
