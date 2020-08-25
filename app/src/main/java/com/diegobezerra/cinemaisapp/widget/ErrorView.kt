package com.diegobezerra.cinemaisapp.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.shared.result.Error
import com.diegobezerra.shared.result.Error.Data
import com.diegobezerra.shared.result.Error.Network
import com.diegobezerra.shared.result.Error.NoConnection
import com.diegobezerra.shared.result.Error.Timeout
import kotlinx.android.synthetic.main.error_view.view.image_view
import kotlinx.android.synthetic.main.error_view.view.text_message
import kotlinx.android.synthetic.main.error_view.view.text_title

class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.error_view, this)
        setFromAttrs(attrs, defStyleAttr)
    }

    /**
     * Set the empty view data obtained from attributes
     * @param attrs The attributes of the XML tag that is inflating the view. This value may be null.
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style
     * resource that supplies default values for the view. Can be 0 to not look for defaults.
     */
    private fun setFromAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.ErrorView, defStyleAttr, 0).run {
            set(
                getDrawable(R.styleable.ErrorView_errorIcon),
                getString(R.styleable.ErrorView_errorTitle),
                getString(R.styleable.ErrorView_errorMessage)
            )
            if (hasValue(R.styleable.ErrorView_errorIconTint)) {
                setIconTint(getColor(R.styleable.ErrorView_errorIconTint, 0))
            }
            recycle()
        }
    }

    fun setIconTint(color: Int) {
        if (color == 0) {
            image_view.colorFilter = null
        } else {
            image_view.setColorFilter(ContextCompat.getColor(context, color))
        }
    }

    /**
     * Set the error view data
     * @param drawable icon of error view
     * @param title title of error view
     * @param message message of error view
     */
    fun set(drawable: Drawable?, title: String?, message: String?) {
        image_view.setImageDrawable(drawable)
        text_title.text = title
        text_message.text = message
    }

    /**
     * Clears the error view data
     */
    fun clear() {
        set(null, null, null)
    }
}

@BindingAdapter("error")
fun ErrorView.setError(error: Error?) {
    if (error == null) {
        clear()
        return
    }

    val res = resources
    val data = when (error) {
        is Network -> {
            Data(
                R.drawable.ic_error_network,
                res.getString(R.string.error_title_network),
                res.getString(R.string.error_message_network)
            )
        }
        is Timeout -> {
            Data(
                R.drawable.ic_error_network,
                res.getString(R.string.error_title_timeout),
                res.getString(R.string.error_message_timeout)
            )
        }
        is NoConnection -> {
            Data(
                R.drawable.ic_error_network,
                res.getString(R.string.error_title_no_connection),
                res.getString(R.string.error_message_no_connection)
            )
        }
        else -> {
            Data(
                R.drawable.ic_error_default,
                res.getString(R.string.error_title_unknown),
                res.getString(R.string.error_message_unknown)
            )
        }
    }
    set(ContextCompat.getDrawable(context, data.icon), data.title, data.message)
}