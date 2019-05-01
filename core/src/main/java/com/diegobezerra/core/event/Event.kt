package com.diegobezerra.core.event

import androidx.lifecycle.Observer

open class Event<out T>(private val data: T) {

    var consumed = false

    fun getDataIfNotConsumed(): T? {
        return if (consumed) {
            null
        } else {
            consumed = true
            data
        }
    }
}

class EventObserver<T>(private val onEventConsume: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getDataIfNotConsumed()?.let { data ->
            onEventConsume(data)
        }
    }
}