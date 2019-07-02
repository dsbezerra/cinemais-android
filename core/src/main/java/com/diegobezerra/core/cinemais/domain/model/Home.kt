package com.diegobezerra.core.cinemais.domain.model

import com.diegobezerra.core.cinemais.domain.model.Banner.Action.MOVIE
import com.diegobezerra.core.cinemais.domain.model.Banner.Action.NEWS
import com.diegobezerra.core.cinemais.domain.model.Banner.Action.NONE
import com.diegobezerra.core.cinemais.domain.model.Banner.Action.PROMO

data class Home(
    val backdrop: String,
    val banners: List<Banner>,
    val playingMovies: List<Movie>,
    val upcomingMovies: List<Movie>,
    val cinemas: Cinemas
)

data class Banner(
    val imageUrl: String,
    val htmlUrl: String
) {
    /** Banner touch action */
    enum class Action {
        NONE,
        MOVIE,
        NEWS,
        PROMO,
    }

    var resourceId: Int = 0
    var action: Action = NONE

    init {

        when {
            htmlUrl.contains("noticias_page.php") -> {
                action = NEWS
                parseId("\\?cn=(\\d+)\$".toRegex())
            }
            htmlUrl.contains("filme.php") -> {
                action = MOVIE
                parseId("\\?cf=(\\d+)\$".toRegex())
            }
            htmlUrl.contains("promocao_inloco.php") -> {
                action = PROMO
                parseId("\\?cp=(\\d+)\$".toRegex())
            }
        }
    }

    private fun parseId(regex: Regex) {
        resourceId = regex.find(htmlUrl)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
}