package com.diegobezerra.cinemaisapp.data.local

import android.content.Context
import com.diegobezerra.cinemaisapp.util.update
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesHelper @Inject constructor(
    @ApplicationContext context: Context
) {

    companion object {
        const val PREFS_NAME = "cinemais"
        const val PREF_SELECTED_CINEMA = "pref_selected_cinema"
        const val PREF_SELECTED_FILTERS = "pref_selected_filters"
        const val PREF_DARK_THEME = "pref_dark_theme"
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

    fun saveSelectedCinemaId(cinemaId: Int) {
        if (cinemaId == 0) {
            getSelectedCinemaId()?.also {
                Timber.d("Unsubscribe from $it")
                FirebaseMessaging.getInstance().unsubscribeFromTopic("theater_$it")
            }
        } else {
            Timber.d("Subscribe to $cinemaId")
            FirebaseMessaging.getInstance().subscribeToTopic("theater_$cinemaId")
        }
        prefs.update {
            putInt(PREF_SELECTED_CINEMA, cinemaId)
        }
    }

    fun saveSelectedFilters(filters: HashSet<String>) {
        prefs.update {
            putString(PREF_SELECTED_FILTERS, filters.joinToString(","))
        }
    }

    fun getSelectedFilters(): HashSet<String> {
        val filters = prefs.getString(PREF_SELECTED_FILTERS, null) ?: ""
        return if (filters.isNotEmpty()) {
            filters.split(",").toHashSet()
        } else {
            hashSetOf()
        }
    }

    fun isDarkTheme(): Boolean =
        prefs.getBoolean(PREF_DARK_THEME, false)
}