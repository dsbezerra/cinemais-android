package com.diegobezerra.core.cinemais.data.cinemas

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Cinemas
import okhttp3.ResponseBody
import org.jsoup.select.Elements
import retrofit2.Converter

object CinemaisCinemasConverter : Converter<ResponseBody, Cinemas> {

    override fun convert(value: ResponseBody): Cinemas {
        return parseCinemas(value.asJsoup().select("#teste li"))
    }

    fun parseCinemas(elements: Elements): Cinemas {
        val result = mutableListOf<Cinema>()
        elements.forEach { element ->
            val id = element.attr("id").toIntOrNull()
            id?.let {
                val text = element.text()
                val parts = text.split(" - ")
                var cinema: Cinema? = null
                when (parts.size) {
                    // First is cinema/city name and the second is the federative unit
                    2 -> {
                        cinema = Cinema(
                            id = id,
                            name = parts[0],
                            cityName = parts[0],
                            fu = parts[1]
                        )
                    }
                    // First is cinema name, second is city name and the last one is the
                    // federative unit
                    3 -> {
                        cinema = Cinema(
                            id = id,
                            name = parts[0],
                            cityName = parts[1],
                            fu = parts[2]
                        )
                    }
                    else -> Unit // No-op
                }
                cinema?.let {
                    result.add(cinema)
                }
            }
        }
        return Cinemas(
            cinemas = result
        )
    }
}