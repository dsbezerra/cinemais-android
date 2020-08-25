package com.diegobezerra.cinemaisapp.ui.main.cinemas

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.diegobezerra.cinemaisapp.R

@BindingAdapter("name", "cityName")
fun setCinemaName(
    textView: TextView,
    name: String,
    cityName: String,
) {
    textView.text = if (name != cityName) {
        textView.resources.getString(R.string.label_cinema, name, cityName)
    } else {
        name
    }
}