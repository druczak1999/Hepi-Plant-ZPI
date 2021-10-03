package com.example.hepiplant.adapter.recyclerview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hepiplant.R;
import com.example.hepiplant.dto.SalesOfferDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SalesOffersRecyclerViewAdapter extends RecyclerView.Adapter<SalesOffersRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "SalesOffersRVAdapter";
    private static final String CURRENCY = "zł";
    private List<SalesOfferDto> dataSet;
    private ItemClickListener clickListener;

    // Provide a reference to the type of views that you are using (custom ViewHolder).
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView price;
        private final TextView location;
        private final TextView title;
        private final TextView tags;
        private final TextView body;
        private final TextView comments;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            price = (TextView) view.findViewById(R.id.offerPriceTextView);
            location = (TextView) view.findViewById(R.id.offerLocationTextView);
            title = (TextView) view.findViewById(R.id.offerTitleTextView);
            tags = (TextView) view.findViewById(R.id.offerTagsTextView);
            body = (TextView) view.findViewById(R.id.offerBodyTextView);
            comments = (TextView) view.findViewById(R.id.offerCommentsCountTextView);
        }

        public TextView getPrice() {
            return price;
        }

        public TextView getLocation() {
            return location;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getTags() {
            return tags;
        }

        public TextView getBody() {
            return body;
        }

        public TextView getComments() {
            return comments;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // Initialize the dataset of the Adapter.
    public SalesOffersRecyclerViewAdapter(Context context, SalesOfferDto[] dataSet) {
        this.dataSet = new ArrayList<>(Arrays.asList(dataSet));
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.sales_offer_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.v(TAG, "onBindViewHolder() position: "+position);
        viewHolder.getPrice().setText(String.format(Locale.GERMANY,"%.2f %s",
                dataSet.get(position).getPrice().doubleValue(), CURRENCY));
        viewHolder.getLocation().setText(dataSet.get(position).getLocation());
        viewHolder.getTitle().setText(dataSet.get(position).getTitle());
        StringBuilder tags = new StringBuilder();
        for (String s : dataSet.get(position).getTags()) {
            tags.append(" #").append(s);
        }
        if(tags.toString().length() == 0){
            viewHolder.getTags().setVisibility(View.GONE);
        } else {
            viewHolder.getTags().setVisibility(View.VISIBLE);
            viewHolder.getTags().setText(tags.toString().trim());
        }
        viewHolder.getBody().setText(dataSet.get(position).getBody());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    // convenience method for getting data at click position
    public SalesOfferDto getItem(int id) {
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
    public void updateData(SalesOfferDto[] newDataSet){
        this.dataSet = new ArrayList<>(Arrays.asList(newDataSet));
    }
}
