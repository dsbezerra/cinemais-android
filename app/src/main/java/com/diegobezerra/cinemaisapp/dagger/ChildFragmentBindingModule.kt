package com.diegobezerra.cinemaisapp.dagger

import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleDayFragment
import com.diegobezerra.core.dagger.scopes.ChildFragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChildFragmentBindingModule {

    @ChildFragmentScope
    @ContributesAndroidInjector
    internal abstract fun scheduleDayFragment(): ScheduleDayFragment

}