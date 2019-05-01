package com.diegobezerra.core.cinemais.data.home

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.data.cinemas.CinemaisCinemasConverter.parseCinemas
import com.diegobezerra.core.cinemais.data.movie.CinemaisMovieConverter
import com.diegobezerra.core.cinemais.domain.model.Banner
import com.diegobezerra.core.cinemais.domain.model.HomeData
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Posters
import okhttp3.ResponseBody
import org.jsoup.nodes.Element
import retrofit2.Converter

object CinemaisHomeConverter : Converter<ResponseBody, HomeData> {

    override fun convert(value: ResponseBody): HomeData {
        return parseHomeData(value.asJsoup())
    }

    private fun parseHomeData(element: Element): HomeData {
        val backdrop = "(https?://w*\\.cinemais.com.br/fotos/wallpaper/\\d+\\.jpg)"
            .toRegex().find(element.select("style").first().data())?.groupValues?.get(1)
        val banners = parseBanners(element.select("div > div.bannerIndex > ul.bannerContainer").first())
        val playingMovies = parsePlayingMovies(element.select("#carousel_ul").first())
        val upcomingMovies = parseUpcomingMovies(element.select("#proxul").first())
        val cinemas = parseCinemas(element.select("#teste li"))
        return HomeData(
            backdrop = backdrop ?: "",
            banners = banners,
            playingMovies = playingMovies,
            upcomingMovies = upcomingMovies,
            cinemas = cinemas
        )
    }

    private fun parseBanners(element: Element): List<Banner> {
        val result = mutableListOf<Banner>()
        element.select("li").forEach {
            val href = it.select("a").attr("href")
            val src = it.select("img").attr("src")
            if (href != "" && src != "") {
                result.add(Banner(imageUrl = src, htmlUrl = href))
            }
        }
        return result
    }

    private fun parsePlayingMovies(element: Element): List<Movie> {
        val result = mutableListOf<Movie>()
        element.select("li").forEach {
            val href = it.select("a").attr("href")
            val img = it.select("img")
            val posterUrl = img.attr("src")
            val id = Movie.getIdFromUrl(href)
            result.add(Movie(
                id = id,
                title = img.attr("alt"),
                posters =  CinemaisMovieConverter.parsePoster(posterUrl, Posters.SMALL),
                htmlUrl = href
            ))

        }
        return result
    }

    private fun parseUpcomingMovies(element: Element): List<Movie> {
        val result = mutableListOf<Movie>()
        element.select("li").forEach {
            val htmlUrl = it.select("a").attr("href")
            if (!htmlUrl.contains("proximos_lancamentos")) {
                val posterUrl = it.select("img").attr("src")
                val title = it.select("h3").text()
                val synopsis = it.select("p").first().text().let { s ->
                    if (s.startsWith("Veja mais detalhes do filme") || s.isEmpty()) {
                        "Toque para ver mais detalhes do filme"
                    } else {
                        s
                    }
                }
                val id = Movie.getIdFromUrl(htmlUrl)
                result.add(Movie(
                    id = id,
                    title = title,
                    posters = CinemaisMovieConverter.parsePoster(posterUrl, Posters.SMALL),
                    htmlUrl = htmlUrl,
                    synopsis = synopsis
                ))
            }
        }
        return result
    }
}