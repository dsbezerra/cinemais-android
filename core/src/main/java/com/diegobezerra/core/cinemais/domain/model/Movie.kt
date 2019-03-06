package com.diegobezerra.core.cinemais.domain.model

import java.util.*

data class Movie(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val distributor: String?,
    val rating: Int,
    val runtime: Int,
    val releaseDate: Date?,
    val countries: List<String>,
    val genres: List<String>
) {
}