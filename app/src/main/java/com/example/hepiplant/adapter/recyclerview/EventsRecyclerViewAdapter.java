package com.example.hepiplant.adapter.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hepiplant.PopUpArchive;
import com.example.hepiplant.R;
import com.example.hepiplant.dto.EventDto;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EventsRecyclerViewAdapter extends RecyclerView.Adapter<EventsRecyclerViewAdapter.ViewHolder>{

    private List<EventDto> dataSet;
    private EventsRecyclerViewAdapter.ItemClickListener clickListener;
    private static final String TAG = "EventsListAdapter";
    private Context contextAll;
    private Intent intent;

    // Provide a reference to the type of views that you are using (custom ViewHolder).
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView image;
        private final TextView date;
        private final TextView event;
        private final TextView plant;
        private final CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            image = view.findViewById(R.id.eventIcon);
            plant = view.findViewById(R.id.plantNameTextView);
            date = view.findViewById(R.id.eventDateTextView);
            event = view.findViewById(R.id.eventDescriptionTextView);
            checkBox = view.findViewById(R.id.eventCheckBox);
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

        public CheckBox getCheckBox(){return checkBox;}

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // Initialize the dataset of the Adapter.
    public EventsRecyclerViewAdapter(Context context, EventDto[] dataSet) {
        intent = new Intent(context,PopUpArchive.class);
        contextAll =context;
        this.dataSet = new ArrayList<EventDto>(Arrays.asList(dataSet));
    }

    // Create new views (invoked by the layout manager)
    @Override
    public EventsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.v(TAG,"viewholder");
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_row_item, viewGroup, false);
        return new EventsRecyclerViewAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(EventsRecyclerViewAdapter.ViewHolder viewHolder, final int position) {
        Log.v(TAG,"photo for: "+dataSet.get(position).getEventName());
        viewHolder.getEvent().setText(dataSet.get(position).getEventName());
        if(dataSet.get(position).getEventDate() != null){
            //TODO check format
            String str = dataSet.get(position).getEventDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
            Log.v(TAG,dateTime.toString());
            Log.v(TAG,LocalDateTime.now().toString());
            if(dateTime.isBefore((ChronoLocalDateTime)LocalDateTime.now())){
                viewHolder.getDate().setText(dataSet.get(position).getEventDate());
                viewHolder.getDate().setTextColor(Color.RED);
            }
            else{
                viewHolder.getDate().setText(dataSet.get(position).getEventDate());
            }

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

        viewHolder.getCheckBox().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.getCheckBox().isChecked()){
                    intent.putExtra("eventName",dataSet.get(position).getEventName());
                    intent.putExtra("eventDate",dataSet.get(position).getEventDate());
                    intent.putExtra("eventId",dataSet.get(position).getId());
                    intent.putExtra("eventDescription",dataSet.get(position).getEventDescription());
                    intent.putExtra("plantId",dataSet.get(position).getPlantId());
                    intent.putExtra("plantName",dataSet.get(position).getPlantName());
                    contextAll.startActivity(intent);
                }
            }
        });
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
    public void setClickListener(EventsRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
