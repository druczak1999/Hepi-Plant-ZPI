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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView price;
        private final TextView location;
        private final TextView title;
        private final TextView tags;
        private final TextView body;
        private final ImageView photo;
        private final TextView comments;
        private final TextView author;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            price = view.findViewById(R.id.offerPriceTextView);
            location = view.findViewById(R.id.offerLocationTextView);
            title = view.findViewById(R.id.offerTitleTextView);
            tags = view.findViewById(R.id.offerTagsTextView);
            body = view.findViewById(R.id.offerBodyTextView);
            photo = view.findViewById(R.id.offerPhotoImageView);
            comments = view.findViewById(R.id.offerCommentsCountTextView);
            author = view.findViewById(R.id.postAuthorTextView);
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

        public ImageView getPhoto() {
            return photo;
        }

        public TextView getAuthor(){ return author; }

        public TextView getComments() {
            return comments;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public SalesOffersRecyclerViewAdapter(Context context, SalesOfferDto[] dataSet) {
        this.dataSet = new ArrayList<>(Arrays.asList(dataSet));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.sales_offer_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.v(TAG, "onBindViewHolder() position: "+position);
        viewHolder.getPrice().setText(String.format(Locale.GERMANY,"%.2f %s",
                dataSet.get(position).getPrice().doubleValue(), CURRENCY));
        viewHolder.getLocation().setText(dataSet.get(position).getLocation());
        viewHolder.getTitle().setText(dataSet.get(position).getTitle());
        viewHolder.getAuthor().setText(dataSet.get(position).getUsername());
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

    public SalesOfferDto getItem(int id) {
        return dataSet.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Change the dataset of the adapter (call adapter.notifyItemRangeChanged() afterwards)
    public void updateData(SalesOfferDto[] newDataSet){
        this.dataSet = new ArrayList<>(Arrays.asList(newDataSet));
    }
}
