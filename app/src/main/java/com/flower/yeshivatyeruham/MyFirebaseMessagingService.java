package com.flower.yeshivatyeruham;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.flower.yeshivatyeruham.AttendanceDialog.checkDateFormat;
import static com.flower.yeshivatyeruham.DataClass.NOT_UPDATED;
import static com.flower.yeshivatyeruham.DataClass.notificationId;

/**
 * get the message from server if the application is open or background, and handle it.
 * if not, android handle the MessageDialog.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }

//        sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getData().get("data"),
//                remoteMessage.getData().get("link"), remoteMessage.getNotification().getTitle());

//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean sendNtf = sharedPreferences.getBoolean("show_ntf",
//                this.getResources().getBoolean(R.bool.show_ntf_default));
//        if (sendNtf) {
        if (remoteMessage.getData().get("link").contains("נוכחות")) {     //checks whether the link is נוכחות
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("strings", MODE_PRIVATE);
            String date=sharedPref.getString("attLastDate", "");
            DateFormat dateFormat=checkDateFormat(date);

            try {
                if (new Date().before(dateFormat.parse(date)) && sharedPref.getBoolean("response", false))   //checks whether the person already responded to this attendance
                    return;
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            if (!isInList(getApplicationContext(), sharedPref.getString("myName", "")))
//                return;
        }
        sendNotification(remoteMessage.getData().get("body"),
                remoteMessage.getData().get("data"),
                remoteMessage.getData().get("link"),
                remoteMessage.getData().get("title"));
//        }

        if (!(remoteMessage.getData().get("link")).contains("נוכחות"))
            saveNotification(remoteMessage.getData().get("data"),
                    remoteMessage.getData().get("link"),
                    remoteMessage.getData().get("title"));


        //editor2.putString(getString(R.string.notificationData),oldData + remoteMessage.getNotification().getBody() + "D");
        //editor.putString(getString(R.string.silence), "true,10:30,1-2-3,true=1,false,true|");
        //editor2.commit();

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void saveNotification(String text, String link, String title) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.savedNtf), MODE_PRIVATE);

        String titles = sharedPref.getString(getString(R.string.savedNtf_titles), "");
        String texts = sharedPref.getString(getString(R.string.savedNtf_texts), "");
        String links = sharedPref.getString(getString(R.string.savedNtf_links), "");

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.savedNtf_titles), title + "<^>" + titles);
        editor.putString(getString(R.string.savedNtf_texts), text + "<^>" + texts);
        editor.putString(getString(R.string.savedNtf_links), link + "<^>" + links);

//        editor.putString(getString(R.string.savedNtf_titles), "");
//        editor.putString(getString(R.string.savedNtf_texts), "");
//        editor.putString(getString(R.string.savedNtf_links), "");

        editor.apply();
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody, String longtext, String link, String Title) {
        Intent intent;
        if (link.contains("נוכחות")) {
            intent = new Intent(this, AttendanceDialog.class);
            DataClass.updateStat=NOT_UPDATED;
        } else {
            intent = new Intent(this, MessageDialog.class);
            intent.putExtra("data", longtext);
            intent.putExtra("link", link);
            intent.putExtra("title", Title);
            intent.putExtra("massege", true);
        }
//       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("DEFAULT_CHANNEL", "הודעות אחרות", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "DEFAULT_CHANNEL")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(Title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId++ /* ID of notification */, notificationBuilder.build());
    }

    public static boolean isInList(Context context, String name) {
        try {
            FileInputStream fis = context.openFileInput(context.getString(R.string.localStudentsFN));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line;
            List<String> nameArr=new ArrayList<>(Arrays.asList(name.split(" ")));
            name=nameArr.remove(nameArr.size()-1)+" "+ TextUtils.join(" ", nameArr);
            do {
                line = br.readLine();
                if (line==null) break;
                if (line.equals(name))
                    return true;
            } while (line != null || line.isEmpty());
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
