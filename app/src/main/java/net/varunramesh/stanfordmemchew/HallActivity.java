package net.varunramesh.stanfordmemchew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class HallActivity extends Activity {

    public static String TAG = "HallActivity";
    private Hall hall;

    class UpdateTask extends AsyncTask<Void, Void, List<Comment>> {
        Context context;
        SwipeRefreshLayout swiper;
        Hall currentHall;

        public UpdateTask(Context context, SwipeRefreshLayout swiper, Hall currentHall) {
            this.context = context;
            this.swiper = swiper;
            this.currentHall = currentHall;
        }

        @Override
        protected List<Comment> doInBackground(Void... voids) {
            MemChewService mcs = new MemChewService();
            List<Comment> comments = mcs.listComments(currentHall.mealid);
            //MockService ms = new MockService();
            //List<Comment> comments = ms.listComments();
            return comments;
        }

        @Override
        protected void onPostExecute(List<Comment> comments) {
            Log.v(TAG, comments.size() + " Comments Found...");

            ListView comment_list = (ListView)findViewById(R.id.comment_list);
            comment_list.setAdapter(new CommentsAdapter(context, R.layout.comment_card, comments));

            if(swiper != null) swiper.setRefreshing(false);
        }
    }

    public class CommentsAdapter extends ArrayAdapter<Comment> {

        public CommentsAdapter(Context context, int resource, List<Comment> objects) {
            super(context, resource, R.id.comment_text, objects);
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            final Comment comment = this.getItem(position);
            final View item_view = super.getView(position, convertView, parent);

            ((TextView) item_view.findViewById(R.id.comment_text)).setText(comment.text);
            ((TextView) item_view.findViewById(R.id.comment_time)).setText(comment.time);

            final MemChewService service = new MemChewService();

            // Code for handling the ratings
            final TextView score = (TextView)item_view.findViewById(R.id.upvote_count);
            score.setText(Integer.toString(comment.upvotes - comment.downvotes));

            final ImageButton upvoteButton = (ImageButton) item_view.findViewById(R.id.upvote_button);
            final ImageButton downvoteButton = (ImageButton) item_view.findViewById(R.id.downvote_button);
            final int upvoteColor = getContext().getResources().getColor(R.color.upvote_color);
            final int downvoteColor = getContext().getResources().getColor(R.color.downvote_color);
            final int defaultColor = getContext().getResources().getColor(R.color.default_color);

            if(comment.rating.equals("upvote")) {
                upvoteButton.setColorFilter(upvoteColor);
                score.setTextColor(upvoteColor);
            } else if(comment.rating.equals("downvote")) {
                downvoteButton.setColorFilter(downvoteColor);
                score.setTextColor(downvoteColor);
            } else {
                upvoteButton.setColorFilter(defaultColor);
                downvoteButton.setColorFilter(defaultColor);
                score.setTextColor(defaultColor);
            }

            upvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AsyncTask<Void, Void, Integer>(){
                        @Override
                        protected Integer doInBackground(Void... voids) { return service.rate(comment.id, true); }

                        @Override
                        protected void onPostExecute(Integer result) {
                            if(result.equals(MemChewService.UPVOTED)) {
                                comment.upvotes++;
                                comment.rating = "upvoted";
                                upvoteButton.setColorFilter(upvoteColor);
                                score.setTextColor(upvoteColor);
                            } else if(result.intValue() == MemChewService.ALREADY_VOTED) {
                                Toast.makeText(getContext(), "Already Voted.", Toast.LENGTH_SHORT).show();
                            }
                            score.setText(Integer.toString(comment.upvotes - comment.downvotes));
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
                            return service.rate(comment.id, false);
                        }

                        @Override
                        protected void onPostExecute(Integer result) {
                            if (result.equals(MemChewService.DOWNVOTED)) {
                                comment.downvotes++;
                                comment.rating = "downvoted";
                                downvoteButton.setColorFilter(downvoteColor);
                                score.setTextColor(downvoteColor);
                            } else if(result.equals(MemChewService.ALREADY_VOTED)) {
                                Toast.makeText(getContext(), "Already Voted.", Toast.LENGTH_SHORT).show();
                            }
                            score.setText(Integer.toString(comment.upvotes - comment.downvotes));
                        }
                    }.execute();
                }
            });

            return item_view;
        }
    }

    class CommentTask extends AsyncTask<Void, Void, Integer> {

        String meal_id;
        String text;
        Hall hall;
        Context context;

        public CommentTask(Context context, String text, Hall hall){
            this.text = text;
            this.hall = hall;
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            MemChewService mcs = new MemChewService();
            return mcs.comment(hall.mealid, text);
        }

        @Override
        protected void onPostExecute(Integer result){
            new UpdateTask(context, null, hall).execute();
        }
    }

    public void submitComment() {
        final EditText edit = ((EditText) findViewById(R.id.editText));
        new CommentTask(this, edit.getText().toString(), hall).execute();
        edit.setText("");
        findViewById(R.id.comment_list).requestFocus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_hall);

        final HallActivity context = this;
        hall = (Hall)getIntent().getSerializableExtra("hall");
        setTitle(hall.name);

        ((ImageButton) findViewById(R.id.imageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { context.submitComment(); }
        });

        ((EditText)findViewById(R.id.editText)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEND) context.submitComment();
                return false;
            }
        });

        final SwipeRefreshLayout swiper = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                new UpdateTask(context, swiper, hall).execute();
            }
        });

        swiper.setColorSchemeColors(Color.rgb(229, 28, 0),
                Color.rgb(177, 28, 8),
                Color.rgb(126, 26, 11),
                Color.rgb(177, 28, 8));

        new UpdateTask(this, swiper, hall).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hall, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.open_site:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(hall.url)));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
