package net.varunramesh.stanfordmemchew;

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
            HttpGet httpGet = new HttpGet(BASE_URL + "halls");

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
}
