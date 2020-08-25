package com.diegobezerra.cinemaisapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CinemaisApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        NotificationHelper.createChannels(this)

        // Apply night mode
        getSharedPreferences(PreferencesHelper.PREFS_NAME, Context.MODE_PRIVATE).apply {
            val isNightMode = getBoolean(PreferencesHelper.PREF_DARK_THEME, false)
            AppCompatDelegate.setDefaultNightMode(
                if (isNightMode)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

}