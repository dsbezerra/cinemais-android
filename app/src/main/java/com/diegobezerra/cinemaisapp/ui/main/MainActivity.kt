package com.diegobezerra.cinemaisapp.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.diegobezerra.cinemaisapp.BuildConfig
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.ui.about.AboutActivity
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaFragment
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasFragment
import com.diegobezerra.cinemaisapp.ui.main.home.HomeFragment
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesFragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    companion object {
        const val FRAGMENT_ID = R.id.fragment_container
    }

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private lateinit var fragment: Fragment
    private lateinit var interstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNav()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(FRAGMENT_ID, HomeFragment().also {
                    fragment = it
                })
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentById(FRAGMENT_ID) as Fragment
        }

        setupAds()
    }

    override fun onResume() {
        super.onResume()

        if (interstitialAd.isLoaded) {
            interstitialAd.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.about -> {
                AboutActivity.startActivity(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBottomNav() {
        findViewById<BottomNavigationView>(R.id.bottom_nav)?.let {
            it.itemIconTintList = null
            it.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.home -> {
                        switchFragment(HomeFragment())
                        true
                    }
                    R.id.movies -> {
                        switchFragment(MoviesFragment())
                        true
                    }
                    R.id.theaters -> {
                        preferencesHelper.getSelectedCinemaId().apply {
                            if (this != null) {
                                switchToCinemaFragment(this)
                            } else {
                                switchFragment(CinemasFragment())
                            }
                        }
                        true
                    }
                    else -> false
                }
            }
            // NOTE: No-op. Added just to prevent above listener being called
            // whenever we reselect an item
            it.setOnNavigationItemReselectedListener {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        interstitialAd.adListener = null
    }

    private fun setupAds() {
        interstitialAd = InterstitialAd(applicationContext)
        interstitialAd.adUnitId = BuildConfig.INTERSTITIAL
        interstitialAd.loadAd(AdRequest.Builder().build())
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
    }

    private fun switchFragment(to: Fragment) {
        supportFragmentManager.beginTransaction().run {
            replace(FRAGMENT_ID, to.also {
                fragment = it
            })
        }.commit()
    }

    fun switchToCinemaFragment(cinemaId: Int) {
        switchFragment(CinemaFragment.newInstance(cinemaId))
    }

    override fun onBackPressed() {
        if (fragment is CinemaFragment) {
            preferencesHelper.setSelectedCinemaId(0)
            switchFragment(CinemasFragment())
        } else {
            super.onBackPressed()
        }
    }
}
