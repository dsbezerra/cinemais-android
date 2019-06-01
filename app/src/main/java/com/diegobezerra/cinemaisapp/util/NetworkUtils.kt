package com.diegobezerra.cinemaisapp.util

import android.app.Activity
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager

class NetworkUtils {
    companion object {

        fun isWifiConnection(activity: Activity): Boolean {
            val wifiManager =
                activity.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled && wifiManager.connectionInfo != null &&
                wifiManager.connectionInfo.ipAddress != 0
        }
    }
}