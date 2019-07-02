package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.diegobezerra.cinemaisapp.R

class CinemaisSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    companion object {

        private val PROGRESS_BACKGROUND_ATTR = intArrayOf(R.attr.background_color)
        private val PROGRESS_COLORS =
            intArrayOf(R.color.cinemais_green, R.color.cinemais_blue, R.color.cinemais_pink)

    }

    init {
        setProgressBackgroundColorSchemeColor(
            context.obtainStyledAttributes(PROGRESS_BACKGROUND_ATTR).run {
                val defaultColor = ContextCompat.getColor(context, R.color.background)
                val bgColor = getColor(0, defaultColor)
                recycle()
                bgColor
            })
        setColorSchemeResources(*PROGRESS_COLORS)
    }
}