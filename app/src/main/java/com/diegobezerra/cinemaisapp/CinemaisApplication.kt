package com.diegobezerra.cinemaisapp

import com.diegobezerra.cinemaisapp.dagger.DaggerAppComponent
import com.google.android.gms.ads.MobileAds
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import timber.log.Timber

class CinemaisApplication : DaggerApplication() {

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
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

}