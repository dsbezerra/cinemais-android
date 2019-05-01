package com.diegobezerra.core.cinemais.data

import java.nio.charset.Charset

class TestUtil {

    companion object {

        fun loadHtmlPage(path: String, charset: String = "UTF-8"): String {
            val inputStream = TestUtil::class.java.getResourceAsStream(path)
            return inputStream?.bufferedReader(Charset.forName(charset)).use {
                it!!.readText()
            }
        }

        fun loadCinemaisHtmlPage(name: String, charset: String = "CP1252") =
            loadHtmlPage("/cinemais_$name.html", charset)
    }

}