package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.diegobezerra.cinemaisapp.R
import kotlinx.android.synthetic.main.empty_view.view.text_message

class EmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.empty_view, this)
        setFromAttrs(attrs, defStyleAttr)
    }

    /**
     * Set the empty view data obtained from attributes
     * @param attrs The attributes of the XML tag that is inflating the view. This value may be null.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style
     * resource that supplies default values for the view. Can be 0 to not look for defaults.
     */
    private fun setFromAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.EmptyView, defStyleAttr, 0).run {
            set(getString(R.styleable.EmptyView_emptyMessage))
            recycle()
        }
    }

    /**
     * Set the empty view data
     * @param message message of error view
     */
    fun set(message: String?) {
        text_message.text = message
    }

    /**
     * Clears the empty view data
     */
    fun clear() {
        set(null)
    }
}