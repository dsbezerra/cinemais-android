package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.data.CinemaisService.Companion.ENDPOINT
import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Posters
import com.diegobezerra.core.cinemais.domain.model.Trailer
import okhttp3.ResponseBody
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import retrofit2.Converter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

object CinemaisMovieConverter : Converter<ResponseBody, Movie> {

    private const val SYNOPSIS = "sinopse"
    private const val CAST = "elenco"
    private const val SCREENPLAY = "roteiro"
    private const val PRODUCTION = "produção"
    private const val EXECUTIVE_PRODUCTION = "produção executiva"
    private const val DIRECTION = "direção"
    private const val COUNTRY = "país"
    private const val GENRES = "gênero"
    private const val RUNTIME = "duração"
    private const val RELEASE = "lançamento nacional"
    private const val DISTRIBUTION = "distribuição"

    override fun convert(value: ResponseBody): Movie {
        return parseMovie(value.asJsoup().select("#filmeContainer").first())
    }

    private fun parseMovie(element: Element): Movie {
        val posterUrl = element.select("#sideBar > img").attr("src")
        val posters = parsePoster(posterUrl, Posters.MEDIUM)

        // NOTE: The movie id can be found in the poster url
        val id = posterUrl.replace("\\D+".toRegex(), "").toInt()
        val htmlUrl = "${ENDPOINT}filmes/filme.php?cf=$id"
        val title = element.select("h1").first().text()
        val originalTitle = element.select("small")
            .text().replace("^\\(|,\\s\\d{4}\\)\$".toRegex(), "")

        var synopsis = ""
        var cast = listOf<String>()
        var screenplay = listOf<String>()
        var executiveProduction = listOf<String>()
        var production = listOf<String>()
        var direction = listOf<String>()
        element.select("#filmesContainer")
            .first()
            .children()
            .forEach {
                if (it.`is`("h3")) {
                    val label = it.text().toLowerCase()
                    val content = it.nextElementSibling().text()
                    when (label) {
                        SYNOPSIS -> {
                            synopsis = content
                        }
                        CAST -> {
                            var castText = content
                            if (castText.contains("Vozes de:")) {
                                castText = castText.replace("Vozes de:", "").trim()
                            }
                            cast = castText.split(", ")
                        }
                        SCREENPLAY -> {
                            screenplay = content.split(", ")
                        }
                        EXECUTIVE_PRODUCTION -> {
                            executiveProduction = content.split(", ")
                        }
                        PRODUCTION -> {
                            production = content.split(", ")
                        }
                        DIRECTION -> {
                            direction = content.split(", ")
                        }
                    }
                }
            }

        var runtime = 0
        var releaseDate: Date? = null
        var distributor: String? = null
        var countries = listOf<String>()
        var genres = listOf<String>()

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
                        RUNTIME -> runtime = contentText.replace("\\D+".toRegex(), "")
                            .toInt()
                        RELEASE -> {
                            releaseDate = try {
                                SimpleDateFormat("dd/MM/yyyy").parse(contentText)
                            } catch (e: ParseException) {
                                null
                            }
                        }
                        DISTRIBUTION -> distributor = contentText
                        else -> {
                            // Do nothing
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        val rating = parseRating(rows.select("img").first())

        val trailersUrl = element.select("#sideBar a[title='Trailers']")
            .first()?.attr("href") ?: ""
        val trailerId =
            "ct=(\\d+)\$".toRegex().find(trailersUrl)?.groupValues?.get(1) ?: ""
        val playingCinemas = parsePlayingCinemas(element.select("#filmeContainer > div.salaExibicao > ul li"))
        return Movie(
            id = id,
            title = title,
            originalTitle = originalTitle,
            posters = posters,
            htmlUrl = htmlUrl,
            synopsis = synopsis,
            cast = cast,
            screenplay = screenplay,
            executiveProduction = executiveProduction,
            production = production,
            direction = direction,
            distributor = distributor,
            rating = rating,
            runtime = runtime,
            releaseDate = releaseDate,
            countries = countries,
            genres = genres,
            trailer = if (trailerId != "") {
                Trailer.cinemais(trailerId, trailersUrl)
            } else {
                null
            },
            playingCinemas = playingCinemas
        )
    }

    private fun parsePlayingCinemas(elements: Elements): List<Cinema> {
        val result = mutableListOf<Cinema>()
        val regex = "(.+)\\((?:(.+)-([A-Z]{2}))\\)".toRegex()
        elements.forEach { element ->
            val id = Cinema.idFromUrl(element.select("a").first().attr("href"))
            id?.let { cinemaId ->
                val groupValues = regex.find(element.text())?.groupValues
                groupValues?.let {
                    if (it.size == 4) {
                        result.add(
                            Cinema(
                                id = cinemaId,
                                name = it[1].replace("Cinemais", "").trim(),
                                cityName = it[2],
                                federativeUnit = it[3]
                            )
                        )
                    }
                }
            }
        }
        return result
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

    fun parsePoster(posterUrl: String, size: String): Posters {
        return Posters(
            small = posterUrl.replace(size, "pequeno"),
            medium = posterUrl.replace(size, "medio"),
            large = posterUrl.replace(size, "grande")
        )
    }
}