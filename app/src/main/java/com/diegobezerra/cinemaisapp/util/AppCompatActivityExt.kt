package com.diegobezerra.cinemaisapp.util

import android.content.Context
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
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
        val isNightMode = getBoolean(PreferencesHelper.PREF_DARK_THEME, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
        delegate.applyDayNight()
    }
}

fun AppCompatActivity.setFragment(@IdRes fragmentId: Int, instance: () -> Fragment) {
    supportFragmentManager
        .beginTransaction()
        .replace(fragmentId, instance.invoke())
        .commit()
}