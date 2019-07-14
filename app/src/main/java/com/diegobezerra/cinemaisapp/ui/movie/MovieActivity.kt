package com.diegobezerra.cinemaisapp.ui.movie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.util.setupTheme
import dagger.android.support.DaggerAppCompatActivity

class MovieActivity : DaggerAppCompatActivity() {

    private lateinit var fragment: MovieFragment

    companion object {
        const val EXTRA_MOVIE_ID = "extra.MOVIE_ID"
        const val FRAGMENT_ID = R.id.fragment_container

        fun getStartIntent(context: Context, movieId: Int): Intent {
            return Intent(context, MovieActivity::class.java).apply {
                putExtra(EXTRA_MOVIE_ID, movieId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        if (savedInstanceState == null) {
            val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, 0)
            supportFragmentManager.beginTransaction()
                .add(FRAGMENT_ID, MovieFragment.newInstance(movieId).also {
                    fragment = it
                })
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentById(FRAGMENT_ID) as MovieFragment
        }
    }

    override fun onBackPressed() {
        if (!fragment.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
