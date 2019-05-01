package com.diegobezerra.cinemaisapp.dagger

import android.content.Context
import com.diegobezerra.cinemaisapp.CinemaisApplication
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    fun provideContext(application: CinemaisApplication): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun providesPreferencesHelper(context: Context): PreferencesHelper =
        PreferencesHelper(context)
}