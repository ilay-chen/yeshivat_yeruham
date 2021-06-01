package com.flower.yeshivatyeruham;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.renderscript.RenderScript;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static com.flower.yeshivatyeruham.DataClass.notificationId;

public class RecordingService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String ACTION_START_RECORD = "ACTION_START_RECORD";

    public static final String ACTION_STOP_RECORD = "ACTION_STOP_RECORD";

    public static final String ACTION_PAUSE_RECORD = "ACTION_PAUSE_RECORD";

    public static final String ACTION_RESTART_RECORD = "ACTION_RESTART_RECORD";

    String AudioSavePathInDevice = "", AudioSavePath = DataClass.AudioSavePath;
    String tempName = "שיעור לא מתויג ";
    MediaRecorder mediaRecorder ;
    AudioRecord ar;
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer;
    //day []days = new day[8];
    private RecMicToMp3 mRecMicToMp3;
    VisualizerView visualizerView;

    NotificationManager notificationManager;
    int recordNotification;
    //    public static int curVol = 0;
    public static int amplitude = 0;
    NotificationCompat.Builder mBuilder;


    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    public RecordingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG_FOREGROUND_SERVICE, "My foreground service onCreate().");
    }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if(intent != null)
            {
                createNotificationChannel();
                Intent notificationIntent = new Intent(this, RecordingActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        0, notificationIntent, 0);

                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("הקלטה מתבצעת ברקע")
                        .setContentText("לחץ לחזרה")
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(pendingIntent)
                        .build();

                startForeground(1, notification);

                String action = intent.getAction();

                switch (action)
                {
                    case ACTION_START_FOREGROUND_SERVICE:
                        startForegroundService();
                       // Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopForegroundService();
                        //Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                        break;
                    case ACTION_START_RECORD:
                        AudioSavePathInDevice = intent.getStringExtra("fileName");
                        startRecord();
                        //Toast.makeText(getApplicationContext(), "You click Play button.", Toast.LENGTH_LONG).show();
                        break;
                    case ACTION_STOP_RECORD:
                        stopRecord();
                        //Toast.makeText(getApplicationContext(), "You click Pause button.", Toast.LENGTH_LONG).show();
                        stopForegroundService();
                        break;
                    case ACTION_PAUSE_RECORD:
                        pauseRecord();
                        //Toast.makeText(getApplicationContext(), "You click Pause button.", Toast.LENGTH_LONG).show();
                        break;
                    case ACTION_RESTART_RECORD:
                        restartRecord();
                        //Toast.makeText(getApplicationContext(), "You click Pause button.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
            return super.onStartCommand(intent, flags, startId);
        }

    public void pauseRecord(){
        mRecMicToMp3.pause();
    }

    public void restartRecord(){
        mRecMicToMp3.reStart();
    }


    public void startRecord()
    {
        MediaRecorderReady();
        mRecMicToMp3.start();
    }

    public void stopRecord()
    {
        mRecMicToMp3.stop();
    }

        /* Used to build and start foreground service. */
        private void startForegroundService()
        {
            Log.d(TAG_FOREGROUND_SERVICE, "Start foreground service.");

//            mBuilder = new NotificationCompat.Builder(this, "Recording_CHANNEL")
//                    .setContentTitle("מבצעת הקלטה ברקע")
//                    .setContentText("לחץ לחזרה")
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setTicker("מתחיל בהקלטה...")
//                    .setOngoing(true);
//
//            notificationManager =
//                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//            Intent intent = new Intent(this, RecordingActivity.class);
//            intent.setAction(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//            mBuilder.setContentIntent(pendingIntent);
//
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                NotificationChannel channel = new NotificationChannel("Recording_CHANNEL", "הקלטה", NotificationManager.IMPORTANCE_HIGH);
//                NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                notificationManager.createNotificationChannel(channel);
//                //mBuilder.setFullScreenIntent(pendingIntent, true);
//            }
//
//            recordNotification = notificationId++;
//            //mBuilder.setWhen(System.currentTimeMillis());
//            notificationManager.notify(recordNotification, mBuilder.build());
//
//            NotificationManager notificationManager =
//                    (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
//            Notification notification = new Notification(/* your notification */);
//
//            //notification.setLatestEventInfo(this, /* your content */, pendingIntent);
//            notificationManager.notify(2, notification);
//
//            // Build the notification.
//             //notification = mBuilder.build();
//
//            // Start foreground service.
//            startForeground(1, notification);
        }

        private void stopForegroundService()
        {
            Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");

            // Stop foreground service and remove the notification.
            stopForeground(true);

            // Stop the foreground service.
            stopSelf();
        }

    // start recording Object
    public void MediaRecorderReady(){
        mRecMicToMp3 = new RecMicToMp3(AudioSavePathInDevice, 44100);

        mRecMicToMp3.setHandle(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RecMicToMp3.MSG_REC_STARTED:
                        break;
                    case RecMicToMp3.MSG_REC_STOPPED:
                        break;
                    case RecMicToMp3.MSG_ERROR_GET_MIN_BUFFERSIZE:
                        break;
                    case RecMicToMp3.MSG_ERROR_CREATE_FILE:
                        break;
                    case RecMicToMp3.MSG_ERROR_REC_START:
                        break;
                    case RecMicToMp3.MSG_ERROR_AUDIO_RECORD:
                        break;
                    case RecMicToMp3.MSG_ERROR_AUDIO_ENCODE:
                        break;
                    case RecMicToMp3.MSG_ERROR_WRITE_FILE:
                        break;
                    case RecMicToMp3.MSG_ERROR_CLOSE_FILE:
                        break;
                    default:
                        break;
                }
            }
        });

    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    }