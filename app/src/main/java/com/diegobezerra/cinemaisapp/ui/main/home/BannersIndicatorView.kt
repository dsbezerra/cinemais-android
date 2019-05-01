package com.diegobezerra.cinemaisapp.ui.main.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import timber.log.Timber

class BannersIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(
    context, attrs, defStyleAttr
), ViewPager.OnPageChangeListener {

    companion object {
        const val GREEN = 0xFFCBDB2A
        const val BLUE = 0xFF0295FF
        const val PINK = 0xFFFF00C7

        /**  TODO: Add these as styleable attrs **/
        const val DEFAULT_GAP = 10f
        const val DEFAULT_RADIUS = 5f
    }

    private var gradientPaint = Paint().apply {
        isAntiAlias = true
    }
    private val inactivePaint = Paint().apply {
        color = 0x32000000.toInt()
        isAntiAlias = true
    }

    private var itemCount = -1
    private var itemWidth = 0f
    private var currentLeft = 0f

    // NOTE: This was created just to avoid adding two page change listeners to the viewpager.
    private var pageSelectedListener: ((Int) -> Unit)? = null

    private val gradientShader by lazy {
        LinearGradient(
            0f, 0f, right.toFloat(), bottom.toFloat(),
            intArrayOf(GREEN.toInt(), BLUE.toInt(), PINK.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    var viewPager: ViewPager? = null
        set(value) {
            field = value
            ensureHasOnPageChangeListener()
            refreshValues()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (itemCount > 0) {
            val width = measuredWidth.toFloat()
            itemWidth = (width - DEFAULT_GAP * (itemCount - 1)) / itemCount
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!isViewPagerAttached()) return

        val saveCount = canvas.save()
        for (i in 0..itemCount) {
            canvas.drawRoundRect(
                0f,
                0f,
                itemWidth,
                bottom.toFloat(),
                DEFAULT_RADIUS,
                DEFAULT_RADIUS,
                inactivePaint
            )
            canvas.translate(itemWidth + DEFAULT_GAP, 0f)
        }
        canvas.restoreToCount(saveCount)

        if (gradientPaint.shader == null) {
            gradientPaint.shader = gradientShader
        }

        val current = viewPager?.currentItem!!
        val gap = DEFAULT_GAP * current
        val currentRight = currentLeft + itemWidth
        canvas.drawRoundRect(
            currentLeft + gap,
            0f,
            currentRight + gap,
            bottom.toFloat(),
            DEFAULT_RADIUS,
            DEFAULT_RADIUS,
            gradientPaint
        )
    }

    private fun refreshValues() {
        if (!isViewPagerAttached()) return

        viewPager?.run {
            itemCount = adapter?.count!!
        }

        invalidate()
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        onPageScroll(position, positionOffset)
    }

    override fun onPageSelected(position: Int) {}

    private fun isViewPagerAttached(): Boolean {
        return viewPager != null && viewPager?.adapter != null && viewPager?.adapter?.count!! > 0
    }

    private fun ensureHasOnPageChangeListener() {
        viewPager?.let {
            it.removeOnPageChangeListener(this)
            it.addOnPageChangeListener(this)
        }
    }

    private fun onPageScroll(position: Int, positionOffset: Float) {
        val currentIndicatorStart = position * itemWidth
        currentLeft = currentIndicatorStart + positionOffset * itemWidth
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        viewPager?.let {
            savedState.currentItem = it.currentItem
        }
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        viewPager?.apply {
            currentItem = state.currentItem
        }
    }

    internal class SavedState : BaseSavedState {
        var currentItem: Int = 0

        constructor(superState: Parcelable?) : super(superState) {}

        private constructor(`in`: Parcel) : super(`in`) {
            this.currentItem = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(this.currentItem)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}