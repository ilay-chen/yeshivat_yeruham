package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;

import static com.flower.yeshivatyeruham.DataClass.sksRoot;
import static com.flower.yeshivatyeruham.DataClass.studentRoot;

public class GuestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Context ctx=getBaseContext();
        ImageButton sksBtn, downloadsBtn, favoritesBtn, donationBtn;
        sksBtn = (ImageButton) findViewById(R.id.sks_btn);
        downloadsBtn = (ImageButton) findViewById(R.id.downloads_btn);
        favoritesBtn = (ImageButton) findViewById(R.id.favorites_btn);
        donationBtn = (ImageButton) findViewById(R.id.donation_btn);
        Glide.with(this)
                .load(R.drawable.sks_icon)
                .into(sksBtn);
        Glide.with(this)
                .load(R.drawable.downloads_icon)
                .into(downloadsBtn);
        Glide.with(this)
                .load(R.drawable.favorites_icon)
                .into(favoritesBtn);
        Glide.with(this)
                .load(R.drawable.donate_icon)
                .into(donationBtn);


        sksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataClass.isOnline(ctx)){
                    Intent sksIntent = new Intent(getBaseContext(), SksActivity.class);
                    sksIntent.putExtra("rootPath", sksRoot);
                    startActivity(sksIntent);
                }
            }
        });


        downloadsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent downloadsIntent = new Intent(getBaseContext(), DownloadsActivity.class);
                startActivity(downloadsIntent);
            }
        });


        favoritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent favoritesIntent = new Intent(getBaseContext(), FavoritesActivity.class);
                startActivity(favoritesIntent);
            }
        });

        donationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.yhy.co.il/content/view/629/179/lang,he/"));
                startActivity(browserIntent);
            }
        });
    }

    public boolean onCreateOptionMenu(Menu menu) {
        MenuInflater menuInf = getMenuInflater();
        menuInf.inflate(R.menu.guest_menu, menu);
        return true;
    }

}
