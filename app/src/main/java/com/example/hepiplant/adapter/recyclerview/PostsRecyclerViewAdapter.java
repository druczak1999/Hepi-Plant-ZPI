package com.example.hepiplant.adapter.recyclerview;

import static com.example.hepiplant.helper.LangUtils.getCommentsSuffix;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView date;
        private final TextView title;
        private final TextView tags;
        private final TextView body;
        private final ImageView photo;
        private final TextView comments;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            date = view.findViewById(R.id.postDateTextView);
            title = view.findViewById(R.id.postTitleTextView);
            tags = view.findViewById(R.id.postTagsTextView);
            body = view.findViewById(R.id.postBodyTextView);
            photo = view.findViewById(R.id.postPhotoImageView);
            comments = view.findViewById(R.id.postCommentsCountTextView);
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

        public ImageView getPhoto() {
            return photo;
        }

        public TextView getComments() {
            return comments;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public PostsRecyclerViewAdapter(Context context, PostDto[] dataSet) {
        this.dataSet = new ArrayList<>(Arrays.asList(dataSet));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.post_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

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
        ImageView photoImageView = viewHolder.getPhoto();
        if(dataSet.get(position).getPhoto()!=null){
            Log.v(TAG,"Attempting photo bind for data: " + dataSet.get(position).getPhoto());
            try {
                photoImageView.setImageURI(Uri.parse(dataSet.get(position).getPhoto()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    photoImageView.setClipToOutline(true);
                }
            } catch (Exception e) {
                e.getMessage();
            }
        } else {
            photoImageView.setVisibility(View.GONE);
        }
        int commentsCount = dataSet.get(position).getComments().size();
        String commentsText = commentsCount + getCommentsSuffix(commentsCount);
        viewHolder.getComments().setText(commentsText);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public PostDto getItem(int id) {
        return dataSet.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Change the dataset of the adapter (call adapter.notifyItemRangeChanged() afterwards)
    public void updateData(PostDto[] newDataSet){
        this.dataSet = new ArrayList<>(Arrays.asList(newDataSet));
    }
}
