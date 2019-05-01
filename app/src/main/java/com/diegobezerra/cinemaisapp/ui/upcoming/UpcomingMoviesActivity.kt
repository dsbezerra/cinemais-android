package com.diegobezerra.cinemaisapp.ui.upcoming

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.R.id
import com.diegobezerra.cinemaisapp.ui.main.movies.TabMoviesFragment
import com.diegobezerra.cinemaisapp.util.setupActionBar
import com.diegobezerra.core.cinemais.data.movie.MoviesRepository.Companion.UPCOMING
import dagger.android.support.DaggerAppCompatActivity

class UpcomingMoviesActivity : DaggerAppCompatActivity() {

    companion object {

        const val FRAGMENT_ID = id.fragment_container

        fun getStartIntent(context: Context): Intent {
            return Intent(context, UpcomingMoviesActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming_movies)

        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(FRAGMENT_ID, TabMoviesFragment.newInstance(UPCOMING))
                .commit()
        }
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
