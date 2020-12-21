package com.diegobezerra.core.cinemais.data.movie.trailer

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Trailer
import okhttp3.ResponseBody
import org.jsoup.nodes.Element
import retrofit2.Converter

object CinemaisTrailerConverter : Converter<ResponseBody, Trailer> {

    // If a match is successful then ID will be in group 5
    // https://gist.github.com/afeld/1254889
    private val REGEX = "(youtu\\.be/|youtube\\.com/(watch\\?(.*&)?v=|(embed|v)/))([^?&\"'>]+)"
        .toRegex()

    override fun convert(value: ResponseBody): Trailer {
        return parseTrailer(value.asJsoup().body())
    }

    private fun parseTrailer(element: Element): Trailer {
        val src = element.select("#filmeContainer > object > embed").attr("src")
        val matchResult = REGEX.find(src)
        return if (matchResult != null) {
             Trailer.youtube(matchResult.groupValues[5], src)
        } else {
            Trailer.EMPTY
        }
    }

}