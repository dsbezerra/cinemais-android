package com.diegobezerra.cinemaisapp.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.diegobezerra.cinemaisapp.BuildConfig
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.base.BaseActivity
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.tasks.CheckPremieresWorker
import com.diegobezerra.cinemaisapp.ui.about.AboutActivity
import com.diegobezerra.cinemaisapp.ui.cinema.CinemaFragment
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasFragment
import com.diegobezerra.cinemaisapp.ui.main.home.HomeFragment
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesFragment
import com.diegobezerra.cinemaisapp.ui.settings.SettingsActivity
import com.diegobezerra.cinemaisapp.util.switchToAdded
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest.Builder
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseActivity() {

    companion object {

        const val FRAGMENT_ID = R.id.fragment_container

        const val INTERSTITIAL_INTERVAL = 1000 * 60 * 2

    }

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private var fragment: MainFragment? = null
    private val fragments: MutableList<MainFragment> = mutableListOf()

    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        setupWorkers()
        if (savedInstanceState == null) {
            showFragment(HomeFragment.TAG)
        } else {
            fragment = supportFragmentManager.findFragmentById(FRAGMENT_ID) as MainFragment
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

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        if (fragment is MainFragment && !fragments.contains(fragment)) {
            Timber.d("Attaching fragment: %s", fragment.tag)
            fragments.add(fragment)
        }

        Timber.d("Fragments size: %d", fragments.size)
    }

    private fun setupViews() {
        val navContainer = findViewById<LinearLayout>(R.id.nav_container)
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

        // TODO: Add check for ads enabled or disabled
        setupAds(navContainer)
    }

    private fun setupWorkers() {
        if (!preferencesHelper.isCheckPremieresScheduled()) {
            CheckPremieresWorker.scheduleToNextThursday(this)
            preferencesHelper.setCheckPremieresScheduled(true)
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

    private fun setupAds(navContainer: LinearLayout) {
        AdView(this).apply {
            adSize = AdSize.SMART_BANNER
            adUnitId = BuildConfig.BANNER
            if (navContainer.childCount == 2) {
                navContainer.addView(this, 0)
            }
            loadAd(Builder().build())
        }

        interstitialAd = InterstitialAd(applicationContext).apply {
            adUnitId = BuildConfig.INTERSTITIAL
            adListener = object : AdListener() {
                override fun onAdClosed() {
                    preferencesHelper.setInterstitialLastDisplayTimestamp(System.currentTimeMillis())
                    interstitialAd?.loadAd(Builder().build())
                }
            }
            loadAd(Builder().build())
        }
    }

    private fun showFragment(
        tag: String,
        args: Bundle = Bundle.EMPTY,
        init: () -> MainFragment? = { getFragmentByTag(tag) }
    ) {
        supportFragmentManager.beginTransaction().run {

            fragment?.transition(this, tag)

            switchToAdded(tag, fragments)?.also {
                if (it.arguments != args) {
                    it.arguments = args
                }
                fragment = it as MainFragment
            }

            // If the fragment was never added then add it
            if (supportFragmentManager.findFragmentByTag(tag) == null) {
                init()?.let { frag ->
                    val ft = this
                    add(FRAGMENT_ID, frag.apply {
                        transition(ft, tag)
                    }, tag)
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
            preferencesHelper.setSelectedCinemaId(0)
            showFragment(CinemasFragment.TAG)
        } else {
            super.onBackPressed()
        }
    }
}