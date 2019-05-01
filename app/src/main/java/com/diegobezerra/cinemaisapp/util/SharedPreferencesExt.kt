package com.diegobezerra.cinemaisapp.util

import android.content.SharedPreferences
import androidx.core.content.edit

fun SharedPreferences.update(action: SharedPreferences.Editor.() -> Unit
) {
    edit {
        action()
        apply()
    }
}