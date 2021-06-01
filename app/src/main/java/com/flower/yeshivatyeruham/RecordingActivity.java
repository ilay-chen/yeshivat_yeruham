package com.flower.yeshivatyeruham;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.flower.yeshivatyeruham.DataClass.notificationId;

/**
 * activity to record the lesson.
 * with start, pause and stop option.
 */
public class RecordingActivity extends AppCompatActivity {

    ImageButton recordButton, stopButton;
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

    Timer timer;
    TimerTask timerTask;
    TextView timerView;
    Boolean isRecording = false, isPause = false;
    long startTime = 0, pausedTime = 0, startPause = 0;
    int recordNotification;
    //    public static int curVol = 0;
    public static int amplitude = 0;
    NotificationCompat.Builder mBuilder;


    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recordButton= (ImageButton) findViewById(R.id.record_button);
        stopButton = (ImageButton) findViewById(R.id.stop_button);

        timerView = (TextView) findViewById(R.id.timer_clock);
        visualizerView = (VisualizerView) findViewById(R.id.visualizer);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    if (!isPause)
                        pauseRecord();
                    else restartRecord();
                }
                else {
                    startRecord();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording) {
                    stopRecord();
                }
            }
        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean stayAwake = sharedPreferences.getBoolean("screen_on",
                this.getResources().getBoolean(R.bool.screen_on_default));
        if (stayAwake){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

//        new Thread(){
//
//            public void run(){
//                tv.setText("" + curVol);
//            }
//        }.start();
    }

    public void pauseRecord(){
        //mRecMicToMp3.pause();
        Intent serviceIntent = new Intent(this, RecordingService.class);
        serviceIntent.setAction(RecordingService.ACTION_PAUSE_RECORD);
        ContextCompat.startForegroundService(this, serviceIntent);

        startPause = System.currentTimeMillis();
        recordButton.setImageResource(R.drawable.start_recording);
        isPause = true;
    }

    public void restartRecord(){
        //mRecMicToMp3.reStart();
        Intent serviceIntent = new Intent(this, RecordingService.class);
        serviceIntent.setAction(RecordingService.ACTION_RESTART_RECORD);
        ContextCompat.startForegroundService(this, serviceIntent);

        //pauseTime = 0;
        pausedTime += System.currentTimeMillis() -startPause;
        isPause = false;
        recordButton.setImageResource(R.drawable.pause_record);
    }


    public void startRecord()
    {

        isRecording = true;

        int i = 1;
        String tempTempName = tempName + i;
        while (isNameExsist(tempTempName)) {
            i++;
            tempTempName = tempName + i;
        }
        tempName = tempTempName + ".mp3";

        if (checkPermission()) {

            AudioSavePathInDevice = AudioSavePath + tempName;

            //MediaRecorderReady();

            //mRecMicToMp3.start();

//            mBuilder = new NotificationCompat.Builder(this, "Recording_CHANNEL")
//                    .setContentTitle("מבצעת הקלטה ברקע")
//                    .setContentText("לחץ לחזרה")
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setTicker("מתחיל בהקלטה...")
//                    .setPriority(Notification.PRIORITY_MAX)
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
//                NotificationChannel channel = new NotificationChannel("Recording_CHANNEL", "הקלטה", NotificationManager.IMPORTANCE_LOW);
//                NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                notificationManager.createNotificationChannel(channel);
//                mBuilder.setFullScreenIntent(pendingIntent, true);
//            }
//
//            recordNotification = notificationId++;
            //notificationManager.notify(recordNotification, mBuilder.build());

            // Build the notification.
            //Notification notification = builder.build();

            // Start foreground service.

            Intent serviceIntent;


            //ContextCompat.startForegroundService(this, serviceIntent);

            startTimer();
            if(!isRecording)
            {
                serviceIntent = new Intent(this, RecordingService.class);
                serviceIntent.setAction(RecordingService.ACTION_START_FOREGROUND_SERVICE);
                ContextCompat.startForegroundService(this, serviceIntent);

            }
            serviceIntent = new Intent(this, RecordingService.class);
            serviceIntent.setAction(RecordingService.ACTION_START_RECORD);
            serviceIntent.putExtra("fileName", AudioSavePathInDevice);
            ContextCompat.startForegroundService(this, serviceIntent);

        }
        else
            Toast.makeText(RecordingActivity.this, "אין הרשאות הקלטה", Toast.LENGTH_SHORT).show();

        recordButton.setImageResource(R.drawable.pause_record);
        stopButton.setVisibility(View.VISIBLE);


//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("Recording_CHANNEL", "הקלטה", NotificationManager.IMPORTANCE_LOW);
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        mBuilder = new NotificationCompat.Builder(this, "Recording_CHANNEL")
//                .setContentTitle("מבצעת הקלטה ברקע")
//                .setContentText("לחץ לחזרה")
//                .setSmallIcon(R.drawable.ic_notification)
//                .setTicker("מתחיל בהקלטה...")
//                .setOngoing(true);
//
//        notificationManager =
//                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Intent intent = new Intent(this, RecordingActivity.class);
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        mBuilder.setContentIntent(pendingIntent);
//
//        recordNotification = notificationId++;
//        notificationManager.notify(recordNotification, mBuilder.build());
    }

    public void stopRecord()
    {
        isRecording = false;

        //mRecMicToMp3.stop();

        Intent serviceIntent = new Intent(this, RecordingService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

        serviceIntent = new Intent(this, RecordingService.class);
        serviceIntent.setAction(RecordingService.ACTION_STOP_RECORD);
        ContextCompat.startForegroundService(this, serviceIntent);


        stoptimertask();

        Intent saveRecord = new Intent(getBaseContext(), TaggingActivity.class);
        saveRecord.putExtra("tempName", tempName);
        startActivity(saveRecord);
        //notificationManager.cancel(recordNotification);

        finish();
    }

    public Boolean isNameExsist(String name)
    {
        String [] filesNames = getAllRecordsName();
        if(filesNames==null) return false;
        for(String str : filesNames)
            if(str.equals(name+".mp3"))
                return true;
        return false;
    }

    public String[] getAllRecordsName() {
        String path = AudioSavePath;

//        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files == null) return null;
        String []filesNames = new String[files.length];
//        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            filesNames[i] = files[i].getName();
//            Log.d("Files", "FileName:" + files[i].getName());
        }

        return filesNames;
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

    /**
     * start the clock
     */
    public void startTimer() {
        //set a new Timer
        startTime = System.currentTimeMillis() + (1000*60*60*2);
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 10, 10); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * handle the clock
     */
    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
//                        Calendar calendar = Calendar.getInstance();
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
//                        final String strDate = simpleDateFormat.format(calendar.getTime());

                        if(!isPause) {
                            long millis = System.currentTimeMillis() - startTime - pausedTime;
//                        int seconds = (int) (millis / 1000);
//                        seconds = seconds % 60;
//                        int minutes = seconds / 60;
//                        int hours = minutes / 60;

                            DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                            String dateFormatted = formatter.format(millis);
                            timerView.setText(dateFormatted);

                            visualizerView.addAmplitude((amplitude / 100) +100); // update the VisualizeView
                            visualizerView.invalidate(); // refresh the VisualizerView
                        }
//                        timerView.setText(String.format("%d:%d:%d", hours, minutes, seconds));

                        //show the toast
//                        int duration = Toast.LENGTH_SHORT;
//                        Toast toast = Toast.makeText(getApplicationContext(), strDate, duration);
//                        toast.show();
                    }
                });
            }
        };
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    public void Message(String msg)
    {
//        AlertDialog.Builder alertDialog;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            alertDialog = new AlertDialog.Builder(this, R.style.Alert);
//        }
//        else {
//            alertDialog = new AlertDialog.Builder(this);
//
//        }
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this);
        alertDialog
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRecMicToMp3!= null && mRecMicToMp3.isRecording())
            mRecMicToMp3.stop();

        if (notificationManager!=null)
            notificationManager.cancel(recordNotification);
    }

    @Override
    public void onBackPressed() {
        if(mRecMicToMp3!= null && mRecMicToMp3.isRecording())
            Message("אתה בטוח שאתה רוצה לעצור את ההקלטה?");
        else
            super.onBackPressed();
    }
}