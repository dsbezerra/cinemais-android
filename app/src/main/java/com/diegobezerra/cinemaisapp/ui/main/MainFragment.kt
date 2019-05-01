package com.diegobezerra.cinemaisapp.ui.main

import android.os.Bundle
import dagger.android.support.DaggerFragment

abstract class MainFragment : DaggerFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (requireActivity() as MainActivity).supportActionBar?.let {
            it.title = title()
        }
    }

    open fun id(): Int = 0
    open fun title(): String? = null

}