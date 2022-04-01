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
            sectionViewHolder.sectionTitle.setText(item.title);
            sectionViewHolder.sectionButton.setOnClickListener(v -> Toast.makeText(v.getContext(),
                                                                                   "Action clicked at " + position,
                                                                                   Toast.LENGTH_SHORT).show());
        } else {
            CustomAdapter.ItemViewHolder itemViewHolder = (CustomAdapter.ItemViewHolder) holder;
            itemViewHolder.itemTitle.setText(item.title);
            itemViewHolder.itemDescription.setText(item.description);
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

        TextView itemTitle;
        TextView itemDescription;

        ItemViewHolder(ItemViewBinding binding) {
            super(binding.getRoot());

            itemTitle = binding.itemTitle;
            itemDescription = binding.itemDescription;
        }
    }

    private static final class SectionViewHolder extends CustomAdapter.BaseViewHolder {

        TextView sectionTitle;
        TextView sectionButton;

        SectionViewHolder(SectionViewBinding binding) {
            super(binding.getRoot());
            sectionTitle = binding.sectionTitle;
            sectionButton = binding.sectionButton;
        }
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {

        BaseViewHolder(View itemView) {
            super(itemView);
        }
    }
}