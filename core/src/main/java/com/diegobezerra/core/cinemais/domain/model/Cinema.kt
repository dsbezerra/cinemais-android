package com.diegobezerra.core.cinemais.domain.model

data class Cinema(
    val id: Int,
    val name: String,
    val cityName: String,
    val federativeUnit: String,
    var location: Location? = null,
    var information: List<String> = listOf()
) {
    companion object {

        private val ID_REGEX = "cc=(\\d+)".toRegex()

        fun idFromUrl(url: String): Int? = ID_REGEX.find(url)?.groupValues?.get(1)?.toInt()
    }
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val addressLine: String
)

class Cinemas(
    val cinemas: List<Cinema> = listOf()
)