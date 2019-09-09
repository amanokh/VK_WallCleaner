package ru.excalc.vk282;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKSdk;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.excalc.vk282.data.DataDbHelper;
import ru.excalc.vk282.data.DbCursor;
import ru.excalc.vk282.data.DbValues;
import ru.excalc.vk282.services.DeleteService;


public class WallActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    private DataDbHelper dbHelper;
    private SQLiteDatabase db;
    public static String userId;
    private int offset;
    private Button button3;
    private Button deleteButton;
    private ImageButton menuButton;
    private DbCursor mDbAdapter;
    private CheckBox repostsCheck;
    private CheckBox ownCheck;
    private CheckBox alienCheck;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private View header;
    private String userPicUri;
    private CircleImageView userPic;
    public static Bitmap bmp;
    private DatePickerDialog dialogFrom;
    private DatePickerDialog dialogTo;
    private Calendar fromCalendar;
    private Calendar toCalendar;
    private Boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccentDark));
        }
        mDbAdapter = new DbCursor(this);
        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();
        drawer = findViewById(R.id.drawer_layout);
        doubleBackToExitPressedOnce = false;

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        button3 = findViewById(R.id.deleteButton);
        Button e1 = findViewById(R.id.dateFrom);
        Button e2 = findViewById(R.id.dateTo);
        e2.setOnClickListener(this);
        e1.setOnClickListener(this);
        menuButton = findViewById(R.id.menuButton);
        deleteButton = findViewById(R.id.deleteButton);
        repostsCheck = findViewById(R.id.repostsCheck);
        ownCheck = findViewById(R.id.ownCheck);
        alienCheck = findViewById(R.id.alienCheck);

        button3.setOnClickListener(this);
        menuButton.setOnClickListener(this);

        repostsCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                disableButton();
                countPosts();
            }
        });
        ownCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                disableButton();
                countPosts();
            }
        });
        alienCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                disableButton();
                countPosts();
            }
        });

        dbHelper = new DataDbHelper(this);
        db = dbHelper.getWritableDatabase();
        offset = 1;
        //db.delete(DbValues.PostsEntry.TABLE_NAME, null, null);
        //getPosts();

        Cursor cursorAll = mDbAdapter.getAll();
        SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy", new Locale("ru"));
        int colIndex = cursorAll.getColumnIndex(DbValues.PostsEntry.COLUMN_TIMESTAMP);
        long timeStampFrom = 0;
        long timeStampTo = 0;

        if (cursorAll.moveToFirst()){
            timeStampFrom = (long) cursorAll.getInt(colIndex)*1000;
            Date date = new Date(timeStampFrom);
            toCalendar = Calendar.getInstance();
            toCalendar.setTime(date);
            Button editTextTo = findViewById(R.id.dateTo);
            editTextTo.setText(format.format(date));
        }
        if (cursorAll.moveToLast()){
            timeStampTo = (long) cursorAll.getInt(colIndex)*1000;
            Date date = new Date(timeStampTo);
            fromCalendar = Calendar.getInstance();
            fromCalendar.setTime(date);
            Button editTextFrom = findViewById(R.id.dateFrom);
            editTextFrom.setText(format.format(date));
        }
        countPosts();
        disableButton();

        VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_max,domain")).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                String userDomain;
                header = navigationView.getHeaderView(0);

                VKApiUser user = ((VKList<VKApiUser>)response.parsedModel).get(0);
                try {
                    userDomain = "@" + user.fields.getString("domain");
                } catch (JSONException e) {userDomain="";}
                String userName = user.first_name + " " + user.last_name;
                TextView userNameTV = header.findViewById(R.id.userName);
                TextView userUrlTV = header.findViewById(R.id.userUrl);
                userNameTV.setText(userName);
                userUrlTV.setText(userDomain);
                userPicUri = user.photo_max;
                userId = String.valueOf(user.id);
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            URL url = new URL(userPicUri);
                            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            userPic = header.findViewById(R.id.userPic);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                this.finishAffinity();
                super.finish();
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_wall) {

        } else if (id == R.id.nav_help) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/amnkhn_live"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_send) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/amnkhn"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_logout){
            VKSdk.logout();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deleteButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Удаление записей")
                        .setMessage("Все выбранные вами записи будут удалены с вашей стены. Это действие необратимо. Продолжить?")
                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                deletePosts();
                            }
                        })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.menuButton:
                drawer.openDrawer(GravityCompat.START);
                break;
            case R.id.dateFrom:
                int fromYear = fromCalendar.get(Calendar.YEAR);
                int fromMonth = fromCalendar.get(Calendar.MONTH);
                int fromDay = fromCalendar.get(Calendar.DAY_OF_MONTH);
                dialogFrom = new DatePickerDialog(this, 0, dateFromSet, fromYear, fromMonth, fromDay);
                dialogFrom.show();
                break;
            case R.id.dateTo:
                int toYear = toCalendar.get(Calendar.YEAR);
                int toMonth = toCalendar.get(Calendar.MONTH);
                int toDay = toCalendar.get(Calendar.DAY_OF_MONTH);
                dialogTo = new DatePickerDialog(this, 0, dateToSet, toYear, toMonth, toDay);
                dialogTo.show();
                break;
        }
        // закрываем подключение к БД
        //dbHelper.close();
    }
    DatePickerDialog.OnDateSetListener dateFromSet = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy", new Locale("ru"));
            Button editTextFrom = findViewById(R.id.dateFrom);
            fromCalendar = Calendar.getInstance();
            fromCalendar.set(year,monthOfYear,dayOfMonth,0,0,0);
            Date date = fromCalendar.getTime();
            editTextFrom.setText(format.format(date));
            countPosts();
        }
    };
    DatePickerDialog.OnDateSetListener dateToSet = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy", new Locale("ru"));
            Button editTextFrom = findViewById(R.id.dateTo);
            toCalendar = Calendar.getInstance();
            toCalendar.set(year,monthOfYear,dayOfMonth,23,59,59);
            Date date = toCalendar.getTime();
            editTextFrom.setText(format.format(date));
            countPosts();
        }
    };

    private void disableButton(){
        if ((repostsCheck.isChecked() || ownCheck.isChecked() || alienCheck.isChecked()) && countPosts()!=0) deleteButton.setEnabled(true);
        else deleteButton.setEnabled(false);
    }
    private void parseVKJSON(VKResponse response){
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
                Cursor cursorAll = mDbAdapter.getAll();
                SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy", new Locale("ru"));
                int colIndex = cursorAll.getColumnIndex(DbValues.PostsEntry.COLUMN_TIMESTAMP);
                long timeStampFrom = 0;
                long timeStampTo = 0;

                if (cursorAll.moveToFirst()){
                    timeStampFrom = (long) cursorAll.getInt(colIndex)*1000;
                    Date date = new Date(timeStampFrom);
                    toCalendar = Calendar.getInstance();
                    toCalendar.setTime(date);
                    Button editTextTo = findViewById(R.id.dateTo);
                    editTextTo.setText(format.format(date));
                }
                if (cursorAll.moveToLast()){
                    timeStampTo = (long) cursorAll.getInt(colIndex)*1000;
                    Date date = new Date(timeStampTo);
                    fromCalendar = Calendar.getInstance();
                    fromCalendar.setTime(date);
                    Button editTextFrom = findViewById(R.id.dateFrom);
                    editTextFrom.setText(format.format(date));
                }
                countPosts();

            }
        } catch (JSONException e) {}

    }
    private void getPosts(){
        VKRequest request = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID, userId, VKApiConst.COUNT, "100", VKApiConst.OFFSET, String.valueOf(offset)));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response){
                parseVKJSON(response);
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
    private void deletePosts(){
        final String EXTRA_TYPES = "extra_types";
        long timeFrom = fromCalendar.getTimeInMillis() / 1000;
        long timeTo = toCalendar.getTimeInMillis() / 1000;

        //Log.d("timefromto", String.valueOf(timeFrom)+" "+String.valueOf(timeTo));

        ArrayList<String> selArgs = new ArrayList<String>();
        String selection = "time >= " + String.valueOf(timeFrom)+ " AND time <= " + String.valueOf(timeTo) + " AND ";


        if (repostsCheck.isChecked()) selArgs.add("type = 0");
        if (ownCheck.isChecked()) selArgs.add("type = 1");
        if (alienCheck.isChecked()) selArgs.add("type = 2");

        selection += TextUtils.join(" OR ", selArgs);

        Intent intent = new Intent(this, DeleteService.class);
        intent.putExtra(EXTRA_TYPES, selection);
        intent.putExtra("userid", userId);
        startService(intent);

        Intent intentActivity = new Intent(this, PendingActivity.class);
        startActivity(intentActivity);

    }
    private int countPosts(){
        long timeFrom = fromCalendar.getTimeInMillis() / 1000;
        long timeTo = toCalendar.getTimeInMillis() / 1000;
        //Log.d("timefromto", String.valueOf(timeFrom)+" "+String.valueOf(timeTo));
        Cursor mCursorOwn = mDbAdapter.getOwn((int)timeFrom, (int)timeTo);
        Cursor mCursorReposts = mDbAdapter.getReposts((int)timeFrom, (int)timeTo);
        Cursor mCursorAlien = mDbAdapter.getAlien((int)timeFrom, (int)timeTo);

        int OwnCount = mCursorOwn.getCount();
        int RepostsCount = mCursorReposts.getCount();
        int AlienCount = mCursorAlien.getCount();

        int OwnBool = ownCheck.isChecked() ? 1:0;
        int RepostsBool = repostsCheck.isChecked() ? 1:0;
        int AlienBool = alienCheck.isChecked() ? 1:0;

        int all = OwnCount*OwnBool+RepostsCount*RepostsBool+AlienCount*AlienBool;

        TextView repostsT = findViewById(R.id.repostsCount1);
        TextView ownT = findViewById(R.id.ownCount1);
        TextView alienT = findViewById(R.id.alienCount1);
        Button deleteButton = findViewById(R.id.deleteButton);

        repostsT.setText(" ·  " + RepostsCount);
        ownT.setText(" ·  " + OwnCount);
        alienT.setText(" ·  " + AlienCount);
        deleteButton.setText("удалить "+ String.valueOf(all)+Utils.caseCount(all));

        return all;
    }


}
