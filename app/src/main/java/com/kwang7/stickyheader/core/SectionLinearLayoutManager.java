package com.kwang7.stickyheader.core;

import android.content.Context;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kwang7.stickyheader.core.handler.SectionHeaderHandler;
import com.kwang7.stickyheader.model.SectionItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SectionLinearLayoutManager extends LinearLayoutManager {

    private final AdapterDataProvider sectionProvider;
    private SectionHeaderHandler sectionHeaderHandler;

    private final List<Integer> headerPositions = new ArrayList<>();

    private ViewHolderFactory viewHolderFactory;

    private int headerElevation = SectionHeaderHandler.NO_ELEVATION;

    @Nullable
    private SectionHeaderListener sectionHeaderListener;

    public SectionLinearLayoutManager(Context context, AdapterDataProvider headerProvider) {
        this(context, VERTICAL, false, headerProvider);
    }

    public SectionLinearLayoutManager(Context context, int orientation, boolean reverseLayout, AdapterDataProvider headerProvider) {
        super(context, orientation, reverseLayout);

        this.sectionProvider = headerProvider;
    }

    public void setStickyHeaderListener(@Nullable SectionHeaderListener listener) {
        this.sectionHeaderListener = listener;
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler.setListener(listener);
        }
    }

    public void elevateHeaders(boolean elevateHeaders) {
        elevateHeaders(elevateHeaders ? SectionHeaderHandler.DEFAULT_ELEVATION : SectionHeaderHandler.NO_ELEVATION);
    }

    public void elevateHeaders(int dpElevation) {
        this.headerElevation = dpElevation > 0 ? dpElevation : SectionHeaderHandler.NO_ELEVATION;
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler.setElevateHeaders(headerElevation);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        cacheHeaderPositions();
        if (sectionHeaderHandler != null) {
            resetHeaderHandler();
        }
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPositionWithOffset(position, 0);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollVerticallyBy(dy, recycler, state);
        if (Math.abs(scroll) > 0) {
            if (sectionHeaderHandler != null) {
                sectionHeaderHandler.updateHeaderState(findFirstVisibleItemPosition(), getVisibleHeaders(), viewHolderFactory, findFirstCompletelyVisibleItemPosition() == 0);
            }
        }
        return scroll;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollHorizontallyBy(dx, recycler, state);
        if (Math.abs(scroll) > 0) {
            if (sectionHeaderHandler != null) {
                sectionHeaderHandler.updateHeaderState(findFirstVisibleItemPosition(), getVisibleHeaders(), viewHolderFactory, findFirstCompletelyVisibleItemPosition() == 0);
            }
        }
        return scroll;
    }

    @Override
    public void removeAndRecycleAllViews(RecyclerView.Recycler recycler) {
        super.removeAndRecycleAllViews(recycler);
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler.clearHeader();
        }
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        viewHolderFactory = new ViewHolderFactory(view);
        sectionHeaderHandler = new SectionHeaderHandler(view);
        sectionHeaderHandler.setElevateHeaders(headerElevation);
        sectionHeaderHandler.setListener(sectionHeaderListener);
        if (headerPositions.size() > 0) {
            sectionHeaderHandler.setHeaderPositions(headerPositions);
            resetHeaderHandler();
        }
        super.onAttachedToWindow(view);
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler.clearVisibilityObserver();
        }
        super.onDetachedFromWindow(view, recycler);
    }

    private void resetHeaderHandler() {
        sectionHeaderHandler.reset(getOrientation());
        sectionHeaderHandler.updateHeaderState(findFirstVisibleItemPosition(), getVisibleHeaders(), viewHolderFactory, findFirstCompletelyVisibleItemPosition() == 0);
    }

    private Map<Integer, View> getVisibleHeaders() {
        Map<Integer, View> visibleHeaders = new LinkedHashMap<>();

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int dataPosition = getPosition(view);
            if (headerPositions.contains(dataPosition)) {
                visibleHeaders.put(dataPosition, view);
            }
        }
        return visibleHeaders;
    }

    private void cacheHeaderPositions() {
        headerPositions.clear();
        List<?> adapterData = sectionProvider.getAdapterData();
        if (adapterData == null) {
            if (sectionHeaderHandler != null) {
                sectionHeaderHandler.setHeaderPositions(headerPositions);
            }
            return;
        }

        for (int i = 0; i < adapterData.size(); i++) {
            if (adapterData.get(i) instanceof SectionItem) {
                headerPositions.add(i);
            }
        }
        if (sectionHeaderHandler != null) {
            sectionHeaderHandler.setHeaderPositions(headerPositions);
        }
    }

    public interface SectionHeaderListener {

        void headerAttached(View headerView, int adapterPosition);

        void headerDetached(View headerView, int adapterPosition);
    }
}
