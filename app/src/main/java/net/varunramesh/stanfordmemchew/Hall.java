package net.varunramesh.stanfordmemchew;

/**
 * Created by varun on 10/17/14.
 */
public class Hall {

    public String id;
    public String name;
    public String url;
    public boolean open;

    public String meal;
    public String mealid;
    public int upvotes;
    public int downvotes;
    public String rating;

    public Hall(String id, String name, String url, int upvotes, boolean open){
        this.id = id;
        this.name = name;
        this.url = url;
        this.upvotes = upvotes;
        this.open = open;
    }
}
