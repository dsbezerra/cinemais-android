package com.diegobezerra.cinemaisapp.util

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager

class NetworkUtils {
    companion object {

        fun isWifiConnection(context: Context): Boolean {
            val wifiManager =
                context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled && wifiManager.connectionInfo != null &&
                wifiManager.connectionInfo.ipAddress != 0
        }
    }
}