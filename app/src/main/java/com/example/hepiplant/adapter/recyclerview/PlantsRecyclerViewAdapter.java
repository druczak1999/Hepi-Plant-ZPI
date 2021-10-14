package com.example.hepiplant.adapter.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hepiplant.R;
import com.example.hepiplant.dto.PlantDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlantsRecyclerViewAdapter extends RecyclerView.Adapter<PlantsRecyclerViewAdapter.ViewHolder> {

    private List<PlantDto> dataSet;
    private ItemClickListener clickListener;
    private static final String TAG = "PlantsListAdapter";

    // Provide a reference to the type of views that you are using (custom ViewHolder).
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView image;
        private final TextView name;
        private final TextView species;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            image = view.findViewById(R.id.plantIconImageView);

            name = view.findViewById(R.id.postBodyTextView);
            species = view.findViewById(R.id.postCommentsCountTextView);
        }

        public ImageView getImage() {
            return image;
        }

        public TextView getName() {
            return name;
        }

        public TextView getSpecies() {
            return species;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // Initialize the dataset of the Adapter.
    public PlantsRecyclerViewAdapter(Context context, PlantDto[] dataSet) {
        this.dataSet = new ArrayList<>(Arrays.asList(dataSet));
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.v(TAG,"viewholder");
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.plant_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.v(TAG,"photo for: "+dataSet.get(position).getName());
        viewHolder.getName().setText(dataSet.get(position).getName());
        if(dataSet.get(position).getSpecies() != null){
            viewHolder.getSpecies().setText(dataSet.get(position).getSpecies().getName());
        } else {
            viewHolder.getSpecies().setText("");
        }
        Log.v(TAG,"photo: "+dataSet.get(position).getPhoto());
        if(dataSet.get(position).getPhoto()!=null){
            try {
                viewHolder.getImage().setImageURI(Uri.parse(dataSet.get(position).getPhoto()));
            } catch (Exception e) {
                e.getMessage();

            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    // convenience method for getting data at click position
    public PlantDto getItem(int id) {
        return dataSet.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Change the dataset of the adapter (call adapter.notifyItemRangeChanged() afterwards)
    public void updateData(PlantDto[] newDataSet){
        this.dataSet = new ArrayList<>(Arrays.asList(newDataSet));
    }
}
