package com.diegobezerra.cinemaisapp.ui.main

import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import dagger.android.support.DaggerFragment

abstract class MainFragment : DaggerFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (requireActivity() as MainActivity).supportActionBar?.let {
            it.title = title()
        }
    }

    open fun transition(ft: FragmentTransaction, to: String) {
        ft.setTransition(TRANSIT_FRAGMENT_FADE)
    }

    open fun id(): Int = 0

    open fun title(): String? = null

}