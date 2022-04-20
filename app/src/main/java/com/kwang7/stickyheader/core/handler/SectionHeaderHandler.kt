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

class SectionHeaderHandler(private val recyclerView: RecyclerView) {

    private var currentViewHolder: RecyclerView.ViewHolder? = null
    private var currentHeader: View? = null
    private val checkMargins: Boolean
    private var headerPositions: List<Int>? = null
    private var orientation = 0
    private var dirty = false
    private var lastBoundPosition = INVALID_POSITION
    private var headerElevation = NO_ELEVATION.toFloat()
    private var cachedElevation = NO_ELEVATION
    private var listener: SectionHeaderListener? = null
    private val visibilityObserver = OnGlobalLayoutListener { currentHeader?.visibility = recyclerView.visibility }

    fun setHeaderPositions(headerPositions: List<Int>?) {
        this.headerPositions = headerPositions
    }

    fun updateHeaderState(firstVisiblePosition: Int,
                          visibleHeaders: Map<Int, View>,
                          viewFactory: ViewHolderFactory,
                          atTop: Boolean) {
        val headerPositionToShow = if (atTop) INVALID_POSITION else getHeaderPositionToShow(
                firstVisiblePosition,
                visibleHeaders[firstVisiblePosition]
        )
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
        recyclerView.post { checkElevation() }
    }

