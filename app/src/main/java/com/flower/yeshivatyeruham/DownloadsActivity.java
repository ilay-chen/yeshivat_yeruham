package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static com.flower.yeshivatyeruham.DataClass.scanFile;


/**
 * present the files and lesson that had downloaded
 */
public class DownloadsActivity extends AppCompatActivity {

    TextView empty;
    RecyclerView data;
    RecyclerView.Adapter adapter;
    Context ctx;
    int index = 0;
    int top = 0;
    List<String> allDownloads;
    RecyclerView.LayoutManager mLayoutManager;
    String DownSavePath = DataClass.DownSavePath;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ctx = this;
        empty = (TextView) findViewById(R.id.empty_downloads);
        data = (RecyclerView) findViewById(R.id.downloads_list);
        allDownloads = getDownloadsNames();
        mLayoutManager = new LinearLayoutManager(this);
        data.setLayoutManager(mLayoutManager);
//        adapter = new CustomFilesList(this, allDownloads, null, new CustomFilesList.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                Intent playLesson = new Intent(ctx, PlayLessonDialog.class);
//                playLesson.putExtra("fileName", getDownloadsNames().get(position));
//                startActivity(playLesson);
//            }
//        } , null);
        data.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int pos = data.getChildAdapterPosition(viewHolder.itemView);
                if (swipeDir == ItemTouchHelper.LEFT){
                    final String fileName = allDownloads.get(viewHolder.getAdapterPosition());

                    AlertDialog.Builder alertDialog= new AlertDialog.Builder(ctx);
                    alertDialog
                            .setMessage("האם אתה בטוח שברצונך למחוק קובץ זה?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String path = DownSavePath;
//                                    Log.d("Files", "Path: " + path);
                                    File directory = new File(path);
                                    File[] files = directory.listFiles();
                                    if (files == null) return;
//                                    Log.d("Files", "Size: " + files.length);
                                    for (int i = 0; i < files.length; i++) {
                                        if (files[i].getName().equals(fileName)) {
                                            files[i].delete();
                                            scanFile(files[i]);
                                            //files[i].getCanonicalFile().delete();
//                                            Log.d("Files", "FileName:" + files[i].getName());
                                        }
                                    }
                                    setAdapter();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing

                                }
                            })
                            .show();
                    adapter.notifyItemChanged(pos);
                }
                else if (swipeDir == ItemTouchHelper.RIGHT){
                    final String fileName = allDownloads.get(viewHolder.getAdapterPosition());
                    Intent intent =  new Intent(Intent.ACTION_SEND);
                    intent.setType("*/*");

                    File lessonFile = new File(DownSavePath + fileName);

                    Uri dataURI = FileProvider.getUriForFile(ctx,
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
                    adapter.notifyItemChanged(pos);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Get RecyclerView item from the ViewHolder
                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    if (dX > 0) {
            /* Set your color for positive displacement */
                        p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.share_color));
                        // Draw Rect with varying right side, equal to displacement dX
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);
                    } else if (dX < 0){
            /* Set your color for negative displacement */
                        p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.delete_color));
                        // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        }).attachToRecyclerView(data);
//        data.setAdapter(adapter);
//        adapter = new CustomRecordsList(this, allDownloads, data);

//        data.setOnItemClickListener(new AdapterView.OnItemClickListener()
//        {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
//            {
//                Intent playLesson = new Intent(ctx, PlayLessonDialog.class);
//                playLesson.putExtra("fileName", getDownloadsNames().get(position));
//                startActivity(playLesson);
//            }
//        });
//        List<String> allDownloads= getFavoritesNames();
//        if(allDownloads.isEmpty()) {
//            empty.setVisibility(View.VISIBLE);
//            data.setVisibility(View.GONE);
//        }
//        else {
//            empty.setVisibility(View.GONE);
//            data.setVisibility(View.VISIBLE);
//
//            adapter = new CustomRecordsList(this, getFavoritesNames(), data);
//            data.setAdapter(adapter);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPos();
    }

    @Override
    public void onResume() {
        super.onResume();
        allDownloads = getDownloadsNames();
        if(allDownloads.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            data.setVisibility(View.GONE);
        }
        else {
            empty.setVisibility(View.GONE);
            data.setVisibility(View.VISIBLE);
        }

        setAdapter();
    }
    void setAdapter(){
        getPos();
        ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(index, top);
        allDownloads = getDownloadsNames();
        adapter = new CustomFilesList(this, allDownloads, null, new CustomFilesList.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String fileName = getDownloadsNames().get(position);
                File lessonFile = new File(DownSavePath + fileName);

                Uri dataURI = FileProvider.getUriForFile(DownloadsActivity.this,
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

            }
        } ,new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int pos = data.getChildAdapterPosition(v);
                Intent playLesson = new Intent(ctx, PlayLessonDialog.class);
                playLesson.putExtra("fileName", getDownloadsNames().get(pos));
                startActivity(playLesson);
                return true;
            }
        }, "downloads");
        data.setAdapter(adapter);
    }
    void getPos(){
        if (adapter != null) {
            index = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
//        index = data.getFirstVisiblePosition();
            View v = data.getChildAt(0);
            top = (v == null) ? 0 : (v.getTop() - data.getPaddingTop());
        }
    }
    public List<String> getDownloadsNames() {
        String path = DownSavePath;

//        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<String> filesNames = new ArrayList<>();
        if(files==null)
            files = new File[0];

        Arrays.sort(files, new Comparator<File>(){
            public int compare(File f2, File f1)
            {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            } });
//        List<String> filesNames = new ArrayList<>();
//        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            filesNames.add(i ,files[i].getName());
//            if(isAudioFile(files[i].getName())) {
//                rootNames.add(j, files[i].getName());
//                filesNames.add(j, rootNames.get(j));
//                filesNames.set(j, filesNames.get(j).substring(0,filesNames.get(j).lastIndexOf(".") ));
//                Log.d("Files", "FileName:" + files[i].getName());
//                j++;
//            }
        }
        return filesNames;
    }
}
