package com.diegobezerra.cinemaisapp.ui.tickets

import android.os.Bundle
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.base.RevealActivity
import com.diegobezerra.cinemaisapp.util.setupActionBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TicketsActivity : RevealActivity() {

    companion object {
        const val EXTRA_CINEMA_ID = "extra.CINEMA_ID"

        const val FRAGMENT_ID = R.id.fragment_container
    }

    override fun onBeforeSetup(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_tickets)
        setupActionBar(R.id.toolbar) {
            title = getString(R.string.title_tickets)
            setDisplayHomeAsUpEnabled(true)
        }
        if (savedInstanceState == null) {
            val cinemaId = intent.getIntExtra(EXTRA_CINEMA_ID, 0)
            val fragment = if (cinemaId != 0) {
                TicketsFragment.newInstance(cinemaId)
            } else {
                finish()
                return
            }
            supportFragmentManager.beginTransaction()
                .add(FRAGMENT_ID, fragment)
                .commit()
        }
    }
}
