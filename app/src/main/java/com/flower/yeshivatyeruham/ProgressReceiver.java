package com.flower.yeshivatyeruham;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Receiver to get the upload lesson progress (because it in separate service)
 * sent progress to activity - to update view.
 */
public class ProgressReceiver extends BroadcastReceiver {
    public ProgressReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //LessonsList uploadActivity = ((DataClass) context.getApplicationContext()).LessonsList;
        Intent action = new Intent("Finish");
        action.putExtra("action",intent.getIntExtra("action",0));
        action.putExtra("percent",intent.getDoubleExtra("percent",0));
        context.sendBroadcast(action);
        //uploadActivity.showProgress();
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
