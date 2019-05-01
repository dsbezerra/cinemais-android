package com.diegobezerra.cinemaisapp.util

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

fun Fragment.setupToolbarAsActionBar(
    rootView: View, @IdRes toolbarId: Int,
    action: ActionBar.() -> Unit
): Toolbar {
    val toolbar = rootView.findViewById<Toolbar>(toolbarId)
    (requireActivity() as AppCompatActivity).run {
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            action()
        }
    }
    return toolbar
}