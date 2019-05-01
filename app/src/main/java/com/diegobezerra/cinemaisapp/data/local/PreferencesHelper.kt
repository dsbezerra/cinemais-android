package com.diegobezerra.cinemaisapp.data.local

import android.content.Context
import com.diegobezerra.cinemaisapp.util.update
import javax.inject.Inject

class PreferencesHelper @Inject constructor(context: Context) {

    companion object {

        const val PREFS_NAME = "cinemais"
        const val PREF_SELECTED_CINEMA = "pref_selected_cinema"
        const val PREF_SELECTED_FILTERS = "pref_selected_filters"

    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedCinemaId(): Int? {
        val id = prefs.getInt(PREF_SELECTED_CINEMA, 0)
        return if (id == 0) {
            null
        } else {
            id
        }
    }

    fun getSelectedFilters(): String? = prefs.getString(PREF_SELECTED_FILTERS, null)

    fun setSelectedCinemaId(cinemaId: Int) {
        prefs.update {
            putInt(PREF_SELECTED_CINEMA, cinemaId)
        }
    }

}