package com.diegobezerra.cinemaisapp.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import com.diegobezerra.cinemaisapp.R

class ImageUtils {

    companion object {

        fun placeholder(context: Context): ColorDrawable = ColorDrawable(
            ContextCompat.getColor(context, R.color.placeholder)
        )
    }
}