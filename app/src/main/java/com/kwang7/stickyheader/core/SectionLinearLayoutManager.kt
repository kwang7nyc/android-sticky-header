package com.kwang7.stickyheader.core

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.kwang7.stickyheader.core.handler.SectionHeaderHandler
import kotlin.math.abs

open class SectionLinearLayoutManager(
    context: Context?,
    orientation: Int,
    reverseLayout: Boolean,
    private val adapterDataProvider: AdapterDataProvider
) : LinearLayoutManager(context, orientation, reverseLayout) {

    private var sectionHeaderHandler: SectionHeaderHandler? = null
    private val headerPositions: MutableList<Int> = ArrayList()
    private lateinit var viewHolderFactory: ViewHolderFactory
    private var headerElevation = SectionHeaderHandler.NO_ELEVATION
    private var sectionHeaderListener: SectionHeaderListener? = null

    constructor(context: Context?, adapterDataProvider: AdapterDataProvider) : this(context, VERTICAL, false, adapterDataProvider)

    fun setSectionHeaderListener(listener: SectionHeaderListener?) {
        sectionHeaderListener = listener
        sectionHeaderHandler?.setListener(listener)
    }

    fun elevateHeaders(elevateHeaders: Boolean) {
        elevateHeaders(if (elevateHeaders) SectionHeaderHandler.DEFAULT_ELEVATION else SectionHeaderHandler.NO_ELEVATION)
    }

    fun elevateHeaders(dpElevation: Int) {
        headerElevation = if (dpElevation > 0) dpElevation else SectionHeaderHandler.NO_ELEVATION
        sectionHeaderHandler?.apply { setElevateHeaders(headerElevation) }
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        cacheHeaderPositions()
        if (sectionHeaderHandler != null) {
            resetHeaderHandler()
        }
    }

    override fun scrollToPosition(position: Int) {
        super.scrollToPositionWithOffset(position, 0)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        val scroll = super.scrollVerticallyBy(dy, recycler, state)
        if (abs(scroll) > 0) {
            sectionHeaderHandler?.updateHeaderState(
                    findFirstVisibleItemPosition(),
                    visibleHeaders,
                    viewHolderFactory,
                    findFirstCompletelyVisibleItemPosition() == 0
            )
        }
        return scroll
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        val scroll = super.scrollHorizontallyBy(dx, recycler, state)
        if (abs(scroll) > 0) {
            sectionHeaderHandler?.updateHeaderState(
                    findFirstVisibleItemPosition(),
                    visibleHeaders,
                    viewHolderFactory,
                    findFirstCompletelyVisibleItemPosition() == 0
            )
        }
        return scroll
    }

    override fun removeAndRecycleAllViews(recycler: Recycler) {
        super.removeAndRecycleAllViews(recycler)
        sectionHeaderHandler?.clearHeader()
    }

    override fun onAttachedToWindow(view: RecyclerView) {
        viewHolderFactory = ViewHolderFactory(view)
        sectionHeaderHandler = SectionHeaderHandler(view)
        sectionHeaderHandler?.let {
            it.setElevateHeaders(headerElevation)
            it.setListener(sectionHeaderListener)
        }

        if (headerPositions.size > 0) {
            sectionHeaderHandler?.setHeaderPositions(headerPositions)
            resetHeaderHandler()
        }
        super.onAttachedToWindow(view)
    }

    override fun onDetachedFromWindow(view: RecyclerView, recycler: Recycler) {
        sectionHeaderHandler?.clearVisibilityObserver()
        super.onDetachedFromWindow(view, recycler)
    }

    private fun resetHeaderHandler() {
        sectionHeaderHandler?.let {
            it.reset(orientation)
            it.updateHeaderState(findFirstVisibleItemPosition(),
                    visibleHeaders,
                    viewHolderFactory,
                    findFirstCompletelyVisibleItemPosition() == 0)
        }
    }

    private val visibleHeaders: Map<Int, View>
        get() {
            val visibleHeaders: MutableMap<Int, View> = LinkedHashMap()
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                val dataPosition = getPosition(view!!)
                if (headerPositions.contains(dataPosition)) {
                    visibleHeaders[dataPosition] = view
                }
            }
            return visibleHeaders
        }

    private fun cacheHeaderPositions() {
        headerPositions.clear()
        val adapterData = adapterDataProvider.adapterData
        if (adapterData == null) {
            sectionHeaderHandler?.setHeaderPositions(headerPositions)
            return
        }

        adapterData.forEachIndexed { index, item -> item.let { if (it.isHeader) headerPositions.add(index) } }
        sectionHeaderHandler?.setHeaderPositions(headerPositions)
    }

    interface SectionHeaderListener {
        fun headerAttached(headerView: View?, adapterPosition: Int)
        fun headerDetached(headerView: View?, adapterPosition: Int)
    }
}