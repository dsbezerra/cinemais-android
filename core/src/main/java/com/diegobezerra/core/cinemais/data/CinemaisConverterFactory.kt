package com.diegobezerra.core.cinemais.data

import com.diegobezerra.core.cinemais.data.CinemaisService.Companion.ENDPOINT
import com.diegobezerra.core.cinemais.data.cinemas.CinemaisCinemasConverter
import com.diegobezerra.core.cinemais.data.cinemas.location.CinemaisLocationConverter
import com.diegobezerra.core.cinemais.data.cinemas.tickets.CinemaisTicketsConverter
import com.diegobezerra.core.cinemais.data.home.CinemaisHomeConverter
import com.diegobezerra.core.cinemais.data.movie.CinemaisMovieConverter
import com.diegobezerra.core.cinemais.data.movie.image.CinemaisImagesConverter
import com.diegobezerra.core.cinemais.data.movie.trailer.CinemaisTrailerConverter
import com.diegobezerra.core.cinemais.data.nowplaying.CinemaisNowPlayingConverter
import com.diegobezerra.core.cinemais.data.schedule.CinemaisScheduleConverter
import com.diegobezerra.core.cinemais.data.upcoming.CinemaisUpcomingConverter
import com.diegobezerra.core.cinemais.domain.model.HomeData
import com.diegobezerra.core.cinemais.domain.model.Images
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.core.cinemais.domain.model.Trailer
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class CinemaisConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return when {
            annotations.any { it is CinemaisNowPlaying } -> CinemaisNowPlayingConverter
            annotations.any { it is CinemaisUpcoming } -> CinemaisUpcomingConverter
            annotations.any { it is CinemaisCinemas } -> CinemaisCinemasConverter
            else -> when (type) {
                HomeData::class.java -> CinemaisHomeConverter
                Movie::class.java -> CinemaisMovieConverter
                Images::class.java -> CinemaisImagesConverter
                Trailer::class.java -> CinemaisTrailerConverter
                Schedule::class.java -> CinemaisScheduleConverter
                Location::class.java -> CinemaisLocationConverter
                Tickets::class.java -> CinemaisTicketsConverter
                else -> null
            }
        }
    }
}

// The content-type header says the charset is ISO-8859-1 but some
// pages have – (U+2013 EN DASH) character which is covered by the CP1252
// https://en.wikipedia.org/wiki/Windows-1252#Character_set
fun ResponseBody.asJsoup(charset: String = "CP1252"): Document {
    return if (contentType() == null) {
        Jsoup.parse(string(), ENDPOINT)
    } else {
        Jsoup.parse(byteStream(), charset, ENDPOINT)
    }
}
