package com.diegobezerra.cinemaisapp.util

import androidx.lifecycle.MutableLiveData

fun <T : Any?> MutableLiveData<T>.setValueIfNew(newValue: T?) {
    if (value != newValue) value = newValue
}

fun <T : Any?> MutableLiveData<T>.postValueIfNew(newValue: T?) {
    if (value != newValue) postValue(newValue)
}