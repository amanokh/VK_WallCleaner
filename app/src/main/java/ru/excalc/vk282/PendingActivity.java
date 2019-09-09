package ru.excalc.vk282;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.excalc.vk282.services.DeleteService;

public class PendingActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private Button stopButton;
    private int mShortAnimationDuration;
    private Boolean isStopped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isStopped = false;
        setContentView(R.layout.activity_pending);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("elapse"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverClose,
                new IntentFilter("close_service"));
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        progressBar = findViewById(R.id.progressView);
        progressBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DeleteService.class);
                stopService(intent);
                PendingActivity.super.onBackPressed();
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1);
            }
        });
        ImageView userPic = findViewById(R.id.userPicProgress);
        userPic.setImageBitmap(WallActivity.bmp);


    }

    @Override
    public void onBackPressed() {
        if (!isStopped) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Остановка удаления")
                    .setMessage("Вы действительно хотите остановить удаление?")
                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getApplicationContext(), DeleteService.class);
                            stopService(intent);
                            PendingActivity.super.onBackPressed();
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
        } else PendingActivity.super.onBackPressed();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                int[] message = intent.getIntArrayExtra("message");
                TextView counter = findViewById(R.id.counter);
                counter.setText(message[0] + "/" + message[1]);
                double step = ((double) message[0] / (double) message[1]) * 100;
                progressBar.setProgress((int) step);
            }

    };
    private BroadcastReceiver mMessageReceiverClose = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isStopped = true;
            Intent intentService = new Intent(getApplicationContext(), DeleteService.class);
            stopService(intentService);
            ProgressBar doneCircle = findViewById(R.id.progressViewDone);
            TextView progressText = findViewById(R.id.counter);
            TextView header = findViewById(R.id.header);
            TextView desc = findViewById(R.id.desc);

            stopButton.setVisibility(View.GONE);
            doneCircle.setAlpha(0f);
            doneCircle.setVisibility(View.VISIBLE);
            doneCircle.animate()
                    .alpha(1f)
                    .setDuration(mShortAnimationDuration)
                    .setListener(null);
            progressText.animate()
                    .alpha(0f)
                    .setDuration(mShortAnimationDuration)
                    .setListener(null);
            header.setText("Записи удалены");
            desc.setText("Теперь ваша стена очищена от того, что вы выбрали.");
        }

    };

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverClose);
        super.onDestroy();
    }
}
