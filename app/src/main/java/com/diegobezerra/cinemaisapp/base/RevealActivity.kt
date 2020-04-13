package com.diegobezerra.cinemaisapp.base

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager
import androidx.core.animation.doOnEnd
import com.diegobezerra.cinemaisapp.R
import dagger.android.support.DaggerAppCompatActivity
import kotlin.math.hypot

abstract class RevealActivity : DaggerAppCompatActivity() {

    companion object {
        const val EXTRA_REVEAL_X = "extra.REVEAL_X"
        const val EXTRA_REVEAL_Y = "extra.REVEAL_Y"
        const val EXTRA_REVEAL_START_RADIUS = "extra.REVEAL_START_RADIUS"

        fun makeReveal(options: IntArray, extras: Bundle?): Bundle {
            if (options.size != 3) {
                throw IllegalStateException("options must have 3 properties")
            }
            return makeReveal(options[0], options[1], options[2], extras)
        }

        fun makeReveal(x: Int, y: Int, startRadius: Int, extras: Bundle?): Bundle {
            return Bundle().apply {
                putInt(EXTRA_REVEAL_X, x)
                putInt(EXTRA_REVEAL_Y, y)
                putInt(EXTRA_REVEAL_START_RADIUS, startRadius)
                if (extras != null) {
                    putAll(extras)
                }
            }
        }
    }

    private var root: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBeforeSetup(savedInstanceState)
        setup()
    }

    private fun setup() {
        root = findViewById(R.id.root) ?: throw IllegalStateException("missing root view")

        setupWindow()
        root?.addOnLayoutChangeListener(
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
        val background = root?.background
        if (background is ColorDrawable) {
            window.statusBarColor = background.color
        }
    }

    private fun reveal() {
        findViewById<View>(R.id.root)?.let {
            val centerX = intent.getIntExtra(EXTRA_REVEAL_X, 0)
            val centerY = intent.getIntExtra(EXTRA_REVEAL_Y, 0)
            val startRadius = intent.getIntExtra(EXTRA_REVEAL_START_RADIUS, 0)
                .toFloat()
            if (centerX != 0 && centerY != 0) {
                val endRadius =
                    hypot(it.width.toDouble(), it.height.toDouble()).toFloat()
                ViewAnimationUtils.createCircularReveal(
                    it,
                    centerX,
                    centerY,
                    startRadius,
                    endRadius
                ).apply {
                    it.visibility = View.VISIBLE
                    start()
                }
            }
        }
    }

    private fun unreveal() {
        findViewById<View>(R.id.root)?.let {
            val centerX = intent.getIntExtra(EXTRA_REVEAL_X, 0)
            val centerY = intent.getIntExtra(EXTRA_REVEAL_Y, 0)
            val startRadius = intent.getIntExtra(EXTRA_REVEAL_START_RADIUS, 0)
                .toFloat()
            if (centerX != 0 && centerY != 0) {
                val endRadius =
                    hypot(it.width.toDouble(), it.height.toDouble()).toFloat()
                ViewAnimationUtils.createCircularReveal(
                    it,
                    centerX,
                    centerY,
                    endRadius,
                    startRadius
                ).apply {
                    doOnEnd { _ ->
                        it.visibility = View.INVISIBLE
                        finishAfterTransition()
                    }
                    start()
                }
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

    abstract fun onBeforeSetup(savedInstanceState: Bundle?)
}