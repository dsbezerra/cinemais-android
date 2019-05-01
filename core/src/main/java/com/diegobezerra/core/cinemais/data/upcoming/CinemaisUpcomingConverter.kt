package com.diegobezerra.core.cinemais.data.upcoming

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.data.movie.CinemaisMovieConverter.parsePoster
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Posters
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import okhttp3.ResponseBody
import org.jsoup.select.Elements
import retrofit2.Converter
import java.text.ParseException
import java.text.SimpleDateFormat

object CinemaisUpcomingConverter : Converter<ResponseBody, List<Movie>> {

    override fun convert(value: ResponseBody): List<Movie> {
        return parseUpcoming(
            value.asJsoup()
                .select("#LancamentosContainer > div > div.Poster")
        )
    }

    private fun parseUpcoming(elements: Elements): List<Movie> {
        val result = mutableListOf<Movie>()
        val format = SimpleDateFormat("dd MMMM yyyy", BRAZIL)
        elements.forEach {
            val title = it.select("h5")
                .text()
            val htmlUrl = it.select("a")
                .first()
                .attr("href")
            val posterUrl = it.select("img")
                .attr("src")
            val posters = parsePoster(posterUrl, Posters.MEDIUM)
            val id = Movie.getIdFromUrl(htmlUrl)
            val release = try {
                format.parse(
                    it.select("small").text()
                        .replace(" de ", " ")
                )
            } catch (e: ParseException) {
                null
            }
            result.add(
                Movie(
                    id = id,
                    title = title,
                    posters = posters,
                    htmlUrl = htmlUrl,
                    releaseDate = release
                )
            )
        }
        return result
    }
}
