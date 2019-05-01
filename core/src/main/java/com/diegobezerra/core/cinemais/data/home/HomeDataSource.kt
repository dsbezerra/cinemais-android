package com.diegobezerra.core.cinemais.data.home

import com.diegobezerra.core.cinemais.domain.model.HomeData
import io.reactivex.Single

interface HomeDataSource {
    fun getHome(): Single<HomeData>
}