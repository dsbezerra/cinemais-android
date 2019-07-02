package com.diegobezerra.cinemaisapp.dagger

import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.data.cinemas.remote.CinemasRemoteDataSource
import com.diegobezerra.core.cinemais.data.home.HomeRepository
import com.diegobezerra.core.cinemais.data.home.remote.HomeRemoteDataSource
import com.diegobezerra.core.cinemais.data.movie.MovieRepository
import com.diegobezerra.core.cinemais.data.movie.remote.MovieRemoteDataSource
import com.diegobezerra.core.dagger.cinemais.CinemaisDataModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        CinemaisDataModule::class
    ]
)
class RepositoryModule {

    @Singleton
    @Provides
    fun providesHomeRepository(
        remoteDataSource: HomeRemoteDataSource
    ): HomeRepository {
        return HomeRepository(remoteDataSource)
    }

    @Singleton
    @Provides
    fun providesCinemasRepository(
        homeRepository: HomeRepository,
        remoteDataSource: CinemasRemoteDataSource
    ): CinemaRepository {
        return CinemaRepository(homeRepository, remoteDataSource)
    }

    @Singleton
    @Provides
    fun provideMoviesRepository(
        remoteDataSource: MovieRemoteDataSource
    ): MovieRepository {
        return MovieRepository(remoteDataSource)
    }
}