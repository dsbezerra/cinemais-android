package com.diegobezerra.core.cinemais.data.movie.image

import com.diegobezerra.core.cinemais.data.TestUtil
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Test

class CinemaisImagesConverterTest {

    @Test
    fun parsesHtml() {
        val response = ResponseBody.create(null, TestUtil.loadCinemaisHtmlPage("images"))
        val result = CinemaisImagesConverter.convert(response)

        Assert.assertNotNull(result)
        Assert.assertEquals(true, result.exists())
        Assert.assertNotEquals(0, result.backdrops.size)
        Assert.assertNotEquals(0, result.posters.size)
    }

}