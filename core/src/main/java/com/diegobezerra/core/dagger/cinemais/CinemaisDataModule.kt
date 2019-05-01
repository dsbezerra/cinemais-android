package com.diegobezerra.core.dagger.cinemais

import com.diegobezerra.core.cinemais.data.CinemaisConverterFactory
import com.diegobezerra.core.cinemais.data.CinemaisService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Module
class CinemaisDataModule {

    @Provides
    fun provideConverterFactory(): CinemaisConverterFactory =
        CinemaisConverterFactory()

    @Provides
    fun provideCinemaisService(
        converterFactory: CinemaisConverterFactory
    ): CinemaisService {
        // NOTE: The page sometimes takes too long to respond
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .client(client)
            .baseUrl(CinemaisService.ENDPOINT)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(CinemaisService::class.java)
    }
}