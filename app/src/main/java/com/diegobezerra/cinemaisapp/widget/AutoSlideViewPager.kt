package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager
import com.diegobezerra.cinemaisapp.R

class AutoSlideViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewPager(context, attrs) {

    companion object {

        const val DEFAULT_SLIDE_INTERVAL = 5000

    }

    private var isIntervalActive = false
    private var isUserInteracting = false

    private var previousState = SCROLL_STATE_IDLE
    private var lastPosition = 0
    private var interval: Long = 0L
    private var runnable: Runnable? = null

    private val defaultPageChangeListener = object : SimpleOnPageChangeListener() {
        override fun onPageScrollStateChanged(state: Int) {
            isUserInteracting = state == SCROLL_STATE_DRAGGING

            if (previousState == SCROLL_STATE_SETTLING &&
                state == SCROLL_STATE_IDLE && !isIntervalActive) {
                startInterval(false)
            }

            previousState = state
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            if (lastPosition != position && !isUserInteracting) {
                resetInterval()
            } else if (isIntervalActive && isUserInteracting) {
                stopInterval()
            }
            lastPosition = position
        }
    }

    init {
        val arr =
            context.obtainStyledAttributes(attrs, R.styleable.AutoSlideViewPager, defStyleAttr, 0)
        interval =
            arr.getInt(R.styleable.AutoSlideViewPager_slideInterval, DEFAULT_SLIDE_INTERVAL)
                .toLong()
        arr.recycle()

        addOnPageChangeListener(defaultPageChangeListener)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isIntervalActive) {
            startInterval(true)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (isIntervalActive) {
            stopInterval()
        }
    }

    private fun startInterval(firstLayout: Boolean) {
        adapter ?: return

        // Make sure we get our smooth scroll.
        if (firstLayout) {
            requestLayout()
        }

        runnable = Runnable {
            adapter?.let {
                val nextItem = if (currentItem + 1 < it.count) {
                    currentItem + 1
                } else {
                    0
                }
                setCurrentItem(nextItem, true)
                if (isIntervalActive) {
                    handler.postDelayed(runnable, interval)
                }
            }
        }
        isIntervalActive = true
        handler.postDelayed(runnable, interval)
    }

    private fun resetInterval() {
        stopInterval()
        startInterval(false)
    }

    private fun stopInterval() {
        isIntervalActive = false
        handler?.removeCallbacks(runnable)
    }

}