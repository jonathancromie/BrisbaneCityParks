package com.jonathancromie.brisbanecityparks;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Jonathan on 03-Dec-15.
 */
public class Review {

//    @SerializedName("id")
//    private String id;

    private int stars;

    @SerializedName("comment")
    private String comment;

//    private Date date;

    public Review() {

    }

    public Review(int stars, String comment) {
        setStars(stars);
        setComment(comment);
    }

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

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
