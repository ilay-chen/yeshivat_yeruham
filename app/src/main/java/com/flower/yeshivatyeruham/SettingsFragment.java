package com.flower.yeshivatyeruham;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.flower.yeshivatyeruham.DataClass.PicSavePath;
import static com.flower.yeshivatyeruham.DataClass.ServerPass;
import static com.flower.yeshivatyeruham.DataClass.cxt;
import static com.flower.yeshivatyeruham.DataClass.makeNoMedia;
import static com.flower.yeshivatyeruham.DataClass.netFTPPics;

// COMPLETED (1) Implement OnPreferenceChangeListener
public class SettingsFragment extends PreferenceFragmentCompat implements
        OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    ArrayList selectedItems = new ArrayList<String>();
    int time = 0;
    Context context;
    Uri resultUri;
    Fragment settingsFragment = this;
    ProgressDialog progress;
    String mode;
    String num;
    String group;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        context = getContext();
//        resultUri = Uri.parse(AppSavePath + "myPic.jpg");
//        resultUri = FileProvider.getUriForFile(context,
//                BuildConfig.APPLICATION_ID + ".provider",
//                new File(AppSavePath + "myPic.jpg"));

        // Add visualizer preferences, defined in the XML file in res->xml->pref
        addPreferencesFromResource(R.xml.pref);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();

//        Preference sendMsg = prefScreen.findPreference("send_msgs");
        SharedPreferences sharedPref = getContext().getSharedPreferences("strings", MODE_PRIVATE);
        if (sharedPref.getInt("myClassificationNum",2) != 0){
            prefScreen.findPreference("send_msgs").setVisible(false);
        }
        else {
            prefScreen.findPreference("send_msgs").setVisible(true);
        }

        prefScreen.findPreference("update_pic").setVisible(true);

        prefScreen.findPreference("attendanceList").setVisible(false);
        prefScreen.findPreference("attendance").setVisible(false);

//        if (sharedPref.getInt("myClassificationNum",2) == 0){
//            prefScreen.findPreference("update_pic").setVisible(true);
//        }
//        else {
//            prefScreen.findPreference("update_pic").setVisible(false);
//        }

            prefScreen.findPreference("sign_out").setSummary(sharedPref.getString("myName", "") + " התנתק מחשבון המשתמש שלך");

//        prefScreen.findPreference("send_msgs").setVisible(true);

        int count = prefScreen.getPreferenceCount();
        // Go through all of the preferences, and set up their preference summary.
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            // You don'createTrd need to set up preference summaries for checkbox preferences because
            // they are already set up in xml using summaryOff and summary On
            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }

        addPreferencesListener();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Figure out which preference was changed
        Preference preference = findPreference(key);
        if (null != preference) {
            // Updates the summary for the preference
            if (!(preference instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
            if (key.equals("files_in_library")) {
                makeNoMedia();
            }
            if (key.equals("show_pics")){
                if (!sharedPreferences.getBoolean(preference.getKey(), true)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog
                            .setMessage("תמונות אנשי קשר לא יוצגו.\nהאם ברצונך גם למחוק את התמונות שכבר נשמרו?")
                            .setPositiveButton("כן, בטח!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    File dir = new File(PicSavePath);
                                    if (dir.isDirectory()) {
                                        String[] children = dir.list();
                                        for (int i = 0; i < children.length; i++) {
                                            new File(dir, children[i]).delete();
                                        }

                                        SharedPreferences sharedPref = cxt.getSharedPreferences(getString(R.string.savedPics), MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString(getString(R.string.savedPics), "");
                                        editor.apply();
                                    }
                                }
                            })
                            .setNegativeButton("לא, זה בסדר...", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                }
                else {
                    DataClass dc = (DataClass) cxt.getApplicationContext();
                    dc.updateFile(false, false);
                }
            }
        }
    }

    /**
     * Updates the summary for the preference
     *
     * @param preference The preference to be updated
     * @param value      The value that the preference was updated to
     */
    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            // For list preferences, figure out the label of the selected value
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                // Set the summary to that label
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference) {
            // For EditTextPreferences, set the summary to the value's simple string representation.
            preference.setSummary(value);
        }
    }

    // COMPLETED (2) Override onPreferenceChange. This method should try to convert the new preference value
    // to a float; if it cannot, show a helpful error message and return false. If it can be converted
    // to a float check that that float is between 0 (exclusive) and 3 (inclusive). If it isn'createTrd, show
    // an error message and return false. If it is a valid number, return true.

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // In this context, we're using the onPreferenceChange listener for checking whether the
        // size setting was set to a valid value.
