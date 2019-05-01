package com.diegobezerra.cinemaisapp.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaViewModel
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasViewModel
import com.diegobezerra.cinemaisapp.ui.main.home.HomeViewModel
import com.diegobezerra.cinemaisapp.ui.main.movies.TabMoviesViewModel
import com.diegobezerra.cinemaisapp.ui.movie.MovieViewModel
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasViewModel
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleViewModel
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: CinemaisViewModelFactory):
            ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeFragmentViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CinemasViewModel::class)
    abstract fun bindCinemasFragmentViewModel(viewModel: CinemasViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScheduleViewModel::class)
    abstract fun bindScheduleDayViewModel(viewModel: ScheduleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CinemaViewModel::class)
    abstract fun bindCinemaFragmentViewModel(viewModel: CinemaViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TabMoviesViewModel::class)
    abstract fun bindUpcomingMoviesFragmentViewModel(viewModel: TabMoviesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MovieViewModel::class)
    abstract fun bindMovieFragmentViewModel(viewModel: MovieViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayingCinemasViewModel::class)
    abstract fun bindPlayingCinemasViewModel(viewModel: PlayingCinemasViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TicketsViewModel::class)
    abstract fun bindTicketsViewModel(viewModel: TicketsViewModel): ViewModel
}