package com.diegobezerra.core.cinemais.domain.model

// Used to hold trailer data from Youtube or Cinemais
data class Trailer(
    val id: String,
    val url: String,
    val source: String
) {
    companion object {

        const val SOURCE_CINEMAIS = "Cinemais"
        const val SOURCE_YOUTUBE = "YouTube"

        fun cinemais(id: String, url: String) = Trailer(id, url, SOURCE_CINEMAIS)

        fun youtube(id: String, url: String) = Trailer(id, url, SOURCE_YOUTUBE)
    }

    fun isValid() = id != ""

    fun isCinemais() = isValid() && source == SOURCE_CINEMAIS

    fun isYoutube() = isValid() && source == SOURCE_YOUTUBE
}