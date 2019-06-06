package com.diegobezerra.core.cinemais.data.schedule

import com.diegobezerra.core.cinemais.data.TestUtil
import com.diegobezerra.core.cinemais.domain.model.Session
import okhttp3.ResponseBody
import org.junit.Assert.assertNotNull
import org.junit.Test

class CinemaisScheduleConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, TestUtil.loadCinemaisHtmlPage("schedule"))
        val result = CinemaisScheduleConverter.convert(response)
        assertNotNull(result)
    }

}