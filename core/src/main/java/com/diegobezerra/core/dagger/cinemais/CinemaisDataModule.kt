package com.diegobezerra.core.dagger.cinemais

import android.content.Context
import com.diegobezerra.core.cinemais.data.CinemaisConverterFactory
import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.util.isNetworkConnected
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
class CinemaisDataModule {

    @Provides
    fun provideConverterFactory(): CinemaisConverterFactory =
        CinemaisConverterFactory()

    @Provides
    fun provideCinemaisService(
        context: Context,
        converterFactory: CinemaisConverterFactory
    ): CinemaisService {
        // NOTE: Each cache must have its own directory.
        // See https://github.com/square/okhttp/wiki/Recipes#response-caching for more information!
        val cacheSize = 10L * 1024 * 1024 // 10 MiB
        val maxStale = 60L * 60 * 24 * 6 // 6 days
        val maxAge = 5 // 5 secs
        val client = OkHttpClient.Builder()
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
        return Retrofit.Builder()
            .baseUrl(CinemaisService.ENDPOINT)
            .addConverterFactory(converterFactory)
            .client(client)
            .build()
            .create(CinemaisService::class.java)
    }
}