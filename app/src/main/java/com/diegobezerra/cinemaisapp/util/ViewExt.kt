package com.diegobezerra.cinemaisapp.util

import android.text.Html
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("fromHtml")
fun TextView.setFromHtml(content: String?) {
    text = if (content != null) Html.fromHtml(content) else ""
}

@BindingAdapter("goneUnless")
fun goneUnless(view: View, visible: Boolean) {
    view.visibility = if (visible) VISIBLE else GONE
}