package com.jonathancromie.brisbanecityparks;

/**
 * Created by Jonathan on 11/28/2015.
 */
public class Parks {
    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("park_code")
    private String parkCode;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("name")
    private String name;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("street")
    private String street;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("suburb")
    private String suburb;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("easting")
    private String easting;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("northing")
    private String northing;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("latitude")
    private String latitude;

    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("longitude")
    private String longitude;

    /**
     * Parks constructor
     */
    public Parks() {

    }

    @Override
    public String toString() {
        return getText();
    }

    /**
     * Initializes a new ToDoItem
     *
     * @param text
     *            The item text
     * @param id
     *            The item id
     */
    public Parks(String text, String id) {
        this.setText(text);
        this.setId(id);
    }

    /**
     * Returns the item text
     */
    public String getText() {
        return name;
    }

    /**
     * Sets the item text
     *
     * @param text
     *            text to set
     */
    public final void setText(String text) {
        name = text;
    }

    /**
     * Returns the item id
     */
    public String getId() {
        return mId;
    }

    /**
     * Sets the item id
     *
     * @param id
     *            id to set
     */
    public final void setId(String id) {
        mId = id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Parks && ((Parks) o).name == name;

    }
}
