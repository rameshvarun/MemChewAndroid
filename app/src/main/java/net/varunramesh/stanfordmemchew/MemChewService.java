package net.varunramesh.stanfordmemchew;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

/**
 * Created by varun on 10/17/14.
 */
public class MemChewService implements GenericService{
    private static String BASE_URL = "http://varunramesh.net:3000/";
    private static Gson gson = new Gson();
    public static final String TAG = "MemChewService";

    public static String USER_ID_KEY = "user_id";


    private Context context;

    public MemChewService(Context context){

        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        if(!prefs.contains(USER_ID_KEY)){
            SharedPreferences.Editor e = prefs.edit();
            e.putString(USER_ID_KEY, UUID.randomUUID().toString());
            e.commit();
        }
    }

    public List<Hall> listHalls() {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(BASE_URL + "halls?user=" + getUniqueID());

            Log.d("MemChewService", httpGet.getRequestLine().toString());

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String string = EntityUtils.toString(httpEntity, "UTF-8");

            List<Hall> data = gson.fromJson(string, new TypeToken<List<Hall>>() {
            }.getType());

            return data;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Comment> listComments(String meal_id){
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(BASE_URL + "comments?user=" + getUniqueID()+"&meal="+meal_id);

            Log.d("MemChewService", httpGet.getRequestLine().toString());

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String string = EntityUtils.toString(httpEntity, "UTF-8");

            List<Comment> data = gson.fromJson(string, new TypeToken<List<Comment>>() {
            }.getType());

            return data;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final int ALREADY_VOTED = 0;
    public static final int UPVOTED = 1;
    public static final int DOWNVOTED = 2;
    public static final int ERROR = 3;
    //public static final int COMMENTED = 4;

    public int rate(String meal_id, boolean upvote){
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String request = BASE_URL + "rate?item=" + meal_id + "&user="+getUniqueID()+"&action=";
            if(upvote) request += "upvote";
            else request += "downvote";

            HttpGet httpGet = new HttpGet(request);

            Log.d(TAG, httpGet.getRequestLine().toString());

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String string = EntityUtils.toString(httpEntity, "UTF-8");
            Log.d(TAG, string);

            JSONObject result = new JSONObject(string);
            if(result.has("error")){
                if(result.getString("error").toLowerCase().equals("already voted")) {
                    return ALREADY_VOTED;
                }
                else {
                    Log.e(TAG, result.getString("error"));
                    return ERROR;
                }
            }

            if(result.getString("result").toLowerCase().equals("downvoted"))
                return DOWNVOTED;
            else if(result.getString("result").toLowerCase().equals("upvoted"))
                return UPVOTED;
            else {
                Log.e(TAG, "Unkown result");
                return ERROR;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return ERROR;
        }
    }

    public String comment(String meal_id, String text){

        try {
            HttpClient httpClient = new DefaultHttpClient();
            String request = BASE_URL + "comment?meal=" + meal_id + "&user="+getUniqueID()+"&comment="+ URLEncoder.encode(text, "UTF-8");
            HttpGet httpGet = new HttpGet(request);

            Log.d(TAG, httpGet.getRequestLine().toString());

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String response = EntityUtils.toString(httpEntity, "UTF-8");
            Log.d(TAG, response);

            Comment data = gson.fromJson(new JSONObject(response).getString("comment"), Comment.class);
            return data.id;

        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }

    public String getUniqueID(){
        String id = context.getSharedPreferences("USER_ID", Context.MODE_PRIVATE).getString(USER_ID_KEY, "NULL");
        if(id.equals("NULL")) Log.d("MemChewService", "Device unique ID not found.");
        return id;

    }
}
