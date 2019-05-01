package com.diegobezerra.core.cinemais.data.nowplaying

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.data.movie.CinemaisMovieConverter
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Posters.Companion.SMALL
import okhttp3.ResponseBody
import org.jsoup.select.Elements
import retrofit2.Converter

object CinemaisNowPlayingConverter : Converter<ResponseBody, List<Movie>> {

    override fun convert(value: ResponseBody): List<Movie> {
        return parseNowPlaying(value.asJsoup().select("#indexContainer"))
    }

    private fun parseNowPlaying(elements: Elements): List<Movie> {
        val result = mutableListOf<Movie>()

        // Look in premiere containers
        elements.select("div.estreiasContainer > div.estreiasContainerSide a").forEach {
            val htmlUrl = it.attr("href")
            val id = Movie.getIdFromUrl(htmlUrl)
            if (id != 0) {
                val title = it.attr("title")
                val posterUrl = it.select("img").attr("src")
                val posters = CinemaisMovieConverter.parsePoster(posterUrl, SMALL)
                result.add(Movie(id = id, title = title, posters = posters, htmlUrl = htmlUrl))
            }
        }

        // Look in right side container
        val fakePosterUrl = "http://www.claquete.com/fotos/filmes/poster/{id}_pequeno.jpg"
        elements.select("#indexContainer > div.continuacaoContainer > ul li a").forEach {
            val htmlUrl = it.attr("href")
            val id = Movie.getIdFromUrl(htmlUrl)
            if (id != 0) {
                val title = it.attr("title")
                val posters = CinemaisMovieConverter.parsePoster(
                    fakePosterUrl.replace("{id}", id.toString()), SMALL
                )
                result.add(Movie(id = id, title = title, posters = posters, htmlUrl = htmlUrl))
            }
        }

        return result
    }
}
