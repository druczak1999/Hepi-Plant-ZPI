package com.example.hepiplant.adapter.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hepiplant.PopUpDeleteTag;
import com.example.hepiplant.R;
import com.example.hepiplant.dto.TagDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TagsRecyclerViewAdapter extends RecyclerView.Adapter<TagsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "TagsRVAdapter";
    private List<TagDto> dataSet;
    private ItemClickListener clickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView id;
        private final TextView name;
        private final TextView delete;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            id = view.findViewById(R.id.tagIdTextView);
            name = view.findViewById(R.id.tagNameTextView);
            delete = view.findViewById(R.id.tagDeleteLinkTextView);
        }

        public TextView getId() {
            return id;
        }

        public TextView getName() {
            return name;
        }

        public TextView getDelete() {
            return delete;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public TagsRecyclerViewAdapter(Context context, TagDto[] dataSet) {
        this.dataSet = new ArrayList<>(Arrays.asList(dataSet));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.tag_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.v(TAG, "onBindViewHolder() position: "+position);
        viewHolder.getId().setText(String.format(Locale.GERMANY,"%d", dataSet.get(position).getId()));
        viewHolder.getName().setText(String.format("#%s", dataSet.get(position).getName()));
        viewHolder.getDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "onItemClick() Delete");
                Intent intent = new Intent(viewHolder.getId().getContext(), PopUpDeleteTag.class);
                intent.putExtra("tagId", dataSet.get(viewHolder.getAdapterPosition()).getId());
                viewHolder.getId().getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public TagDto getItem(int id) {
        return dataSet.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Change the dataset of the adapter (call adapter.notifyItemRangeChanged() afterwards)
    public void updateData(TagDto[] newDataSet){
        this.dataSet = new ArrayList<>(Arrays.asList(newDataSet));
    }
}