//        if (preference.getKey().equals("files_in_library")) {
//            makeNoMedia(Boolean.valueOf(newValue.toString()));
//        }
        return true;
    }

    private void addPreferencesListener() {
        Preference reset = (Preference) findPreference("sign_out");
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference pref)
            {
                AlertDialog.Builder alertDialog= new AlertDialog.Builder(getActivity());
                alertDialog
                        .setMessage("האם אתה בטוח שברצונך להתנתק?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                logOut(getActivity());
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
                return true;
            }
        });
        Preference refresh = (Preference) findPreference("refresh_files");
        refresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference pref) {
                DataClass dc = (DataClass) cxt.getApplicationContext();
                dc.updateFile(true, true);
                return true;
            }
        });

        Preference send = (Preference) findPreference("send_msgs");
        send.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference pref)
            {
                chooseGroup();

                return true;
            }
        });

        Preference report = (Preference) findPreference("bug_report");
        report.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference pref)
            {
                sendEmail();

                return true;
            }
        });

        final Preference picUpdate = (Preference) findPreference("update_pic");
        picUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference pref)
            {
//                ImageView iv = new ImageView(context);
//                iv.setImageResource(R.drawable.background);
                Log.i("mycheck","aaa1");

                AlertDialog.Builder alertDialog= new AlertDialog.Builder(getActivity());
                alertDialog
//                        .setCustomTitle(iv)
                        .setMessage("אנא בחר תמונת פנים גדולה וברורה.\nנא לא להעלות תמונה שאינה מאפשרת זיהוי בקלות או תמונה שאינה שלכם.\nלהסרת תמונתך מהאפליקציה לחץ 'הסר תמונה'.")
//                        .setMessage("אנא בחר תמונת פנים ברורה.\n נא לא להעלות תמונה שלא מאפשרת זיהוי בקלות או שאינה שלכם.\n להסרת תמונתך מהאפליקציה יש ללחוץ לחיצה ארוכה על 'עדכן תמונה'.")
                        .setPositiveButton("סבבה, הבנתי", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("mycheck","aaa2");

                                mode  = "upload";
                                Log.i("mycheck","aaa3");
                                Context c= getContext();
                                if(c!=null) {

                                    CropImage.activity()
                                            .setGuidelines(CropImageView.Guidelines.ON)
                                            .setRequestedSize(400, 400, CropImageView.RequestSizeOptions.RESIZE_INSIDE)
                                            .setAspectRatio(1, 1)

                                            .start(c,settingsFragment);
                                }
                            }
                        })
                        .setNeutralButton("הסר תמונה", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mode  = "delete";
                                showDialog();
                            }
                        })
//                        .setNegativeButton("בעצם לא...", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // do nothing
//                            }
//                        })
                        .show();
                return true;
            }
        });


//        picUpdate.
//    }
//    private static abstract class LongClickablePreference extends CheckBoxPreference implements View.OnLongClickListener {
//        public LongClickableCheckBoxPreference(Context context) {
//            super(context);
//        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("mycheck","o a r 0");
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.i("mycheck","o a r 1");
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = CropImage.getPickImageResultUri(context, data);
                Log.i("mycheck","o a r 2");

                resultUri = result.getUri();
                SharedPreferences sharedPref = getContext().getSharedPreferences("strings", MODE_PRIVATE);
