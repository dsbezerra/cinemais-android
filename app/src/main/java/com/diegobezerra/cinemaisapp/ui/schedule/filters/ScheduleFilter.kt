package com.diegobezerra.cinemaisapp.ui.schedule.filters

import androidx.databinding.ObservableBoolean

class ScheduleFilter(
    val id: String, // Matches session values
    val labelRes: Int,
    isChecked: Boolean
) {
    val isChecked = ObservableBoolean(isChecked)
}