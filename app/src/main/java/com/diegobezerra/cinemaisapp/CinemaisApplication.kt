package com.diegobezerra.cinemaisapp

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.diegobezerra.cinemaisapp.dagger.DaggerAppComponent
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import timber.log.Timber

class CinemaisApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
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