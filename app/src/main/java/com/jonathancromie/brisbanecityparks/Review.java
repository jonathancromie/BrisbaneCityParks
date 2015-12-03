package com.jonathancromie.brisbanecityparks;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Jonathan on 03-Dec-15.
 */
public class Review {

    @SerializedName("starts")
    private int stars;

    @SerializedName("comment")
    private String comment;

    // testing purposes
    private List<Review> reviews;

//    private Date date;

    public Review() {

    }

    public Review(int stars, String comment) {
        this.stars = stars;
        this.comment = comment;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // testing
    private void initialiseData() {
        reviews.add(new Review(5, "great"));
        reviews.add(new Review(1, "terrible"));
        reviews.add(new Review(3, "average"));
        reviews.add(new Review(0, "out of range"));
        reviews.add(new Review(7, "out of range"));
    }
}
