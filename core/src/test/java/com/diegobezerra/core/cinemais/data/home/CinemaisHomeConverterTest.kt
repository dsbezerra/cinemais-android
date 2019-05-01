package com.diegobezerra.core.cinemais.data.home

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.ResponseBody
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisHomeConverterTest {

    @Test
    fun parsesHtml() {
        val data = TestUtil.loadCinemaisHtmlPage("home")
        val response = ResponseBody.create(null, data)
        val result = CinemaisHomeConverter.convert(response)

        assertNotNull(result)
        assertNotNull(result.banners)
        assertNotNull(result.playingMovies)
        assertNotNull(result.upcomingMovies)
        assertNotEquals(0, result.banners.size)
        assertNotEquals(0, result.playingMovies.size)
        assertNotEquals(0, result.upcomingMovies.size)
    }

}