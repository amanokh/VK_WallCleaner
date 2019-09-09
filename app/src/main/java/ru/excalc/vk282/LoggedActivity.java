package ru.excalc.vk282;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;


public class LoggedActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);


        VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_max","domain")).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VKApiUser user = ((VKList<VKApiUser>)response.parsedModel).get(0);
                try {
                    String userDomain = user.fields.getString("domain");
                } catch (JSONException e) {String userDomain="fusk";}
                String userName = user.first_name + " " + user.last_name;
                String userPicUri = user.photo_max;

            }
        });
        Button b_wall = findViewById(R.id.b_wall);
        View.OnClickListener CleanWall = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), WallActivity.class));
            }
        };
        b_wall.setOnClickListener(CleanWall);
    }
}

