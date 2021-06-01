package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.flower.yeshivatyeruham.DataClass.*;

import static com.flower.yeshivatyeruham.DataClass.WIFINAME;
import static com.flower.yeshivatyeruham.DataClass.scanFile;
import static com.flower.yeshivatyeruham.DataClass.version;

/**
 * activity open after record finished, or when use want to edit lesson.
 * responsible of get the detils of lesson from the user, end set it on the lesson file.
 * olso, user can save the lesson, or save & upload instantly.
 */
public class TaggingActivity extends AppCompatActivity {

    Button uploadNow, uploadLater;
    TextView lessonsTextView;
    private String AudioSavePath = DataClass.AudioSavePath, fileName,
            ALBUM = "", ARTIST = "", TITLE = "", TRACK = "", YEAR = "", COMMENT = "";
    EditText LessonName, teacherName;
    Spinner []Spinners = new Spinner[2];
    allTeacher allData;
    Boolean isOnLine = false, needToReset = true, isSpecial = false;
    List names = new ArrayList();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagging);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Spinners[0] = (Spinner)findViewById(R.id.teacher);
        Spinners[1] = (Spinner)findViewById(R.id.lessonname);

        LessonName = (EditText)findViewById(R.id.editText);
        LessonName.requestFocus();


        teacherName = (EditText)findViewById(R.id.special_teacher);

        lessonsTextView = (TextView)findViewById(R.id.lesson_text_view);

        Intent intent = getIntent();
        fileName = intent.getStringExtra("tempName");

        uploadNow = (Button) findViewById(R.id.uploadNow);
        uploadLater = (Button) findViewById(R.id.uploadLater);

        final File myFile = new File(AudioSavePath + fileName);

        isOnLine();

        tryGetFile();

        uploadNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOnLine();
                if(isOnLine)
                {
                    if(isChosen()) {
                        tag();

                        Intent saveRecord = new Intent(getBaseContext(), UploadDialog.class);
                        saveRecord.putExtra("tempName", fileName);
                        saveRecord.putExtra("upload", 1);
                        startActivityForResult(saveRecord, 1);
                    }
                }
                else
                {

                    Toast.makeText(TaggingActivity.this,"אין חיבור, אנא התחבר לרשת הישיבה או בחר באפשרות 'סיים תיוג' והעלה את השיעור מאוחר יותר", Toast.LENGTH_LONG).show();
                }
            }
        });

        uploadLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isChosen()) {
                    tag();

                    finish();
                }
            }
        });
    }

    public Boolean isChosen() {
        ((TextView) Spinners[0].getSelectedView()).setError(null);
        if (Spinners[1].getVisibility() == View.VISIBLE) {
            ((TextView) Spinners[1].getSelectedView()).setError(null);
        }
        if (teacherName.getVisibility() == View.VISIBLE){
            teacherName.setError(null);
        }
        LessonName.setError(null);


        if (Spinners[0].getSelectedItem().toString().equals("בחר מעביר")){
            ((TextView) Spinners[0].getSelectedView()).setError("בחר מעביר");

            TextView tErr = ((TextView) findViewById(R.id.teacherInvisibleError));
            tErr.requestFocus();
            tErr.setError("בחר מעביר");
//            Toast.makeText(TaggingActivity.this, "אנא בחר מעביר", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Spinners[1].getSelectedItem().toString().equals("בחר שיעור")&& !isSpecial){
            ((TextView) Spinners[1].getSelectedView()).setError("בחר סדרת שיעורים");
            TextView lErr = ((TextView) findViewById(R.id.lessonInvisibleError));
            lErr.requestFocus();
            lErr.setError("בחר סדרת שיעורים");
//            Toast.makeText(TaggingActivity.this, "אנא בחר סדרת שיעורים", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Spinners[0].getSelectedItem().toString().equals("אחר")){
            String teacher = teacherName.getText().toString();
            if (teacher.equals("")){
                teacherName.requestFocus();
                teacherName.setError("הכנס שם מעביר");
//                Toast.makeText(TaggingActivity.this, "אנא הכנס שם המעביר", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (teacher.contains("\\")|| teacher.contains("~") || teacher.contains("/")|| teacher.contains(":")|| teacher.contains("?")|| teacher.contains("<")|| teacher.contains(">")|| teacher.contains("|")|| teacher.contains("*") || teacher.contains(" - ")){
                teacherName.requestFocus();
                teacherName.setError("תו לא חוקי");
                Toast.makeText(TaggingActivity.this, "שם המעביר לא יכול להכיל את התווים: \\, /, :, ?, <, >, ~, -,*, |", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        String lesson = LessonName.getText().toString();
        if (lesson.equals("")){
            LessonName.requestFocus();
            LessonName.setError("הכנס נושא שיעור");
//            Toast.makeText(TaggingActivity.this, "אנא הכנס את נושא השיעור", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (lesson.contains("\\") || lesson.contains("~")|| lesson.contains("/")|| lesson.contains(":")|| lesson.contains("?")|| lesson.contains("<")|| lesson.contains(">")|| lesson.contains("|")|| lesson.contains("*")){
            LessonName.requestFocus();
            LessonName.setError("תו לא חוקי");
            Toast.makeText(TaggingActivity.this, "שם השיעור לא יכול להכיל את התווים: \\, /, :, ?, <, >, ~, *, |", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Boolean finish = data.getBooleanExtra("finish", false);
                if (finish)
                    finish();
            }
        }
    }

    public void isOnLine()
    {
        WifiManager connManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //NetworkInfo myWiFi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiInfo mWiFi = connManager.getConnectionInfo();
        if(mWiFi != null && mWiFi.getSSID()!=null &&
                (mWiFi.getSSID().contains(WIFINAME.split(",")[0]) ||
                        mWiFi.getSSID().contains(WIFINAME.split(",")[1])))
        {
            isOnLine = true;
        }
    }

    public void tryGetFile()
    {
        try {
            Tagging(readFile());
            initialize();
            fillTags();

        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(TaggingActivity.this, "קובץ מידע חסר.. רענן קבצים", Toast.LENGTH_SHORT).show();
            //new TaggingActivity.readFromServer().execute("false");
        }
    }

    public void tag()
    {
        File myFile = new File(AudioSavePath + fileName);

        if(isSpecial)
        {
            ALBUM = "שיעור מיוחד";
            if(Spinners[0].getSelectedItem().equals("אחר"))
                ARTIST = teacherName.getText().toString();
            else
                ARTIST = Spinners[0].getSelectedItem().toString();
        }
        else {
            ALBUM = Spinners[1].getSelectedItem().toString();
            ARTIST = Spinners[0].getSelectedItem().toString();
        }
        TITLE = LessonName.getText().toString().replaceAll("\"", "\'\'");
        YEAR = "";
        TRACK = "0";
        if (COMMENT == null || COMMENT.equals("")) {
            COMMENT = getDate();
            YEAR = getYear();
        }

        MP3File f = null;
        try {
            f = (MP3File)AudioFileIO.read(myFile);
            Tag tag = f.getTag();
            if (tag == null)
            {
                tag = new ID3v23Tag();
            }
            ID3v23Tag v23tag = (ID3v23Tag)tag;
            String appVersionName;
            try {
                appVersionName =  "(" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName + ")";
            } catch (PackageManager.NameNotFoundException e) {
                appVersionName = "";
            }
            SharedPreferences sharedPref = getSharedPreferences("strings", MODE_PRIVATE);
            String name = sharedPref.getString("myName", "");

            v23tag.setField(FieldKey.ARTIST, ARTIST);
            v23tag.setField(FieldKey.ALBUM, ALBUM);
            v23tag.setField(FieldKey.TITLE, TITLE);
            v23tag.setField(FieldKey.TRACK, TRACK);
            v23tag.setField(FieldKey.YEAR, YEAR);
            v23tag.setField(FieldKey.COMMENT, COMMENT);
            v23tag.setField(FieldKey.ENCODER, "הוקלט באמצעות האפליקציה הישיבתית " + appVersionName);
            v23tag.setField(FieldKey.GENRE, "הוקלט באמצעות האפליקציה הישיבתית " + appVersionName);
            //DONE change FieldKey.COMPOSER to FieldKey.CONDUCTOR.
            if (!name.equals("")) {v23tag.setField(FieldKey.CONDUCTOR, "הוקלט ע\"י " + name);}

            f.setTag(v23tag);
            //f.commit();
            AudioFileIO.write(f);
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        } catch (CannotWriteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //tag.getFields(FieldKey.ARTIST);

        fileName = TRACK + " " + ARTIST + " - " + ALBUM + " - " + TITLE + " - " + COMMENT + ".mp3";
        File newName = new File(AudioSavePath, fileName);
        myFile.renameTo(newName);
        scanFile(newName);


        //upLoadDataFile();
    }

    public String readFile() throws Exception {
        FileInputStream fis = openFileInput("tagData.xml");
        BufferedReader myReader = new BufferedReader(
                new InputStreamReader(fis));
        String aDataRow = "";
        String aBuffer = "";
        while ((aDataRow = myReader.readLine()) != null) {
            aBuffer += aDataRow + "\n";
        }
        fis.close();
        return aBuffer;
    }

    public String getDate()
    {
        JewishCalendar jd = new JewishCalendar(); // current date 23 Nissan, 5773
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true); // change formatting to Hebrew
        return hdf.format(jd);
    }

    public String getYear()
    {
        JewishCalendar jd = new JewishCalendar(); // current date 23 Nissan, 5773
        return jd.getJewishYear() +"";
//        HebrewDateFormatter hdf = new HebrewDateFormatter();
//        hdf.setHebrewFormat(true); // change formatting to Hebrew
//        return hdf.format(jd);
    }

    @Test
    public void Tagging(String xml) throws Exception {

        if(versionIsOk(Double.parseDouble(xml.substring(xml.indexOf("<version>")+9,xml.indexOf("</version>"))))) {
            Serializer serializer = new Persister();
            xml = xml.substring(xml.indexOf("<all>"), xml.indexOf("</all>") + 6);
            allData = serializer.read(allTeacher.class, xml);
            Log.d("test", allData.toString());
        }
        else {
//            Intent search = new Intent(this, MainActivity.class);
//            startActivity(search);
            Message("גירסת האפליקציה שברשותך אינה עדכנית, אנא עדכן לגירסה האחרונה בחנות האפליקציות.\nהשיעור נשמר, ותוכל להמשיך לתייג לאחר מכן.",2);
            Toast.makeText(this, "גירסה נמוכה של אפליקציה, אנא עדכן לגירסה האחרונה בחנות האפליקציות.", Toast.LENGTH_LONG).show();
        }
    }

    public Boolean versionIsOk(double tagVersion)
    {
        if(tagVersion > version)
            return false;
        return true;
    }

    public List<String> getLessons(String teacherName)
    {
        List lessons = new ArrayList();
        lessons.add("בחר שיעור");
        for(myTeacher teacher : allData.teachers)
            if(teacher.name.equals(teacherName)) {
                for(myLesson lesson : teacher.lessons)
                    lessons.add(lesson.lessonname);
            }
        lessons.add("אחר");
        return lessons;
    }

    public void initialize()
    {
        names.add("בחר מעביר");
        for(myTeacher teacher : allData.teachers)
            names.add(teacher.name);
        names.add("אחר");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, names);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinners[0].setAdapter(dataAdapter);

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new String[] {"בחר שיעור"});
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinners[1].setAdapter(dataAdapter);

        Spinners[0].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!Spinners[0].getSelectedItem().toString().equals("בחר מעביר") && needToReset) {
                    ArrayAdapter<String> dataAdapter;
                    dataAdapter = new ArrayAdapter<String>(TaggingActivity.this,
                            android.R.layout.simple_spinner_item, getLessons(Spinners[0].getSelectedItem().toString()));
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Spinners[1].setAdapter(dataAdapter);
                    if (!Spinners[0].getSelectedItem().toString().equals("אחר") ){
                        isSpecial = false;
                        Spinners[1].setVisibility(View.VISIBLE);
                        lessonsTextView.setVisibility(View.VISIBLE);
                        teacherName.setVisibility(View.GONE);
                    }
                    else{
                        isSpecial = true;
                        Spinners[1].setVisibility(View.GONE);
                        lessonsTextView.setVisibility(View.GONE);
                        teacherName.setVisibility(View.VISIBLE);
                    }
//                    if (specialLesson.isChecked()) {
//                        teacherName.setText(Spinners[0].getSelectedItem().toString());
//                    }
                }
                else if(!needToReset)
                    needToReset = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // sometimes you need nothing here
            }
        });

        Spinners[1].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (Spinners[1].getSelectedItem().toString().equals("אחר") ){
                    isSpecial = true;
                }
                else isSpecial = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void fillTags()
    {
        File myFile = new File(AudioSavePath + fileName);

        MP3File f = null;
        try {
            f = (MP3File) AudioFileIO.read(myFile);
            Tag tag = f.getTag();
            ID3v23Tag v23tag = (ID3v23Tag)tag;

            if (tag==null||tag.isEmpty()) return;

            ARTIST = v23tag.getValue(FieldKey.ARTIST,0);
            ALBUM = v23tag.getValue(FieldKey.ALBUM,0);
            TITLE = v23tag.getValue(FieldKey.TITLE,0);
            YEAR = v23tag.getValue(FieldKey.YEAR,0);
            COMMENT = v23tag.getValue(FieldKey.COMMENT,0);

            //v23tag.setField(FieldKey.TRACK, TRACK);
            f.setTag(v23tag);
            //f.commit();
            AudioFileIO.write(f);
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        } catch (CannotWriteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(ARTIST!=null&&ALBUM!=null&&TITLE!=null) {
            if(ALBUM.equals("שיעור מיוחד")) {
                isSpecial = true;

                if (names.indexOf(ARTIST) == -1)
                {
                    Spinners[0].setSelection(names.indexOf("אחר"));
                    Spinners[1].setVisibility(View.GONE);
                    lessonsTextView.setVisibility(View.GONE);
                    teacherName.setVisibility(View.VISIBLE);
                    teacherName.setText(ARTIST);
                }
                else {
                    Spinners[0].setSelection(names.indexOf(ARTIST));

                    if (!Spinners[0].getSelectedItem().toString().equals("בחר מעביר")) {
                        ArrayAdapter<String> dataAdapter;
                        dataAdapter = new ArrayAdapter<String>(TaggingActivity.this,
                                android.R.layout.simple_spinner_item, getLessons(Spinners[0].getSelectedItem().toString()));
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        Spinners[1].setAdapter(dataAdapter);
                        List a = getLessons(Spinners[0].getSelectedItem().toString());
                        int i = a.indexOf("אחר");
                        Spinners[1].setSelection(i);
                    }
                }
            }
            else {
                Spinners[0].setSelection(names.indexOf(ARTIST));

                if (!Spinners[0].getSelectedItem().toString().equals("בחר מעביר")) {
                    ArrayAdapter<String> dataAdapter;
                    dataAdapter = new ArrayAdapter<String>(TaggingActivity.this,
                            android.R.layout.simple_spinner_item, getLessons(Spinners[0].getSelectedItem().toString()));
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Spinners[1].setAdapter(dataAdapter);

                    List a = getLessons(Spinners[0].getSelectedItem().toString());
                    int i = a.indexOf(ALBUM);
                    Spinners[1].setSelection(i);
                }
            }
            needToReset = false;
            //Spinners[1].setSelection(getLessons(Spinners[0].getSelectedItem().toString()).indexOf(ALBUM));
            LessonName.setText(TITLE);
        }
        //tag.getFields(FieldKey.ARTIST);
    }
    public void Message(String msg, final int action)
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
                        if(action==2)
                            finish();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        Message("האם אתה בטוח שברצונך לשמור קובץ זה ללא שינוי בתיוג?", 1);
    }
}