//                Toast.makeText(context, resultUri.getPath(), Toast.LENGTH_SHORT).show();
                if (sharedPref.getInt("myClassificationNum",2) == 0) {
                    showDialog();
                }
                else {
                    num = sharedPref.getString("myPhoneNum", "");
                    group = sharedPref.getString("myGroupName", "");
                    new readFromServer().execute("");
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(context, "שגיאה בבחירת תמונה", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(context, "שגיאה", Toast.LENGTH_SHORT).show();
        }


    }

    private void showDialog() {
        LayoutInflater factory = LayoutInflater.from(context);
        final View dialogView = factory.inflate(R.layout.dialog_update_pic, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(true);
        dialog.setView(dialogView);
        final EditText etNum = (EditText) dialogView.findViewById(R.id.pic_num_et);
        final Spinner spinGroup = (Spinner) dialogView.findViewById(R.id.pic_group_spinner);
        Button okBtn = (Button) dialogView.findViewById(R.id.update_pic);
        if (mode.equals("delete")){
            okBtn.setText("הסר תמונה");
        }
        SharedPreferences sp = cxt.getSharedPreferences(getString(R.string.allGroups),MODE_PRIVATE);
        String allGroups = sp.getString(getString(R.string.allGroups),"err");
        final String[] groups = allGroups.split("#");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinGroup.setAdapter(dataAdapter);

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialogView.findViewById(R.id.update_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num = etNum.getText().toString();
                String[] tmp = spinGroup.getSelectedItem().toString().split(" ");
                group = tmp[tmp.length-1];
                Log.d("test", num + " , " + group);
                dialog.dismiss();
                new readFromServer().execute("");
            }
        });

        dialogView.findViewById(R.id.cancel_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private class readFromServer extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            if (mode.equals("upload")) {
                Log.i("mycheck","aaa upload");

                progress = ProgressDialog.show(context, "", "מעדכן תמונה...");
            }
            else {
                progress = ProgressDialog.show(context, "", "מסיר תמונה...");
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        @Override
        protected Boolean doInBackground(String... params) {
            try {
//                Bitmap beforeBitmap = BitmapFactory.decodeFile(resultUri.getPath());
//                Log.i("Before", beforeBitmap.getWidth()+"-"+beforeBitmap.getHeight());
//
//                Bitmap afterBitmap = getCompressedBitmap(resultUri.getPath());
//                Log.i("After", afterBitmap.getWidth() + "-" + afterBitmap.getHeight());
                FTPClient mFTPClient;
                mFTPClient = new FTPClient();
                mFTPClient.setAutodetectUTF8(true);
                mFTPClient.connect("yhy.co.il");

                mFTPClient.enterLocalPassiveMode();
                mFTPClient.login("mp3site@yhy.co.il", ServerPass);

//                InputStream inputStream = new ByteArrayInputStream(aBuffer.getBytes());
//                File f = new File(resultUri.getPath());
//                InputStream inputStream = f;
//                InputStream inputStream = new FileInputStream(f);
//                InputStream fis = new FileInputStream(new File(context.getCacheDir(), resultUri.getLastPathSegment()));

//                File file = new File(context.getCacheDir(), resultUri.getLastPathSegment());

//                Uri uri = FileProvider.getUriForFile(context, cxt.getApplicationContext().getPackageName() + ".provider", file);

//                InputStream fis = context.openFileInput(resultUri.getLastPathSegment());
                String ftpFTP = netFTPPics + group + "/" + num +".jpg";
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                mFTPClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                Boolean result;
                Log.i("check","1");

                if (mode.equals("upload")) {
                    Log.i("mycheck","aaa4");
                    mFTPClient.makeDirectory(netFTPPics + group + "/");
                    Bitmap original = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(resultUri));
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    original.compress(Bitmap.CompressFormat.JPEG, 75, out);
                    Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                    Log.e("Original   dimensions", original.getWidth() + " " + original.getHeight() + ". " + original.getByteCount());
                    Log.e("Compressed dimensions", decoded.getWidth() + " " + decoded.getHeight() + ". " + decoded.getByteCount());
                    result = mFTPClient.storeFile(ftpFTP, new ByteArrayInputStream(out.toByteArray()));

                }
                else {
                    result = mFTPClient.deleteFile(ftpFTP);
                }
//                Boolean result = mFTPClient.storeFile(ftpFTP, context.getContentResolver().openInputStream(resultUri));
//                inputStream.close();
//                fis.close();
                mFTPClient.disconnect();

                return result;
            } catch (Exception e) {
                Log.v("test", "errr");
                e.printStackTrace();
//                Toast.makeText(context, "שגיאה בעדכון התמונה", Toast.LENGTH_SHORT).show();
                return false;
            }
//            FTPClient mFTPClient;
//            mFTPClient = new FTPClient();
//            try {
//                mFTPClient.setAutodetectUTF8(true);
//
//                mFTPClient.connect("yhy.co.il");
//                mFTPClient.enterLocalPassiveMode();
//                mFTPClient.login("mp3site@yhy.co.il", ServerPass);
//
//                InputStream in = mFTPClient.retrieveFileStream(netFTPPics);
//
//                BufferedReader myReader = new BufferedReader(
//                        new InputStreamReader(in));
//                String aDataRow = "";
//                String aBuffer = "";
//                while ((aDataRow = myReader.readLine()) != null) {
//                    aBuffer += aDataRow + "\n";
//                }
//                in.close();
//
//                FileOutputStream out;// = new FileOutputStream(DataPath);
//                out = this.openFileOutput(Filename, Context.MODE_PRIVATE);
//                out.write(aBuffer.getBytes());
//                out.flush();
//                out.close();
//
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putString(Filename, newFileVers);
//                editor.apply();
//                Log.d("test", Filename + " ended");
//                mFTPClient.disconnect();
//
////            updateGroups();
//
//                return true;
//                //sfos.write("Test".getBytes());
//                //sfos.close();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                return false;
//                //Toast.makeText(getBaseContext(), e.getMessage(),
//                //      Toast.LENGTH_SHORT).show();
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//                return false;
//                //Toast.makeText(getBaseContext(), e.getMessage(),
//                //Toast.LENGTH_SHORT).show();
//            } catch (SmbException e) {
//                e.printStackTrace();
//                return false;
//                //Toast.makeText(getBaseContext(), e.getMessage(),
//                //Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//                //Toast.makeText(getBaseContext(), e.getMessage(),
//                //Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//
//        }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progress.dismiss();
            if (aBoolean){
                DataClass dc = (DataClass) cxt.getApplicationContext();
                dc.updateFile(true, false);
                if (mode.equals("upload")) {
                    Toast.makeText(context, "התמונה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "התמונה הוסרה בהצלחה", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(context, "שגיאה בביצוע הפעולה", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void sendEmail() {
        Log.i("test", "Send email");
        Uri data = null;
        String appVersionName;
        int appVersionCode;
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int androidVersion = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;
        SharedPreferences sharedPref = getContext().getSharedPreferences("strings", MODE_PRIVATE);

        try {
            appVersionName = cxt.getPackageManager().getPackageInfo(cxt.getPackageName(), 0).versionName;
            appVersionCode = cxt.getPackageManager().getPackageInfo(cxt.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            appVersionName = "לא ידועה";
            appVersionCode = 0;
            e.printStackTrace();
        }
        String phoneNumLogin = "";
        if (!sharedPref.getString("myPhoneNum","").equals("")) {
            phoneNumLogin = "שם המדווח: " + sharedPref.getString("myName", "")
                    + "\nמספר פלאפון: " + sharedPref.getString("myPhoneNum", "")
                    + "\nמחזור: " + sharedPref.getString("myGroupName", "") + " - " + sharedPref.getInt("myGroupNum",-1) + "\n";
        }

        data = Uri.parse("mailto:?subject=" + "דיווח על האפליקציה הישיבתית - גרסה " + appVersionName
                + "&body=" + "\n\n\n********\n"
                + phoneNumLogin
                + "משתמש: " + sharedPref.getString("myName","לא ידוע")
                + "\nגרסת אפליקציה: " + appVersionName + " - " + appVersionCode
                + "\nמכשיר :" + manufacturer + " - " + model
                + "\nגרסת אנדרואיד: " + androidVersion+ " - " + versionRelease
                + "&to=" + getString(R.string.report_bug_email));

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("plain/text");

//        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "some@email.address" });
//        intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
//        intent.putExtra(Intent.EXTRA_TEXT, "mail body");
//        startActivity(Intent.createChooser(intent, ""));

        intent.setData(data);

        Intent chooser = Intent.createChooser(intent, "שלח מייל");
        if (intent.resolveActivity(cxt.getPackageManager()) != null) {
            startActivity(chooser);
        }
        else
            Toast.makeText(cxt, "לא נמצאה אפליקציה תומכת", Toast.LENGTH_SHORT).show();
    }


    public static void logOut(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences("strings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        try {

            String topic = LoginActivity.getNumberAsString(sharedPref.getInt("myGroupNum",-1));

            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
        } catch (ClassCastException e){
            Log.d("err","ClassCastException");
        }

        //SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loggedIn", false);
        editor.putString("user", "null");
        editor.putString("myName", "");
        editor.putInt("userVer", -1);
        editor.putString("myGroupNum", "");
        editor.putString("myGroupName", "");
        editor.putInt("myClassificationNum",-1);
        editor.putInt("myGroupNum", -1);
        editor.apply();

        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void chooseGroup(){
        SharedPreferences sharedPref = cxt.getSharedPreferences(getString(R.string.allGroups), MODE_PRIVATE);

        String groups = sharedPref.getString(getString(R.string.allGroups),"err");
        //String[] s = getResources().getStringArray(R.array.tribe_array);
        final String[] items = groups.split("#");
//                String[] items = {"a", "b"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        selectedItems.clear();

        builder.setTitle("בחר מחזור")
                .setCancelable(false)
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        indexSelected = items.length - indexSelected - 1;
                        if (isChecked) {
                            selectedItems.add(indexSelected);
//                    Toast.makeText(cxt, "מחזור " +indexSelected + " נוסף", Toast.LENGTH_SHORT).show();
                        }
                        else if (selectedItems.contains(indexSelected)) {
                            selectedItems.remove(Integer.valueOf(indexSelected));
//                    Toast.makeText(cxt, "מחזור " +indexSelected + " הוסר", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setPositiveButton("הבא", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                chooseTime();
            }
        }).setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //do nothing
            }
        }).show();
    }

    private void chooseTime(){
        String[] opt = {"שעה", "שעתיים", "יום אחד", "יומיים", "שבוע", "חודש"};
        final long[] times = {TimeUnit.HOURS.toSeconds(1), TimeUnit.HOURS.toSeconds(2),
                TimeUnit.DAYS.toSeconds(1), TimeUnit.DAYS.toSeconds(2),
                TimeUnit.DAYS.toSeconds(7), TimeUnit.DAYS.toSeconds(30)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setTitle("בחר זמן תוקף להודעה")
                .setSingleChoiceItems(opt, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        time = (int) times[which];
                    }
                }).setPositiveButton("הבא", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent message = new Intent(cxt, SendMessagesActivity.class);
                message.putExtra("groups", selectedItems);
                message.putExtra("time", time);
                startActivity(message);
            }
        }).setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //do nothing
            }
        }).show();
    }
}