package net.varunramesh.stanfordmemchew;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by jeanluc on 10/17/14.
 */
public class MockService implements GenericService{

    public MockService(){

    }

    public List<Hall> listHalls(){
        List<Hall> dummyData = new ArrayList<Hall>();
        Random r = new Random();

        for(int i=0; i<10; i++){
            Hall temp = new Hall(String.valueOf(i), "Name"+i, "www.derp"+i+".com", r.nextBoolean(), "bfast", "Derp", 4, 5, "bad", "4", 5);
            dummyData.add(temp);
        }
        return dummyData;
    }

    public List<Comment> listComments(String hall_id){
        List<Comment> badComments = new ArrayList<Comment>();
        Random r = new Random();

        for(int i=0; i<10; i++){
            Comment temp = new Comment("ID"+i, "Happy customer no. "+i, "", r.nextInt(100)+" minutes ago", r.nextInt(10), r.nextInt(10), "none");
            badComments.add(temp);
        }
        return badComments;
    }
}
