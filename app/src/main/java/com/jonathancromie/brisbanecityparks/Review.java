package com.jonathancromie.brisbanecityparks;

import com.google.gson.annotations.SerializedName;

import java.util.Date;


/**
 * Created by Jonathan on 03-Dec-15.
 */
public class Review {

    @SerializedName("stars")
    private int stars;

    @SerializedName("comment")
    private String comment;

    @SerializedName("date")
    private Date date;

    public Review() {

    }

    public Review(String comment, int stars, Date date) {
        setStars(stars);
        setComment(comment);
        setDate(date);
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
