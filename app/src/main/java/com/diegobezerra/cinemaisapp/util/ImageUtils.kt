package com.diegobezerra.cinemaisapp.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.GlideOptions
import com.diegobezerra.cinemaisapp.GlideOptions.bitmapTransform
import com.diegobezerra.cinemaisapp.R

class ImageUtils {

    companion object {

        @Volatile
        private var PLACEHOLDER: Drawable? = null

        @Volatile
        private var POSTER_PLACEHOLDER: Drawable? = null

        @Volatile
        private var POSTER_OPTIONS: GlideOptions? = null

        @Volatile
        private var DEFAULT_TRANSITION: DrawableTransitionOptions? = null

        fun getPlaceholder(context: Context): Drawable =
            PLACEHOLDER ?: synchronized(this) {
                PLACEHOLDER ?: buildPlaceholder(context).also {
                    PLACEHOLDER = it
                }
            }

        fun getPosterPlaceholder(context: Context): Drawable =
            POSTER_PLACEHOLDER ?: synchronized(this) {
                POSTER_PLACEHOLDER ?: buildPosterPlaceholder(context).also {
                    POSTER_PLACEHOLDER = it
                }
            }

        fun getPosterOptions(context: Context): GlideOptions =
            POSTER_OPTIONS ?: synchronized(this) {
                POSTER_OPTIONS ?: buildPosterOptions(context).also {
                    POSTER_OPTIONS = it
                }
            }

        fun getDefaultTransition(): DrawableTransitionOptions =
            DEFAULT_TRANSITION ?: synchronized(this) {
                DEFAULT_TRANSITION ?: buildDefaultTransition().also {
                    DEFAULT_TRANSITION = it
                }
            }

        /**
         * Load poster utility.
         */
        fun loadPoster(src: String, target: ImageView) {
            GlideApp.with(target.context)
                .load(src)
                .transition(getDefaultTransition())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(getPosterPlaceholder(target.context))
                .apply(getPosterOptions(target.context))
                .into(target)
        }

        private fun buildPosterPlaceholder(context: Context): Drawable {
            return ContextCompat.getDrawable(context, R.drawable.poster_placeholder)
                ?: getPlaceholder(context)
        }

        private fun buildPlaceholder(context: Context): ColorDrawable {
            val a = context.obtainStyledAttributes(intArrayOf(R.attr.placeholder_color))
            val color = a.getColor(0, ContextCompat.getColor(context, R.color.placeholder))
            a.recycle()
            return ColorDrawable(color)
        }

        private fun buildPosterOptions(context: Context) =
            bitmapTransform(RoundedCorners(context.resources.getDimension(R.dimen.movie_poster_radius).toInt()))

        private fun buildDefaultTransition() =
            withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build())

    }
}
