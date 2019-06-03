package com.diegobezerra.cinemaisapp.util

import android.content.Context
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper.Companion.PREFS_NAME

fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action()
    }
}

fun AppCompatActivity.setupTheme() {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).apply {
        val value = getBoolean(PreferencesHelper.PREF_DARK_THEME, false)
        if (value) {
            setTheme(R.style.Theme_Cinemais_Dark)
        } else {
            setTheme(R.style.Theme_Cinemais)
        }
    }
}