package com.flower.yeshivatyeruham;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.flower.yeshivatyeruham.DataClass.cxt;

public class SendMessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_messages);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final ArrayList selectedGroups = getIntent().getStringArrayListExtra("groups");
        final int time = getIntent().getIntExtra("time", 0);
        SharedPreferences sharedPref = cxt.getSharedPreferences(
                getString(R.string.allGroups),
                MODE_PRIVATE);
        String allGroups = sharedPref.getString(getString(R.string.allGroups),"err");
        final String[] items = allGroups.split("#");
        String s = "";
        for (int i = 0; i < selectedGroups.size(); i++){
            s = s + items[items.length - Integer.parseInt(selectedGroups.get(i).toString()) - 1] + ", ";
        }
        s = s.substring(0, s.length()-2);

        TextView details = (TextView) findViewById(R.id.details);
        details.setText("מחזורים: " + s + ".\nזמן: " +  TimeUnit.SECONDS.toHours(time) + " שעות.");

        (findViewById(R.id.title)).requestFocus();

        Button send = (Button) findViewById(R.id.send);
        for (int i = 0; i < selectedGroups.size(); i++){
            if (selectedGroups.get(i).toString().equals("0")){
                selectedGroups.set(i, "staff");
            }
        }
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s =
//                        31 + "<^>"
                        selectedGroups.toString().substring(1, selectedGroups.toString().length()-1) + "#"
                        + time + "#"
                        + ((EditText)findViewById(R.id.title)).getText() + "#"
                        + ((EditText)findViewById(R.id.displayText)).getText() + "#"
                        + ((EditText)findViewById(R.id.content)).getText() + "#";
                String link = ((EditText)findViewById(R.id.link)).getText().toString();
                if (link.equals("")){
                    link = "null";
                }
                s = s + link;


        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("messages");

        myRef.setValue(s);

                Toast.makeText(getApplicationContext(), "ההודעה נשלחה!", Toast.LENGTH_LONG).show();
//                Intent main = new Intent(cxt, MainActivity.class);
//                startActivity(main);
                finish();

            }
        });

    }
}
