package com.jonathancromie.brisbanecityparks;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jonathan on 03-Dec-15.
 */
public class Review {

    @SerializedName("starts")
    private int stars;

    @SerializedName("comment")
    private String comment;

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
}
