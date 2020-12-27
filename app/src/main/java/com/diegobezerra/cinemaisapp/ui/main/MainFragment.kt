package com.diegobezerra.cinemaisapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.diegobezerra.cinemaisapp.util.safeRequireActivity

abstract class MainFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        safeRequireActivity(MainActivity::class) { activity ->
            activity.supportActionBar?.let {
                it.title = title()
            }
        }
    }

    open fun id(): Int = 0

    open fun title(): String? = null

}