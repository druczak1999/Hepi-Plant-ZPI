package com.example.hepiplant.adapter.recyclerview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hepiplant.R;
import com.example.hepiplant.dto.PostDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostsRecyclerViewAdapter extends RecyclerView.Adapter<PostsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "PostsRVAdapter";
    private List<PostDto> dataSet;
    private ItemClickListener clickListener;

    // Provide a reference to the type of views that you are using (custom ViewHolder).
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView date;
        private final TextView title;
        private final TextView tags;
        private final TextView body;
        private final TextView comments;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            date = (TextView) view.findViewById(R.id.postDateTextView);
            title = (TextView) view.findViewById(R.id.postTitleTextView);
            tags = (TextView) view.findViewById(R.id.postTagsTextView);
            body = (TextView) view.findViewById(R.id.postBodyTextView);
            comments = (TextView) view.findViewById(R.id.postCommentsCountTextView);
        }

        public TextView getDate() {
            return date;
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
    public PostsRecyclerViewAdapter(Context context, PostDto[] dataSet) {
        this.dataSet = new ArrayList<>(Arrays.asList(dataSet));
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.post_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.v(TAG, "onBindViewHolder() position: "+position);
        viewHolder.getDate().setText(dataSet.get(position).getCreatedDate());
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
    public PostDto getItem(int id) {
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
    public void updateData(PostDto[] newDataSet){
        this.dataSet = new ArrayList<>(Arrays.asList(newDataSet));
    }
}
