package com.kwang7.stickyheader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.kwang7.stickyheader.core.AdapterDataProvider;
import com.kwang7.stickyheader.databinding.ItemViewBinding;
import com.kwang7.stickyheader.databinding.SectionViewBinding;
import com.kwang7.stickyheader.model.ListItem;

import java.util.ArrayList;
import java.util.List;

public final class CustomAdapter extends ListAdapter<ListItem, CustomAdapter.BaseViewHolder> implements
                                                                                             AdapterDataProvider {

    private final List<ListItem> dataList = new ArrayList<>();

    private static DiffUtil.ItemCallback<ListItem> DiffCallback = new DiffUtil.ItemCallback<ListItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            return oldItem.title.equals(newItem.title);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            return oldItem == newItem;
        }
    };

    protected CustomAdapter() {
        super(DiffCallback);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == 0
               ? new CustomAdapter.ItemViewHolder(ItemViewBinding.inflate(LayoutInflater.from(parent.getContext())))
               : new CustomAdapter.SectionViewHolder(SectionViewBinding.inflate((LayoutInflater.from(parent.getContext()))));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        final ListItem item = dataList.get(position);

        if (item.isHeader) {
            CustomAdapter.SectionViewHolder sectionViewHolder = (CustomAdapter.SectionViewHolder) holder;
            sectionViewHolder.bind(item);
            sectionViewHolder.sectionButton.setOnClickListener(v -> Toast.makeText(v.getContext(),
                                                                                   "Action clicked at " + position,
                                                                                   Toast.LENGTH_SHORT).show());
        } else {
            CustomAdapter.ItemViewHolder itemViewHolder = (CustomAdapter.ItemViewHolder) holder;
            itemViewHolder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ListItem item = dataList.get(position);
        return item.isHeader ? 1 : 0;
    }

    @Override
    public List<ListItem> getAdapterData() {
        return dataList;
    }

    public void setDataList(List<ListItem> items) {
        dataList.clear();
        dataList.addAll(items);
        notifyDataSetChanged();
    }

    public void addDataList(List<ListItem> items) {
        if (items != null) {
            int start = dataList.size();
            dataList.addAll(items);
            notifyItemRangeInserted(start, items.size());
        }
    }

    private static final class ItemViewHolder extends CustomAdapter.BaseViewHolder {
        ItemViewBinding binding;

        ItemViewHolder(ItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListItem item) {
            binding.setItem(item);
            binding.executePendingBindings();
        }
    }

    private static final class SectionViewHolder extends CustomAdapter.BaseViewHolder {
        TextView sectionButton;
        SectionViewBinding binding;

        SectionViewHolder(SectionViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            sectionButton = binding.sectionButton;
        }

        public void bind(ListItem item) {
            binding.setItem(item);
            binding.executePendingBindings();
        }
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {

        BaseViewHolder(View itemView) {
            super(itemView);
        }
    }
}