package com.diegobezerra.cinemaisapp

import com.diegobezerra.cinemaisapp.dagger.DaggerAppComponent
import com.diegobezerra.cinemaisapp.dagger.DaggerWorkerFactory
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
    }

}