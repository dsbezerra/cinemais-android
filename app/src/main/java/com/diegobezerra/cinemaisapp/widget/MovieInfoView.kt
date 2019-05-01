package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.diegobezerra.cinemaisapp.R

class MovieInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val labelView: TextView by lazy { findViewById<TextView>(R.id.label) }
    private val contentView: TextView by lazy { findViewById<TextView>(R.id.content) }

    init {
        val arr = context.obtainStyledAttributes(attrs, R.styleable.MovieInfoView,
            defStyleAttr, 0)
        val label = arr.getString(R.styleable.MovieInfoView_infoLabel)
        val content = arr.getString(R.styleable.MovieInfoView_infoContent)
        arr.recycle()

        LayoutInflater.from(context)
            .inflate(R.layout.movie_info_view, this, true)

        labelView.text = label
        contentView.text = content
    }

    fun setLabel(label: String) {
        labelView.text = label
    }

    fun setContent(content: String) {
        contentView.text = content
    }
}