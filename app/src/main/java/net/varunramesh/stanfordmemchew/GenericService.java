package net.varunramesh.stanfordmemchew;

import java.util.List;

/**
 * Created by jeanluc on 10/17/14.
 */
public interface GenericService {

    public List<Hall> listHalls();

    public List<Comment> listComments(String meal_id);

}
