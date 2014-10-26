package net.varunramesh.stanfordmemchew;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HallActivity extends Activity {

    public static String TAG = "HallActivity";
    private Hall hall;
    private View card_content;

    class UpdateTask extends AsyncTask<Void, Void, List<Comment>> {
        Context context;
        SwipeRefreshLayout swiper;
        Hall currentHall;
        String idToFind;

        public UpdateTask(Context context, SwipeRefreshLayout swiper, Hall currentHall, String idToFind) {
            this.context = context;
            this.swiper = swiper;
            this.currentHall = currentHall;
            this.idToFind = idToFind;
        }

        @Override
        protected List<Comment> doInBackground(Void... voids) {
            MemChewService mcs = new MemChewService(getApplicationContext());
            List<Comment> comments = mcs.listComments(currentHall.mealid);

            List<Hall> halls = mcs.listHalls();
            if(halls == null){
                return null;
            }else{
                for(Hall newHall : halls)
                    if(hall.id.equals(newHall.id)) hall = newHall;
                return comments;
            }
        }

        @Override
        protected void onPostExecute(List<Comment> comments) {

            if(comments == null){
                Toast.makeText(context, "Comments not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.v(TAG, comments.size() + " Comments Found...");

            HomeActivity.PopulateInfo(hall, card_content, context, false);

            ListView comment_list = (ListView)findViewById(R.id.comment_list);
            comment_list.setAdapter(new CommentsAdapter(context, R.layout.comment_card, comments));

            if(swiper != null) swiper.setRefreshing(false);

            if(idToFind != null){
                for(int i=0; i<comment_list.getCount(); i++){
                    Comment c = (Comment) comment_list.getItemAtPosition(i);
                    if(c.id.equals(idToFind)){
                        comment_list.setSelection(i);

                        break;
                    }
                }
            }
        }
    }

    public class CommentsAdapter extends ArrayAdapter<Comment> {

        SharedPreferences prefs = getSharedPreferences("USER_ID", MODE_PRIVATE);

        public CommentsAdapter(Context context, int resource, List<Comment> objects) {
            super(context, resource, R.id.comment_text, objects);
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            final Comment comment = this.getItem(position);
            final View item_view = super.getView(position, convertView, parent);

            if(prefs.contains(MemChewService.USER_ID_KEY)){
                String id = prefs.getString(MemChewService.USER_ID_KEY, "NULL");
                if(comment.user.toString().equals(id)){
                    int color = getContext().getResources().getColor(R.color.user_comment_color);
                    setCommentColor(item_view, color);
                }else{
                    setCommentColor(item_view, Color.WHITE);
                }
            }

            TextView comment_text = (TextView) item_view.findViewById(R.id.comment_text);
            if(comment.text != null) {
                comment_text.setVisibility(View.VISIBLE);
                comment_text.setText(comment.text);
            }
            else comment_text.setVisibility(View.GONE);

            ImageView comment_image = (ImageView) item_view.findViewById(R.id.comment_image);
            if(comment.image != null) {
                comment_image.setVisibility(View.VISIBLE);

                byte[] bytes = Base64.decode(comment.image, Base64.DEFAULT);
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                comment_image.setImageBitmap(image);
            } else comment_image.setVisibility(View.GONE);

            ((TextView) item_view.findViewById(R.id.comment_time)).setText(comment.time);

            final MemChewService service = new MemChewService(getApplicationContext());

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
                downvoteButton.setColorFilter(defaultColor);
                score.setTextColor(upvoteColor);
            } else if(comment.rating.equals("downvote")) {
                downvoteButton.setColorFilter(downvoteColor);
                upvoteButton.setColorFilter(defaultColor);
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

    class CommentTask extends AsyncTask<Void, Void, String> {

        String text;
        String image;

        Hall hall;
        Context context;

        public CommentTask(Context context, String text, Hall hall, String image){
            this.text = text;
            this.hall = hall;
            this.context = context;
            this.image = image;
        }

        @Override
        protected String doInBackground(Void... voids) {
            MemChewService mcs = new MemChewService(getApplicationContext());
            return mcs.comment(hall.mealid, text, image);
        }

        @Override
        protected void onPostExecute(String result){
            new UpdateTask(context, null, hall, result).execute();
        }
    }

    public void submitComment() {
        final EditText edit = ((EditText) findViewById(R.id.editText));
        new CommentTask(this, edit.getText().toString(), hall, null).execute();
        edit.setText("");
        findViewById(R.id.comment_list).requestFocus();
    }

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File mediaStorageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "Could not create directory to store external images.");
                return;
            }
        }

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "capture.jpg");
        if(mediaFile.exists()) mediaFile.delete();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public static final int MAX_DIMENSION = 800;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File mediaStorageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "capture.jpg");

                // Resize image
                Bitmap capture = BitmapFactory.decodeFile(mediaFile.getPath());

                float ratio = Math.min((float)MAX_DIMENSION / capture.getWidth(), (float)MAX_DIMENSION / capture.getHeight());
                ratio = Math.min(ratio, 1.0f); // Clamp ratio to under 1.0

                int w = Math.round(ratio * capture.getWidth());
                int h = Math.round(ratio * capture.getHeight());
                Bitmap resized = Bitmap.createScaledBitmap(capture, w, h, true);

                // Compress to PNG
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 50, out);
                byte[] bytes = out.toByteArray();

                String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                Log.d(TAG, "Base 64 Encoded Image is " + base64.length() + " characters long.");

                new CommentTask(this, null, hall, base64).execute();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_hall);

        final HallActivity context = this;
        hall = (Hall)getIntent().getSerializableExtra("hall");
        setTitle(hall.name);

        card_content = findViewById(R.id.card_content);

        ((ImageButton) findViewById(R.id.sendcomment)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { context.submitComment(); }
        });
        ((ImageButton) findViewById(R.id.takephoto)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { context.takePhoto(); }
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
                new UpdateTask(context, swiper, hall, null).execute();
            }
        });

        swiper.setColorSchemeColors(Color.rgb(229, 28, 0),
                Color.rgb(177, 28, 8),
                Color.rgb(126, 26, 11),
                Color.rgb(177, 28, 8));

        HomeActivity.PopulateInfo(hall, card_content, this, false);
        new UpdateTask(this, swiper, hall, null).execute();
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
            case R.id.open_map:
                String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f(%s)", hall.latitude, hall.longitude, hall.name);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }catch(ActivityNotFoundException ex){
                    Toast.makeText(this, "Could not find map application.", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCommentColor(View comment_view, int color){
        comment_view.setBackgroundColor(color);
        comment_view.findViewById(R.id.info_block).setBackgroundColor(color);
        comment_view.findViewById(R.id.vote_block).setBackgroundColor(color);
    }
}
