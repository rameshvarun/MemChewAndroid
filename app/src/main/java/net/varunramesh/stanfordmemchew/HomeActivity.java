package net.varunramesh.stanfordmemchew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;


public class HomeActivity extends Activity{

    public static View PopulateInfo(final Hall hall, View item_view, final Context context, boolean isHomepage) {

        final TextView hall_name = (TextView)item_view.findViewById(R.id.hall_name);
        final TextView meal_desc = (TextView)item_view.findViewById(R.id.meal_desc);

        hall_name.setText(hall.name);

        final TextView score = (TextView)item_view.findViewById(R.id.upvote_count);
        final LinearLayout voteBlock = (LinearLayout) item_view.findViewById(R.id.vote_block);
        final LinearLayout infoBlock = (LinearLayout) item_view.findViewById(R.id.info_block);
        final LinearLayout cardContent = (LinearLayout) item_view.findViewById(R.id.card_content);
        final TextView closing_time = (TextView)item_view.findViewById(R.id.closing_time);
        final TextView num_comments = (TextView)item_view.findViewById(R.id.num_comments);

        final MemChewService service = new MemChewService(context);

        score.setText(Integer.toString(hall.upvotes - hall.downvotes));
        if (isHomepage) {
            cardContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hall.open) {
                        Log.d(TAG, "Launching Hall Activity");
                        Intent intent = new Intent(context, HallActivity.class);
                        intent.putExtra("hall", hall);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, hall.name + " is closed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if(hall.open) {
            if (isHomepage) ((View) item_view.findViewById(R.id.card_shadow)).setBackgroundResource(R.color.card_highlight);
            voteBlock.setVisibility(View.VISIBLE);
            meal_desc.setVisibility(View.VISIBLE);

            if(hall.mealdesc != null) { // If we have a descriptionw
                meal_desc.setText(hall.mealdesc);
            } else { // Default text when there is no description
                String text = "Open for " + Character.toUpperCase(hall.meal.charAt(0)) + hall.meal.substring(1);
                meal_desc.setText(text);
            }

            closing_time.setText("Closes " + hall.closes);
            num_comments.setText(hall.comments + " comments");


            final ImageButton upvoteButton = (ImageButton) item_view.findViewById(R.id.upvote_button);
            final ImageButton downvoteButton = (ImageButton) item_view.findViewById(R.id.downvote_button);
            final int upvoteColor = context.getResources().getColor(R.color.upvote_color);
            final int downvoteColor = context.getResources().getColor(R.color.downvote_color);
            final int defaultColor = context.getResources().getColor(R.color.default_color);

            if(hall.rating.equals("upvote")) {
                upvoteButton.setColorFilter(upvoteColor);
                score.setTextColor(upvoteColor);
                downvoteButton.setColorFilter(defaultColor);
            } else if(hall.rating.equals("downvote")) {
                downvoteButton.setColorFilter(downvoteColor);
                score.setTextColor(downvoteColor);
                upvoteButton.setColorFilter(defaultColor);
            } else {
                downvoteButton.setColorFilter(defaultColor);
                upvoteButton.setColorFilter(defaultColor);
                score.setTextColor(defaultColor);
            }

            upvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AsyncTask<Void, Void, Integer>(){
                        @Override
                        protected Integer doInBackground(Void... voids) { return service.rate(hall.mealid, true); }

                        @Override
                        protected void onPostExecute(Integer result) {
                            if(result.equals(MemChewService.UPVOTED)) {
                                hall.upvotes++;
                                hall.rating = "upvoted";
                                upvoteButton.setColorFilter(upvoteColor);
                                score.setTextColor(upvoteColor);
                            } else if(result.intValue() == MemChewService.ALREADY_VOTED) {
                                Toast.makeText(context, "Already Voted.", Toast.LENGTH_SHORT).show();
                            }
                            score.setText(Integer.toString(hall.upvotes - hall.downvotes));
                        }
                    }.execute();
                }
            });


            downvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AsyncTask<Void, Void, Integer>() {
                        @Override
                        protected Integer doInBackground(Void... voids) {
                            return service.rate(hall.mealid, false);
                        }

                        @Override
                        protected void onPostExecute(Integer result) {
                            if (result.equals(MemChewService.DOWNVOTED)) {
                                hall.downvotes++;
                                hall.rating = "downvoted";
                                downvoteButton.setColorFilter(downvoteColor);
                                score.setTextColor(downvoteColor);
                            } else if(result.equals(MemChewService.ALREADY_VOTED)) {
                                Toast.makeText(context, "Already Voted.", Toast.LENGTH_SHORT).show();
                            }
                            score.setText(Integer.toString(hall.upvotes - hall.downvotes));
                        }
                    }.execute();
                }
            });
        }else {
            if (isHomepage) ((View) item_view.findViewById(R.id.card_shadow)).setBackgroundColor(context.getResources().getColor(R.color.card_shadow));
            voteBlock.setVisibility(View.GONE);
            meal_desc.setVisibility(View.GONE);
            closing_time.setText("Closed");

            num_comments.setText("");
        }

        return item_view;
    }

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
        }

        @Override
        protected List<Hall> doInBackground(Void... voids) {
            MemChewService mcs = new MemChewService(getApplicationContext());
            List<Hall> halls = mcs.listHalls();
            return halls;
        }

        @Override
        protected void onPostExecute(List<Hall> halls) {

            ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar);
            spinner.setVisibility(View.INVISIBLE);

            if(halls == null){
                Toast.makeText(context, "Could not load dining halls.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(swiper != null) swiper.setRefreshing(false);
            Log.v(TAG, halls.size() + " Halls Found...");

            ListView hall_list = (ListView)findViewById(R.id.hall_list);
            hall_list.setAdapter(new HallsAdapter(context, R.layout.hall_card, halls));
        }
    }

    public class HallsAdapter extends ArrayAdapter<Hall> {

        public HallsAdapter(Context context, int resource, List<Hall> objects) {
            super(context, resource, R.id.hall_name, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Hall hall = this.getItem(position);
            final View item_view = super.getView(position, convertView, parent);
            return PopulateInfo(hall, item_view,getContext(), true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Dining Halls");

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

        final ListView hall_list = (ListView)findViewById(R.id.hall_list);
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
        return super.onOptionsItemSelected(item);
    }
}
