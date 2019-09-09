package ru.excalc.vk282.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import ru.excalc.vk282.PendingActivity;
import ru.excalc.vk282.R;
import ru.excalc.vk282.Utils;
import ru.excalc.vk282.WallActivity;
import ru.excalc.vk282.data.DbCursor;
import ru.excalc.vk282.data.DbValues;

public class DeleteService extends Service {
    private DbCursor mDbAdapter;
    private String selection_types;
    private boolean isStopped;
    private String userId;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private NotificationChannel mNotificationChannel;
    private static final int notifyId = 1;
    private Notification notification;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Log.d("test",selection_types);

            Cursor delCursor = mDbAdapter.getAllByDate(selection_types);
            int allCount = delCursor.getCount();
            int thisCount = 0;
            if (delCursor.moveToFirst()) {
                // определяем номера столбцов по имени в выборке
                int owner_id = delCursor.getColumnIndex(DbValues.PostsEntry.COLUMN_FROM_ID);
                int post_id = delCursor.getColumnIndex(DbValues.PostsEntry.COLUMN_POST_ID);
                int type_id = delCursor.getColumnIndex(DbValues.PostsEntry.COLUMN_TYPE);
                do {
                    // получаем значения по номерам столбцов и пишем все в лог
                    thisCount++;
                    String postId = delCursor.getString(post_id);
                    VKRequest deleteRequest = VKApi.wall().delete(VKParameters.from(VKApiConst.OWNER_ID, userId, VKApiConst.POST_ID, postId));
                    deleteRequest.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response){
                            //Log.d("VKdel", "post deleted");
                            //Log.d("response",response.responseString);
                        }
                        @Override
                        public void onError(VKError error) {
                            Log.d("VKdel", "posts err");
                            Log.d("VKdel", error.errorReason);
                            Log.d("VKdel", String.valueOf(error.errorCode));
                            Toast.makeText(getApplicationContext(), error.errorMessage, Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                            Log.d("VKreq", "posts fail");
                        }
                    });
                    SystemClock.sleep(500);
                    if (!isStopped) {
                        Intent intentMessage = new Intent("elapse");
                        intentMessage.putExtra("message", new int[]{thisCount, allCount});
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentMessage);
                        mNotifyBuilder.setContentText("Удалено " + String.valueOf(thisCount) + " из " + String.valueOf(allCount) + Utils.caseCount(allCount))
                                .setProgress(allCount, thisCount, false);
                        mNotificationManager.notify(notifyId, mNotifyBuilder.build());
                    }
                } while (delCursor.moveToNext() && !isStopped);
                Intent intentMessage1 = new Intent("close_service");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentMessage1);
                stopForeground(true);
                if (!isStopped) {
                    Intent notificationIntent = new Intent(getApplicationContext(),
                            WallActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                            notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    mNotifyBuilder.setContentText("Удалено " + String.valueOf(allCount) + Utils.caseCount(allCount))
                            .setContentTitle("Очистка завершена")
                            .setProgress(0, 0, false)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent);
                    mNotificationManager.notify(notifyId, mNotifyBuilder.build());
                }
                isStopped = true;
            } else //Log.d("DBRead", "0 rows");
            delCursor.close();
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();
        isStopped = false;
        mDbAdapter = new DbCursor(this);

        Intent notificationIntent = new Intent(getApplicationContext(),
                PendingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationChannel = new NotificationChannel("MAIN_CHANNEL", "Уведомления", NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
            mNotifyBuilder = new NotificationCompat.Builder(this, "MAIN_CHANNEL");
        } else mNotifyBuilder = new NotificationCompat.Builder(this);

        mNotifyBuilder.setContentTitle("Идёт удаление постов")
                .setContentText("Ожидание")
                .setSmallIcon(R.drawable.ic_menu_wall)
                .setContentIntent(pendingIntent)
                .setProgress(0,0,true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setAutoCancel(false);
        startForeground(notifyId, mNotifyBuilder.build());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        startForeground(notifyId, mNotifyBuilder.build());
        selection_types = intent.getStringExtra("extra_types");
        userId = intent.getStringExtra("userid");
        new Thread(runnable).start();


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (!isStopped) Toast.makeText(this, "Некоторые записи не были удалены", Toast.LENGTH_LONG).show();
        isStopped = true;
        //mNotificationManager.cancel(notifyId);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
