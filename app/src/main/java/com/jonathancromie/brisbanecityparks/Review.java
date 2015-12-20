package com.jonathancromie.brisbanecityparks;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Jonathan on 03-Dec-15.
 */
public class Review {

    @SerializedName("stars")
    private int stars;

    @SerializedName("comment")
    private String comment;

//    private Date date;

    public Review() {

    }

    public Review(String comment, int stars) {
        setStars(stars);
        setComment(comment);
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
