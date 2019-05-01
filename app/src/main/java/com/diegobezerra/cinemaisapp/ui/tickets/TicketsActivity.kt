package com.diegobezerra.cinemaisapp.ui.tickets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.util.setupActionBar
import dagger.android.support.DaggerAppCompatActivity

class TicketsActivity : DaggerAppCompatActivity() {

    companion object {

        const val EXTRA_CINEMA_ID = "extra.CINEMA_ID"
        const val FRAGMENT_ID = R.id.fragment_container

        fun startActivity(context: Context, cinemaId: Int){
            context.run {
                val intent = Intent(this, TicketsActivity::class.java).apply {
                    putExtra(EXTRA_CINEMA_ID, cinemaId)
                }
                startActivity(intent)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
