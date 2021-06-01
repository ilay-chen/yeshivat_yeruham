package com.flower.yeshivatyeruham;

import android.app.IntentService;
import android.content.Intent;

public class RecognizeService extends IntentService {
    public RecognizeService(){
        super("RecognizeService");
    }
    @Override
    protected void onHandleIntent(Intent intent){

//        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        tm.listen(new CallListener(getApplicationContext()), PhoneStateListener.LISTEN_CALL_STATE);
    }

   @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        onHandleIntent(intent);
        return START_STICKY;
    }
}

