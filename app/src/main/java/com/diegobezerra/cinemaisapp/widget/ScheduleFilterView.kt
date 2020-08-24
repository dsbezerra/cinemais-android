package com.diegobezerra.cinemaisapp.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AnimationUtils
import android.widget.Checkable
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withTranslation
import com.diegobezerra.cinemaisapp.R

/**
 * A custom view for displaying filters. Allows a custom presentation of the tag color and selection
 * state.
 *
 * Adapted from Google I/O 2018 source.
 */
class ScheduleFilterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Checkable {

    var selectedTextColor: Int? = null

    var text: CharSequence? = null
        set(value) {
            field = value
            requestLayout()
        }

    private var progress = 0f
        set(value) {
            if (field != value) {
                field = value
                postInvalidateOnAnimation()
            }
        }

    private val defaultColors = intArrayOf(
        ContextCompat.getColor(context, R.color.cinemais_green),
        ContextCompat.getColor(context, R.color.cinemais_blue)
    )
    private val defaultPositions = floatArrayOf(0f, 1f)

    private val color by lazy {
        LinearGradient(
            -(width.toFloat() / 2f),
            0f,
            width.toFloat() + width.toFloat() / 2f,
            0f,
            defaultColors,
            defaultPositions,
            CLAMP
        )
    }

    private var lastX: Float = 0f

    private val padding: Int

    private val outlinePaint: Paint

    private val textPaint: TextPaint

    private val backgroundPaint: Paint

    private val touchFeedback: Drawable

    private lateinit var textLayout: StaticLayout

    private var progressAnimator: ValueAnimator? = null

    private val interp =
        AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_slow_in)

    @ColorInt
    private val defaultTextColor: Int

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ScheduleFilterView,
            R.attr.scheduleFilterViewStyle,
            R.style.Widget_Cinemais_ScheduleFilters
        )
        outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = a.getColorOrThrow(R.styleable.ScheduleFilterView_android_strokeColor)
            strokeWidth = a.getDimensionOrThrow(R.styleable.ScheduleFilterView_outlineWidth)
            style = STROKE
        }
        defaultTextColor = a.getColorOrThrow(R.styleable.ScheduleFilterView_android_textColor)
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = defaultTextColor
            textSize = a.getDimensionOrThrow(R.styleable.ScheduleFilterView_android_textSize)
        }
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        touchFeedback = a.getDrawableOrThrow(R.styleable.ScheduleFilterView_foreground).apply {
            callback = this@ScheduleFilterView
        }
        padding = a.getDimensionPixelSizeOrThrow(R.styleable.ScheduleFilterView_android_padding)
        isChecked = a.getBoolean(R.styleable.ScheduleFilterView_android_checked, false)
        a.recycle()
        clipToOutline = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val nonTextWidth = (4 * padding) +
            (2 * outlinePaint.strokeWidth).toInt()
        val availableTextWidth = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec) - nonTextWidth
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(widthMeasureSpec) - nonTextWidth
            MeasureSpec.UNSPECIFIED -> Int.MAX_VALUE
            else -> Int.MAX_VALUE
        }
        createLayout(availableTextWidth)
        val w = nonTextWidth + textWidth(textLayout)
        val h = padding + textLayout.height + padding
        setMeasuredDimension(w, h)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, w, h, h / 2f)
            }
        }
        touchFeedback.setBounds(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        val strokeWidth = outlinePaint.strokeWidth
        val halfStroke = strokeWidth / 2f
        val rounding = (height - strokeWidth) / 2f

        // Outline
        if (progress < 1f) {
            canvas.drawRoundRect(
                halfStroke,
                halfStroke,
                width - halfStroke,
                height - halfStroke,
                rounding,
                rounding,
                outlinePaint
            )
        }

        if (backgroundPaint.shader == null) {
            backgroundPaint.shader = color
        }
        val radius = lerp(0f, width.toFloat(), progress)
        canvas.drawCircle(lastX, height / 2f, radius, backgroundPaint)

        // Text
        val textX = strokeWidth + padding * 2f
        val selectedColor = selectedTextColor
        textPaint.color = if (selectedColor != null && selectedColor != 0 && progress > 0) {
            ColorUtils.blendARGB(defaultTextColor, selectedColor, progress)
        } else {
            defaultTextColor
        }
        canvas.withTranslation(x = textX, y = (height - textLayout.height) / 2f) {
            textLayout.draw(canvas)
        }
        // Touch feedback
        touchFeedback.draw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                super.onTouchEvent(event)
            }
            else -> super.onTouchEvent(event)
        }
    }

    /**
     * Starts the animation to enable/disable a filter and invokes a function when done.
     */
    fun animateCheckedAndInvoke(checked: Boolean, onEnd: (() -> Unit)?) {
        val newProgress = if (checked) 1f else 0f
        if (newProgress != progress) {
            progressAnimator?.cancel()
            progressAnimator = ValueAnimator.ofFloat(progress, newProgress).apply {
                addUpdateListener {
                    progress = it.animatedValue as Float
                }
                doOnEnd {
                    progress = newProgress
                    onEnd?.invoke()
                }
                interpolator = interp
                duration = if (checked) SELECTING_DURATION else DESELECTING_DURATION
                start()
            }
        }
    }

    override fun isChecked() = progress == 1f

    override fun toggle() {
        progress = if (progress == 0f) 1f else 0f
    }

    override fun setChecked(checked: Boolean) {
        progress = if (checked) 1f else 0f
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == touchFeedback
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        touchFeedback.state = drawableState
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        touchFeedback.jumpToCurrentState()
    }

    override fun drawableHotspotChanged(x: Float, y: Float) {
        super.drawableHotspotChanged(x, y)
        touchFeedback.setHotspot(x, y)
    }

    @Suppress("DEPRECATION")
    private fun createLayout(textWidth: Int) {
        textLayout = if (VERSION.SDK_INT >= VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text!!, 0, text?.length!!, textPaint, textWidth).build()
        } else {
            StaticLayout(text, textPaint, textWidth, ALIGN_NORMAL, 1f, 0f, true)
        }
    }

    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    private fun textWidth(layout: StaticLayout): Int {
        var width = 0f
        for (i in 0 until layout.lineCount) {
            width = width.coerceAtLeast(layout.getLineWidth(i))
        }
        return width.toInt()
    }

    companion object {
        private const val SELECTING_DURATION = 350L
        private const val DESELECTING_DURATION = 200L
    }
}
