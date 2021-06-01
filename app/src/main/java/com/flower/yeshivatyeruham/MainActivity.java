package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

//import static android.Manifest.permission.READ_CALL_LOG;
//import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
//import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static com.flower.yeshivatyeruham.DataClass.cxt;
import static com.flower.yeshivatyeruham.DataClass.makeDir;
import static com.flower.yeshivatyeruham.RecordingActivity.RequestPermissionCode;


/**
 * main activity, hold all the fragments, maintain the swipe and the buttons to swipe between the fragments
 * also request permissions needed if have'nt already.
 */
public class MainActivity extends AppCompatActivity implements ContactsFragment.OnFragmentInteractionListener,
        RecordsFragment.OnFragmentInteractionListener, SksFragment.OnFragmentInteractionListener {

    ImageButton ContactButton, ListenButton, RecordsButton;
    int fragmentSwitch = 2;
    static Boolean firstTime = false;
    ViewPagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        List<AppUser> users = new ArrayList();
//
//        AppUser au = new AppUser("yhyUser","pr,navrchbu", 0, 1);
//        users.add(au);
//        au = new AppUser("yhy1","yhy12345", 1, 2);
//        users.add(au);
//
//        for(int i = 1; i < 50; i++)
//        {
//            au = new AppUser("yhy" + i,"yhy12345", 2, i + 100);
//            users.add(au);
//        }
//
//                // Write a message to the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("allData");
//
//        myRef.child("users").setValue(users);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("strings", MODE_PRIVATE);

        if (!isUserVer(sharedPref))
            SettingsFragment.logOut(this);
        //firstTime = sharedPref.getBoolean("firstTime",false);
        firstTime = sharedPref.getBoolean("theFirstTime", true);
//        startRequestPermission();
        if (firstTime)
            isFirstTime();
        if (!checkPermission())
            startRequestPermission();
        makeDir();

        try {
            final String ver = cxt.getPackageManager().getPackageInfo(cxt.getPackageName(), 0).versionName;
            final SharedPreferences sp = getApplicationContext().getSharedPreferences("whatsNew", MODE_PRIVATE);
            if (sp.getString("whatsNew" + ver, "empty").equals("empty"))
            {
                AlertDialog.Builder alertDialog= new AlertDialog.Builder(this);
                alertDialog
                        .setCancelable(false)
                        .setTitle(R.string.whats_new_in_update_title)
                        .setMessage(R.string.whats_new_in_update_text)
                        .setPositiveButton(R.string.whats_new_in_update_confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("whatsNew" + ver, "done");
                                editor.apply();
                            }
                        })
                        .show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        DataClass dc = (DataClass) getApplication();
        dc.updateFile(false, false);

        ContactButton = (ImageButton) findViewById(R.id.contact_button);
        ListenButton = (ImageButton) findViewById(R.id.listen_button);
        RecordsButton = (ImageButton) findViewById(R.id.records_button);



//        changeFragment(new ContactsFragment(), fragmentSwitch);

        ContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentSwitch = 2;
//                mViewPager.setCurrentItem(2, true);
                changeFragment(fragmentSwitch, true);
            }
        });

        ListenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentSwitch = 1;
//                mViewPager.setCurrentItem(1, true);
                changeFragment(fragmentSwitch, true);
            }
        });

        RecordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mViewPager.setCurrentItem(0, true);
                fragmentSwitch = 0;
                changeFragment(fragmentSwitch, true);
            }
        });



//        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 3);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        changeFragment(fragmentSwitch, true);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                changeFragment(position, false);
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here

            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int scrollState) {
                // Code goes here
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                    if (scrollState != ViewPager.SCROLL_STATE_IDLE) {
//                        final int childCount = mViewPager.getChildCount();
//                        for (int i = 0; i < childCount; i++)
//
//                            mViewPager.getChildAt(i).setLayerType(View.LAYER_TYPE_NONE, null);
//                    }
//                }
            }
        });

    }

    public void isFirstTime()
    {
        Intent search = new Intent(this, StartupActivity.class);
        startActivity(search);
    }

    public void changeFragment(int fragmentSwitch, boolean scroll)
    {
        if (scroll)
            mViewPager.setCurrentItem(fragmentSwitch, true);
        else {
            switch (fragmentSwitch)
            {
                case 2: ContactButton.setImageResource(R.drawable.contacts_selected);
                    ListenButton.setImageResource(R.drawable.listen);
                    RecordsButton.setImageResource(R.drawable.records);
                    break;
                case 1: ContactButton.setImageResource(R.drawable.contacts);
                    ListenButton.setImageResource(R.drawable.listen_selected);
                    RecordsButton.setImageResource(R.drawable.records);
                    break;
                case 0: ContactButton.setImageResource(R.drawable.contacts);
                    ListenButton.setImageResource(R.drawable.listen);
                    RecordsButton.setImageResource(R.drawable.records_selected);
                    break;
                default: ContactButton.setImageResource(R.drawable.contacts_selected);
                    ListenButton.setImageResource(R.drawable.listen);
                    RecordsButton.setImageResource(R.drawable.records);
                    break;
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void startRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(cxt)){
                requestFloatingPermission();
            }
            requestPermission(this);
        }
    }

    public void requestFloatingPermission() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this);
        alertDialog
                .setMessage("על מנת לאפשר שימוש מלא באפליקציה אנא אשר את ההרשאות הבאות")
                .setPositiveButton("סבבה, הבנתי", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this,"הרשאה אושרה, תודה",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this,"ההרשאה נדחתה",Toast.LENGTH_LONG).show();
                }
                requestPermission(this);
            }
        }
    }

    public static void requestPermission(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        	ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_PHONE_STATE, READ_CONTACTS, ACCESS_COARSE_LOCATION/*, READ_CALL_LOG, RECEIVE_SMS*/}, RequestPermissionCode);
        else
            ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_PHONE_STATE, READ_CONTACTS}, RequestPermissionCode);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        makeDir();
                    } else {
                        Toast.makeText(this,"הרשאה נדחתה",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean checkPermission() {
        Boolean floating = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            floating = Settings.canDrawOverlays(this);
        }
        int result = ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(this,
                RECORD_AUDIO);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            	int result2 = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);
            	return result == PackageManager.PERMISSION_GRANTED
                 && result1 == PackageManager.PERMISSION_GRANTED
                 && result2 == PackageManager.PERMISSION_GRANTED
                 && floating;
            }
            else
                return result == PackageManager.PERMISSION_GRANTED
                    && result1 == PackageManager.PERMISSION_GRANTED
                    && floating;
    }
    public  static List getInfo(String key, Context context){
        try {
            FileInputStream fis= context.openFileInput(context.getString(R.string.properties));
            BufferedReader br= new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line= br.readLine();
            List <String> info=new ArrayList<>();
            while(line!=null){
                if(line.contains("#"))
                    if(line.contains(key)) {
                        line=br.readLine();
                        while ( line != null && !line.isEmpty()) {
                            info.add(line);
                            line=br.readLine();
                        }
                        return info;
                    }
                br.readLine();
            }
            return info;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUserVer(SharedPreferences sharedPref){
        int ver = sharedPref.getInt("userVer",-1);
        return (ver >= LoginActivity.USER_VER);
    }

}

