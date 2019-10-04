package com.diegobezerra.core.result

sealed class Result<out T : Any> {

    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}

suspend fun <T : Any, K : Any> getRemoteAndCache(
    call: suspend () -> Result<T>,
    cacheMap: HashMap<K, T>,
    entryKey: K
): Result<T> {
    val result = call()
    if (result is Result.Success) {
        cacheMap[entryKey] = result.data
    }
    return result
}