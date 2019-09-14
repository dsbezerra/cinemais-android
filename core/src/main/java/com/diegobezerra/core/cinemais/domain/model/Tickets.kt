package com.diegobezerra.core.cinemais.domain.model

data class Weekdays(
    val weekdays: List<Int> = emptyList(),
    val holidays: Boolean = false,
    val exceptHolidays: Boolean = false,
    val exceptPreviews: Boolean = false
) {

    fun isEmpty(): Boolean = weekdays.isEmpty()

}

data class Ticket(
    val weekdays: Weekdays?,
    val full: Float,
    val half: Float,
    val format: String,
    val vip: Boolean,
    val magic: Boolean
)


data class Tickets(
    val tickets: List<Ticket>,
    val buyOnlineUrl: String
)