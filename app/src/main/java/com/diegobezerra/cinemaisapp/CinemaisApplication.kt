package com.diegobezerra.cinemaisapp

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.diegobezerra.cinemaisapp.dagger.DaggerAppComponent
import com.diegobezerra.cinemaisapp.dagger.DaggerWorkerFactory
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper.Companion
import com.google.android.gms.ads.MobileAds
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import timber.log.Timber
import javax.inject.Inject

class CinemaisApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

    @Inject
    lateinit var workerFactory: DaggerWorkerFactory

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
        MobileAds.initialize(this, BuildConfig.ADMOB_APP_ID)
        WorkerHelper.initialize(this, workerFactory)
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