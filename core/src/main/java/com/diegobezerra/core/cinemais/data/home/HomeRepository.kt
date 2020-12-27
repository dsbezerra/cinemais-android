package com.diegobezerra.core.cinemais.data.home

import com.diegobezerra.core.cinemais.data.home.remote.HomeRemoteDataSource
import com.diegobezerra.core.cinemais.domain.model.Home
import com.diegobezerra.shared.result.Result
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val remoteDataSource: HomeRemoteDataSource
) {


    private var cachedHome: Home? = null

    /**
     * Retrieves home page data.
     */
    suspend fun getHome(): Result<Home> {
        return if (cachedHome != null) {
            Result.Success(cachedHome!!)
        } else {
            getRemoteAndCacheHome()
        }
    }

    private suspend fun getRemoteAndCacheHome(): Result<Home> {
        return remoteDataSource.getHome().also {
            if (it is Result.Success) {
                cachedHome = it.data
            }
        }
    }
}