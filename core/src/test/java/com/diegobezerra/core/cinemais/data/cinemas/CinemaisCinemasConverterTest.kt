package com.diegobezerra.core.cinemais.data.cinemas

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.ResponseBody
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisCinemasConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, TestUtil.loadCinemaisHtmlPage("home"))
        val result = CinemaisCinemasConverter.convert(response)
        assertNotNull(result)
    }

}