package com.diegobezerra.shared.result

sealed class Error {

    object Unknown : Error()

    object Network : Error()

    object Timeout : Error()

    object NoConnection : Error()

    data class Data(val icon: Int, val title: String, val message: String)

}