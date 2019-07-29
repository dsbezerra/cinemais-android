package com.diegobezerra.cinemaisapp.dagger

import com.diegobezerra.cinemaisapp.ui.cinema.CinemaFragment
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasFragment
import com.diegobezerra.cinemaisapp.ui.main.home.HomeFragment
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesFragment
import com.diegobezerra.cinemaisapp.ui.main.movies.TabMoviesFragment
import com.diegobezerra.cinemaisapp.ui.movie.MovieFragment
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasFragment
import com.diegobezerra.cinemaisapp.ui.schedule.filters.ScheduleFiltersFragment
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsFragment
import com.diegobezerra.core.dagger.scopes.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun homeFragment(): HomeFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun moviesFragment(): MoviesFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun cinemasFragment(): CinemasFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun cinemaFragment(): CinemaFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun scheduleFiltersFragment(): ScheduleFiltersFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun upcomingMoviesFragment(): TabMoviesFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun movieFragment(): MovieFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun playingCinemasFragment(): PlayingCinemasFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun ticketsFragment(): TicketsFragment
}