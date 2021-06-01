


package com.flower.yeshivatyeruham;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


// Created by dell on 24 אוקטובר 2017.


 public class FTPSearchActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        connectToFTP connectToFTP=new connectToFTP();
        connectToFTP.execute();

    }
}

