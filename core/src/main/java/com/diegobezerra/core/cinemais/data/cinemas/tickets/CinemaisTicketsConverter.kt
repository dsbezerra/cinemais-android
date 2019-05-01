package com.diegobezerra.core.cinemais.data.cinemas.tickets

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Tickets
import okhttp3.ResponseBody
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import retrofit2.Converter

object CinemaisTicketsConverter : Converter<ResponseBody, Tickets> {

    override fun convert(value: ResponseBody): Tickets {
        return parseTickets(value.asJsoup().body())
    }

    fun parseTickets(element: Element): Tickets {
        val elements = element.childNodes().filter {
            when (it) {
                is Element -> {
                    !it.`is`("a") && !it.text().contains("TABELA DE PREÇOS")
                }
                is TextNode -> {
                    val text = it.text().trim()
                    text != "" && text != "A venda de ingressos da Cinemais é realizada pela Velox Tickets." && text !=
                        "Clique abaixo para ser redirecionado para uma página externa para efetuar sua compra."
                }
                else -> false
            }
        }
        val result = elements.map {
            it.outerHtml()
        }
        // Trim <br> at start and end
        val trimmed = trimLineBreakElements(result)
        val buyOnlineUrl = element.select("a[title='Comprar ingressos on-line']").attr("href")
        return Tickets(
            content = trimmed.joinToString(""),
            buyOnlineUrl = buyOnlineUrl
        )
    }

    private fun trimLineBreakElements(input: List<String>): List<String> {
        if (input.isEmpty()) return input

        var start = 0
        for (i in 0 until input.size) {
            if (input[i] == "<br>") start += 1
            else break
        }

        var end = input.size - 1
        for (i in end downTo start) {
            if (input[i] == "<br>") end -= 1
            else break
        }

        return input.slice(IntRange(start, end))
    }

}