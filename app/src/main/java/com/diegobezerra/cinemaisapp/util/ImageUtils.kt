package com.diegobezerra.cinemaisapp.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.diegobezerra.cinemaisapp.R

class ImageUtils {

    companion object {

        fun placeholder(context: Context): ColorDrawable {
            val a = context.obtainStyledAttributes(intArrayOf(R.attr.placeholder_color))
            val color = a.getColor(0, ContextCompat.getColor(context, R.color.placeholder))
            a.recycle()
            return ColorDrawable(color)
        }

        fun posterTransformation(context: Context) =
            RoundedCorners(context.resources.getDimension(R.dimen.spacing_small).toInt())

    }
}