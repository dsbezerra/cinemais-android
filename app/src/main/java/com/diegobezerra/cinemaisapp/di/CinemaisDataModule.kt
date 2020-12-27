package com.diegobezerra.cinemaisapp.di

import android.content.Context
import com.diegobezerra.core.cinemais.data.CinemaisConverterFactory
import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.data.cinemas.remote.CinemasRemoteDataSource
import com.diegobezerra.core.cinemais.data.home.HomeRepository
import com.diegobezerra.core.cinemais.data.home.remote.HomeRemoteDataSource
import com.diegobezerra.core.cinemais.data.movie.MovieRepository
import com.diegobezerra.core.cinemais.data.movie.remote.MovieRemoteDataSource
import com.diegobezerra.core.util.isNetworkConnected
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object CinemaisDataModule {

    @Singleton
    @Provides
    fun providesConverterFactory(): CinemaisConverterFactory =
        CinemaisConverterFactory()

    @Singleton
    @Provides
    fun providesOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        // NOTE: Each cache must have its own directory.
        // See https://github.com/square/okhttp/wiki/Recipes#response-caching for more information!
        val cacheSize = 10L * 1024 * 1024 // 10 MiB
        val maxStale = 60L * 60 * 24 * 6 // 6 days
        val maxAge = 5 // 5 secs
        return Builder()
            .cache(Cache(context.cacheDir, cacheSize))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().run {
                    if (isNetworkConnected(context)!!) {
                        header("Cache-Control", "public, max-age=$maxAge").build()
                    } else {
                        header(
                            "Cache-Control",
                            "public, only-if-cached, max-stale=$maxStale"
                        ).build()
                    }
                }
                chain.proceed(request)
            }
            .build()
    }

    @Singleton
    @Provides
    fun providesCinemaisService(
        client: OkHttpClient,
        converterFactory: CinemaisConverterFactory
    ): CinemaisService {
        return Retrofit.Builder()
            .baseUrl(CinemaisService.ENDPOINT)
            .addConverterFactory(converterFactory)
            .client(client)
            .build()
            .create(CinemaisService::class.java)
    }

    @Singleton
    @Provides
    fun providesHomeRepository(
        remoteDataSource: HomeRemoteDataSource
    ): HomeRepository = HomeRepository(remoteDataSource)

    @Singleton
    @Provides
    fun providesCinemaRepository(
        homeRepository: HomeRepository,
        remoteDataSource: CinemasRemoteDataSource
    ): CinemaRepository = CinemaRepository(homeRepository, remoteDataSource)

    @Singleton
    @Provides
    fun providesMovieRepository(
        remoteDataSource: MovieRemoteDataSource
    ): MovieRepository = MovieRepository(remoteDataSource)
}