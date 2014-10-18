package net.varunramesh.stanfordmemchew;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;


public class HomeActivity extends Activity{

    public static String TAG = "HallsActivity";

    class ListHallsTask extends AsyncTask<Void, Void, List<Hall>> {
        Context context;
        SwipeRefreshLayout swiper;
        public ListHallsTask(Context context, SwipeRefreshLayout swiper) {
            this.context = context;
            this.swiper = swiper;
        }

        @Override
        protected void onPreExecute() {
            ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar);
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Hall> doInBackground(Void... voids) {
            MemChewService mcs = new MemChewService();
            List<Hall> halls = mcs.listHalls();
            //MockService ms = new MockService();
            //List<Hall> halls = ms.listHalls();
            return halls;
        }

        @Override
        protected void onPostExecute(List<Hall> halls) {
            Log.v(TAG, halls.size() + " Halls Found...");

            ListView hall_list = (ListView)findViewById(R.id.hall_list);
            hall_list.setAdapter(new EventAdapter(context, R.layout.hall_card, halls));

            ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar);
            spinner.setVisibility(View.VISIBLE);

            if(swiper != null) swiper.setRefreshing(false);
        }
    }

    public class EventAdapter extends ArrayAdapter<Hall> {

        public EventAdapter(Context context, int resource, List<Hall> objects) {
            super(context, resource, R.id.event_shortdesc, objects);
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            Hall hall = this.getItem(position);
            View item_view = super.getView(position, convertView, parent);
            ((TextView)item_view.findViewById(R.id.event_shortdesc)).setText(hall.name+"\nID: "+hall.id);
            ((TextView)item_view.findViewById(R.id.event_desc)).setText(hall.url+"\nOpen: "+hall.open);

            if(hall.open)
                ((View)item_view.findViewById(R.id.card_shadow)).setBackgroundResource(android.R.color.holo_blue_bright);

            return item_view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Context context = this;
        final SwipeRefreshLayout swiper = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                new ListHallsTask(context, swiper).execute();
            }
        });

        swiper.setColorSchemeColors(Color.rgb(229, 28, 0),
                Color.rgb(177, 28, 8),
                Color.rgb(126, 26, 11),
                Color.rgb(177, 28, 8));

        new ListHallsTask(this, null).execute();


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
