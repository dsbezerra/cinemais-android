package com.diegobezerra.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.diegobezerra.shared.result.Result

fun isNetworkConnected(context: Context): Boolean? {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    if (activeNetwork != null && activeNetwork.isConnected)
        return true
    return false
}

suspend fun <T : Any> safeRequest(call: suspend () -> T): Result<T> {
    return try {
        Result.Success(call())
    } catch (e: Exception) {
        Result.Error(e)
    }
}