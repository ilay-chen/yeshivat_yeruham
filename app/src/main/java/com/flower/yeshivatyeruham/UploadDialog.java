package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.flower.yeshivatyeruham.DataClass.*;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static com.flower.yeshivatyeruham.DataClass.*;

/**
 * activity show details of lessons, and responsible of actions on recorded lessons:
 * play - play the lesson
 * delete - delete the file
 * share - share the file
 * upload - send the file to BackgroundService to upload.
 *
 * activity have multiple situations:
 * 1. justPlay
 * 2. justUpload
 * 3. allDetails
 */
public class UploadDialog extends AppCompatActivity {

    private ProgressDialog pdia;
    private String fileName, ALBUM, ARTIST, TITLE, TRACK, YEAR, COMMENT;
    private TextView Title, teacherName, lessonName, lessonSubject, date;
    private ImageButton upLoad, Taging, delete ,play;
    private Boolean isOnLine = false;
    private allTeacher allData;
    private int upload = 0;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra("action",0);
            switch (action) {
                case 1:
                    EndProgress();
                    break;
                case 2:
                    updateProgress((int)intent.getDoubleExtra("percent",0));
                    break;
            }
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_upload);
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        registerReceiver(broadcastReceiver, new IntentFilter("Finish"));

        Intent intent = getIntent();
        fileName = intent.getStringExtra("tempName");
        upload = intent.getIntExtra("upload",0);

        initializeButtons();

        isOnline();
        tryGetFile();
    }

    public void isOnline()
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
            Tagging(readFile(DataPath));
            initialize();
            try {
                getTag();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Taging.setClickable(false);
            upLoad.setClickable(false);
            Toast.makeText(UploadDialog.this, "קובץ מידע חסר או פגום", Toast.LENGTH_SHORT).show();
            //new TaggingActivity.readFromServer().execute("false");
        }
    }

    @Test
    public void Tagging(String xml) throws Exception {

        Serializer serializer = new Persister();
        //skip file header, making err
        //xml = xml.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>","");
        //allData = serializer.read(allTeacher.class, xml.substring(38));
        xml = xml.substring(xml.indexOf("<all>"),xml.indexOf("</all>")+6);
        allData = serializer.read(allTeacher.class, xml);
        //test mytest = serializer.read(test.class, test);
        Log.d("test",allData.toString());
    }

    public String readFile(String path) throws IOException {
        FileInputStream fis = openFileInput(path);
        BufferedReader myReader = new BufferedReader(
                new InputStreamReader(fis));
        String aDataRow = "";
        String aBuffer = "";
        while ((aDataRow = myReader.readLine()) != null) {
            aBuffer += aDataRow + "\n";
        }
        return aBuffer;
    }

    public void initializeButtons()
    {
        upLoad = (ImageButton) findViewById(R.id.upload_button);
        Taging = (ImageButton) findViewById(R.id.taging_button);
        delete = (ImageButton) findViewById(R.id.delete_button);
        play = (ImageButton) findViewById(R.id.play_button);

        switch (upload)
        {
            case 0: allDetails();
                break;
            case 1: justUpload();
                break;
            case 2: justPlay();
                break;
            default:
                break;
        }
    }

    public void initialize()
    {
        List names = new ArrayList();

        for (myTeacher teacher : allData.teachers)
            names.add(teacher.name);

        teacherName = (TextView)findViewById(R.id.teacher_name);
        lessonName = (TextView)findViewById(R.id.lesson_name);
        lessonSubject = (TextView)findViewById(R.id.lesson_subject);
        date = (TextView)findViewById(R.id.date);

        Title = (TextView)findViewById(R.id.title);

    }

    public void allDetails()
    {
        upLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new UploadDialog.writeToServer().execute("");
                Message("האם אתה בטוח שברצונך להעלות קובץ לרשת?", 1);
            }
        });

        Taging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent saveRecord = new Intent(getBaseContext(), TaggingActivity.class);
                saveRecord.putExtra("tempName",fileName);
                startActivity(saveRecord);
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message("האם אתה בטוח שברצונך למחוק קובץ זה?", 2);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent player = new Intent(cxt, PlayerActivity.class);
//                player.putExtra("folder", AudioSavePath);
//                player.putExtra("file", fileName);
//                startActivity(player);

                File lessonFile = new File(AudioSavePath + fileName);

                Uri dataURI = FileProvider.getUriForFile(UploadDialog.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        lessonFile);

                Intent intent =  new Intent(Intent.ACTION_VIEW, dataURI);
                intent.setDataAndType(dataURI,"audio/mpeg");
                intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                Intent chooser = Intent.createChooser(intent, "בחר נגן להשמעת השיעור");
                chooser.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
                else
                    Toast.makeText(UploadDialog.this, "לא נמצא נגן מתאים", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void justUpload()
    {
        Taging.setVisibility(View.INVISIBLE);
        play.setVisibility(View.INVISIBLE);

        TextView title = (TextView)findViewById(R.id.title);
        title.setText("העלאת שיעור");

        upLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new UploadDialog.writeToServer().execute("");
                uploadAction();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void justPlay()
    {

        teacherName.setVisibility(View.GONE);
        lessonName.setVisibility(View.GONE);
        lessonSubject.setVisibility(View.GONE);
        date.setVisibility(View.GONE);

        Taging.setVisibility(View.INVISIBLE);
        upLoad.setVisibility(View.GONE);

        TextView title = (TextView)findViewById(R.id.title);
        title.setText("בחר פעולה");

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new UploadDialog.writeToServer().execute("");

                File lessonFile = new File(AudioSavePath + fileName);

                Uri data = FileProvider.getUriForFile(cxt, cxt.getApplicationContext().getPackageName() + ".provider", lessonFile);

                Intent intent =  new Intent(Intent.ACTION_VIEW, data);
                intent.setDataAndType(data,"audio/mp3");
                intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                Intent chooser = Intent.createChooser(intent, "בחר נגן להשמעת השיעור");
                chooser.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
                else
                    Toast.makeText(UploadDialog.this, "לא נמצא נגן מתאים", Toast.LENGTH_SHORT).show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message("האם אתה בטוח שברצונך למחוק קובץ זה?", 3);
            }
        });
    }

    public void deleteHashtag()
    {
        File myFile = new File(AudioSavePath + fileName);
        //fileName = fileName.replace("#","");
        fileName = TRACK + " " + ARTIST + " - " + ALBUM + " - " + TITLE + " - " + COMMENT + ".mp3";
        File newName = new File(AudioSavePath, fileName);
        myFile.renameTo(newName);
        scanFile(newName);
    }

    public void uploadAction()
    {
        isOnline();
        if(isOnLine) {//start service
            deleteHashtag();
            StartProgress();
            Intent upload = new Intent(UploadDialog.this,BackgroundService.class);
            upload.putExtra("tempName", fileName);
            startService(upload);
        }
        //new UploadDialog.readFromServer().execute("true");
        else
            Toast.makeText(UploadDialog.this, "אין חיבור", Toast.LENGTH_SHORT).show();
    }

    public void deleteAction(int action)
    {
        if(action == 1) deleteRecordsName(AudioSavePath, fileName);
        else if(action == 2) deleteRecordsName(DownSavePath, fileName);
        finish();
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
                .setPositiveButton("כן, בטוח!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (action)
                        {
                            case 1: uploadAction();
                                break;
                            case 2: deleteAction(1);
                                break;
                            case 3: deleteAction(2);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton("חס וחלילה, לא!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    public void deleteRecordsName(String path, String name) {

        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files == null) return;
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            if(files[i].getName().equals(name)) {
                files[i].delete();
                scanFile(files[i]);
                //files[i].getCanonicalFile().delete();
                Log.d("Files", "FileName:" + files[i].getName());
            }
        }
    }

    public void getTag()
    {
        File myFile = new File(AudioSavePath + fileName);

        MP3File f = null;
        try {
            f = (MP3File) AudioFileIO.read(myFile);
            //f = new MP3File(myFile);
            Tag tag = f.getTag();
            ID3v23Tag v23tag = (ID3v23Tag)tag;

            ARTIST = v23tag.getValue(FieldKey.ARTIST,0);
            ALBUM = v23tag.getValue(FieldKey.ALBUM,0);
            TITLE = v23tag.getValue(FieldKey.TITLE,0);
            TRACK = v23tag.getValue(FieldKey.TRACK,0);
            YEAR = v23tag.getValue(FieldKey.YEAR,0);
            COMMENT = v23tag.getValue(FieldKey.COMMENT,0);

            teacherName.setText("שם הרב המעביר: " + ARTIST);
            lessonName.setText("סדרת שיעורים: " + ALBUM);
            lessonSubject.setText("נושא השיעור: " + TITLE);
            date.setText("תאריך ההקלטה: " + COMMENT);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //tag.getFields(FieldKey.ARTIST);

        //upLoadDataFile();
    }

    public void offLine()
    {
        isOnLine = false;
        Toast.makeText(this,"שגיאה, אין חיבור", Toast.LENGTH_SHORT).show();
    }

    public void StartProgress()
    {
        pdia = new ProgressDialog(UploadDialog.this);
        pdia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdia.setMessage("מתחבר...");
        pdia.setCancelable(false);
        pdia.show();
    }

    public void EndProgress()
    {
        pdia.cancel();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("finish",true);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public void updateProgress(int percent)
    {
        pdia.setMessage("מעלה...");
        pdia.setProgress(percent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
