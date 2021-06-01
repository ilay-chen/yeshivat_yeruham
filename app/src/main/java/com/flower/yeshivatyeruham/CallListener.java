package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.flower.yeshivatyeruham.DataClass.PicSavePath;
import static com.flower.yeshivatyeruham.DataClass.cxt;
import static com.flower.yeshivatyeruham.DataClass.notificationId;

/**
 * called when the phone get a call.
 * check if the nuber is in the contacts of the application and not already in phone.
 * if true - write the name and Shiur.
 * depend on preference choice - recognize phone calls.
 */
public class CallListener extends BroadcastReceiver {
    String error = "error";
    private static boolean ring = false;
    private static boolean callReceived = false;
    static String calledNum = "";
    Toast m_currentToast = null;
    Runnable timer;
    static View view;

//    private static WindowManager mWindowManager;

    protected Context ctx;
    public CallListener(){
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
            InputStream is = ctx.openFileInput("contacts_database.txt");
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

    public Boolean isFirstTime()
    {
        SharedPreferences sharedPref = ctx.getSharedPreferences("strings", ctx.MODE_PRIVATE);

        //firstTime = sharedPref.getBoolean("firstTime",false);
        return sharedPref.getBoolean("theFirstTime",true);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        ctx = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        Boolean recognize = sharedPreferences.getBoolean("call_recognition",
                cxt.getResources().getBoolean(R.bool.call_recognition_default));
        if (recognize) {
            final TelephonyManager telephony = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);

            telephony.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (!isFirstTime())
                        if (incomingNumber != null) {
                            switch (state) {
                                case TelephonyManager.CALL_STATE_RINGING:
                                    calledNum = incomingNumber;
                                    if(!ring){
                                        ring = true;
                                        Log.d("call", "ring");
                                        if (!incomingNumber.equals("")) {
                                            String name = getName(incomingNumber);
                                            if (!name.equals(error)) {
                                                if (!name.equals(error) && !contactExists(incomingNumber)) {
//
//                                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
//                                                .setContentTitle("שיחה נכנסת")
//                                                .setContentText(name + " - " + incomingNumber)
//                                                .setSmallIcon(R.drawable.ic_notification)
//                                                .setAutoCancel(true);
//                                        NotificationManager notificationManager =
//                                                (NotificationManager) ctx.getSystemService(ctx.NOTIFICATION_SERVICE);
//
//                                        notificationManager.notify(notificationId++, mBuilder.build());

                                                    whileToast(name, incomingNumber);
                                                }
                                            }
                                        }
                                    }
                                    break;

                                case TelephonyManager.CALL_STATE_OFFHOOK:
                                    if (ring) {
                                        ring = false;
                                        Log.d("call", "offhook");
                                        callReceived = true;
                                        try {
                                            ((WindowManager) ctx.getSystemService(Service.WINDOW_SERVICE)).removeView(view);
                                        } catch (Exception e){
                                            e.printStackTrace();

                                        }
//                                        if (view != null) {
//                                            mWindowManager.removeViewImmediate(view);
//                                            mWindowManager = null;
//                                            view = null;
//                                        }
                                    }
                                    break;

                                case TelephonyManager.CALL_STATE_IDLE:
                                    if (ring) {
                                        ring = false;
                                        Log.d("call", "idle");
                                        try {
                                            ((WindowManager) ctx.getSystemService(Service.WINDOW_SERVICE)).removeView(view);
                                        } catch (Exception e){
                                            e.printStackTrace();

                                        }
                                    //                                        if (view != null) {
//                                            mWindowManager.removeViewImmediate(view);
//                                            mWindowManager = null;
//                                            view = null;
//                                        }
                                        incomingNumber = calledNum;
                                        calledNum = "";
                                        if (!callReceived) {
                                            if (!incomingNumber.equals("")) {
                                                String name = getName(incomingNumber);
                                                if (!name.equals(error)) {
                                                    if (!name.equals(error) && !contactExists(incomingNumber)) {
                                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                            NotificationChannel channel = new NotificationChannel("Calls_SMS_CHANNEL", "זיהוי שיחות והודעות", NotificationManager.IMPORTANCE_LOW);
                                                            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
                                                            notificationManager.createNotificationChannel(channel);
                                                        }

                                                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, "Calls_SMS_CHANNEL")
                                                                .setContentTitle("שיחה שלא נענתה")
                                                                .setContentText(name + " - " + incomingNumber)
                                                                .setSmallIcon(R.drawable.ic_notification)
                                                                .setTicker("שיחה שלא נענתה: " + name)
                                                                .setAutoCancel(true);
                                                        NotificationManager notificationManager =
                                                                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

                                                        Intent details = new Intent(context, DetailsActivity.class);
                                                        details.putExtra("name", name.split(", מחזור ")[0]);
                                                        details.putExtra("number", incomingNumber);
                                                        details.putExtra("group", name.split(", מחזור ")[1]);

                                                        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                                                                details, PendingIntent.FLAG_UPDATE_CURRENT);
                                                        mBuilder.setContentIntent(contentIntent);

                                                        notificationManager.notify(notificationId++, mBuilder.build());
                                                    }
                                                }
                                            }
                                        }
                                        callReceived = false;
                                    }
                                    break;

                                default:
                                    break;
                            }
                        }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public void whileToast(final String name, final String num)
    {
        try{
//            mWindowManager = (WindowManager)ctx.getSystemService(WINDOW_SERVICE);
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.call_toast, null);
            TextView tv = (TextView) view.findViewById(R.id.txt);
            ImageButton close = (ImageButton) view.findViewById(R.id.closeButton);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.setVisibility(View.GONE);
                }
            });
            ImageView image = (ImageView) view.findViewById(R.id.image);
            tv.setText(name + "\nמתקשר אליך");
            //        image = new ImageView(ctx);
            //        image.setImageResource(R.drawable.add_contact_btn);

            final WindowManager.LayoutParams paramsF = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            paramsF.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            paramsF.x=0;
            paramsF.y=0;
            ((WindowManager) ctx.getSystemService(Service.WINDOW_SERVICE)).addView(view, paramsF);

            File f = new File(PicSavePath + num + ".jpeg");
            Glide.with(ctx)
                    .load(f)
                    .bitmapTransform(new CustomContactsList.CropSquareTransformation(ctx))
                    .placeholder(R.drawable.small_contact_pic)
                    .signature(new StringSignature((String.valueOf(f.lastModified()))))
                    .into(image);

            view.setOnTouchListener(new View.OnTouchListener() {
                WindowManager.LayoutParams paramsT = paramsF;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            ((WindowManager) ctx.getSystemService(Service.WINDOW_SERVICE)).updateViewLayout(v, paramsF);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e){
            view = null;
            Log.d("call", "err");

            final Handler handler = new Handler();
            timer = new Runnable(){
                @Override
                public void run() {

                    if (ring) {
                        handler.postDelayed(this, 3500);
                        Toast t = Toast.makeText(ctx, name + " מתקשר אליך", Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view = inflater.inflate(R.layout.call_toast, null);
                        TextView tv = (TextView) view.findViewById(R.id.txt);
                        ImageView image = (ImageView) view.findViewById(R.id.image);
                        tv.setText(name + "\nמתקשר אליך");
                        t.setView(view);
                        t.show();
                        File f = new File(PicSavePath + num + ".jpeg");
                        Glide.with(ctx)
                                .load(f)
                                .bitmapTransform(new CustomContactsList.CropSquareTransformation(ctx))
                                .placeholder(R.drawable.small_contact_pic)
                                .signature(new StringSignature((String.valueOf(f.lastModified()))))
                                .into(image);
                    }
                }
            };
            timer.run();
            e.printStackTrace();
        }
    }
}


