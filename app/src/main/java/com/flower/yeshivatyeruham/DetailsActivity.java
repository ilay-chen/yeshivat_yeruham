package com.flower.yeshivatyeruham;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.flower.yeshivatyeruham.ContactsActivity.convertNum;
import static com.flower.yeshivatyeruham.DataClass.PicSavePath;

public class DetailsActivity extends AppCompatActivity {
    ImageButton callBtn, smsBtn, copyBtn, addBtn;
    ImageView image;
    String num, name, group;
    TextView nameView, groupView, numberView;
    AlertDialog.Builder dialog;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        nameView = (TextView)findViewById(R.id.name);
        groupView = (TextView)findViewById(R.id.group);
        numberView = (TextView)findViewById(R.id.number);

        dialog =  new AlertDialog.Builder(this);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.callButton:
                        onCall(num);
                        break;
                    case R.id.smsButton:
                        sms(num);
                        break;
                    case R.id.copyButton:
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            clip = ClipData.newPlainText("text", num);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            clipboard.setPrimaryClip(clip);
                        }
                        Toast.makeText(DetailsActivity.this, "מספר הועתק", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.addButton:
                        addContact(name, num);
                        break;
                    default:
                        break;
                }
            }
        };

        callBtn = (ImageButton) findViewById(R.id.callButton);
        smsBtn = (ImageButton) findViewById(R.id.smsButton);
        copyBtn = (ImageButton) findViewById(R.id.copyButton);
        addBtn = (ImageButton) findViewById(R.id.addButton);
        image = (ImageView) findViewById(R.id.detailsImage);

        callBtn.setOnClickListener(listener);
        smsBtn.setOnClickListener(listener);
        copyBtn.setOnClickListener(listener);
        addBtn.setOnClickListener(listener);
        getDetails();
        Context context = image.getContext();

//        File f = null;
//        f = new File(PicSavePath + num + ".jpeg");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean showPics = sharedPreferences.getBoolean("show_pics",
                this.getResources().getBoolean(R.bool.show_pics_default));
        if (showPics){
            File f = new File(PicSavePath + num + ".jpeg");

            Glide.with(context)
                    .load(f)
                    .bitmapTransform(new CustomContactsList.CropSquareTransformation(context))
                    .placeholder(R.drawable.big_contact_pic)
                    .signature(new StringSignature((String.valueOf(f.lastModified()))))
                    .into(image);
        }
    }

    protected void getDetails(){
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            List segments = uri.getPathSegments();
            num = segments.get(0).toString();
            name = segments.get(1).toString();
            group = segments.get(2).toString();
        }
        else {
            num = intent.getStringExtra("number");
            name = intent.getStringExtra("name");
            group = intent.getStringExtra("group");
        }

        nameView.setText(name);
        numberView.setText(num);
        groupView.setText("|   " + group);
    }
    public void onCall(String num) {
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + num));
        startActivity(callIntent);
    }
    public void sms(final String num) {
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        dialog.setTitle("שליחת הודעה");
        dialog.setMessage("האם תרצה לשלוח הודעת ס.מ.ס או וואטסאפ?");
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent swipeLeftIntent = new Intent();
                swipeLeftIntent.setAction(Intent.ACTION_VIEW);
                swipeLeftIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                swipeLeftIntent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + convertNum((String) num)));

                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(swipeLeftIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                if(isIntentSafe){
                    startActivity(swipeLeftIntent);
                }
            }
        });


        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + num));
                startActivity(sendIntent);
            }
        });
        dialog.show();

    }
    public void addContact(String name, String num) {
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, num);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            File file = new File(PicSavePath + num + ".jpeg");
            if (file.exists()){
                byte[] pic = new byte[(int) file.length()];
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(pic, 0, pic.length);
                    buf.close();
                    ArrayList data = new ArrayList();
                    ContentValues row = new ContentValues();
                    row.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                    row.put(ContactsContract.CommonDataKinds.Photo.PHOTO, pic);
                    data.add(row);
                    intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        startActivity(intent);
    }
}
