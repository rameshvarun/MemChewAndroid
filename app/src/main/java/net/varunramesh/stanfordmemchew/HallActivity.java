package net.varunramesh.stanfordmemchew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    class UpdateTask extends AsyncTask<Void, Void, List<Comment>> {
        Context context;
        SwipeRefreshLayout swiper;

        public UpdateTask(Context context, SwipeRefreshLayout swiper) {
            this.context = context;
            this.swiper = swiper;
        }

        @Override
        protected List<Comment> doInBackground(Void... voids) {
            //MemChewService mcs = new MemChewService();
            //List<Comment> halls = mcs.listHalls();
            MockService ms = new MockService();
            List<Comment> comments = ms.listComments();
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









            return item_view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_hall);

        final Hall hall = (Hall)getIntent().getSerializableExtra("hall");
        setTitle(hall.name);

        ((ImageButton) findViewById(R.id.imageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText) findViewById(R.id.editText)).setSelection(2);
            }
        });

        final SwipeRefreshLayout swiper = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);

            }
        });

        swiper.setColorSchemeColors(Color.rgb(229, 28, 0),
                Color.rgb(177, 28, 8),
                Color.rgb(126, 26, 11),
                Color.rgb(177, 28, 8));

        new UpdateTask(this, swiper).execute();
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

        return super.onOptionsItemSelected(item);
    }
}
