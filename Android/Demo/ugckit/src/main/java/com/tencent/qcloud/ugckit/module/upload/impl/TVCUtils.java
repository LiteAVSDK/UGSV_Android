package com.tencent.qcloud.ugckit.module.upload.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tencent.liteav.basic.util.TXCTimeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.UUID;

public class TVCUtils {
    private static final String TAG = "TVCUtils";
    private static String g_simulate_idfa = "";

    private static String byteArrayToHexString(byte[] data) {
        char[] out = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String string2Md5(String value)
    {
        String MD5 = "";

        if (null == value) return MD5;

        try {
            MessageDigest mD = MessageDigest.getInstance("MD5");
            MD5 = byteArrayToHexString( mD.digest(value.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (MD5 == null) MD5 = "";

        return MD5;
    }

    //IMEI：
    public static String doRead(Context context) {
        String imei = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null)  imei = tm.getDeviceId();
            if (imei == null) imei = "";
        } catch (Exception e) {
        }
        return string2Md5(imei);
    }

    //Android_ID
    public static String getOrigAndroidID(Context context) {

        String aid = "";
        try {
            aid = Settings.Secure.getString(context.getContentResolver(), "android_id");
        } catch (Throwable e) {

        }

        return string2Md5(aid);
    }

    //MAC
    public static String getOrigMacAddr(Context context) {
        String macAddress = "";
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wInfo = wm != null ? wm.getConnectionInfo() : null;
            macAddress = wInfo != null ? wInfo.getMacAddress() : null;
            if (macAddress != null) {
                macAddress = string2Md5(macAddress.replaceAll(":", "").toUpperCase());
            }
        } catch (Exception e) {
        }
        if (macAddress == null) {
            macAddress = "";
        }
        return macAddress;
    }

    // SimulateIDFA
    public  static String getSimulateIDFA(Context context) {
        if (g_simulate_idfa != null && g_simulate_idfa.length() > 0) {
            return g_simulate_idfa;
        }

        String idfa = null;
        String idfaInSP = null;
        String idfaInFile = null;

        File sdcardDir = context.getExternalFilesDir(null);
        if (sdcardDir == null) {
            return g_simulate_idfa;
        }
        //读SP
        SharedPreferences sp = context.getSharedPreferences("com.tencent.ugcpublish.dev_uuid", Context.MODE_PRIVATE);
        idfaInSP = sp.getString("key_user_id", "");

        try {
            //读文件
            String userIdFilePath = sdcardDir.getAbsolutePath() + "/txrtmp/spuid";
            File userIdFile = new File(userIdFilePath);
            if (userIdFile.exists()) {
                FileInputStream fin = new FileInputStream(userIdFile);
                int length = fin.available();
                if (length > 0) {
                    byte[] buffer = new byte[length];
                    fin.read(buffer);
                    idfaInFile = new String(buffer, "UTF-8");
                }
                fin.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "read UUID from file failed! reason: " + e.getMessage());
        }

        if (idfaInSP != null && idfaInSP.length() > 0) {
            idfa = idfaInSP;
        } else if (idfaInFile != null && idfaInFile.length() > 0) {
            idfa = idfaInFile;
        }

        if (idfa == null || idfa.length() == 0) {
            //UUID：16进制字符串(UTC毫秒时间(6字节) + 以开机到现在的时间戳为种子的随机数(4字节) + MD5(应用包名 + 系统生成UUID)(16字节)
            idfa = "";
            long utcTimeMS = System.currentTimeMillis();
            long tickTimeMS = TXCTimeUtil.getTimeTick();
            String packetName = getPackageName(context);
            for (int i = 5; i >= 0; --i) {
                idfa += String.format("%02x", (byte)((utcTimeMS >> (i*8)) & 0xff));
            }
            for (int i = 3; i >= 0; --i) {
                idfa += String.format("%02x", (byte)((tickTimeMS >> (i*8)) & 0xff));
            }
            idfa += string2Md5(packetName + UUID.randomUUID().toString());
        }

        g_simulate_idfa = idfa;
        Log.i(TAG, "UUID:" + g_simulate_idfa);
        if (idfaInFile == null || !idfaInFile.equals(idfa)) {
            try {
                //存文件
                String userIdDirPath = sdcardDir.getAbsolutePath() + "/txrtmp";
                File userIdDir = new File(userIdDirPath);
                if (!userIdDir.exists()) userIdDir.mkdir();
                String userIdFilePath = sdcardDir.getAbsolutePath() + "/txrtmp/spuid";
                File userIdFile = new File(userIdFilePath);
                if (!userIdFile.exists()) userIdFile.createNewFile();
                FileOutputStream fout = new FileOutputStream(userIdFile);
                byte[] bytes = idfa.getBytes();
                fout.write(bytes);
                fout.close();
            } catch (Exception e) {
                Log.e(TAG, "write UUID to file failed! reason: " + e.getMessage());
            }
        }

        if (idfaInSP == null || !idfaInSP.equals(idfa)) {
            //存SP
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("key_user_id", idfa);
            editor.commit();
        }
        return g_simulate_idfa;
    }

    public static String getDevUUID(Context context) {
        return getSimulateIDFA(context);
    }

    /*
    * 获取网络类型
    */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 网络是否正常
     * @param context Context
     * @return true 表示网络可用
     */
    public static int getNetWorkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();

            if (type.equalsIgnoreCase("WIFI")) {
                return TVCConstants.NETTYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if(mobileInfo != null) {
                    switch (mobileInfo.getType()) {
                        case ConnectivityManager.TYPE_MOBILE:// 手机网络
                            switch (mobileInfo.getSubtype()) {
                                case TelephonyManager.NETWORK_TYPE_UMTS:
                                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                                case TelephonyManager.NETWORK_TYPE_HSDPA:
                                case TelephonyManager.NETWORK_TYPE_HSUPA:
                                case TelephonyManager.NETWORK_TYPE_HSPA:
                                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                                case TelephonyManager.NETWORK_TYPE_EHRPD:
                                case TelephonyManager.NETWORK_TYPE_HSPAP:
                                    return TVCConstants.NETTYPE_3G;
                                case TelephonyManager.NETWORK_TYPE_CDMA:
                                case TelephonyManager.NETWORK_TYPE_GPRS:
                                case TelephonyManager.NETWORK_TYPE_EDGE:
                                case TelephonyManager.NETWORK_TYPE_1xRTT:
                                case TelephonyManager.NETWORK_TYPE_IDEN:
                                    return TVCConstants.NETTYPE_2G;
                                case TelephonyManager.NETWORK_TYPE_LTE:
                                    return TVCConstants.NETTYPE_4G;
                                default:
                                    return TVCConstants.NETTYPE_NONE;
                            }
                    }
                }
            }
        }

        return TVCConstants.NETTYPE_NONE;
    }

    /**
     * 获取 应用包名
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        String packagename = "";
        if (context != null) {
            try {
                PackageInfo info;
                info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                // 当前版本的包名
                packagename = info.packageName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return packagename;
    }

    /**
     * 获取 应用名
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appname = "";
        if (context != null) {
            try {
                PackageManager packageManager;
                ApplicationInfo info;
                packageManager =  context.getPackageManager();
                info = packageManager.getApplicationInfo(context.getPackageName(), 0);

                // 当前版本的包名
                appname = (String)packageManager.getApplicationLabel(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return appname;
    }
}
