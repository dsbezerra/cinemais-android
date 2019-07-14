package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.diegobezerra.cinemaisapp.R

class MovieInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val labelColor by lazy {
        ContextCompat.getColor(context, R.color.primary_text)
    }

    var label: String? = null
        set(value) {
            if (field != value) {
                field = value
                update()
            }
        }

    var content: String? = null
        set(value) {
            if (field != value) {
                field = value
                update()
            }
        }

    private val contentView: TextView by lazy { findViewById<TextView>(R.id.content) }

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.movie_info_view, this, true)

        val arr = context.obtainStyledAttributes(
            attrs, R.styleable.MovieInfoView,
            defStyleAttr, 0
        )
        label = arr.getString(R.styleable.MovieInfoView_miLabel)
        content = arr.getString(R.styleable.MovieInfoView_miContent)
        arr.recycle()
    }

    private fun update() {
        val string = SpannableString("$label   $content").apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                label?.length!!,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(labelColor),
                0,
                label?.length!!,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        contentView.text = string
    }

    fun setContentOrHideIfEmpty(newContent: String) {
        if (content != newContent) {
            content = newContent
        }
        isGone = newContent.isEmpty()
    }
}