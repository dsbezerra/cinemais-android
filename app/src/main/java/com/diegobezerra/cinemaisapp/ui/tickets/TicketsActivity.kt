package com.diegobezerra.cinemaisapp.ui.tickets

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.util.setupActionBar
import dagger.android.support.DaggerAppCompatActivity

class TicketsActivity : DaggerAppCompatActivity() {

    companion object {

        const val EXTRA_REVEAL_X = "extra.REVEAL_X"
        const val EXTRA_REVEAL_Y = "extra.REVEAL_Y"
        const val EXTRA_REVEAL_START_RADIUS = "extra.REVEAL_START_RADIUS"
        const val EXTRA_CINEMA_ID = "extra.CINEMA_ID"

        const val FRAGMENT_ID = R.id.fragment_container
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

        setup()
    }

    private fun setup() {
        setupWindow()
        findViewById<View>(R.id.root).addOnLayoutChangeListener(
            object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    v?.let {
                        it.removeOnLayoutChangeListener(this)
                        reveal()
                    }
                }
            }
        )
    }

    private fun setupWindow() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.alt_green)
    }

    private fun reveal() {
        val view = findViewById<View>(R.id.root)
        val centerX = intent.getIntExtra(EXTRA_REVEAL_X, 0)
        val centerY = intent.getIntExtra(EXTRA_REVEAL_Y, 0)
        val startRadius = intent.getIntExtra(EXTRA_REVEAL_START_RADIUS, 0)
            .toFloat()
        if (centerX != 0 && centerY != 0) {
            val endRadius =
                Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
            ViewAnimationUtils.createCircularReveal(
                view,
                centerX,
                centerY,
                startRadius,
                endRadius
            )
                .apply {
                    view.visibility = View.VISIBLE
                    start()
                }
        }
    }

    private fun unreveal() {
        val view = findViewById<View>(R.id.root)
        val centerX = intent.getIntExtra(EXTRA_REVEAL_X, 0)
        val centerY = intent.getIntExtra(EXTRA_REVEAL_Y, 0)
        val startRadius = intent.getIntExtra(EXTRA_REVEAL_START_RADIUS, 0)
            .toFloat()
        if (centerX != 0 && centerY != 0) {
            val endRadius =
                Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
            ViewAnimationUtils.createCircularReveal(
                view,
                centerX,
                centerY,
                endRadius,
                startRadius
            )
                .apply {
                    doOnEnd {
                        view.visibility = View.INVISIBLE
                        finishAfterTransition()
                    }
                    start()
                }
        }
    }

    override fun onBackPressed() {
        unreveal()
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
