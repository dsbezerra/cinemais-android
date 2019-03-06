package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.domain.model.Movie
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Converter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

private const val BASE_URI = "https://cinemais.com.br"

object CinemaisMovieConverter : Converter<ResponseBody, Movie> {

    const val COUNTRY = "país"
    const val GENRES = "gênero"
    const val RUNTIME = "duração"
    const val RELEASE = "lançamento nacional"
    const val DISTRIBUTION = "distribuição"

    override fun convert(value: ResponseBody): Movie {
        return parseMovie(Jsoup.parse(value.string(), BASE_URI).select("#filmeContainer").first())
    }

    private fun parseMovie(element: Element): Movie {
        var runtime = 0
        var releaseDate: Date? = null
        var distributor: String? = null
        var countries = listOf<String>()
        var genres = listOf<String>()

        val title = element.select("h1").first().text()
        val originalTitle = element.select("small").text()
            .replace("^\\(|,\\s\\d{4}\\)\$".toRegex(), "")
        val rows = element.select("#filmes_conteudo tr")
        try {
            val headers = rows.select("td")
            val contents = rows[1].select("td")
            headers.forEachIndexed { index, it ->
                val header = it.select("strong")
                if (header.isNotEmpty() && index > 0) {
                    val contentText = contents[index - 1].text()
                    when (it.text().toLowerCase()) {
                        COUNTRY -> countries = contentText.split(", ")
                        GENRES -> genres = contentText.split(", ")
                        RUNTIME -> runtime = contentText.replace("\\D+".toRegex(), "").toInt()
                        RELEASE -> {
                            releaseDate = try {
                                SimpleDateFormat("dd/MM/yyyy").parse(contentText)
                            } catch (e: ParseException) {
                                null
                            }
                        }
                        DISTRIBUTION -> distributor = contentText
                        else -> println(contentText)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        val rating = parseRating(rows.select("img").first())

        return Movie(
            id = 0,
            title = title,
            originalTitle = originalTitle,
            distributor = distributor,
            rating = rating,
            runtime = runtime,
            releaseDate = releaseDate,
            countries = countries,
            genres = genres
        )
    }

    private fun parseRating(element: Element): Int {
        val src = element.attr("src")

        when (src.substringAfterLast("/")) {
            "ICO_LIV_programacao_GR.png" -> return -1
            "ICO_10_programacao_GR.png" -> return 10
            "ICO_12_programacao_GR.png" -> return 12
            "ICO_14_programacao_GR.png" -> return 14
            "ICO_16_programacao_GR.png" -> return 16
            "ICO_18_programacao_GR.png" -> return 18
        }

        return 0
    }
}