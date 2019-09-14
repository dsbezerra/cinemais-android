package com.diegobezerra.core.util

fun String.breakBySpaces() = breakByToken(' ')

fun String.breakByToken(tok: Char): Pair<String, String> {
    val result = Pair(this, "")
    if (isEmpty()) {
        return result
    }
    var i = 0
    while (true) {
        if (i >= length) {
            break
        }
        if (get(i) == tok) {
            return Pair(substring(0, i).trim(), substring(i + 1).trim())
        }
        i++
    }
    return result
}