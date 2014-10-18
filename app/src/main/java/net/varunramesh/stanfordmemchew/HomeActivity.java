package net.varunramesh.stanfordmemchew;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;


public class HomeActivity extends Activity {

    public static String TAG = "HallsActivity";

    class ListHallsTask extends AsyncTask<Void, Void, List<Hall>> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<Hall> doInBackground(Void... voids) {
            List<Hall> halls = MemChewService.listHalls();
            return halls;
        }

        @Override
        protected void onPostExecute(List<Hall> halls) {
            Log.v(TAG, halls.size() + " Halls Found...");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        new ListHallsTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
