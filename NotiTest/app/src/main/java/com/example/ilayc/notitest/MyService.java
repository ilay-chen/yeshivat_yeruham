package com.example.ilayc.notitest;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;

import java.security.Provider;

public class MyService extends Service {
    public MyService() {
    }

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    int id = 1;

    public NotificationManager getmNotifyManager() {
        return mNotifyManager;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("אירוע חדש!")
                .setContentText("iugytdfyr")
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1 /* ID

        of notification */, notificationBuilder.build());

        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext());
        mBuilder.setContentTitle("הורדה ברקע")
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification);

        mBuilder.setProgress(0, 0, false);
        // Displays the progress bar for the first time.
        //allIds.add(notificationId++);

        mNotifyManager.notify(0, mBuilder.build());

//        mNotifyManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setContentTitle("Picture Download")
//                .setContentText("Download in progress")
//                .setSmallIcon(R.drawable.ic_notification);
//// Start a lengthy operation in a background thread
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        int incr;
//                        // Do the "lengthy" operation 20 times
//                        for (incr = 0; incr <= 100; incr += 5) {
//                            // Sets the progress indicator to a max value, the
//                            // current completion percentage, and "determinate"
//                            // state
//                            mBuilder.setProgress(100, incr, false);
//                            // Displays the progress bar for the first time.
//                            mNotifyManager.notify(id, mBuilder.build());
//                            // Sleeps the thread, simulating an operation
//                            // that takes time
//                            try {
//                                // Sleep for 5 seconds
//                                Thread.sleep(5 * 1000);
//                            } catch (InterruptedException e) {
//                                //Log.d(TAG, "sleep failure");
//                            }
//                        }
//                        // When the loop is finished, updates the notification
//                        mBuilder.setContentText("Download complete")
//                                // Removes the progress bar
//                                .setProgress(0, 0, false);
//                        mNotifyManager.notify(id, mBuilder.build());
//                    }
//                }
//// Starts the thread by calling the run() method in its Runnable
//        ).start();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        int a = 0;
        return null;
    }
}
