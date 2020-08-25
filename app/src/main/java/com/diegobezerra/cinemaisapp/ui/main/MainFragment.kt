package com.diegobezerra.cinemaisapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class MainFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (requireActivity() as MainActivity).supportActionBar?.let {
            it.title = title()
        }
    }

    open fun id(): Int = 0

    open fun title(): String? = null

}