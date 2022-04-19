package com.kwang7.stickyheader.core.handler

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.Px
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kwang7.stickyheader.core.SectionLinearLayoutManager.SectionHeaderListener
import com.kwang7.stickyheader.core.ViewHolderFactory

class SectionHeaderHandler(private val mRecyclerView: RecyclerView) {

    private var currentViewHolder: RecyclerView.ViewHolder? = null
    private var currentHeader: View? = null
    private val checkMargins: Boolean
    private var mHeaderPositions: List<Int>? = null
    private var orientation = 0
    private var dirty = false
    private var lastBoundPosition = INVALID_POSITION
    private var headerElevation = NO_ELEVATION.toFloat()
    private var cachedElevation = NO_ELEVATION
    private var listener: SectionHeaderListener? = null
    private val visibilityObserver = OnGlobalLayoutListener {
        val visibility = mRecyclerView.visibility
        if (currentHeader != null) {
            currentHeader!!.visibility = visibility
        }
    }

    fun setHeaderPositions(headerPositions: List<Int>?) {
        mHeaderPositions = headerPositions
    }

    fun updateHeaderState(firstVisiblePosition: Int,
                          visibleHeaders: Map<Int, View>,
                          viewFactory: ViewHolderFactory,
                          atTop: Boolean) {
        val headerPositionToShow = if (atTop) INVALID_POSITION else getHeaderPositionToShow(firstVisiblePosition,
                visibleHeaders[firstVisiblePosition])
        val headerToCopy = visibleHeaders[headerPositionToShow]
        if (headerPositionToShow != lastBoundPosition) {
            if (headerPositionToShow == INVALID_POSITION || checkMargins && headerAwayFromEdge(headerToCopy)) {
                dirty = true
                safeDetachHeader()
                lastBoundPosition = INVALID_POSITION
            } else {
                lastBoundPosition = headerPositionToShow
                val viewHolder = viewFactory.getViewHolderForPosition(headerPositionToShow)
                attachHeader(viewHolder, headerPositionToShow)
            }
        } else if (checkMargins && headerAwayFromEdge(headerToCopy)) {
            detachHeader(lastBoundPosition)
            lastBoundPosition = INVALID_POSITION
        }
        checkHeaderPositions(visibleHeaders)
        mRecyclerView.post { checkElevation() }
    }

    private fun checkHeaderPositions(visibleHeaders: Map<Int, View>) {
        if (currentHeader == null) {
            return
        }
        if (currentHeader!!.height == 0) {
            waitForLayoutAndRetry(visibleHeaders)
            return
        }
        var reset = true
        for ((key, nextHeader) in visibleHeaders) {
            if (key <= lastBoundPosition) {
                continue
            }
            reset = offsetHeader(nextHeader) == -1f
            break
        }
        if (reset) {
            resetTranslation()
        }
        currentHeader!!.visibility = View.VISIBLE
    }

    fun setElevateHeaders(dpElevation: Int) {
        if (dpElevation != NO_ELEVATION) {
            cachedElevation = dpElevation
        } else {
            headerElevation = NO_ELEVATION.toFloat()
            cachedElevation = NO_ELEVATION
        }
    }

    fun reset(orientation: Int) {
        this.orientation = orientation
        lastBoundPosition = INVALID_POSITION
        dirty = true
        safeDetachHeader()
    }

    fun clearHeader() {
        detachHeader(lastBoundPosition)
    }

    fun clearVisibilityObserver() {
        mRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(visibilityObserver)
    }

    fun setListener(listener: SectionHeaderListener?) {
        this.listener = listener
    }

    private fun offsetHeader(nextHeader: View): Float {
        val shouldOffsetHeader = shouldOffsetHeader(nextHeader)
        var offset = -1f
        if (shouldOffsetHeader) {
            if (orientation == LinearLayoutManager.VERTICAL) {
                offset = -(currentHeader!!.height - nextHeader.y)
                currentHeader!!.translationY = offset
            } else {
                offset = -(currentHeader!!.width - nextHeader.x)
                currentHeader!!.translationX = offset
            }
        }
        return offset
    }

    private fun shouldOffsetHeader(nextHeader: View): Boolean {
        return if (orientation == LinearLayoutManager.VERTICAL) {
            nextHeader.y < currentHeader!!.height
        } else {
            nextHeader.x < currentHeader!!.width
        }
    }

    private fun resetTranslation() {
        if (orientation == LinearLayoutManager.VERTICAL) {
            currentHeader!!.translationY = 0f
        } else {
            currentHeader!!.translationX = 0f
        }
    }

    private fun getHeaderPositionToShow(firstVisiblePosition: Int, headerForPosition: View?): Int {
        var headerPositionToShow = INVALID_POSITION
        if (headerIsOffset(headerForPosition)) {
            val offsetHeaderIndex = mHeaderPositions!!.indexOf(firstVisiblePosition)
            if (offsetHeaderIndex > 0) {
                return mHeaderPositions!![offsetHeaderIndex - 1]
            }
        }
        for (headerPosition in mHeaderPositions!!) {
            headerPositionToShow = if (headerPosition <= firstVisiblePosition) {
                headerPosition
            } else {
                break
            }
        }
        return headerPositionToShow
    }

    private fun headerIsOffset(headerForPosition: View?): Boolean {
        return headerForPosition != null && if (orientation == LinearLayoutManager.VERTICAL) headerForPosition.y > 0 else headerForPosition.x > 0
    }

