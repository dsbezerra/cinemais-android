package com.diegobezerra.core.cinemais.data.upcoming

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.ResponseBody
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisUpcomingConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, TestUtil.loadCinemaisHtmlPage("upcoming"))
        val result = CinemaisUpcomingConverter.convert(response)

        assertNotNull(result)
        assertNotEquals(0, result.size)
    }

}