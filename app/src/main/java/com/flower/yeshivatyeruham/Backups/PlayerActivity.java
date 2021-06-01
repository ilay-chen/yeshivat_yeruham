package com.flower.yeshivatyeruham.Backups;

//import android.media.AudioManager;
//import android.media.MediaPlayer;
import android.media.AudioManager;
//import android.media.MediaPlayer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.flower.yeshivatyeruham.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {
    private Button b1,b2,b3,b4;
    private ImageView iv;
    private MediaPlayer mediaPlayer;

    private double startTime = 0;
    private double finalTime = 0;

    private Handler myHandler = new Handler();;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private TextView tx1,tx2,tx3;

    public static int oneTimeOnly = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        b1 = (Button) findViewById(R.id.forward);
        b2 = (Button) findViewById(R.id.pause);
        b3 = (Button)findViewById(R.id.play);
        b4 = (Button)findViewById(R.id.rewind);
        iv = (ImageView)findViewById(R.id.imageView);

        tx1 = (TextView)findViewById(R.id.time_passed);
        tx2 = (TextView)findViewById(R.id.time_total);
        tx3 = (TextView)findViewById(R.id.song_name);
        String folder = getIntent().getStringExtra("folder");
        String file = getIntent().getStringExtra("file");
        tx3.setText(file);


//        mediaPlayer = MediaPlayer.create(this, R.raw.a);
        File f = new File(folder + file);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        mediaPlayer.
        try {

            FileInputStream inputStream = new FileInputStream(f);
//
//            Uri uri = Uri.fromFile(new File(AppSavePath + "a.mp3"));
//            String s = uri.toString();
            mediaPlayer.setDataSource(inputStream.getFD());

//            mediaPlayer.setDataSource(new FileSource(new File(folder + file)));
//            mediaPlayer.setPlaybackSpeed(1.2f);
            mediaPlayer.prepare();
//            uri.toString()
        } catch (IOException e) {
            Log.d("test", "fail");
            e.printStackTrace();
        }
//        mediaPlayer.start();
//        mediaPlayer = new MediaPlayer();
//        File f = new File(AppSavePath + "a.mp3");

//        mediaPlayer = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ "/intro.mp3"));
//        try {
//        try {
//            mediaPlayer.setDataSource(this, Uri.parse(AppSavePath + "a.mp3"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        seekbar = (SeekBar)findViewById(R.id.seekBar);
        seekbar.setClickable(false);
        b2.setEnabled(false);

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Playing sound",Toast.LENGTH_SHORT).show();
//                        mediaPlayer.release();
                mediaPlayer.start();

                finalTime = mediaPlayer.getDuration();
                startTime = mediaPlayer.getCurrentPosition();

                if (oneTimeOnly == 0) {
                    seekbar.setMax((int) finalTime);
                    oneTimeOnly = 1;
                }

                finalTime -= TimeUnit.HOURS.toMillis(2);
                String formatted = new SimpleDateFormat("mm:ss").format(new Date((long)finalTime));
                tx2.setText(formatted);

//                tx2.setText(String.format("%02d:%02d",
//                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
//                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
//                                        finalTime)))
//                );

                startTime -= TimeUnit.HOURS.toMillis(2);
                String format = new SimpleDateFormat("mm:ss").format(new Date((long)startTime));
                tx1.setText(format);

//                tx1.setText(String.format("%d min, %d sec",
//                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
//                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
//                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
//                                        startTime)))
//                );

                seekbar.setProgress((int)startTime);
                myHandler.postDelayed(UpdateSongTime,100);
                b2.setEnabled(true);
                b3.setEnabled(false);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Pausing sound",Toast.LENGTH_SHORT).show();
                mediaPlayer.pause();
                b2.setEnabled(false);
                b3.setEnabled(true);
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp+forwardTime)<=finalTime){
                    startTime = startTime + forwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(),"You have Jumped forward 5  seconds",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Cannot jump forward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp-backwardTime)>0){
                    startTime = startTime - backwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(),"You have Jumped backward 5  seconds",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Cannot jump backward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
//        mediaPlayer.release();
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            // New date object from millis
            startTime -= TimeUnit.HOURS.toMillis(2);
            String formatted = new SimpleDateFormat("mm:ss").format(new Date((long)startTime));
            tx1.setText(formatted);
//            tx1.setText(String.format("%d min, %d sec",
//                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
//                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
//                                    toMinutes((long) startTime)))
//            );
            seekbar.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };
}