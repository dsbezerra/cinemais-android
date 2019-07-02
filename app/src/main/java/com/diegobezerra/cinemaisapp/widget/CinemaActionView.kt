package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.diegobezerra.cinemaisapp.R
import kotlinx.android.synthetic.main.cinema_action_view.view.frame_layout
import kotlinx.android.synthetic.main.cinema_action_view.view.image_view
import kotlinx.android.synthetic.main.cinema_action_view.view.text_label

class CinemaActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.cinema_action_view, this)
        setFromAttrs(attrs, defStyleAttr)
    }

    /**
     * Set the cinema action view data obtained from attributes
     * @param attrs The attributes of the XML tag that is inflating the view. This value may be null.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style
     * resource that supplies default values for the view. Can be 0 to not look for defaults.
     */
    private fun setFromAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.CinemaActionView, defStyleAttr, 0).run {
            val fallbackBgColor = ContextCompat.getColor(context, R.color.secondary_text)
            set(
                getDrawable(R.styleable.CinemaActionView_caIcon),
                getString(R.styleable.CinemaActionView_caLabel),
                getColor(R.styleable.CinemaActionView_caColor, fallbackBgColor)
            )
            recycle()
        }
    }

    /**
     * Return reveal information used to perform a Circular Reveal
     * animation starting from circle's origin.
     */
    fun getRevealOptions(): IntArray {
        val outLocation = intArrayOf(0, 0)
        frame_layout.getLocationOnScreen(outLocation)

        val halfWidth = frame_layout.width / 2
        outLocation[0] += halfWidth
        outLocation[1] += halfWidth

        return intArrayOf(outLocation[0], outLocation[1], halfWidth)
    }

    /**
     * Set the cinema action view data
     * @param drawable icon of action view
     * @param label label of action view
     * @param color background color of action view
     */
    fun set(drawable: Drawable?, label: String?, color: Int) {
        image_view.setImageDrawable(drawable)
        text_label.text = label
        DrawableCompat.setTint(frame_layout.background, color)
    }
}