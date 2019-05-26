package com.diegobezerra.core.cinemais.data.movie.image

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Images
import okhttp3.ResponseBody
import org.jsoup.select.Elements
import retrofit2.Converter

object CinemaisImagesConverter : Converter<ResponseBody, Images> {

    override fun convert(value: ResponseBody): Images {
        return parseImages(value.asJsoup().select("#filmesContainer div.imagesFilmeSpot"))
    }

    private fun parseImages(elements: Elements): Images {
        val backdrops = mutableListOf<String>()
        val posters = mutableListOf<String>()
        elements.forEachIndexed { index, container ->
            // First index contains backdrop/still images and second contains poster images
            container.select("img.imageSpot").forEach {
                val src = it.attr("src")
                if (src.isNullOrEmpty()) return@forEach
                when (elements.size) {
                    1 -> posters += src
                    // We only have posters?
                    2 -> when (index) {
                        0 -> backdrops += src
                        1 -> posters += src
                        else -> Unit // TODO: Diagnostic
                    }
                    else -> Unit // TODO: Diagnostic
                }
            }

        }
        return Images(
            backdrops = backdrops,
            posters = posters
        )
    }
}