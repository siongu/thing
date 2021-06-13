package com.common.stdlib.system;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static boolean isNetworkConnected(Context context) {
        boolean ret = true;
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                ret = false;
            } else {
                NetworkInfo networkinfo = manager.getActiveNetworkInfo();
                if (networkinfo == null || !networkinfo.isAvailable() || !networkinfo.isConnectedOrConnecting()) {
                    ret = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    public static NetworkType getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            return NetworkType.TYPE_NONE;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NetworkType.TYPE_WIFI;
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = networkInfo.getSubtype();
            /**copy from {@link TelephonyManager#getNetworkClass(int networkType)} */
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_GSM:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NetworkType.TYPE_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NetworkType.TYPE_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return NetworkType.TYPE_4G;
                default:
                    return NetworkType.TYPE_UNKNOWN;
            }
        }
        return NetworkType.TYPE_UNKNOWN;
    }

    public static String getIp() {
        try {
            for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                 networkInterfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                     inetAddresses.hasMoreElements(); ) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "get ip", e);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    public enum NetworkType {
        TYPE_NONE("NONE"),
        TYPE_UNKNOWN("UNKNOWN"),
        TYPE_WIFI("WIFI"),
        TYPE_2G("2G"),
        TYPE_3G("3G"),
        TYPE_4G("4G");

        NetworkType(String value) {
            this.value = value;
        }

        public String value;
    }

}