    private fun attachHeader(viewHolder: RecyclerView.ViewHolder, headerPosition: Int) {
        if (currentViewHolder === viewHolder) {
            callDetach(lastBoundPosition)
            mRecyclerView.adapter!!.onBindViewHolder(currentViewHolder!!, headerPosition)
            currentViewHolder!!.itemView.requestLayout()
            checkTranslation()
            callAttach(headerPosition)
            dirty = false
            return
        }
        detachHeader(lastBoundPosition)
        currentViewHolder = viewHolder
        mRecyclerView.adapter!!.onBindViewHolder(currentViewHolder!!, headerPosition)
        currentHeader = currentViewHolder!!.itemView
        callAttach(headerPosition)
        resolveElevationSettings(currentHeader!!.context)
        currentHeader!!.visibility = View.INVISIBLE
        mRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(visibilityObserver)
        recyclerParent.addView(currentHeader)
        if (checkMargins) {
            updateLayoutParams(currentHeader!!)
        }
        dirty = false
    }

    private fun currentDimension(): Int {
        if (currentHeader == null) {
            return 0
        }
        return if (orientation == LinearLayoutManager.VERTICAL) {
            currentHeader!!.height
        } else {
            currentHeader!!.width
        }
    }

    private fun headerHasTranslation(): Boolean {
        if (currentHeader == null) {
            return false
        }
        return if (orientation == LinearLayoutManager.VERTICAL) {
            currentHeader!!.translationY < 0
        } else {
            currentHeader!!.translationX < 0
        }
    }

    private fun updateTranslation(diff: Int) {
        if (currentHeader == null) {
            return
        }
        if (orientation == LinearLayoutManager.VERTICAL) {
            currentHeader!!.translationY = currentHeader!!.translationY + diff
        } else {
            currentHeader!!.translationX = currentHeader!!.translationX + diff
        }
    }

    private fun checkTranslation() {
        val view = currentHeader ?: return
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            var previous = currentDimension()
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (currentHeader == null) {
                    return
                }
                val newDimen = currentDimension()
                if (headerHasTranslation() && previous != newDimen) {
                    updateTranslation(previous - newDimen)
                }
            }
        })
    }

    private fun checkElevation() {
        if (headerElevation != NO_ELEVATION.toFloat() && currentHeader != null) {
            if (orientation == LinearLayoutManager.VERTICAL && currentHeader!!.translationY == 0f
                    || orientation == LinearLayoutManager.HORIZONTAL && currentHeader!!.translationX == 0f) {
                elevateHeader()
            } else {
                settleHeader()
            }
        }
    }

    private fun elevateHeader() {
        if (currentHeader!!.tag != null) {
            return
        }
        currentHeader!!.tag = true
        currentHeader!!.animate().z(headerElevation)
    }

    private fun settleHeader() {
        if (currentHeader!!.tag != null) {
            currentHeader!!.tag = null
            currentHeader!!.animate().z(0f)
        }
    }

    private fun detachHeader(position: Int) {
        if (currentHeader != null) {
            recyclerParent.removeView(currentHeader)
            callDetach(position)
            clearVisibilityObserver()
            currentHeader = null
            currentViewHolder = null
        }
    }

    private fun callAttach(position: Int) {
        if (listener != null) {
            listener!!.headerAttached(currentHeader, position)
        }
    }

    private fun callDetach(position: Int) {
        if (listener != null) {
            listener!!.headerDetached(currentHeader, position)
        }
    }

    private fun updateLayoutParams(currentHeader: View) {
        val params = currentHeader.layoutParams as MarginLayoutParams
        matchMarginsToPadding(params)
    }

    private fun matchMarginsToPadding(layoutParams: MarginLayoutParams) {
        @Px val leftMargin = if (orientation == LinearLayoutManager.VERTICAL) mRecyclerView.paddingLeft else 0
        @Px val topMargin = if (orientation == LinearLayoutManager.VERTICAL) 0 else mRecyclerView.paddingTop
        @Px val rightMargin = if (orientation == LinearLayoutManager.VERTICAL) mRecyclerView.paddingRight else 0
        layoutParams.setMargins(leftMargin, topMargin, rightMargin, 0)
    }

    private fun headerAwayFromEdge(headerToCopy: View?): Boolean {
        return headerToCopy != null && if (orientation == LinearLayoutManager.VERTICAL) headerToCopy.y > 0 else headerToCopy.x > 0
    }

    private fun recyclerViewHasPadding(): Boolean {
        return mRecyclerView.paddingLeft > 0 || mRecyclerView.paddingRight > 0 || mRecyclerView.paddingTop > 0
    }

    private val recyclerParent: ViewGroup
        private get() = mRecyclerView.parent as ViewGroup

    private fun waitForLayoutAndRetry(visibleHeaders: Map<Int, View>) {
        val view = currentHeader ?: return
        view.viewTreeObserver.addOnGlobalLayoutListener(
                object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        if (currentHeader == null) {
                            return
                        }
                        recyclerParent.requestLayout()
                        checkHeaderPositions(visibleHeaders)
                    }
                })
    }

    private fun safeDetachHeader() {
        val cachedPosition = lastBoundPosition
        recyclerParent.post {
            if (dirty) {
                detachHeader(cachedPosition)
            }
        }
    }

    private fun resolveElevationSettings(context: Context) {
        if (cachedElevation != NO_ELEVATION && headerElevation == NO_ELEVATION.toFloat()) {
            headerElevation = dp2px(context, cachedElevation)
        }
    }

    private fun dp2px(context: Context, dp: Int): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale
    }

    companion object {
        private const val INVALID_POSITION = -1
        const val NO_ELEVATION = -1
        const val DEFAULT_ELEVATION = 5
    }

    init {
        checkMargins = recyclerViewHasPadding()
    }
}