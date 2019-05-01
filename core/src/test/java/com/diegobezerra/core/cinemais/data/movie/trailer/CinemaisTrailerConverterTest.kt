package com.diegobezerra.core.cinemais.data.movie.trailer

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisTrailerConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, TestUtil.loadHtmlPage("/cinemais_trailer.html"))

        val result = CinemaisTrailerConverter.convert(response)

        assertNotNull(result)
        assertNotNull(result.id)
        assertNotNull(result.url)
        assertEquals("xXcROQVNv30", result.id)
    }

}