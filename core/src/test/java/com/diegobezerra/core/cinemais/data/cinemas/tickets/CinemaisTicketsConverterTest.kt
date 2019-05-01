package com.diegobezerra.core.cinemais.data.cinemas.tickets

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.ResponseBody
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisTicketsConverterTest {

    @Test
    fun parsesHtml() {
        val data = TestUtil.loadCinemaisHtmlPage("tickets")
        val response = ResponseBody.create(null, data)
        val result = CinemaisTicketsConverter.convert(response)
        assertNotNull(result)
    }

}