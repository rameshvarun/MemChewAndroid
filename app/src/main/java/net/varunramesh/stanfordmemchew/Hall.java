package net.varunramesh.stanfordmemchew;

/**
 * Created by varun on 10/17/14.
 */
public class Hall {

    public String id;
    public String name;
    public String url;
    public boolean open;

    public Hall(String id, String name, String url, boolean open){
        this.id = id;
        this.name = name;
        this.url = url;
        this.open = open;
    }
}
