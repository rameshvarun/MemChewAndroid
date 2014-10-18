package net.varunramesh.stanfordmemchew;

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
import java.util.List;

/**
 * Created by varun on 10/17/14.
 */
public class MemChewService implements GenericService{
    private static String BASE_URL = "http://varunramesh.net:3000/";
    private static Gson gson = new Gson();

    public MemChewService(){

    }

    public List<Hall> listHalls() {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(BASE_URL + "halls?user=" + Settings.Secure.ANDROID_ID);

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

    public boolean rate(String meal_id, boolean upvote){
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String request = BASE_URL + "rate?item=" + meal_id + "&user="+Settings.Secure.ANDROID_ID+"&action=";
            if(upvote) request += "upvote";
            else request += "downvote";

            HttpGet httpGet = new HttpGet(request);

            Log.d("MemChewService", httpGet.getRequestLine().toString());

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String string = EntityUtils.toString(httpEntity, "UTF-8");

            JSONObject result = new JSONObject(string);
            if(result.has("error")){
                Log.d("MemChewService", result.getString("error"));
                return false;
            }

            if(result.getString("result").equals("downvoted") && upvote){
                Log.d("MemChewService", "Server downvoted dish while user upvoted.");
            }else if(result.getString("result").equals("upvoted") && !upvote){
                Log.d("MemChewService", "Server upvoted dish while user downvoted.");
            }else {
                Log.d("MemChewService", "Dish successfully "+result.getString("result")+".");
                return true;
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
