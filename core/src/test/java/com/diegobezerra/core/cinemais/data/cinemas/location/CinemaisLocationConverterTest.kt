package com.diegobezerra.core.cinemais.data.cinemas.location

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.ResponseBody
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisLocationConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, TestUtil.loadHtmlPage("/cinemais_location.html"))

        val result = CinemaisLocationConverter.convert(response)

        assertNotNull(result)
    }

}