package ru.excalc.vk282;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.excalc.vk282.data.DataDbHelper;
import ru.excalc.vk282.data.DbValues;

public class LoadingActivity extends AppCompatActivity {

    private DataDbHelper dbHelper;
    private SQLiteDatabase db;
    private int offset;
    private String userPicUri;
    private CircleImageView userPic;
    private String userId;
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ProgressBar progressBar = findViewById(R.id.progressView);
        progressBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
        offset = 1;
        dbHelper = new DataDbHelper(this);
        db = dbHelper.getWritableDatabase();
        db.delete(DbValues.PostsEntry.TABLE_NAME, null, null);

        VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_max,domain")).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VKApiUser user = ((VKList<VKApiUser>)response.parsedModel).get(0);
                userPicUri = user.photo_max;
                userId = String.valueOf(user.id);
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            URL url = new URL(userPicUri);
                            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            userPic = findViewById(R.id.userPicProgress);
                            userPic.post(new Runnable() {
                                public void run() {
                                    userPic.setImageBitmap(bmp);
                                }
                            });
                        } catch (java.net.MalformedURLException e) {}
                        catch (java.io.IOException e) {}
                    }
                }).start();

            }
        });
        getPosts();
    }
    private void parseVKJSON(VKResponse response){


    }
    private void getPosts() {
        new Thread(new Runnable() {
            public void run() {
                VKRequest request = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID, userId, VKApiConst.COUNT, "100", VKApiConst.OFFSET, String.valueOf(offset)));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        parseVKJSON(response);
                        try {
                            JSONObject responseJSON = response.json.getJSONObject("response");
                            JSONArray posts = responseJSON.getJSONArray("items");
                            for (int i = 0; i < posts.length(); i++){
                                int postId;
                                int timestamp;
                                int type;
                                int pubId;

                                JSONObject post = posts.getJSONObject(i);
                                postId = post.getInt("id");
                                timestamp = post.getInt("date");
                                try {
                                    JSONArray copyHistory = post.getJSONArray("copy_history");
                                    type = 0;
                                    pubId = copyHistory.getJSONObject(0).getInt("owner_id");
                                } catch (JSONException e) {
                                    if (post.getInt("from_id")==post.getInt("owner_id")){
                                        type = 1;
                                        pubId = post.getInt("owner_id");
                                    } else {
                                        type = 2;
                                        pubId = post.getInt("from_id");
                                    }
                                }

                                ContentValues postsValues = new ContentValues();

                                postsValues.put(DbValues.PostsEntry.COLUMN_POST_ID, postId);
                                postsValues.put(DbValues.PostsEntry.COLUMN_TIMESTAMP, timestamp);
                                postsValues.put(DbValues.PostsEntry.COLUMN_TYPE, type);
                                postsValues.put(DbValues.PostsEntry.COLUMN_FROM_ID, pubId);

                                long rowID = db.insert(DbValues.PostsEntry.TABLE_NAME, null, postsValues);
                            }



                            if (responseJSON.getInt("count") - offset > 100){
                                offset += 100;
                                getPosts();
                            } else {
                                db.close();
                                dbHelper.close();
                                startActivity(new Intent(getApplicationContext(), WallActivity.class));
                            }
                        } catch (JSONException e) {}
                        //Log.d("VKreq", "posts loaded");
                    }

                    @Override
                    public void onError(VKError error) {
                        Log.d("VKreq", "posts err");
                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                        Log.d("VKreq", "posts fail");
                    }
                });
            }
        }).start();
    }
}
