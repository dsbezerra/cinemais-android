package com.diegobezerra.core.cinemais.domain.model

data class Cinema(
    val id: Int,
    val name: String,
    val cityName: String,
    val fu: String,
    var location: Location? = null,
    var information: List<String> = listOf()
) {
    companion object {

        private val ID_REGEX = "cc=(\\d+)".toRegex()

        fun idFromUrl(url: String): Int? = ID_REGEX.find(url)?.groupValues?.get(1)?.toInt()
    }
}

data class Location(
    var query: String? = null,
    val latitude: Double,
    val longitude: Double,
    val addressLine: String? = null
) {

    fun hasQuery() = !query.isNullOrEmpty()

    fun hasLatLng() = latitude != 0.0 && longitude != 0.0

    fun hasAddressLine() = !addressLine.isNullOrEmpty()

}

class Cinemas(
    val cinemas: List<Cinema> = listOf()
)