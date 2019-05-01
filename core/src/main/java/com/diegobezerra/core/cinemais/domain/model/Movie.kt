package com.diegobezerra.core.cinemais.domain.model

import java.util.*

data class Movie(
    val id: Int = 0,
    val title: String = "",
    val originalTitle: String,
    val posters: Posters,
    val htmlUrl: String = "",
    val synopsis: String = "",
    val cast: List<String> = listOf(),
    val screenplay: List<String> = listOf(),
    val executiveProduction: List<String> = listOf(),
    val production: List<String> = listOf(),
    val direction: List<String> = listOf(),
    val distributor: String? = "",
    val rating: Int = 0,
    val runtime: Int = 0,
    val releaseDate: Date? = null,
    val countries: List<String> = listOf(),
    val genres: List<String> = listOf(),
    var images: Images = Images(),
    var trailer: Trailer? = null,
    val playingCinemas: List<Cinema> = listOf()
) {

    companion object {

        fun getIdFromUrl(url: String): Int {
            return "\\?cf=(\\d+)\$".toRegex().find(url)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        }

    }

    constructor(
        id: Int,
        title: String,
        htmlUrl: String,
        posters: Posters
    ) : this(
        id = id,
        title = title,
        originalTitle = "",
        posters = posters,
        htmlUrl = htmlUrl
    )

    constructor(
        id: Int,
        title: String,
        htmlUrl: String,
        posters: Posters,
        synopsis: String
    ) : this(
        id = id,
        title = title,
        originalTitle = "",
        posters = posters,
        htmlUrl = htmlUrl,
        synopsis = synopsis
    )

    constructor(
        id: Int,
        title: String,
        htmlUrl: String,
        posters: Posters,
        releaseDate: Date?
    ) : this(
        id = id,
        title = title,
        originalTitle = "",
        posters = posters,
        htmlUrl = htmlUrl,
        releaseDate = releaseDate
    )

    fun isPlaying() = playingCinemas.isNotEmpty()

    fun containsTrailer() = trailer?.isYoutube() ?: false
}