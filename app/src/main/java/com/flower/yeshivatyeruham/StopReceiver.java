package com.flower.yeshivatyeruham;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * BroadcastReceiver responsible of get the 'stop download' button click, and stop the right download.
 */
public class StopReceiver extends BroadcastReceiver {

    static Boolean isCancelled = false;
    static int toStop = -1;
    Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;
        String action = intent.getAction();

        if(action.equals("com.Flower.StopReceiver")){
            toStop = intent.getIntExtra("id",-1);
            isCancelled = true;  // this is a class variable
            //sendMessage("hide");
        }
    }
}
