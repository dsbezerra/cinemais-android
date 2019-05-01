package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisMovieConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, TestUtil.loadCinemaisHtmlPage("movie"))

        val movie = CinemaisMovieConverter.convert(response)

        assertNotNull(movie)
        assertNotNull(movie.id)
        assertNotNull(movie.title)
        assertNotNull(movie.originalTitle)
        assertNotNull(movie.synopsis)
        assertNotNull(movie.rating)
        assertNotNull(movie.posters)
        assertNotNull(movie.htmlUrl)
        assertNotNull(movie.runtime)
        assertNotNull(movie.releaseDate)
        assertNotNull(movie.distributor)
        assertNotNull(movie.countries)
        assertNotNull(movie.genres)
        assertNotNull(movie.trailer)
        assertNotNull(movie.trailer?.id)
        assertNotNull(movie.trailer?.url)
    }

}