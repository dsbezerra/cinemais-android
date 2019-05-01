package com.diegobezerra.core.cinemais.data.home.remote

import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.home.HomeDataSource
import com.diegobezerra.core.cinemais.domain.model.HomeData
import io.reactivex.Single
import javax.inject.Inject

class HomeRemoteDataSource @Inject constructor(
    private val service: CinemaisService
) : HomeDataSource {

    override fun getHome(): Single<HomeData> {
        return service.home()
    }
}