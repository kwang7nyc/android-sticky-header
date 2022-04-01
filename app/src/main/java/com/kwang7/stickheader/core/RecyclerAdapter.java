package com.kwang7.stickheader.core;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kwang7.stickheader.databinding.HeaderViewBinding;
import com.kwang7.stickheader.databinding.ItemViewBinding;
import com.kwang7.stickheader.model.HeaderItem;
import com.kwang7.stickheader.model.Item;


import java.util.ArrayList;
import java.util.List;

public final class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.BaseViewHolder> implements AdapterDataProvider {

    private final List<Object> dataList = new ArrayList<>();

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new ItemViewHolder(ItemViewBinding.inflate(LayoutInflater.from(parent.getContext())));
        } else {

            return new HeaderViewHolder(HeaderViewBinding.inflate((LayoutInflater.from(parent.getContext()))));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final BaseViewHolder holder, int position) {
        final Object item = dataList.get(position);
        if (item instanceof Item) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.titleTextView.setText(((Item) item).title);
            itemViewHolder.messageTextView.setText(((Item) item).message);
        } else if (item instanceof HeaderItem) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.titleTextView.setText(((HeaderItem) item).title);

            headerViewHolder.button.setTextColor(((HeaderItem) item).color);
            headerViewHolder.button.setOnClickListener(v -> {
                if (((HeaderItem) item).color == 0xffff5050) {
                    ((HeaderItem) item).color = 0xff777777;
                } else {
                    ((HeaderItem) item).color = 0xffff5050;
                }

                notifyItemChanged(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position) instanceof Item ? 0 : 1;
    }

    @Override
    public List<?> getAdapterData() {
        return dataList;
    }

    public void setDataList(List<Object> items) {
        dataList.clear();
        dataList.addAll(items);
        notifyDataSetChanged();
    }

    public void addDataList(List<Object> items) {
        if (items != null) {
            int start = dataList.size();
            dataList.addAll(items);
            notifyItemRangeInserted(start, items.size());
        }
    }

    private static final class ItemViewHolder extends BaseViewHolder {

        TextView titleTextView;
        TextView messageTextView;

        ItemViewHolder(ItemViewBinding binding) {
            super(binding.getRoot());

            titleTextView = binding.tvTitle;
            messageTextView = binding.tvMessage;;
        }
    }

    private static final class HeaderViewHolder extends BaseViewHolder {

        TextView titleTextView;
        TextView button;

        HeaderViewHolder(HeaderViewBinding binding) {
            super(binding.getRoot());

            titleTextView = binding.tvTitle;;
            button = binding.button;
        }
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {

        BaseViewHolder(View itemView) {
            super(itemView);
        }
    }
}
