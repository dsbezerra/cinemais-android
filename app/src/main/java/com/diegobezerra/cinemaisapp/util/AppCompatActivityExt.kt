package com.diegobezerra.cinemaisapp.util

import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper

fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action()
    }
}

fun AppCompatActivity.setupTheme() {
    PreferenceManager.getDefaultSharedPreferences(this).apply {
        val value = getBoolean(PreferencesHelper.PREF_DARK_THEME, false)
        if (value) {
            setTheme(R.style.Theme_Cinemais_Dark)
        } else {
            setTheme(R.style.Theme_Cinemais)
        }
    }
}