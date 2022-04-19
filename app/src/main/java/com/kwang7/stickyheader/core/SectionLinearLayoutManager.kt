package com.kwang7.stickyheader.core

import android.content.Context
import android.view.View
import com.kwang7.stickyheader.core.AdapterDataProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kwang7.stickyheader.core.handler.SectionHeaderHandler
import com.kwang7.stickyheader.core.ViewHolderFactory
import com.kwang7.stickyheader.core.SectionLinearLayoutManager.SectionHeaderListener
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import java.util.LinkedHashMap

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

    constructor(context: Context?, adapterDataProvider: AdapterDataProvider) : this(context, VERTICAL, false, adapterDataProvider) {}

    fun setSectionHeaderListener(listener: SectionHeaderListener?) {
        sectionHeaderListener = listener
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler!!.setListener(listener)
        }
    }

    fun elevateHeaders(elevateHeaders: Boolean) {
        elevateHeaders(if (elevateHeaders) SectionHeaderHandler.DEFAULT_ELEVATION else SectionHeaderHandler.NO_ELEVATION)
    }

    fun elevateHeaders(dpElevation: Int) {
        headerElevation = if (dpElevation > 0) dpElevation else SectionHeaderHandler.NO_ELEVATION
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler!!.setElevateHeaders(headerElevation)
        }
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
        if (Math.abs(scroll) > 0) {
            if (sectionHeaderHandler != null) {
                sectionHeaderHandler!!.updateHeaderState(findFirstVisibleItemPosition(),
                        visibleHeaders,
                        viewHolderFactory,
                        findFirstCompletelyVisibleItemPosition() == 0)
            }
        }
        return scroll
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        val scroll = super.scrollHorizontallyBy(dx, recycler, state)
        if (Math.abs(scroll) > 0) {
            if (sectionHeaderHandler != null) {
                sectionHeaderHandler!!.updateHeaderState(findFirstVisibleItemPosition(),
                        visibleHeaders,
                        viewHolderFactory,
                        findFirstCompletelyVisibleItemPosition() == 0)
            }
        }
        return scroll
    }

    override fun removeAndRecycleAllViews(recycler: Recycler) {
        super.removeAndRecycleAllViews(recycler)
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler!!.clearHeader()
        }
    }

    override fun onAttachedToWindow(view: RecyclerView) {
        viewHolderFactory = ViewHolderFactory(view)
        sectionHeaderHandler = SectionHeaderHandler(view)
        sectionHeaderHandler!!.setElevateHeaders(headerElevation)
        sectionHeaderHandler!!.setListener(sectionHeaderListener)
        if (headerPositions.size > 0) {
            sectionHeaderHandler!!.setHeaderPositions(headerPositions)
            resetHeaderHandler()
        }
        super.onAttachedToWindow(view)
    }

    override fun onDetachedFromWindow(view: RecyclerView, recycler: Recycler) {
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler!!.clearVisibilityObserver()
        }
        super.onDetachedFromWindow(view, recycler)
    }

    private fun resetHeaderHandler() {
        sectionHeaderHandler!!.reset(orientation)
        sectionHeaderHandler!!.updateHeaderState(findFirstVisibleItemPosition(),
                visibleHeaders,
                viewHolderFactory,
                findFirstCompletelyVisibleItemPosition() == 0)
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
            if (sectionHeaderHandler != null) {
                sectionHeaderHandler!!.setHeaderPositions(headerPositions)
            }
            return
        }
        for (i in adapterData.indices) {
            if (adapterData[i] != null && adapterData[i]!!.isHeader) {
                headerPositions.add(i)
            }
        }
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler!!.setHeaderPositions(headerPositions)
        }
    }

    interface SectionHeaderListener {
        fun headerAttached(headerView: View?, adapterPosition: Int)
        fun headerDetached(headerView: View?, adapterPosition: Int)
    }
}