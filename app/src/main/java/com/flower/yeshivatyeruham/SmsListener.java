package com.flower.yeshivatyeruham;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import static com.flower.yeshivatyeruham.DataClass.ContactPath;
import static com.flower.yeshivatyeruham.DataClass.cxt;
import static com.flower.yeshivatyeruham.DataClass.messagePhoneNumbers;
import static com.flower.yeshivatyeruham.DataClass.notificationId;

/**
 * called when the phone get a call.
 * check if the nuber is in the contacts of the application and not already in phone.
 * if true - write the name and Shiur in a notification.
 * depend on preference choice - recognize massages.
 */
public class SmsListener extends BroadcastReceiver {


    protected static Context ctx;
    String error = "error";
    final SmsManager sms = SmsManager.getDefault();
    static String address, str = null;
    boolean isSame;


//    public SmsListener(){
//    }
//
//    public SmsListener(Context c){
//        super();
//        this.ctx = c.getApplicationContext();
//    }

    public Boolean isFirstTime()
    {
        SharedPreferences sharedPref = cxt.getSharedPreferences("strings", cxt.MODE_PRIVATE);

        //firstTime = sharedPref.getBoolean("firstTime",false);
        return sharedPref.getBoolean("theFirstTime",true);
    }

    public Boolean isAlreadyConnected() {
        SharedPreferences sharedPref = cxt.getSharedPreferences("strings", cxt.MODE_PRIVATE);
        return sharedPref.getBoolean("loggedIn", false);
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!isAlreadyConnected()){
            verifyNum(context, intent);
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        Boolean recognize = sharedPreferences.getBoolean("sms_recognition", cxt.getResources().getBoolean(R.bool.sms_recognition_default));
        if (recognize) {
            final Bundle bundle = intent.getExtras();
            ctx = context.getApplicationContext();
            if (!isFirstTime()){
                try {
                    if (bundle != null) {
                        final Object[] pdusObj = (Object[]) bundle.get("pdus");
                        for (int i = 0; i < pdusObj.length; i++) {
                            SmsMessage currentMessage;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                String format = bundle.getString("format");
                                currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i], format);
                            } else {
                                currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                            }
                            String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                            phoneNumber = phoneNumber.replace("+972", "0");
                            String senderNum = phoneNumber;
                            String message = currentMessage.getDisplayMessageBody();

                            if (phoneNumber != "" && phoneNumber != null && !isAlreadyMissed(phoneNumber)) {
                                String name = getName(phoneNumber);
                                messagePhoneNumbers.add(phoneNumber);
                                if (!name.equals(error)) {
                                    if (!name.equals(error) && !contactExists(phoneNumber)) {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            NotificationChannel channel = new NotificationChannel("Calls_SMS_CHANNEL", "זיהוי שיחות והודעות", NotificationManager.IMPORTANCE_LOW);
                                            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
                                            notificationManager.createNotificationChannel(channel);
                                        }

                                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, "Calls_SMS_CHANNEL")
                                                .setContentTitle("התקבלה הודעה חדשה")
                                                .setContentText(name + " - " + phoneNumber)
                                                .setSmallIcon(R.drawable.ic_notification)
                                                .setTicker("הודעה חדשה: " + name)
                                                .setAutoCancel(true);
                                        NotificationManager notificationManager =
                                                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

                                        Intent details = new Intent(context, DetailsActivity.class);
                                        details.putExtra("name", name.split(", מחזור ")[0]);
                                        details.putExtra("number", phoneNumber);
                                        details.putExtra("group", name.split(", מחזור ")[1]);

                                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                                                details, PendingIntent.FLAG_UPDATE_CURRENT);
                                        mBuilder.setContentIntent(contentIntent);

                                        notificationManager.notify(notificationId++, mBuilder.build());
                                        //Toast.makeText(ctx, name + " מתקשר אליך", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        } // end for loop
                    } // bundle is null
                } catch (Exception e) {
                }
            }
        }
    }

    private void verifyNum(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            SmsMessage[] msgs = getMessagesFromIntent(intent);
            if (msgs != null)
            {
                for (int i = 0; i < msgs.length; i++)
                {
                    address = msgs[i].getOriginatingAddress();
                    str = msgs[i].getMessageBody().toString();
                }
            }
            Log.v("test", "Originating Address : Sender : "+address);
            Log.v("test", "Message from sender : " +str);
            isSame = PhoneNumberUtils.compare(address, LoginActivity.phNo);
            Log.v("Comparison :", "Yes this true. "+isSame);
            if(isSame)
            {
                LoginActivity.wasMyOwnNumber = isSame;
              //  LoginActivity.workDone=true;
            }

            // ---send a broadcast intent to update the SMS received in the
            // activity---
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("SMS_RECEIVED_ACTION");
            broadcastIntent.putExtra("sms", str);
            context.sendBroadcast(broadcastIntent);
        }
    }

    public static SmsMessage[] getMessagesFromIntent(Intent intent)
    {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];
        Bundle bundle = intent.getExtras();

        for (int i = 0; i < messages.length; i++)
        {
            pduObjs[i] = (byte[]) messages[i];
        }

        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++)
        {
            pdus[i] = pduObjs[i];

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String format = bundle.getString("format");
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
            } else {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
        }


        return msgs;
    }

    public Boolean isAlreadyMissed(String phoneNumber)
    {
        for(String phone : messagePhoneNumbers)
            if (phone.equals(phoneNumber))
                return true;
        return false;
    }

    private boolean contactExists (String incoming){
        if (incoming!= null) {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incoming));
            String[] mPNprojection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
            Cursor cur = ctx.getContentResolver().query(lookupUri, mPNprojection, null, null, null);
            try {
                if (cur.moveToFirst()) {
                    return true;
                }
            } finally {
                if (cur != null)
                    cur.close();
            }
            return false;
        }
        return false;
    }

    private String getName (String incomingNumber) {
        BufferedReader reader = null;
        String answer= error;
        try {
            FileInputStream is = ctx.openFileInput(ContactPath);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String mLine = reader.readLine();
            String[] parts = new String[4];
            while (mLine != null) {
                if (!mLine.equals("") && !mLine.contains("#")) {
                    parts = mLine.split(",", 4);
                    if (incomingNumber.equals(parts[1])) {
                        reader.close();
                        answer = parts[0] + ", מחזור " + parts[2];
                        break;
                    }
                }
                mLine = reader.readLine();
            }
        }catch (Exception e) {
        }
        return answer;
    }
}
