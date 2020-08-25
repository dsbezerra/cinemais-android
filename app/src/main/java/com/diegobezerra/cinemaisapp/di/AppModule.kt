package com.diegobezerra.cinemaisapp.di

import android.content.Context
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesPreferencesHelper(@ApplicationContext context: Context): PreferencesHelper =
        PreferencesHelper(context)

}