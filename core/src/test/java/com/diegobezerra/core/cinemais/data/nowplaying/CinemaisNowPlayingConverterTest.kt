package com.diegobezerra.core.cinemais.data.nowplaying

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.ResponseBody
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisNowPlayingConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, TestUtil.loadHtmlPage("/cinemais_nowplaying.html"))

        val result = CinemaisNowPlayingConverter.convert(response)

        assertNotNull(result)
        assertNotEquals(0, result.size)
    }
}