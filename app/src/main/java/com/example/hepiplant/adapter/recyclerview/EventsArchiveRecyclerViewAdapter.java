package com.example.hepiplant.adapter.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hepiplant.PopUpArchive;
import com.example.hepiplant.R;
import com.example.hepiplant.dto.EventDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventsArchiveRecyclerViewAdapter extends RecyclerView.Adapter<EventsArchiveRecyclerViewAdapter.ViewHolder>{

    private List<EventDto> dataSet;
    private EventsArchiveRecyclerViewAdapter.ItemClickListener clickListener;
    private static final String TAG = "EventsListAdapter";
    private Context contextAll;
    private Intent intent;

    // Provide a reference to the type of views that you are using (custom ViewHolder).
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView image;
        private final TextView date;
        private final TextView event;
        private final TextView plant;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            image = view.findViewById(R.id.eventIcon);
            plant = view.findViewById(R.id.plantNameTextView);
            date = view.findViewById(R.id.eventDateTextView);
            event = view.findViewById(R.id.eventDescriptionTextView);
        }

        public ImageView getImage() {
            return image;
        }

        public TextView getDate() {
            return date;
        }

        public TextView getEvent() {
            return event;
        }

        public TextView getPlant(){return plant;}


        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // Initialize the dataset of the Adapter.
    public EventsArchiveRecyclerViewAdapter(Context context, EventDto[] dataSet) {
        intent = new Intent(context,PopUpArchive.class);
        contextAll =context;
        this.dataSet = new ArrayList<EventDto>(Arrays.asList(dataSet));
    }

    // Create new views (invoked by the layout manager)
    @Override
    public EventsArchiveRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.v(TAG,"viewholder");
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_archive_row_item, viewGroup, false);
        return new EventsArchiveRecyclerViewAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(EventsArchiveRecyclerViewAdapter.ViewHolder viewHolder, final int position) {

        Log.v(TAG,"photo for: "+dataSet.get(position).getEventName());
        viewHolder.getEvent().setText(dataSet.get(position).getEventName());
        if(dataSet.get(position).getEventDate() != null){
            viewHolder.getDate().setText(dataSet.get(position).getEventDate());
        } else {
            viewHolder.getDate().setText("");
        }
        viewHolder.getPlant().setText(dataSet.get(position).getPlantName());
        ImageView photoImageView = viewHolder.getImage();
        if(dataSet.get(position).getEventName().toLowerCase().trim().equals("podlewanie"))
            photoImageView.setImageResource(R.drawable.podelwanie);
        else if(dataSet.get(position).getEventName().toLowerCase().trim().equals("zraszanie"))
            photoImageView.setImageResource(R.drawable.zraszanie);
        else if(dataSet.get(position).getEventName().toLowerCase().trim().equals("nawożenie"))
            photoImageView.setImageResource(R.drawable.nawozenie);
        else
            photoImageView.setImageResource(R.drawable.kwiatek);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    // convenience method for getting data at click position
    public EventDto getItem(int id) {
        return dataSet.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(EventsArchiveRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
