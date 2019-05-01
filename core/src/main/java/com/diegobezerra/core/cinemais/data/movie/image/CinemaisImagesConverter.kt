package com.diegobezerra.core.cinemais.data.movie.image

import com.diegobezerra.core.cinemais.data.CinemaisService.Companion.ENDPOINT
import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Images
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object CinemaisImagesConverter : Converter<ResponseBody, Images> {

    override fun convert(value: ResponseBody): Images {
        return parseImages(value.asJsoup().select("#filmesContainer img.imageSpot"))
    }

    private fun parseImages(elements: Elements): Images {
        val images = mutableListOf<String>()
        elements.forEach {
            val src = it.attr("src")
            if (!src.isNullOrEmpty()) {
                images.add(src)
            }
        }
        return Images(
            images = images
        )
    }
}