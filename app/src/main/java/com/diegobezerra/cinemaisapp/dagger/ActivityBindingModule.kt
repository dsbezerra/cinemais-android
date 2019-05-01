package com.diegobezerra.cinemaisapp.dagger

import com.diegobezerra.cinemaisapp.ui.main.MainActivity
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity
import com.diegobezerra.cinemaisapp.ui.upcoming.UpcomingMoviesActivity
import com.diegobezerra.core.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun mainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun movieActivity(): MovieActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun upcomingMoviesActivity(): UpcomingMoviesActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun ticketsActivity(): TicketsActivity

}