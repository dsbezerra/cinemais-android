package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.diegobezerra.cinemaisapp.R

class CinemaisSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    companion object {

        private val PROGRESS_COLORS =
            intArrayOf(R.color.cinemais_green, R.color.cinemais_blue, R.color.cinemais_pink)

    }

    init {
        setProgressBackgroundColorSchemeResource(R.color.background)
        setColorSchemeResources(*PROGRESS_COLORS)
    }
}