    private fun checkHeaderPositions(visibleHeaders: Map<Int, View>) {
        currentHeader?.let {
            if (it.height == 0) {
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

            it.visibility = View.VISIBLE
        }
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

    fun clearHeader() = detachHeader(lastBoundPosition)

    fun clearVisibilityObserver() = recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(visibilityObserver)

    fun setListener(listener: SectionHeaderListener?) {
        this.listener = listener
    }

    private fun offsetHeader(nextHeader: View): Float {
        val shouldOffsetHeader = shouldOffsetHeader(nextHeader)
        var offset = -1f
        if (shouldOffsetHeader) {
            currentHeader?.apply {
                if (orientation == LinearLayoutManager.VERTICAL) {
                    offset = -(height - nextHeader.y)
                    translationY = offset
                } else {
                    offset = -(width - nextHeader.x)
                    translationX = offset
                }
            }
        }
        return offset
    }

    private fun shouldOffsetHeader(nextHeader: View): Boolean {
        return currentHeader?.let {
            return if (orientation == LinearLayoutManager.VERTICAL) {
                nextHeader.y < it.height
            } else {
                nextHeader.x < it.width
            }
        } ?: false
    }

    private fun resetTranslation() {
        currentHeader?.apply {
            if (orientation == LinearLayoutManager.VERTICAL) {
                translationY = 0f
            } else {
                translationX = 0f
            }
        }
    }

    private fun getHeaderPositionToShow(firstVisiblePosition: Int, headerForPosition: View?): Int {
        var headerPositionToShow = INVALID_POSITION
        headerPositions?.let {
            if (headerIsOffset(headerForPosition)) {
                val offsetHeaderIndex = it.indexOf(firstVisiblePosition)
                if (offsetHeaderIndex > 0) {
                    return it[offsetHeaderIndex - 1]
                }
            }

            for (headerPosition in it) {
                headerPositionToShow = if (headerPosition <= firstVisiblePosition) {
                    headerPosition
                } else {
                    break
                }
            }
        }
        return headerPositionToShow
    }

    private fun headerIsOffset(headerForPosition: View?): Boolean =
            headerForPosition != null && if (orientation == LinearLayoutManager.VERTICAL) headerForPosition.y > 0 else headerForPosition.x > 0

    private fun attachHeader(viewHolder: RecyclerView.ViewHolder?, headerPosition: Int) {
        if (currentViewHolder === viewHolder) {
            currentViewHolder?.let {
                callDetach(lastBoundPosition)
                recyclerView.adapter?.apply { onBindViewHolder(it, headerPosition) }
                it.itemView.requestLayout()
                checkTranslation()
                callAttach(headerPosition)
                dirty = false
            }
        } else {
            detachHeader(lastBoundPosition)
            currentViewHolder = viewHolder
            currentViewHolder?.let {
                recyclerView.adapter?.apply { onBindViewHolder(it, headerPosition) }
                currentHeader = it.itemView
            }

            callAttach(headerPosition)
            currentHeader?.let {
                resolveElevationSettings(it.context)
                it.visibility = View.INVISIBLE
                recyclerView.viewTreeObserver.addOnGlobalLayoutListener(visibilityObserver)
                recyclerParent.addView(currentHeader)
                if (checkMargins) {
                    updateLayoutParams(it)
                }
            }
            dirty = false
        }
    }

    private fun currentDimension(): Int {
        return currentHeader?.let {
            return if (orientation == LinearLayoutManager.VERTICAL) {
                it.height
            } else {
                it.width
            }
        } ?: 0
    }

    private fun headerHasTranslation(): Boolean {
        return currentHeader?.let {
            return if (orientation == LinearLayoutManager.VERTICAL) {
                it.translationY < 0
            } else {
                it.translationX < 0
            }
        } ?: false
    }

    private fun updateTranslation(diff: Int) {
        currentHeader?.apply {
            if (orientation == LinearLayoutManager.VERTICAL) {
                translationY += diff
            } else {
                translationX += diff
            }
        }
    }

    private fun checkTranslation() {
        val view = currentHeader ?: return
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            var previousDimen = currentDimension()
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (currentHeader == null) {
                    return
                }
                val newDimen = currentDimension()
                if (headerHasTranslation() && previousDimen != newDimen) {
                    updateTranslation(previousDimen - newDimen)
                }
            }
        })
    }

    private fun checkElevation() {
        if (headerElevation != NO_ELEVATION.toFloat()) {
            currentHeader?.let {
                if (orientation == LinearLayoutManager.VERTICAL && it.translationY == 0f
                        || orientation == LinearLayoutManager.HORIZONTAL && it.translationX == 0f) {
                    elevateHeader()
                } else {
                    settleHeader()
                }
            }
        }
    }

    private fun elevateHeader() {
        currentHeader?.apply {
            if (tag != null)
                return
            tag = true
            animate().z(headerElevation)
        }
    }

    private fun settleHeader() {
        currentHeader?.apply {
            if (tag != null) {
                tag = null
                animate().z(0f)
            }
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
        listener?.apply {
            headerAttached(currentHeader, position)
        }
    }

    private fun callDetach(position: Int) {
        listener?.apply {
            headerDetached(currentHeader, position)
        }
    }

    private fun updateLayoutParams(currentHeader: View) {
        val params = currentHeader.layoutParams as MarginLayoutParams
        matchMarginsToPadding(params)
    }

    private fun matchMarginsToPadding(layoutParams: MarginLayoutParams) {
        @Px val leftMargin = if (orientation == LinearLayoutManager.VERTICAL) recyclerView.paddingLeft else 0
        @Px val topMargin = if (orientation == LinearLayoutManager.VERTICAL) 0 else recyclerView.paddingTop
        @Px val rightMargin = if (orientation == LinearLayoutManager.VERTICAL) recyclerView.paddingRight else 0
        layoutParams.setMargins(leftMargin, topMargin, rightMargin, 0)
    }

    private fun headerAwayFromEdge(header: View?): Boolean = header?.let {
        return if (orientation == LinearLayoutManager.VERTICAL) it.y > 0 else it.x > 0
    } ?: false

    private fun recyclerViewHasPadding(): Boolean {
        return recyclerView.paddingLeft > 0 || recyclerView.paddingRight > 0 || recyclerView.paddingTop > 0
    }

    private val recyclerParent: ViewGroup
        get() = recyclerView.parent as ViewGroup

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