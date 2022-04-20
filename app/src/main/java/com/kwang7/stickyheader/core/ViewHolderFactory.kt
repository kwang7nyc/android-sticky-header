package com.kwang7.stickyheader.core

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

class ViewHolderFactory(private val recyclerView: RecyclerView) {
    private var currentViewHolder: RecyclerView.ViewHolder? = null
    private var currentViewType: Int = -1

    fun getViewHolderForPosition(position: Int): RecyclerView.ViewHolder? {
        recyclerView.adapter?.let {
            if (currentViewType != it.getItemViewType(position)) {
                currentViewType = it.getItemViewType(position)
                currentViewHolder = it.createViewHolder((recyclerView.parent as ViewGroup), currentViewType)
            }
        }
        return currentViewHolder
    }
}