package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.diegobezerra.cinemaisapp.R

class CinemaActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val labelView: TextView by lazy { findViewById<TextView>(R.id.label) }
    private val iconView: ImageView by lazy { findViewById<ImageView>(R.id.icon) }
    private val iconBackground: FrameLayout by lazy { findViewById<FrameLayout>(R.id.bg_target) }

    init {
        val arr = context.obtainStyledAttributes(
            attrs, R.styleable.CinemaActionView,
            defStyleAttr, 0
        )
        val label = arr.getString(R.styleable.CinemaActionView_caLabel)
        val icon = arr.getDrawable(R.styleable.CinemaActionView_caIcon)
        val color = arr.getColor(
            R.styleable.CinemaActionView_caColor,
            ContextCompat.getColor(context, R.color.secondary_text)
        )
        arr.recycle()

        LayoutInflater.from(context)
            .inflate(R.layout.cinema_action_view, this, true)

        DrawableCompat.setTint(iconBackground.background, color)
        labelView.text = label
        iconView.setImageDrawable(icon)
    }

    fun getRevealOptions(): IntArray {
        val outLocation = intArrayOf(0, 0)
        iconBackground.getLocationOnScreen(outLocation)

        val halfWidth = iconBackground.width / 2
        outLocation[0] += halfWidth
        outLocation[1] += halfWidth

        return intArrayOf(outLocation[0], outLocation[1], halfWidth)
    }
}