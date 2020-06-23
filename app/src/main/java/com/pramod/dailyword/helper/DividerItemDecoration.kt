package com.pramod.dailyword.helper

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * Adds interior dividers to a RecyclerView with a LinearLayoutManager or its
 * subclass.
 */
class DividerItemDecoration : ItemDecoration {
    private var mDivider: Drawable
    private var mOrientation = 0
    private val mLeftInset: Int
    private val mRightInset: Int

    /**
     * Sole constructor. Takes in a [Drawable] to be used as the interior
     * divider.
     *
     * @param divider A divider `Drawable` to be drawn on the RecyclerView
     */
    constructor(divider: Drawable) {
        mDivider = divider
        mLeftInset = 0
        mRightInset = 0
    }

    /**
     * Sole constructor. Takes in a [Drawable] to be used as the interior
     * divider.
     *
     * @param divider A divider `Drawable` to be drawn on the RecyclerView
     * @param leftInset Left inset of a divider
     * @param rightInset Right inset of a divider
     */
    constructor(divider: Drawable, leftInset: Int, rightInset: Int) {
        mDivider = divider
        mLeftInset = leftInset
        mRightInset = rightInset
    }

    /**
     * Draws horizontal or vertical dividers onto the parent RecyclerView.
     *
     * @param canvas The [Canvas] onto which dividers will be drawn
     * @param parent The RecyclerView onto which dividers are being added
     * @param state The current RecyclerView.State of the RecyclerView
     */
    override fun onDraw(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            drawHorizontalDividers(canvas, parent)
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVerticalDividers(canvas, parent)
        }
    }

    /**
     * Determines the size and location of offsets between items in the parent
     * RecyclerView.
     *
     * @param outRect The [Rect] of offsets to be added around the child
     * view
     * @param view The child view to be decorated with an offset
     * @param parent The RecyclerView onto which dividers are being added
     * @param state The current RecyclerView.State of the RecyclerView
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) == 0) {
            return
        }
        mOrientation = (parent.layoutManager as LinearLayoutManager?)!!.orientation
        if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            outRect.left = mDivider.intrinsicWidth
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.top = mDivider.intrinsicHeight
        }
    }

    /**
     * Adds dividers to a RecyclerView with a LinearLayoutManager or its
     * subclass oriented horizontally.
     *
     * @param canvas The [Canvas] onto which horizontal dividers will be
     * drawn
     * @param parent The RecyclerView onto which horizontal dividers are being
     * added
     */
    private fun drawHorizontalDividers(
        canvas: Canvas,
        parent: RecyclerView
    ) {
        val parentTop = parent.paddingTop
        val parentBottom = parent.height - parent.paddingBottom
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params =
                child.layoutParams as RecyclerView.LayoutParams
            val parentLeft = mLeftInset + child.right + params.rightMargin
            val parentRight = mRightInset + parentLeft + mDivider.intrinsicWidth
            mDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom)
            mDivider.draw(canvas)
        }
    }

    /**
     * Adds dividers to a RecyclerView with a LinearLayoutManager or its
     * subclass oriented vertically.
     *
     * @param canvas The [Canvas] onto which vertical dividers will be
     * drawn
     * @param parent The RecyclerView onto which vertical dividers are being
     * added
     */
    private fun drawVerticalDividers(
        canvas: Canvas,
        parent: RecyclerView
    ) {
        val parentLeft = mLeftInset + parent.paddingLeft
        val parentRight = mRightInset + parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params =
                child.layoutParams as RecyclerView.LayoutParams
            val parentTop = child.bottom + params.bottomMargin
            val parentBottom = parentTop + mDivider.intrinsicHeight
            mDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom)
            mDivider.draw(canvas)
        }
    }
}