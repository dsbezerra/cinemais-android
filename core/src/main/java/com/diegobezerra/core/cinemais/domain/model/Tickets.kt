package com.diegobezerra.core.cinemais.domain.model

data class Weekday(
    val number: Int,
    val name: String
)

data class Weekdays(
    val weekdays: List<Weekday> = emptyList(),
    val disclaimer: String,
    val holidays: Boolean = false,
    val exceptHolidays: Boolean = false,
    val exceptPreviews: Boolean = false,
    val lastPrefix: String = "e",
) {
    fun isEmpty(): Boolean = weekdays.isEmpty()
}

sealed class Ticket(
    open val weekdays: Weekdays?
) {
    data class Normal(
        override val weekdays: Weekdays?,
        val full: Float,
        val half: Float,
        val format: String,
        val vip: Boolean,
        val magic: Boolean,
    ) : Ticket(weekdays)

    data class DriveIn(
        override val weekdays: Weekdays?,
        val price: Float,
        val people: MinMaxPeople,
    ) : Ticket(weekdays)
}

typealias MinMaxPeople = Pair<Int, Int>

fun MinMaxPeople.isEqual(): Boolean = first == second

data class Tickets(
    val tickets: List<Ticket>,
    val buyOnlineUrl: String
)