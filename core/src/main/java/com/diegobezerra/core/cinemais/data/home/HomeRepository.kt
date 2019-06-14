package com.diegobezerra.core.cinemais.data.home

import com.diegobezerra.core.cinemais.data.home.remote.HomeRemoteDataSource
import com.diegobezerra.core.cinemais.domain.model.HomeData
import io.reactivex.Single
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val remoteDataSource: HomeRemoteDataSource
) {

    private var cachedHomeData: HomeData? = null

    /**
     * Retrieves index page data.
     */
    fun getHome(): Single<HomeData> {
        return if (cachedHomeData != null) {
            Single.just(cachedHomeData)
        } else {
            remoteDataSource.getHome()
                .doOnSuccess { cachedHomeData = it }
        }
    }
}