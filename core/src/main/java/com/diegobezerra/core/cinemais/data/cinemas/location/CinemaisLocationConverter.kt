package com.diegobezerra.core.cinemais.data.cinemas.location

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Location
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import retrofit2.Converter

object CinemaisLocationConverter : Converter<ResponseBody, Location> {

    override fun convert(value: ResponseBody): Location {
        return parseLocation(value.asJsoup())
    }

    private fun parseLocation(element: Element): Location {
        val scriptBlock = element.select("script").first { it.data().trim() != "" }
        val scriptContent = scriptBlock.data()

        val values =
            "LatLng\\((-?\\d+\\.\\d+?),\\s*(-?\\d+\\.\\d+?)\\);".toRegex().find(scriptContent)
                ?.groupValues
        val latLng = try {
            values?.subList(1, values.size)?.map { it.toDouble() } ?: listOf(0.0, 0.0)
        } catch (e: Throwable) {
            listOf(0.0, 0.0)
        }
        val addressLine = "\\s*var\\s*contentString\\s*=\\s*'(.*)';".toRegex()
            .find(scriptContent)?.groupValues?.get(1)?.let {
            val elements = Jsoup.parse(it).select("span")
            var line = ""
            if (elements.size == 2) {
                elements[1].childNodes().forEach { node ->
                    if (node is TextNode) {
                        line += node.text()
                    } else if (node is Element && node.`is`("br")) {
                        line += "\n"
                    }
                }
            }
            line
        } ?: ""

        return Location(
            latitude = latLng[0],
            longitude = latLng[1],
            addressLine = addressLine
        )
    }

}