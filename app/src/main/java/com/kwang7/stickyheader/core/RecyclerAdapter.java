package com.kwang7.stickyheader.core;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kwang7.stickyheader.databinding.ItemViewBinding;
import com.kwang7.stickyheader.databinding.SectionViewBinding;
import com.kwang7.stickyheader.model.SectionItem;
import com.kwang7.stickyheader.model.Item;


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

            return new SectionViewHolder(SectionViewBinding.inflate((LayoutInflater.from(parent.getContext()))));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final BaseViewHolder holder, int position) {
        final Object item = dataList.get(position);
        if (item instanceof Item) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.itemTitle.setText(((Item) item).title);
            itemViewHolder.itemDescription.setText(((Item) item).message);
        } else if (item instanceof SectionItem) {
            SectionViewHolder headerViewHolder = (SectionViewHolder) holder;
            headerViewHolder.sectionTitle.setText(((SectionItem) item).title);

            headerViewHolder.sectionButton.setTextColor(((SectionItem) item).color);
            headerViewHolder.sectionButton.setOnClickListener(v -> {
                if (((SectionItem) item).color == 0xffff5050) {
                    ((SectionItem) item).color = 0xff777777;
                } else {
                    ((SectionItem) item).color = 0xffff5050;
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

        TextView itemTitle;
        TextView itemDescription;

        ItemViewHolder(ItemViewBinding binding) {
            super(binding.getRoot());

            itemTitle = binding.itemTitle;
            itemDescription = binding.itemDescription;;
        }
    }

    private static final class SectionViewHolder extends BaseViewHolder {

        TextView sectionTitle;
        TextView sectionButton;

        SectionViewHolder(SectionViewBinding binding) {
            super(binding.getRoot());

            sectionTitle = binding.sectionTitle;;
            sectionButton = binding.sectionButton;
        }
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {

        BaseViewHolder(View itemView) {
            super(itemView);
        }
    }
}
