package com.diegobezerra.core.cinemais.data.home.remote

import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.home.HomeDataSource
import com.diegobezerra.core.cinemais.domain.model.Home
import com.diegobezerra.shared.result.Result
import com.diegobezerra.core.util.safeRequest
import javax.inject.Inject

class HomeRemoteDataSource @Inject constructor(
    private val service: CinemaisService
) : HomeDataSource {

    override suspend fun getHome(): Result<Home> = safeRequest(
        call = { service.home() }
    )

}