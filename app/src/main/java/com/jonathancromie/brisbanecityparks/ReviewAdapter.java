package com.jonathancromie.brisbanecityparks;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonathancromie on 4/12/2015.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        RatingBar stars;
        TextView comment;
        TextView date;


        public ReviewViewHolder(View itemView) {
            super(itemView);
            stars = (RatingBar) itemView.findViewById(R.id.stars);
            comment = (TextView) itemView.findViewById(R.id.comment);
            date = (TextView) itemView.findViewById(R.id.date);
        }
    }

    List<Review> reviews;
    LayoutInflater layoutInflater;
    ReviewAdapter(LayoutInflater layoutInflater, List<Review> items){
        this.layoutInflater = layoutInflater;
        this.reviews = new ArrayList<Review>();

//        addReview(new Review("2", 5, "Great"));

        for (Review review : items) {
            reviews.add(review);
        }
    }

    public void addReview(Review review) {
        reviews.add(review);
    }
    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_review, parent, false);
        ReviewViewHolder reviewViewHolder = new ReviewViewHolder(v);
        return reviewViewHolder;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.stars.setNumStars(reviews.get(position).getStars());
        holder.comment.setText(reviews.get(position).getComment());
        holder.date.setText(String.valueOf(reviews.get(position).getDate()));

    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
