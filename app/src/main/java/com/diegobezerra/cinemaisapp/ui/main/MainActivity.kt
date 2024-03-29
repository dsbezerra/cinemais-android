package com.diegobezerra.cinemaisapp.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.ui.about.AboutActivity
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaFragment
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasFragment
import com.diegobezerra.cinemaisapp.ui.main.home.HomeFragment
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesFragment
import com.diegobezerra.cinemaisapp.ui.settings.SettingsActivity
import com.diegobezerra.cinemaisapp.util.switchToAdded
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {

        const val FRAGMENT_ID = R.id.fragment_container

    }

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private var fragment: MainFragment? = null
    private val fragments: MutableList<MainFragment> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()

        if (savedInstanceState == null) {
            showFragment(HomeFragment.TAG)
        } else {
            fragment = supportFragmentManager.findFragmentById(FRAGMENT_ID) as MainFragment
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                SettingsActivity.startActivity(this)
                true
            }
            R.id.about -> {
                AboutActivity.startActivity(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        if (fragment is MainFragment && !fragments.contains(fragment)) {
            Timber.d("Attaching fragment: %s", fragment.tag)
            fragments.add(fragment)
        }

        Timber.d("Fragments size: %d", fragments.size)
    }

    private fun setupViews() {
        findViewById<BottomNavigationView>(R.id.bottom_nav).apply {
            itemIconTintList = null
            setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.home -> {
                        showFragment(HomeFragment.TAG)
                        true
                    }
                    R.id.movies -> {
                        showFragment(MoviesFragment.TAG)
                        true
                    }
                    R.id.theaters -> {
                        handleTheatersAction()
                        true
                    }
                    else -> false
                }
            }
            // NOTE: No-op. Added just to prevent setOnNavigationItemSelectedListener being called
            // whenever we reselect an item
            setOnNavigationItemReselectedListener {}
        }
    }

    private fun handleTheatersAction() {
        preferencesHelper.getSelectedCinemaId()?.let {
            showFragment(CinemaFragment.TAG, args = CinemaFragment.newArgs(it)) {
                CinemaFragment.newInstance(it)
            }
        } ?: showFragment(CinemasFragment.TAG)
    }

    fun showCinemaFragment(cinemaId: Int) {
        showFragment(CinemaFragment.TAG, args = CinemaFragment.newArgs(cinemaId)) {
            CinemaFragment.newInstance(cinemaId)
        }
    }

    private fun showFragment(
        tag: String,
        args: Bundle = Bundle.EMPTY,
        init: () -> MainFragment? = { getFragmentByTag(tag) }
    ) {
        supportFragmentManager.beginTransaction().run {
            setCustomAnimations(
                R.animator.fade_in,
                R.animator.fade_out,
                R.animator.fade_in,
                R.animator.fade_out,
            )
            switchToAdded(tag, fragments)?.also {
                if (it.arguments != args) {
                    it.arguments = args
                }
                fragment = it as MainFragment
            }
            // If the fragment was never added then add it
            if (supportFragmentManager.findFragmentByTag(tag) == null) {
                init()?.let { frag ->
                    add(FRAGMENT_ID, frag, tag)
                    fragment = frag
                }
            }
            commit()
        }
    }

    private fun getFragmentByTag(tag: String): MainFragment? {
        for (f in fragments) {
            if (f.tag == tag) {
                return f
            }
        }
        return when (tag) {
            HomeFragment.TAG -> HomeFragment()
            MoviesFragment.TAG -> MoviesFragment()
            CinemasFragment.TAG -> CinemasFragment()
            else -> null
        }
    }

    override fun onBackPressed() {
        if (fragment is CinemaFragment) {
            preferencesHelper.saveSelectedCinemaId(0)
            showFragment(CinemasFragment.TAG)
        } else {
            super.onBackPressed()
        }
    }
}