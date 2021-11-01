package com.example.hepiplant.adapter.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hepiplant.R;
import com.example.hepiplant.SpeciesEditActivity;
import com.example.hepiplant.dto.SpeciesDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SpeciesRecyclerViewAdapter extends RecyclerView.Adapter<SpeciesRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "SpeciesRVAdapter";
    private List<SpeciesDto> dataSet;
    private ItemClickListener clickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView id;
        private final TextView name;
        private final TextView edit;
        private final TextView delete;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            id = view.findViewById(R.id.speciesIdTextView);
            name = view.findViewById(R.id.speciesNameTextView);
            edit = view.findViewById(R.id.speciesEditLinkTextView);
            delete = view.findViewById(R.id.speciesDeleteLinkTextView);
        }

        public TextView getId() {
            return id;
        }

        public TextView getName() {
            return name;
        }

        public TextView getEdit() {
            return edit;
        }

        public TextView getDelete() {
            return delete;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public SpeciesRecyclerViewAdapter(Context context, SpeciesDto[] dataSet) {
        this.dataSet = new ArrayList<>(Arrays.asList(dataSet));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.species_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.v(TAG, "onBindViewHolder() position: " + position);
        viewHolder.getId().setText(String.format(Locale.GERMANY,"%d", dataSet.get(position).getId()));
        viewHolder.getName().setText(dataSet.get(position).getName());
        // todo implement
        viewHolder.getEdit().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "onItemClick() Edit");
                Intent intent = new Intent(viewHolder.getId().getContext(), SpeciesEditActivity.class);
                intent.putExtra("speciesId", dataSet.get(viewHolder.getAdapterPosition()).getId());
                viewHolder.getId().getContext().startActivity(intent);
            }
        });
//        viewHolder.getDelete().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.v(TAG, "onItemClick() Delete");
//                Intent intent = new Intent(viewHolder.getId().getContext(), PopUpDeleteSpecies.class);
//                intent.putExtra("speciesId", dataSet.get(viewHolder.getAdapterPosition()).getId());
//                viewHolder.getId().getContext().startActivity(intent);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public SpeciesDto getItem(int id) {
        return dataSet.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Change the dataset of the adapter (call adapter.notifyItemRangeChanged() afterwards)
    public void updateData(SpeciesDto[] newDataSet){
        this.dataSet = new ArrayList<>(Arrays.asList(newDataSet));
    }
}
