package com.flower.yeshivatyeruham;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static com.flower.yeshivatyeruham.DataClass.DownSavePath;
import static com.flower.yeshivatyeruham.DataClass.scanFile;

/**
 * activity open when click on lessons.
 * have options - play (sorted application to play by file kind), delete, share (also sorted).
 */
public class PlayLessonDialog extends AppCompatActivity {

    Button play, share, delete;
    String fileName;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_play_lesson);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent i = getIntent();
        fileName = i.getStringExtra("fileName");

        play = (Button)findViewById(R.id.play);
        share = (Button)findViewById(R.id.share);
        delete = (Button)findViewById(R.id.delete);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File lessonFile = new File(DownSavePath + fileName);

                Uri dataURI = FileProvider.getUriForFile(PlayLessonDialog.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        lessonFile);

                Intent intent =  new Intent(Intent.ACTION_VIEW, dataURI);

                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = lessonFile.getName().substring(lessonFile.getName().indexOf(".") + 1).toLowerCase();
                String type = mime.getMimeTypeFromExtension(ext);

//                intent.setData(photoURI);
//                intent.setType("*/*");
                intent.setDataAndType(dataURI,type);
                //intent.putExtra("CONTENT_TYPE", "image/*;video/*");
                intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                Intent chooser = Intent.createChooser(intent, "בחר אפליקציה לפתיחה");
                chooser.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
                else
                    Toast.makeText(getApplicationContext(), "לא נמצאה אפליקציה תומכת", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");

                File lessonFile = new File(DownSavePath + fileName);

                Uri dataURI = FileProvider.getUriForFile(PlayLessonDialog.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        lessonFile);

                intent.putExtra(Intent.EXTRA_STREAM, dataURI);
                intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                Intent chooser = Intent.createChooser(intent, "שתף באמצעות");
                chooser.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
                else
                    Toast.makeText(getApplicationContext(), "לא נמצאה אפליקציה תומכת", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialog= new AlertDialog.Builder(PlayLessonDialog.this);
                alertDialog
                        .setMessage("האם אתה בטוח שברצונך למחוק קובץ זה?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String path = DownSavePath;
                                Log.d("Files", "Path: " + path);
                                File directory = new File(path);
                                File[] files = directory.listFiles();
                                if (files == null) return;
                                Log.d("Files", "Size: " + files.length);
                                for (int i = 0; i < files.length; i++) {
                                    if (files[i].getName().equals(fileName)) {
                                        files[i].delete();
                                        scanFile(files[i]);
                                        //files[i].getCanonicalFile().delete();
                                        Log.d("Files", "FileName:" + files[i].getName());
                                    }
                                }
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            }
        });
    }
}
