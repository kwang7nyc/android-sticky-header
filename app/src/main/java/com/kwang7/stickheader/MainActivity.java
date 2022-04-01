package com.kwang7.stickheader;

import android.content.Context;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.kwang7.stickheader.core.RecyclerAdapter;
import com.kwang7.stickheader.core.StickyLinearLayoutManager;
import com.kwang7.stickheader.model.HeaderItem;
import com.kwang7.stickheader.model.Item;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);

        adapter = new RecyclerAdapter();
        adapter.setDataList(genDataList(0));
        StickyLinearLayoutManager layoutManager = new StickyLinearLayoutManager(this, adapter) {
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
        layoutManager.setStickyHeaderListener(new StickyLinearLayoutManager.StickyHeaderListener() {
            @Override
            public void headerAttached(View headerView, int adapterPosition) {
                Log.d("StickyHeader", "Header Attached : " + adapterPosition);
            }

            @Override
            public void headerDetached(View headerView, int adapterPosition) {
                Log.d("StickyHeader", "Header Detached : " + adapterPosition);
            }
        });

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addDataList(genDataList(adapter.getItemCount()));
            }
        }, 5000);
    }

    public static List<Object> genDataList(int start) {
        List<Object> items = new ArrayList<>();
        for (int i = start; i < 100 + start; i++) {
            if (i % 10 == 0) {
                items.add(new HeaderItem("Header " + i));
            } else {
                items.add(new Item("Item " + i, "description " + i));
            }
        }
        return items;
    }
}
