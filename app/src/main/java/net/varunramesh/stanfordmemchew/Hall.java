package net.varunramesh.stanfordmemchew;

import java.io.Serializable;

/**
 * Created by varun on 10/17/14.
 */
public class Hall implements Serializable {

    public String id;
    public String name;
    public String url;
    public boolean open;

    public String meal;
    public String mealid;
    public int upvotes;
    public int downvotes;
    public String rating;

    public String closes;
    public int comments;

    public float latitude;
    public float longitude;

    public Hall(String id, String name, String url, boolean open, String meal, String mealid, int upvotes, int downvotes, String rating, String closes, int comments){
        this.id = id;
        this.name = name;
        this.url = url;
        this.upvotes = upvotes;
        this.meal = meal;
        this.mealid = mealid;
        this.downvotes = downvotes;
        this.rating = rating;
        this.closes = closes;
        this.comments = comments;
        this.open = open;
    }
}
