package net.varunramesh.stanfordmemchew;

/**
 * Created by jeanluc on 10/20/14.
 */
public class Comment {

    String id;
    String text;
    String image;
    String time;
    int upvotes;
    int downvotes;
    String rating;
    String user;

    public Comment(String id, String text, String image, String time, int upvotes, int downvotes, String rating){
        this.id = id;
        this.text = text;
        this.image = image;
        this.time = time;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.rating = rating;
    }

}
