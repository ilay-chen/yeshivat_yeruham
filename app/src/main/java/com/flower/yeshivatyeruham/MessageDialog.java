package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MessageDialog extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    TextView content, linkText, titleText;
    Button ok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setFinishOnTouchOutside(false);
        }
        setContentView(R.layout.dialog_massage);
        Log.d("test", "Message Notification Dialog");

        Intent notification = getIntent();
        String data = notification.getStringExtra("data");
        String link = notification.getStringExtra("link");
        String title = notification.getStringExtra("title");

        titleText = (TextView) findViewById(R.id.title);
        if(title!=null && !title.equals(""))
            titleText.setText(title);

        content = (TextView) findViewById(R.id.content);
        content.setMovementMethod(new ScrollingMovementMethod());
        content.setText(data);

        if(link!=null && !link.equals("null")) {
            linkText = (TextView) findViewById(R.id.link);
            linkText.setMovementMethod(new ScrollingMovementMethod());
            linkText.setVisibility(View.VISIBLE);
            linkText.setText(link);
        }

        ok = (Button) findViewById(R.id.ok);
//        ok.setVisibility(View.GONE);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
