package com.kwang7.stickyheader;

import android.content.Context;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.kwang7.stickyheader.core.CustomAdapter;
import com.kwang7.stickyheader.core.SectionLinearLayoutManager;
import com.kwang7.stickyheader.model.ListItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        adapter = new CustomAdapter();
        adapter.setDataList(genDataList(0));
        SectionLinearLayoutManager layoutManager = new SectionLinearLayoutManager(this, adapter) {
            @Override
            public boolean isAutoMeasureEnabled() {
                return true;
            }

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                RecyclerView.SmoothScroller smoothScroller = new TopSmoothScroller(recyclerView.getContext());
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }

            class TopSmoothScroller extends LinearSmoothScroller {

                TopSmoothScroller(Context context) {
                    super(context);
                }

                @Override
                public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                    return boxStart - viewStart;
                }
            }
        };
        layoutManager.elevateHeaders(5);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        layoutManager.setSectionHeaderListener(new SectionLinearLayoutManager.SectionHeaderListener() {
            @Override
            public void headerAttached(View headerView, int adapterPosition) {
                Log.d(TAG, "Header Attached : " + adapterPosition);
            }

            @Override
            public void headerDetached(View headerView, int adapterPosition) {
                Log.d(TAG, "Header Detached : " + adapterPosition);
            }
        });

        recyclerView.postDelayed(() -> adapter.addDataList(genDataList(adapter.getItemCount())), 5000);
    }

    public static List<ListItem> genDataList(int start) {
        List<ListItem> items = new ArrayList<>();
        for (int i = start; i < 100 + start; i++) {
            if (i % 10 == 0) {
                items.add(new ListItem("Section " + i, null, true));
            } else {
                items.add(new ListItem("Discover item " + i, "description " + i, false));
            }
        }
        return items;
    }
}
