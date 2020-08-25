package com.diegobezerra.core.cinemais.data.home

import com.diegobezerra.core.cinemais.domain.model.Home
import com.diegobezerra.shared.result.Result

interface HomeDataSource {

    suspend fun getHome(): Result<Home>

}