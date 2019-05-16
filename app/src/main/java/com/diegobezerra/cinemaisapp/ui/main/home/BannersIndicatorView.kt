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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import com.diegobezerra.cinemaisapp.R

class BannersIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(
    context, attrs, defStyleAttr
), ViewPager.OnPageChangeListener {

    companion object {
        /**  TODO: Add these as styleable attrs **/
        const val DEFAULT_GAP = 10f
    }

    private var activeIndicatorPaint = Paint().apply {
        isAntiAlias = true
    }
    private val inactiveIndicatorPaint = Paint().apply {
        isAntiAlias = true
        color = 0x34000000
    }

    /**  TODO: Add these as styleable attrs **/
    private val defaultColors = intArrayOf(
        ContextCompat.getColor(context, R.color.cinemais_green),
        ContextCompat.getColor(context, R.color.cinemais_blue),
        ContextCompat.getColor(context, R.color.cinemais_pink)
    )
    private val defaultPositions = floatArrayOf(0f, .5f, 1f)

    private val activeIndicatorShader by lazy {
        LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            defaultColors,
            defaultPositions,
            Shader.TileMode.CLAMP
        )
    }

    // These are calculated by the view
    private var indicatorCount = -1
    private var indicatorWidth = 0f
    private var activeIndicatorLeft = 0f

    var viewPager: ViewPager? = null
        set(value) {
            field = value
            ensureHasOnPageChangeListener()
            refreshValues()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (indicatorCount == 0) {
            return
        }

        val indicatorGap = DEFAULT_GAP * (indicatorCount - 1)
        indicatorWidth = (measuredWidth.toFloat() - indicatorGap) / indicatorCount
    }

    override fun onDraw(canvas: Canvas) {
        if (!isViewPagerAttached()) return

        // Draw all indicators
        val saveCount = canvas.save()
        for (i in 0 until indicatorCount) {
            canvas.drawRect(
                0f,
                0f,
                indicatorWidth,
                height.toFloat(),
                inactiveIndicatorPaint
            )
            // Translate canvas to next x position
            canvas.translate(indicatorWidth + DEFAULT_GAP, 0f)
        }
        canvas.restoreToCount(saveCount)

        // Prepare active indicator shader
        if (activeIndicatorPaint.shader == null) {
            activeIndicatorPaint.shader = activeIndicatorShader
        }

        // Draw active indicator
        val left = activeIndicatorLeft + (DEFAULT_GAP * viewPager?.currentItem!!)
        val right = left + indicatorWidth
        canvas.drawRect(
            left,
            0f,
            right,
            height.toFloat(),
            activeIndicatorPaint
        )
    }

    private fun refreshValues() {
        if (!isViewPagerAttached()) return

        viewPager?.run {
            indicatorCount = adapter?.count!!
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
        val currentIndicatorStart = position * indicatorWidth
        activeIndicatorLeft = currentIndicatorStart + positionOffset * indicatorWidth
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