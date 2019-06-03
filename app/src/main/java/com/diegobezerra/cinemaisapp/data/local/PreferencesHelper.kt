package com.diegobezerra.cinemaisapp.data.local

import android.content.Context
import com.diegobezerra.cinemaisapp.util.update
import javax.inject.Inject

class PreferencesHelper @Inject constructor(context: Context) {

    companion object {

        const val PREFS_NAME = "cinemais"
        const val PREF_SELECTED_CINEMA = "pref_selected_cinema"
        const val PREF_DARK_THEME = "pref_dark_theme"
        const val PREF_INTERSTITIAL_LAST_DISPLAY_TIMESTAMP =
            "pref_interstitial_last_display_timestamp"
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

    fun setSelectedCinemaId(cinemaId: Int) {
        prefs.update {
            putInt(PREF_SELECTED_CINEMA, cinemaId)
        }
    }

    fun isDarkTheme(): Boolean? =
        prefs.getBoolean(PREF_DARK_THEME, false)

    fun getInterstitialLastDisplayTimestamp() =
        prefs.getLong(PREF_INTERSTITIAL_LAST_DISPLAY_TIMESTAMP, 0)

    fun setInterstitialLastDisplayTimestamp(value: Long) {
        prefs.update {
            putLong(PREF_INTERSTITIAL_LAST_DISPLAY_TIMESTAMP, value)
        }
    }
}