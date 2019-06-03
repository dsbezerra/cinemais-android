package com.diegobezerra.cinemaisapp.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.diegobezerra.cinemaisapp.BuildConfig
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.ui.BaseActivity
import com.diegobezerra.cinemaisapp.ui.about.AboutActivity
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaFragment
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasFragment
import com.diegobezerra.cinemaisapp.ui.main.home.HomeFragment
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesFragment
import com.diegobezerra.cinemaisapp.ui.settings.SettingsActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.bottomnavigation.BottomNavigationView
import javax.inject.Inject

class MainActivity : BaseActivity() {

    companion object {

        const val FRAGMENT_ID = R.id.fragment_container

        const val INTERSTITIAL_INTERVAL = 1000 * 60 * 2

    }

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private lateinit var fragment: Fragment
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(FRAGMENT_ID, HomeFragment().also {
                    fragment = it
                })
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentById(FRAGMENT_ID) as Fragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        interstitialAd?.adListener = null
    }

    override fun onResume() {
        super.onResume()

        val lastDisplayTimestamp = preferencesHelper.getInterstitialLastDisplayTimestamp()
        interstitialAd?.let {
            if (it.isLoaded && System.currentTimeMillis() - lastDisplayTimestamp > INTERSTITIAL_INTERVAL) {
                interstitialAd?.show()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
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

    private fun setupViews() {
        val navContainer = findViewById<LinearLayout>(R.id.nav_container)
        findViewById<BottomNavigationView>(R.id.bottom_nav).apply {
            itemIconTintList = null
            setOnNavigationItemSelectedListener { menuItem ->
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
            // NOTE: No-op. Added just to prevent setOnNavigationItemSelectedListener being called
            // whenever we reselect an item
            setOnNavigationItemReselectedListener {}
        }
        // TODO: Add check for ads enabled or disabled
        AdView(this).apply {
            adSize = AdSize.SMART_BANNER
            adUnitId = BuildConfig.BANNER
            if (navContainer.childCount == 2) {
                navContainer.addView(this, 0)
            }
            loadAd(AdRequest.Builder().build())
        }

        interstitialAd = InterstitialAd(applicationContext).apply {
            adUnitId = BuildConfig.INTERSTITIAL
            adListener = object : AdListener() {
                override fun onAdClosed() {
                    preferencesHelper.setInterstitialLastDisplayTimestamp(System.currentTimeMillis())
                    interstitialAd?.loadAd(AdRequest.Builder().build())
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    private fun switchFragment(to: Fragment) {
        supportFragmentManager.beginTransaction().run {
            if (fragment is CinemasFragment && to is CinemaFragment) {
                setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
            } else if (fragment is CinemaFragment && to is CinemasFragment) {
                setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            }
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
