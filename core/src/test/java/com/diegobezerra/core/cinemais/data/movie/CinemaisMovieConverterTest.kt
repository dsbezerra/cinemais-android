package com.diegobezerra.core.cinemais.data.movie

import okhttp3.ResponseBody
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisMovieConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, loadPageHtml("/cinemais_movie.html"))

        val movie = CinemaisMovieConverter.convert(response)

        assertNotNull(movie)
        assertNotNull(movie.id)
        assertNotNull(movie.title)
        assertNotNull(movie.originalTitle)
        assertNotNull(movie.rating)
        assertNotNull(movie.runtime)
        assertNotNull(movie.releaseDate)
        assertNotNull(movie.distributor)
        assertNotNull(movie.countries)
        assertNotNull(movie.genres)
        assertNotNull(movie.posters)
        assertNotNull(movie.htmlUrl)
    }

    private fun loadPageHtml(path: String): String {
        val inputStream = CinemaisMovieConverterTest::class.java.getResourceAsStream(path)
        return inputStream?.bufferedReader().use { it!!.readText() }
    }
}