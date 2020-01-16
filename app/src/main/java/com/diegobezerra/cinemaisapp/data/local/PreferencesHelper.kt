package com.diegobezerra.cinemaisapp.data.local

import android.content.Context
import com.diegobezerra.cinemaisapp.util.update
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber
import javax.inject.Inject

class PreferencesHelper @Inject constructor(context: Context) {

    companion object {

        const val PREFS_NAME = "cinemais"
        const val PREF_SELECTED_CINEMA = "pref_selected_cinema"
        const val PREF_SELECTED_FILTERS = "pref_selected_filters"
        const val PREF_DARK_THEME = "pref_dark_theme"
        const val PREF_INTERSTITIAL_LAST_DISPLAY_TIMESTAMP =
            "pref_interstitial_last_display_timestamp"
        const val PREF_CHECK_PREMIERES_SCHEDULED = "pref_check_premieres_scheduled"
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

    fun isCheckPremieresScheduled(): Boolean =
        prefs.getBoolean(PREF_CHECK_PREMIERES_SCHEDULED, false)

    fun getInterstitialLastDisplayTimestamp() =
        prefs.getLong(PREF_INTERSTITIAL_LAST_DISPLAY_TIMESTAMP, 0)

    fun setInterstitialLastDisplayTimestamp(value: Long) {
        prefs.update {
            putLong(PREF_INTERSTITIAL_LAST_DISPLAY_TIMESTAMP, value)
        }
    }

    fun setCheckPremieresScheduled(value: Boolean) {
        prefs.update {
            putBoolean(PREF_CHECK_PREMIERES_SCHEDULED, value)
        }
    }
}