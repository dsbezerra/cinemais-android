package com.diegobezerra.cinemaisapp.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapted from:
 * https://gist.github.com/hrules6872/b68bf3762e4d1243e480
 */
class SpaceItemDecoration @JvmOverloads constructor(
    private val spaceBetween: Int,
    private val spaceFirstLastItem: Int = 0,
    private val addSpaceAboveFirstItem: Boolean = DEFAULT_ADD_SPACE_ABOVE_FIRST_ITEM,
    private val addSpaceBelowLastItem: Boolean = DEFAULT_ADD_SPACE_BELOW_LAST_ITEM
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (spaceBetween <= 0) {
            return
        }

        if (spaceFirstLastItem <= 0 && (addSpaceAboveFirstItem || addSpaceBelowLastItem)) {
            throw IllegalStateException(
                "Asked for space above first and last item, but space $spaceFirstLastItem is not valid."
            )
        }

        val childLayoutPosition = parent.getChildLayoutPosition(view)
        val first = addSpaceAboveFirstItem && childLayoutPosition < 1
        val between = childLayoutPosition >= 1
        if (first || between) {
            if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
                outRect.top = if (between) spaceBetween else spaceFirstLastItem
            } else {
                outRect.left = if (between) spaceBetween else spaceFirstLastItem
            }
        }

        if (addSpaceBelowLastItem && childLayoutPosition == getTotalItemCount(parent) - 1) {
            if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
                outRect.bottom = spaceFirstLastItem
            } else {
                outRect.right = spaceFirstLastItem
            }
        }
    }

    private fun getTotalItemCount(parent: RecyclerView): Int {
        return parent.adapter!!.itemCount
    }

    private fun getOrientation(parent: RecyclerView): Int {
        return if (parent.layoutManager is LinearLayoutManager) {
            (parent.layoutManager as LinearLayoutManager).orientation
        } else {
            throw IllegalStateException(
                "SpaceItemDecoration can only be used with a LinearLayoutManager."
            )
        }
    }

    companion object {

        private const val DEFAULT_ADD_SPACE_ABOVE_FIRST_ITEM = false
        private const val DEFAULT_ADD_SPACE_BELOW_LAST_ITEM = false

    }
}