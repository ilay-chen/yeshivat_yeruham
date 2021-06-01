package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static com.flower.yeshivatyeruham.DataClass.FTPRoot;
import static com.flower.yeshivatyeruham.DataClass.SMBRoot;
import static com.flower.yeshivatyeruham.DataClass.ServerPass;
import static com.flower.yeshivatyeruham.DataClass.WIFINAME;
import static com.flower.yeshivatyeruham.DataClass.cxt;
import static com.flower.yeshivatyeruham.DataClass.netFTPDownloadsCount;
import static com.flower.yeshivatyeruham.DataClass.netFilesDownloadsCount;
import static com.flower.yeshivatyeruham.DataClass.netSksDownloadsCount;
import static com.flower.yeshivatyeruham.DataClass.notificationId;
import static com.flower.yeshivatyeruham.DataClass.scanFile;
import static com.flower.yeshivatyeruham.DataClass.sksRoot;
import static com.flower.yeshivatyeruham.DataClass.studentRoot;
import static com.flower.yeshivatyeruham.StopReceiver.isCancelled;
import static com.flower.yeshivatyeruham.StopReceiver.toStop;

public class SksActivity extends AppCompatActivity {
    String oldPath;
    Boolean isSks;
    Boolean isWiFi = false;
    String state;
    String newPath;
    String root;
    String downloadsPath = DataClass.DownSavePath;
    View progress;
    EditText favNickEt;
    private ProgressDialog pdia;
    //    TextView pathView;
    int pos;
    List<String> values;
    Context ctx;
    File downloadedFile;
    Activity activity;
    RecyclerView list, pathList;
    RecyclerView.Adapter adapter, pathAdapter;
    RecyclerView.LayoutManager mLayoutManager, pathLayoutManager;
    NtlmPasswordAuthentication auth;
//    Button up, refresh;
    SmbFile sFile;
    int index = 0, top = 0;
    int steps = 0;
    FTPClient mFTPClient;
    Boolean isLoading = false;
    FloatingActionButton saveBtn;
    Boolean uploadMode = false;
    Boolean rollToEnd = false;


    Stack<String[]> stack = new Stack();

    NotificationCompat.Builder mBuilder = null;
    NotificationManager mNotifyManager = null;

    private SwipeRefreshLayout container;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sks);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        ctx = this;
        activity = this;
        auth = null;

        Intent intent = getIntent();
        String action = intent.getAction();

        if (getIntent().getStringExtra("rollList") != null){
            rollToEnd = true;
        }

        if (Intent.ACTION_SEND.equals(action)) {
            if (!handleSend(intent)) { // Handle being sent
                ActivityCompat.finishAffinity(this);
                return;
            }
            newPath = oldPath = studentRoot;

        }else {
            // Handle other intents, such as being started from the home screen
            String s = getIntent().getStringExtra("rootPath");
            if (s.startsWith(studentRoot)) {
                setTitle("סייר קבצים");
            }
            newPath = oldPath = getIntent().getStringExtra("rootPath");
        }

        pathList = (RecyclerView) findViewById(R.id.path_list);
        pathLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        pathList.setLayoutManager(pathLayoutManager);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
//        addItemDecoration(new DividerItemDecoration(this,getResources().getDrawable(R.drawable.divider_background)));
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
        pathList.addItemDecoration(divider);

        progress = findViewById(R.id.loadingPanel);

        container = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call container.setRefreshing(false)
                // once the network request has completed successfully.
                createListView();
                container.setRefreshing(false);
            }
        });
//        container.setColorSchemeResources(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);
//        container.setColorSchemeResources(R.color.colorPrimary);

        ArrayList head = new ArrayList();
        head.add("ראשי");
        pathAdapter = new PathsAdapter(activity, head, null);
        pathList.setAdapter(pathAdapter);

        list = (RecyclerView) findViewById(R.id.sks_list);
        list.setLongClickable(true);
        mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        pathView = (TextView)findViewById(R.id.path_view);
//        up = (Button) findViewById(R.id.up);
//        refresh  = (Button) findViewById(R.id.refresh) ;
//        up.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                up();
//            }
//        });
//        refresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                newPath = oldPath;
//                createListView();
//            }
//        });


        values = new ArrayList();
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int pos = list.getChildAdapterPosition(viewHolder.itemView);
                if (swipeDir == ItemTouchHelper.LEFT){
                    viewHolder.itemView.performLongClick();
//                    AlertDialog.Builder alertDialog= new AlertDialog.Builder(FavoritesActivity.this);
//                    alertDialog
//                            .setMessage("האם אתה בטוח שברצונך למחוק קיצור זה?")
//                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    SharedPreferences sharedPrefPath = getSharedPreferences(getString(R.string.allFavPaths), MODE_PRIVATE);
//                                    String allPaths = sharedPrefPath.getString(getString(R.string.allFavPaths),"");
//                                    String s = pathsArr.get(pos) + "|";
//                                    allPaths = allPaths.replaceAll(pathsArr.get(pos)+ "\\|", "");
//                                    SharedPreferences.Editor editor = sharedPrefPath.edit();
//                                    editor.putString(getString(R.string.allFavPaths), allPaths);
//                                    editor.apply();
//
//                                    sharedPrefPath = getSharedPreferences(getString(R.string.allFavNicks), MODE_PRIVATE);
//                                    String allNicks = sharedPrefPath.getString(getString(R.string.allFavNicks),"");
//                                    s = nicksArr.get(pos) + "|";
//                                    allNicks = allNicks.replaceAll(nicksArr.get(pos)+ "\\|", "");
//
//                                    editor = sharedPrefPath.edit();
//                                    editor.putString(getString(R.string.allFavNicks), allNicks);
//                                    editor.apply();
//                                    onResume();
//
//                                }
//                            })
//                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // do nothing
//                                }
//                            })
//                            .show();
                    adapter.notifyItemChanged(pos);
                }
                else if (swipeDir == ItemTouchHelper.RIGHT){
                    viewHolder.itemView.performClick();
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
                        p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.open_folder_color));
                        // Draw Rect with varying right side, equal to displacement dX
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);
                    } else if (dX < 0){
            /* Set your color for negative displacement */
                        p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.save_fav_color));
                        // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);
                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        }).attachToRecyclerView(list);
        isWiFi = isWIFIAvailable();


        if (newPath.startsWith(sksRoot)) {
            isSks = true;
            root = sksRoot;
        }
        else {
            isSks = false;
            root = studentRoot;
        }
        createListView();
    }

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

    private boolean handleSend(Intent intent) {
        final Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
//        final String s = uri.getLastPathSegment();
//        final Uri uri = intent.getData();

        if (isWIFIAvailable()) {
//        Boolean b = intent.getType() != null;
//        Boolean a = uri != null;
//        Boolean e = aa != null;
//        Boolean c = new File(uri.getPath()).exists();

//            if (intent.getType() != null && uri != null && new File(uri.getPath()).exists()) {
            if (intent.getType() != null && uri != null) {
//                if ("content".equals(uri.getScheme())) {
//                    try {
////                        ParcelFileDescriptor f = getContentResolver().openFileDescriptor(uri, "r");
//                        FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",)
//                        InputStream inStream = getContentResolver().openInputStream(uri);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }



                uploadMode = true;
                saveBtn = (FloatingActionButton) findViewById(R.id.save_file_button);
                //saveBtn.setVisibility(View.VISIBLE);
                saveBtn.show();//?
                setTitle("בחר תיקיה להעלאת הקובץ");
                registerReceiver(broadcastReceiver, new IntentFilter("Finish"));
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent upload = new Intent(SksActivity.this, BackgroundService.class);
                        StartProgress();
                        Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        returnCursor.moveToFirst();
                        String name = returnCursor.getString(nameIndex);
                        long size = returnCursor.getLong(sizeIndex);
                        Log.d("test", "name " + name);
                        Log.d("test", "size " + size);

                        upload.putExtra("uploadPath",SMBRoot + newPath + name);
                        upload.putExtra("uploadSize",size);
                        upload.putExtra("uploadFile", uri);
//                        upload.putExtra("tempName", uri.getPath());
//                        upload.putExtra("netFolder", SMBRoot + newPath + new File(uri.getPath()).getName());
                        startService(upload);
                    }
                });
                return true;
            } else {
                Toast.makeText(this, "שגיאה בפתיחת הקובץ", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        else {
            Toast.makeText(this, "אין חיבור לרשת הישיבה, התחבר ונסה שנית", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    public void StartProgress()
    {
        pdia = new ProgressDialog(this);
        pdia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdia.setMessage("מתחבר...");
        pdia.setCancelable(false);
        pdia.show();
    }

    public void EndProgress()
    {
        pdia.cancel();
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("finish",true);
//        setResult(Activity.RESULT_OK,returnIntent);
//        finish();
        ActivityCompat.finishAffinity(this);
    }

    public void updateProgress(int percent)
    {
        pdia.setMessage("מעלה...");
        pdia.setProgress(percent);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        getPos();
    }

    private void createListView() {
        if (isWIFIAvailable() || isNetworkAvailable()) {

            if(Build.VERSION.SDK_INT >= 11)
                new getFilesList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                new getFilesList().execute("");

            //setAdapter();

//            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                    pos = position;
//                    newPath = adapter.getItem(position);
////                if (oldPath.endsWith(File.separator)) {
////                    newPath = oldPath + newPath;
////                } else {
////                    newPath = oldPath + File.separator + newPath;
////                }
//                    newPath = oldPath + newPath;
//                    index = list.getFirstVisiblePosition();
//                    View v = list.getChildAt(0);
//                    top = (v == null) ? 0 : (v.getTop() - list.getPaddingTop());
//                    indexArr.add(steps, index);
//                    topArr.add(steps++, top);
//                    createListView();
//                }
//            });
//            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                @Override
//                public boolean onItemLongClick(AdapterView<?> arg0, View view, final int pos, long id) {
//                    if (list.getItemAtPosition(pos).toString().endsWith("/")) {
//                        LayoutInflater factory = LayoutInflater.from(ctx);
//                        final View favDialogView = factory.inflate(R.layout.dialog_favorite, null);
//                        final AlertDialog favDialog = new AlertDialog.Builder(ctx).create();
//                        favDialog.setCancelable(false);
//                        favDialog.setView(favDialogView);
//                        final TextView favNickTv = (TextView) favDialogView.findViewById(R.id.textViewFav);
//                        final EditText favNickEt = (EditText) favDialogView.findViewById(R.id.fav_nick_et);
//                        favNickTv.setText("הקלד כינוי לתקית '" + adapter.getItem(pos).replace("/", "") + "'");
//                        favDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                        favDialogView.findViewById(R.id.add_fav).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                String nick = favNickEt.getText().toString();
//                                Toast.makeText(ctx, nick + " נשמר למועדפים בהצלחה", Toast.LENGTH_LONG).show();
//
//                                SharedPreferences sharedPrefPath = getSharedPreferences(getString(R.string.allFavPaths), MODE_PRIVATE);
//                                String allPaths = sharedPrefPath.getString(getString(R.string.allFavPaths), "");
//
//                                SharedPreferences sharedPrefNicks = getSharedPreferences(getString(R.string.allFavNicks), MODE_PRIVATE);
//                                String allNicks = sharedPrefNicks.getString(getString(R.string.allFavNicks), "");
//
//                                allPaths = newPath + adapter.getItem(pos) + "|" + allPaths;
//
//                                allNicks = nick + "|" + allNicks;
//
//                                SharedPreferences.Editor editor = sharedPrefPath.edit();
//                                editor.putString(getString(R.string.allFavPaths), allPaths);
//                                editor.apply();
//
//                                editor = sharedPrefNicks.edit();
//                                editor.putString(getString(R.string.allFavNicks), allNicks);
//                                editor.apply();
//
////                                Toast.makeText(ctx, allNicks, Toast.LENGTH_LONG).show();
////                                Toast.makeText(ctx, allPaths, Toast.LENGTH_LONG).show();
//                                favDialog.dismiss();
//
//                            }
//                        });
//                        favDialogView.findViewById(R.id.cancel_fav).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                favDialog.dismiss();
//                            }
//                        });
//                        favDialog.show();
//                    }
//                        return true;
//
//                    }
//                });

        } else Toast.makeText(ctx, "לא מחובר", Toast.LENGTH_SHORT).show();
    }

    public class getFilesList extends AsyncTask<String, Void, String> {

        @Override protected void onPreExecute() {
            checkIfConnectionChange();

            isLoading = true;

            if(newPath.endsWith("/")) {
//                progress = new ProgressBar(ctx);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    container.animate()
                            .translationX(container.getWidth())
                            .alpha(0.0f)
                            .setDuration(300);
                    progress.animate()
                            .alpha(1.0f)
                            .setDuration(300);
                }
                else {
                    progress.setVisibility(View.VISIBLE);
                    container.setVisibility(View.INVISIBLE);
                }
//                progress.setVisibility(View.VISIBLE);
//                container.setVisibility(View.GONE);
//                progress.setMessage("טוען...");
//                progress.setCancelable(false);
//                progress.show();
            }
        }

        @Override protected String doInBackground(String... params) {
            // Read all files sorted into the values-array
            values = new ArrayList();
//                if (newPath.equals(root) || types.get(pos).equals("folder")){
                //if (newPath.endsWith("/")) {

                    if (isWIFIAvailable()) {
                        state = "smb";
                        return SmbFilesList();
                    } else if (isNetworkAvailable()) {
                        state = "ftp";
                        return FTPFileList();
                    }
                //}

//                        if (!dir.canRead()) {
//                            return "err";
//                        }
//
//                        Arrays.sort(files, new NumberAwareStringComparator() {
//                            @Override
//                            public int compare(SmbFile f1, SmbFile f2) {
//                                String s1 = f1.getName();
//                                String s2 = f2.getName();
//                                if (s1.endsWith("/") && !s2.endsWith("/"))
//                                    return -1;
//                                if (s2.endsWith("/") && !s1.endsWith("/"))
//                                    return 1;
//
//                                return super.compare(f1, f2);
//                            }
//
////                            public int compare(SmbFile f1, SmbFile f2) {
////                                String s1 = f1.getName();
////                                String s2 = f2.getName();
////                                if (s1.endsWith("/") && !s2.endsWith("/"))
////                                    return -1;
////                                if (s2.endsWith("/") && !s1.endsWith("/"))
////                                    return 1;
////                                return f1.getName().compareTo(f2.getName());
////                            }
//                        });
//
//                        if (list != null) {
//
//                            for (int i = 0; i < files.length; i++) {
//                                if (!files[i].getName().startsWith(".")
//                                        && (files[i].getName().endsWith("/")) || isAudioFile(files[i].getName()) || !isSks) {
//                                    values.add(files[i].getName());
//                                }
//                            }
//                        }
//                        return "folder";
//                    } catch (MalformedURLException e) {
//                        e.printStackTrace();
//                        return "err";
//                    } catch (SmbException e) {
//                        e.printStackTrace();
//                        return "err";
//                    }
//                }
//                else if (isAudioFile(newPath) || !isSks) {
////                    progress.setMessage("Downloading...");
//////                    try {
//////                        progress.setMessage("Downloading...");
//////                        downloadedFile = new File(downloadsPath + newPath.substring(newPath.lastIndexOf("/")));
//////                        sFile = new SmbFile(newPath, auth);
//////
//////                        int totalSize = (int)sFile.length();
//////                        byte[] buffer = new byte[totalSize/100];
//////                        double downloadedSize = 0;
//////                        int bufferLength = 0;
//////                        SmbFileInputStream fis = new SmbFileInputStream(sFile);
//////                        FileOutputStream out = new FileOutputStream(downloadedFile);
//////
//////                        while ( (bufferLength = fis.read(buffer)) > 0 )
//////                        {
//////                            fis.read(buffer, 0, bufferLength);
//////                            downloadedSize += bufferLength;
//////                            out.write(buffer);
//////                        }
//////                        out.flush();
//////                        out.close();
//////                        fis.close();
//////                        return "file";
//////                    } catch (SmbException e) {
//////                        e.printStackTrace();
//////                    } catch (FileNotFoundException e) {
//////                        e.printStackTrace();
//////                    } catch (MalformedURLException e) {
//////                        e.printStackTrace();
//////                    } catch (UnknownHostException e) {
//////                        e.printStackTrace();
//////                    } catch (IOException e) {
//////                        e.printStackTrace();
//////                    }
////                    return download();
//                    return "file";
////                    return "err";
//                }else {
//                    return "unsupported";
//                }

            return "err";
        }

        @Override protected void onPostExecute(String s ) {
            isLoading = false;
            if (state.equals("ftp") && !uploadMode){
                setTitle("סק''ש - גישת אינטרנט");
            }

            if (s.equals("folder")){
                setAdapter();
                oldPath = newPath;
//                pathView.setText(newPath.replace(root, "ראשי/"));
            }else if (s.equals("file") && !uploadMode){
                download();

//                Toast.makeText(ctx, "השיעור הורד בהצלחה", Toast.LENGTH_LONG).show();
////                Intent intent =  new Intent(Intent.ACTION_VIEW);
//              File f = new File("smb/mp3@192.168.1.25/mp3" + newPath.replace("smb://192.168.1.25/mp3", ""));
////                intent.setDataAndType(Uri.parse("smb/mp3@192.168.1.25/mp3" + newPath.replace("smb://192.168.1.25/mp3", "")), "audio/*");
////
////                //intent.setType("audio/*");
////                intent.putExtra(Intent.EXTRA_STREAM,Uri.parse("smb/mp3@192.168.1.25/mp3" + newPath.replace("smb://192.168.1.25/mp3", "")));
//
//                Uri myUri = Uri.parse("smb://smb/mp3@192.168.1.25/mp3" + newPath.replace("smb://192.168.1.25/mp3", ""));
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, myUri);
//                intent.putExtra(Intent.EXTRA_STREAM,Uri.parse("smb://smb/mp3@192.168.1.25/mp3" + newPath.replace("smb://192.168.1.25/mp3", "")));
//                intent.setDataAndType(Uri.parse("smb://smb/mp3@192.168.1.25/mp3" + newPath.replace("smb://192.168.1.25/mp3", "")), "audio/*");
//                //intent.setDataAndType(myUri, "audio/*");
//                //startActivity(intent);
//
//                Intent chooser = Intent.createChooser(intent, "בחר נגן להשמעת השיעור");
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(chooser);
//                }
//
//                else
//                    Toast.makeText(ctx, "לא נמצא נגן מתאים", Toast.LENGTH_SHORT).show();

            } else if (s.equals("err")){
                Toast.makeText(ctx, "החיבור נקטע", Toast.LENGTH_SHORT).show();
            }else if (s.equals("unsupported")){
                Toast.makeText(ctx, "סוג הקובץ אינו נתמך", Toast.LENGTH_SHORT).show();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                container.animate()
                        .translationX(0)
                        .alpha(1.0f)
                        .setDuration(300);
                progress.animate()
                        .alpha(0.0f)
                        .setDuration(300);
            }
            else {
                progress.setVisibility(View.INVISIBLE);
                container.setVisibility(View.VISIBLE);
//                progress.setVisibility(View.INVISIBLE);
            }

//            container.setVisibility(View.VISIBLE);
//            progress.setVisibility(View.GONE);
            if (!stack.empty() && stack.peek()[0].equals(newPath)) {
                ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(Integer.parseInt(stack.peek()[1]),
                        Integer.parseInt(stack.pop()[2]));
            }
            if (rollToEnd){
//                ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(Integer.parseInt(stack.peek()[1]),
//                        Integer.parseInt(stack.pop()[2]));
                list.scrollToPosition(adapter.getItemCount() - 1);
                rollToEnd = false;
            }

//            if (indexArr.size() > 0 && indexArr.size() > steps) {
//                ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset((int) indexArr.get(steps),
//                        (int) topArr.get(steps));
//                indexArr.remove(steps);
//                topArr.remove(steps);
//            }
        }
    }

    public void checkIfConnectionChange()
    {
        if (isWIFIAvailable() != isWiFi)
        {
            Toast.makeText(cxt, "שיב לב! מצב רשת השתנה", Toast.LENGTH_SHORT).show();
        }
        isWiFi = isWIFIAvailable();
    }

    public String SmbFilesList()
    {
        values = new ArrayList();
        SmbFile dir = null;
        SmbFile[] files;
//                if (newPath.equals(root) || types.get(pos).equals("folder")){
        if (newPath.endsWith("/")) {
            try {

                    dir = new SmbFile(SMBRoot + newPath, auth);

                    files = dir.listFiles();

                Arrays.sort(files, new NumberAwareStringComparatorSmbFile() {
                    @Override
                        public int compare( SmbFile o1, SmbFile o2) {
                        String s1 = o1.getName();
                        String s2 = o2.getName();
                        if (s1.endsWith("/") && !s2.endsWith("/"))
                            return -1;
                        if (s2.endsWith("/") && !s1.endsWith("/"))
                            return 1;

                        return super.compare(o1, o2);
                    }

//                            public int compare(SmbFile f1, SmbFile f2) {
//                                String s1 = f1.getName();
//                                String s2 = f2.getName();
//                                if (s1.endsWith("/") && !s2.endsWith("/"))
//                                    return -1;
//                                if (s2.endsWith("/") && !s1.endsWith("/"))
//                                    return 1;
//                                return f1.getName().compareTo(f2.getName());
//                            }
                });

                if (list != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (!files[i].getName().startsWith(".")
                                && (!files[i].getName().toLowerCase().endsWith(".lnk"))
//                                && ((files[i].getName().endsWith("/")) || isAudioFile(files[i].getName()) || !isSks)
                                ) {
                            values.add(files[i].getName());
                        }
                    }
                }
                return "folder";
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "err";
            } catch (SmbException e) {
                e.printStackTrace();
                return "err";
            }
        }
        else {
//        else if (isAudioFile(newPath) || !isSks) {
//                    progress.setMessage("Downloading...");
////                    try {
////                        progress.setMessage("Downloading...");
////                        downloadedFile = new File(downloadsPath + newPath.substring(newPath.lastIndexOf("/")));
////                        sFile = new SmbFile(newPath, auth);
////
////                        int totalSize = (int)sFile.length();
////                        byte[] buffer = new byte[totalSize/100];
////                        double downloadedSize = 0;
////                        int bufferLength = 0;
////                        SmbFileInputStream fis = new SmbFileInputStream(sFile);
////                        FileOutputStream out = new FileOutputStream(downloadedFile);
////
////                        while ( (bufferLength = fis.read(buffer)) > 0 )
////                        {
////                            fis.read(buffer, 0, bufferLength);
////                            downloadedSize += bufferLength;
////                            out.write(buffer);
////                        }
////                        out.flush();
////                        out.close();
////                        fis.close();
////                        return "file";
////                    } catch (SmbException e) {
////                        e.printStackTrace();
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    } catch (MalformedURLException e) {
////                        e.printStackTrace();
////                    } catch (UnknownHostException e) {
////                        e.printStackTrace();
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
//                    return download();
            return "file";
//                    return "err";
//        }else {
//            return "unsupported";
        }
    }

    public String FolderNotExist(FTPClient mFTPClient, String path)
    {
        if(!isSks)
        {
            progress.setVisibility(View.INVISIBLE);
            finish();
//            super.onBackPressed();
//            Toast.makeText(cxt, "שיב לב! מצב רשת השתנה", Toast.LENGTH_SHORT).show();
        }

        path = path.replace(sksRoot,FTPRoot);

        try {
            if (!path.equals(FTPRoot)) {
                String[] arr = path.split("/");
                path = path.substring(0 , path.lastIndexOf(arr[arr.length-1]));
            } else return FTPRoot;

            if (mFTPClient.changeWorkingDirectory(path))
                return path;
            else  {
                return FolderNotExist(mFTPClient, path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FTPRoot;
    }

    public String FTPFileList()
    {

        values = new ArrayList();
        FTPFile[] files = null;
//                if (newPath.equals(root) || types.get(pos).equals("folder")){
        if (newPath.endsWith("/")) {
            Log.d("test",newPath);

            try {
//                if (isFirstLoad){
                    mFTPClient = new FTPClient();
                    //mFTPClient.setControlEncoding("UTF-8");
                    mFTPClient.setAutodetectUTF8(true);
                    //mFTPClient.setCharset(Charset.forName("utf-8"));

                    mFTPClient.connect("yhy.co.il");
                    mFTPClient.login("mp3site@yhy.co.il", ServerPass);
                    mFTPClient.enterLocalPassiveMode();
//                    isFirstLoad = false;
//                }



                //mFTPClient.setCharset(Charset.forName("unicode"));

                //newPath = new String(bytes, "unicode");
                //newPath = StringEscapeUtils.escapeJava(newPath);
//                mFTPClient.login("mp3site@yhy.co.il", ServerPass);
                Boolean s = mFTPClient.changeWorkingDirectory(newPath.replace(sksRoot,FTPRoot));
                if (!s)
                {
                    newPath = FolderNotExist(mFTPClient,newPath);
                    mFTPClient.changeWorkingDirectory(newPath.replace(sksRoot,FTPRoot));
                }

                //InputStream inStream = mFTPClient.retrieveFileStream("KABQ.TXT");
                //InputStreamReader isr = new InputStreamReader(inStream, "UTF8");

                files = mFTPClient.listFiles();
//                for(FTPFile ftpf : files)
//                {
//                    ftpf.setName(StringEscapeUtils.unescapeJava(ftpf.getName()));
//                }

                //URL url = new URL(newPath.replace("smb://192.168.1.25/mp3/","http://yhy.co.il/"));


                if (!mFTPClient.isConnected()) {
                    return "err";
                }


                Arrays.sort(files, new NumberAwareStringComparatorFTPFile() {
                    @Override
                    public int compare( FTPFile o1, FTPFile o2) {

//                        String s1 = o1.getName();
//                        String s2 = o2.getName();
                        if (o1.isDirectory() && !o2.isDirectory())
                            return -1;
                        if (o2.isDirectory() && !o1.isDirectory())
                            return 1;

                        return super.compare(o1, o2);
                    }

//                            public int compare(SmbFile f1, SmbFile f2) {
//                                String s1 = f1.getName();
//                                String s2 = f2.getName();
//                                if (s1.endsWith("/") && !s2.endsWith("/"))
//                                    return -1;
//                                if (s2.endsWith("/") && !s1.endsWith("/"))
//                                    return 1;
//                                return f1.getName().compareTo(f2.getName());
//                            }
                });
                mFTPClient.disconnect();

                if (list != null) {

                    for (int i = 0; i < files.length; i++) {
                        if (!files[i].getName().startsWith(".") && files[i].isDirectory()) {
                            values.add(files[i].getName() + "/");
                        }
                        else if (!files[i].getName().startsWith(".")) {
                            values.add(files[i].getName());
                        }
                    }
                }
                return "folder";
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "err";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
//        else if (isAudioFile(newPath) || !isSks) {
//                    progress.setMessage("Downloading...");
////                    try {
////                        progress.setMessage("Downloading...");
////                        downloadedFile = new File(downloadsPath + newPath.substring(newPath.lastIndexOf("/")));
////                        sFile = new SmbFile(newPath, auth);
////
////                        int totalSize = (int)sFile.length();
////                        byte[] buffer = new byte[totalSize/100];
////                        double downloadedSize = 0;
////                        int bufferLength = 0;
////                        SmbFileInputStream fis = new SmbFileInputStream(sFile);
////                        FileOutputStream out = new FileOutputStream(downloadedFile);
////
////                        while ( (bufferLength = fis.read(buffer)) > 0 )
////                        {
////                            fis.read(buffer, 0, bufferLength);
////                            downloadedSize += bufferLength;
////                            out.write(buffer);
////                        }
////                        out.flush();
////                        out.close();
////                        fis.close();
////                        return "file";
////                    } catch (SmbException e) {
////                        e.printStackTrace();
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    } catch (MalformedURLException e) {
////                        e.printStackTrace();
////                    } catch (UnknownHostException e) {
////                        e.printStackTrace();
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
//                    return download();
            return "file";
//                    return "err";
//        }else {
//            return "unsupported";
        }
        return "err";
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setAdapter();
    }

    @Override public void onBackPressed() {
         if (!isLoading){
            up();
        }
    }

    public void up (){
        if (oldPath.equals(root)){
            Toast.makeText(ctx, "תקייה ראשית", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] arr = oldPath.split("/");
        newPath = newPath.substring(0 , oldPath.lastIndexOf(arr[arr.length-1]));
        if (steps != 0) {
            steps--;
        }
        createListView();
    }

    public Boolean isWIFIAvailable() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            wifiInfo.getSSID();
            String name = networkInfo.getExtraInfo();
            String ssid = "\"" + wifiInfo.getSSID() + "\"";

            return((ssid.contains(WIFINAME.split(",")[0]) || wifiInfo.getSSID().contains(WIFINAME.split(",")[1])));
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Boolean isAudioFile(String name){
        if (name.toLowerCase().endsWith(".mp3")
                || name.toLowerCase().endsWith(".wma")
                || name.toLowerCase().endsWith(".wav")
                || name.toLowerCase().endsWith(".aac")
                || name.toLowerCase().endsWith(".aiff")
                || name.toLowerCase().endsWith(".m4a")
                || name.endsWith(".3gp"))
            return true;
        else
            return false;
    }

    public void download() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this);
        alertDialog
                .setMessage("האם ברצונך להוריד קובץ זה?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if(Build.VERSION.SDK_INT >= 11)
                            new startDownload().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, notificationId);
                        else
                            new startDownload().execute(notificationId);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();

        ++notificationId;
    }

    public class startDownload extends AsyncTask<Integer, Void, String> {

        int Id;
        @Override
        protected void onPreExecute() {

// Start a lengthy operation in a background thread
//            new Thread(
//                    new Runnable() {
//                        @Override
//                        public void run() {
//                            int incr;
//                            // Do the "lengthy" operation 20 times
//                            for (incr = 0; incr <= 100; incr+=5) {
                                // Sets the progress indicator to a max value, the
                                // current completion percentage, and "determinate"
                                // state
//                                mBuilder.setProgress(100, incr, false);
//                                // Displays the progress bar for the first time.
//                                mNotifyManager.notify(id, mBuilder.build());
                                // Sleeps the thread, simulating an operation
                                // that takes time
//                                try {
//                                    // Sleep for 5 seconds
//                                    Thread.sleep(5*1000);
//                                } catch (InterruptedException e) {
//                                    Log.d("TAG", "sleep failure");
//                                }
//                            }
//                            // When the loop is finished, updates the notification
////                            mBuilder.setContentText("ההורדה הושלמה")
////                                    // Removes the progress bar
////                                    .setProgress(0,0,false);
////                            mNotifyManager.notify(id, mBuilder.build());
//                        }
//                    }
//// Starts the thread by calling the run() method in its Runnable
//            ).start();

//            progress = new ProgressDialog(ctx);
//            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            progress.setMessage("מוריד...");
//            progress.setCancelable(false);
//            progress.show();
        }

        @Override
        protected String doInBackground(Integer... params) {

            Id = params[0];
            Intent stopSoundIntent = new Intent(ctx,
                    StopReceiver.class)
                    .setAction("com.Flower.StopReceiver")
                    .putExtra("id", Id);

            Intent intent = new Intent();
            intent.setAction("com.Flower.StopReceiver");
            intent.putExtra("id", Id);
            PendingIntent pIntent = PendingIntent.getBroadcast(ctx, Id
                    , stopSoundIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("SKS_CHANNEL", "רשת וסק\"ש", NotificationManager.IMPORTANCE_LOW);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                channel.setVibrationPattern(new long[]{0L});
                channel.enableVibration(true);
                channel.enableLights(false);
                notificationManager.createNotificationChannel(channel);
            }

            mBuilder = new NotificationCompat.Builder(getApplicationContext(), "SKS_CHANNEL");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                mBuilder.setPriority(NotificationManager.IMPORTANCE_LOW);

            mBuilder.setContentTitle("הורדה ברקע")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(newPath.substring(newPath.lastIndexOf("/")+1)))
                    .setContentText(newPath)
                    .addAction(R.drawable.notif_stop,"עצור הורדה", pIntent)
                    .setAutoCancel(false)
                    .setTicker("מתחיל בהורדה")
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_notification);

            mBuilder.setProgress(0, 0, false);
            // Displays the progress bar for the first time.
            //allIds.add(notificationId++);

            mNotifyManager.notify(Id, mBuilder.build());

            try {
                if(newPath.contains("/"))
                    downloadedFile = new File(downloadsPath + newPath.substring(newPath.lastIndexOf("/")));
                else
                    downloadedFile = new File(downloadsPath + newPath);

                if(isWIFIAvailable()) return downloadSMBfile(Id, mBuilder);
                else if (isNetworkAvailable()) return downloadFTPfile(Id, mBuilder);
                else return "err";

//                sFile = new SmbFile(newPath, auth);
//                sFile.setConnectTimeout(5000);
//                sFile.setReadTimeout(5000);
//
//                Log.d("test",newPath);
//                int totalSize = (int)sFile.length();
//                byte[] buffer = new byte[totalSize/100];
//                double downloadedSize = 0;
//                int bufferLength = 0;
//                SmbFileInputStream fis = new SmbFileInputStream(sFile);
//                FileOutputStream out = new FileOutputStream(downloadedFile);
//
//                while ((bufferLength = fis.read(buffer)) > 0)
//                {
//                    double in = (downloadedSize/(double)totalSize)*100;
//                    downloadedSize += bufferLength;
//
//                    //progress.setProgress((int)in);
//                    if((int)in <= 100)
//                    mBuilder.setProgress(100, (int)in, false);
//                    // Displays the progress bar for the first time.
//                    mNotifyManager.notify(params[0], mBuilder.build());
//
//                    out.write(buffer);
//                }
//
//                out.flush();
//                out.close();
//                fis.close();
//                scanFile(downloadedFile);
//                return params[0]+"";
            } catch (SmbException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "err";
        }

        @Override
        protected void onPostExecute(String s) {
             if (s.equals("err")){
                Toast.makeText(ctx, "החיבור נקטע", Toast.LENGTH_LONG).show();
                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                     NotificationChannel channel = new NotificationChannel("SKS_CHANNEL", "רשת וסק\"ש", NotificationManager.IMPORTANCE_DEFAULT);
                     NotificationManager notificationManager = getSystemService(NotificationManager.class);
                     notificationManager.createNotificationChannel(channel);
                 }

                 mBuilder = new NotificationCompat.Builder(getApplicationContext(), "SKS_CHANNEL");
                if(mNotifyManager!=null) {
                    mBuilder.setContentTitle("שגיאה")
                            .setContentText("החיבור נקטע")
                            .setAutoCancel(true)
                            .setTicker("שגיאה בהורדה")
                            .setOngoing(false)
                            .setContentIntent(playLesson())
                            .setProgress(0, 0, false)
                            .setSmallIcon(R.drawable.ic_notification);


                    //DONE add .setSmallIcon(R.drawable.ic_notification);

                    mNotifyManager.notify(Id, mBuilder.build());
                }
            }
            else if (s.equals("stoped"))
            {
                isCancelled = false;
                toStop = -1;

                if (mNotifyManager != null) {
                    mBuilder.setContentTitle("ההורדה נעצרה")
                            // Removes the progress bar
                            .setAutoCancel(true)
                            .setOngoing(false)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setProgress(0, 0, false);
                    mNotifyManager.notify(Id, mBuilder.build());
                }
            }
            else {
//                if (progress != null) {
//                    progress.cancel();
//                }
                 if(mNotifyManager!=null) {
                     mBuilder.setContentTitle("ההורדה הושלמה")
//                             .setContentText(newPath.substring(newPath.lastIndexOf("/")+1))
//                             .setStyle(new NotificationCompat.BigTextStyle().bigText(newPath.substring(newPath.lastIndexOf("/")+1)))
                             // Removes the progress bar
                             .setAutoCancel(true)
                             .setOngoing(false)
                             .setContentIntent(playLesson())
                             .setTicker("ההורדה הושלמה")
                             .setSmallIcon(R.drawable.ic_notification)
                             .setProgress(0, 0, false);
                     mNotifyManager.notify(Id, mBuilder.build());
                     //DONE: CHANGE SET DURATION TO SNACK BAR, ADD COORDINATE LAYOUT AS PARENT IN XML SO CAN BE DISMISSED ON RIGHT SWIPE
                        Snackbar.make(findViewById(R.id.activity_main_sks), "ההורדה הושלמה", Snackbar.LENGTH_INDEFINITE)
                             .setDuration(10000)
                             .setAction("פתח", new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     Intent intent = new Intent();
                                     try {
                                         playLesson().send(cxt, 0, intent);
                                     } catch (PendingIntent.CanceledException e) {
                                         e.printStackTrace();
                                     }
//                                     startActivity(playLesson());
                                 }
                             }).show()

                        ;

                 }

             }
        }
    }

    public PendingIntent playLesson()
    {
        PendingIntent play = null;
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri data = FileProvider.getUriForFile(cxt, cxt.getApplicationContext().getPackageName() + ".provider", downloadedFile);

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = downloadedFile.getName().substring(downloadedFile.getName().indexOf(".") + 1).toLowerCase();
        String type = mime.getMimeTypeFromExtension(ext);

        //intent.setDataAndType(Uri.fromFile(downloadedFile), "audio/mp3");
        intent.setDataAndType(data, type);
        Intent chooser = Intent.createChooser(intent, "בחר אפליקציה לפתיחת הקובץ");
        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
        if (intent.resolveActivity(getPackageManager()) != null) {
            //startActivity(chooser);
            chooser.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            play = PendingIntent.getActivity(ctx, 0,chooser, PendingIntent.FLAG_UPDATE_CURRENT);
            return play;
        } else
            Toast.makeText(ctx, "לא נמצאה אפליקיה תומכת", Toast.LENGTH_SHORT).show();

        return play;
    }

    public String downloadSMBfile(int id, NotificationCompat.Builder mBuilder) throws Exception {

        //startService(new Intent(this, NotificationService.class));

        //String name = newPath.substring(newPath.lastIndexOf("/")+1);
        //NotificationCompat.Builder mBuilder = newBuilder;

        sFile = new SmbFile(SMBRoot + newPath, auth);
        sFile.setConnectTimeout(5000);
        sFile.setReadTimeout(5000);

        Log.d("test",newPath);
        int totalSize = (int)sFile.length();
        byte[] buffer = new byte[totalSize/20];
        double downloadedSize = 0;
        int bufferLength = 0;
        SmbFileInputStream fis = new SmbFileInputStream(sFile);
        FileOutputStream out = new FileOutputStream(downloadedFile);

        while ((bufferLength = fis.read(buffer)) > 0)
        {
            if (isCancelled  && toStop == id)  {
                isCancelled = true;
                toStop = -1;
                return "stoped";
            }

            double in = (downloadedSize/(double)totalSize)*100;
            downloadedSize += bufferLength;

            //progress.setProgress((int)in);
            //mBuilder = new NotificationCompat.Builder(getApplicationContext());
            //progress.setProgress((int)in);
            if((int)in <= 100) {
                mBuilder.setProgress(100, (int) in, false);
            }
            // Displays the progress bar for the first time.
            mNotifyManager.notify(id, mBuilder.build());

            out.write(buffer);
        }

        out.flush();
        out.close();
        fis.close();
        scanFile(downloadedFile);

        updateSMBCounter();

        return id+"";
    }

    public String downloadFTPfile(int id, NotificationCompat.Builder mBuilder) throws Exception {

        //String name = newPath.substring(newPath.lastIndexOf("/")+1);


        mFTPClient = new FTPClient();

        mFTPClient.setAutodetectUTF8(true);

        mFTPClient.connect("yhy.co.il");
        mFTPClient.enterLocalPassiveMode();
        mFTPClient.login("mp3site@yhy.co.il", ServerPass);

        //newPath = new String(bytes, "unicode");
        //newPath = StringEscapeUtils.escapeJava(newPath);
        String p = newPath.substring(0,newPath.lastIndexOf("/")+1).replace(sksRoot,FTPRoot);
        Boolean s = mFTPClient.changeWorkingDirectory(p);
        if (!s)
        {
            newPath = newPath.substring(0,newPath.lastIndexOf("/")+1);
            FTPFileList();
        }

        //FTPFile[] files = mFTPClient.listFiles();
        int totalSize = 0;
        try {
            totalSize = (int)getFileSize(mFTPClient,newPath.substring(newPath.lastIndexOf("/")+1).replace(sksRoot,FTPRoot));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //InputStream inStream = mFTPClient.retrieveFileStream("KABQ.TXT");
        //InputStreamReader isr = new InputStreamReader(inStream, "UTF8");

        // this will be useful so that you can show a tipical 0-100% progress bar
        //int lenghtOfFile = connection.getContentLength();

        // downlod the file

        InputStream fis = mFTPClient.retrieveFileStream(newPath.substring(newPath.lastIndexOf("/")+1).replace(sksRoot,FTPRoot));
        OutputStream out = new FileOutputStream(downloadedFile);
        //Boolean f = mFTPClient.retrieveFile(newPath,out);

        Log.d("test",newPath);

        byte[] buffer = new byte[totalSize/20];
        double downloadedSize = 0;
        int bufferLength = 0;

        while (((bufferLength = fis.read(buffer)) > 0))
        {
            if (isCancelled  && toStop == id)  {
                isCancelled = true;
                toStop = -1;
                return "stoped";
            }

            out.write(buffer, 0,bufferLength);

            double in = (downloadedSize/(double)totalSize)*100;
            downloadedSize += bufferLength;

            //progress.setProgress((int)in);
            if((int)in <= 100) {
                mBuilder.setProgress(100, (int) in, false);
            }
            // Displays the progress bar for the first time.
            mNotifyManager.notify(id, mBuilder.build());
        }

        out.flush();
        out.close();
        fis.close();
        scanFile(downloadedFile);

        mFTPClient.disconnect();
//        updateFTPCounter();
        new updateFTPCounter().execute();

        return id+"";
    }

    private long getFileSize(FTPClient ftp, String fileName) throws Exception {
        long fileSize = 0;
        FTPFile[] files = ftp.listFiles();
        for (FTPFile file : files)
        if (file.getName().equals(fileName) && file.isFile()) {
            return file.getSize();
        }
        Log.i("TAG", "File size = " + fileSize);
        return fileSize;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!uploadMode) {
                super.onBackPressed();
            }
            else {
                ActivityCompat.finishAffinity(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class NumberAwareStringComparatorSmbFile implements Comparator<SmbFile>{

        final Pattern PATTERN = Pattern.compile("(\\D*)(\\d*)");

        public NumberAwareStringComparatorSmbFile() {
        }

        @Override
        public int compare(SmbFile o1, SmbFile o2) {
            Matcher m1 = PATTERN.matcher(o1.getName());
            Matcher m2 = PATTERN.matcher(o2.getName());

            // The only way find() could fail is at the end of a string
            while (m1.find() && m2.find()) {
                // matcher.group(1) fetches any non-digits captured by the
                // first parentheses in PATTERN.
                int nonDigitCompare = m1.group(1).compareTo(m2.group(1));
                if (0 != nonDigitCompare) {
                    return nonDigitCompare;
                }

                // matcher.group(2) fetches any digits captured by the
                // second parentheses in PATTERN.
                if (m1.group(2).isEmpty()) {
                    return m2.group(2).isEmpty() ? 0 : -1;
                } else if (m2.group(2).isEmpty()) {
                    return +1;
                }

                BigInteger n1 = new BigInteger(m1.group(2));
                BigInteger n2 = new BigInteger(m2.group(2));
                int numberCompare = n1.compareTo(n2);
                if (0 != numberCompare) {
                    return numberCompare;
                }
            }

            // Handle if one string is a prefix of the other.
            // Nothing comes before something.
            return m1.hitEnd() && m2.hitEnd() ? 0 :
                    m1.hitEnd()                ? -1 : +1;
        }

//        public int compare(Object s1, Object s2) {
//
//            SmbFile file1 = (SmbFile)s1, file2 = (SmbFile)s2;
//            Matcher m1 = PATTERN.matcher(file1.getName());
//            Matcher m2 = PATTERN.matcher(file2.getName());
//
//            // The only way find() could fail is at the end of a string
//            while (m1.find() && m2.find()) {
//                // matcher.group(1) fetches any non-digits captured by the
//                // first parentheses in PATTERN.
//                int nonDigitCompare = m1.group(1).compareTo(m2.group(1));
//                if (0 != nonDigitCompare) {
//                    return nonDigitCompare;
//                }
//
//                // matcher.group(2) fetches any digits captured by the
//                // second parentheses in PATTERN.
//                if (m1.group(2).isEmpty()) {
//                    return m2.group(2).isEmpty() ? 0 : -1;
//                } else if (m2.group(2).isEmpty()) {
//                    return +1;
//                }
//
//                BigInteger n1 = new BigInteger(m1.group(2));
//                BigInteger n2 = new BigInteger(m2.group(2));
//                int numberCompare = n1.compareTo(n2);
//                if (0 != numberCompare) {
//                    return numberCompare;
//                }
//            }
//
//            // Handle if one string is a prefix of the other.
//            // Nothing comes before something.
//            return m1.hitEnd() && m2.hitEnd() ? 0 :
//                    m1.hitEnd()                ? -1 : +1;
//        }

    }

    private class NumberAwareStringComparatorFTPFile implements Comparator<FTPFile>{

        final Pattern PATTERN = Pattern.compile("(\\D*)(\\d*)");

        public NumberAwareStringComparatorFTPFile() {
        }

        @Override
        public int compare(FTPFile o1, FTPFile o2) {
            Matcher m1 = PATTERN.matcher(o1.getName());
            Matcher m2 = PATTERN.matcher(o2.getName());

            // The only way find() could fail is at the end of a string
            while (m1.find() && m2.find()) {
                // matcher.group(1) fetches any non-digits captured by the
                // first parentheses in PATTERN.
                int nonDigitCompare = m1.group(1).compareTo(m2.group(1));
                if (0 != nonDigitCompare) {
                    return nonDigitCompare;
                }

                // matcher.group(2) fetches any digits captured by the
                // second parentheses in PATTERN.
                if (m1.group(2).isEmpty()) {
                    return m2.group(2).isEmpty() ? 0 : -1;
                } else if (m2.group(2).isEmpty()) {
                    return +1;
                }

                BigInteger n1 = new BigInteger(m1.group(2));
                BigInteger n2 = new BigInteger(m2.group(2));
                int numberCompare = n1.compareTo(n2);
                if (0 != numberCompare) {
                    return numberCompare;
                }
            }

            // Handle if one string is a prefix of the other.
            // Nothing comes before something.
            return m1.hitEnd() && m2.hitEnd() ? 0 :
                    m1.hitEnd()                ? -1 : +1;
        }

//        public int compare(Object s1, Object s2) {
//
//            SmbFile file1 = (SmbFile)s1, file2 = (SmbFile)s2;
//            Matcher m1 = PATTERN.matcher(file1.getName());
//            Matcher m2 = PATTERN.matcher(file2.getName());
//
//            // The only way find() could fail is at the end of a string
//            while (m1.find() && m2.find()) {
//                // matcher.group(1) fetches any non-digits captured by the
//                // first parentheses in PATTERN.
//                int nonDigitCompare = m1.group(1).compareTo(m2.group(1));
//                if (0 != nonDigitCompare) {
//                    return nonDigitCompare;
//                }
//
//                // matcher.group(2) fetches any digits captured by the
//                // second parentheses in PATTERN.
//                if (m1.group(2).isEmpty()) {
//                    return m2.group(2).isEmpty() ? 0 : -1;
//                } else if (m2.group(2).isEmpty()) {
//                    return +1;
//                }
//
//                BigInteger n1 = new BigInteger(m1.group(2));
//                BigInteger n2 = new BigInteger(m2.group(2));
//                int numberCompare = n1.compareTo(n2);
//                if (0 != numberCompare) {
//                    return numberCompare;
//                }
//            }
//
//            // Handle if one string is a prefix of the other.
//            // Nothing comes before something.
//            return m1.hitEnd() && m2.hitEnd() ? 0 :
//                    m1.hitEnd()                ? -1 : +1;
//        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uploadMode) {
            unregisterReceiver(broadcastReceiver);
        }
        if(mNotifyManager!=null)
            mNotifyManager.cancelAll();
    }

    void setAdapter(){
//        getPos();
//        ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(index, top);
        String[] PathArr = newPath.split("/");
        final ArrayList paths = new ArrayList(Arrays.asList(PathArr));
        pathAdapter = new PathsAdapter(activity, paths, new PathsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!isLoading) {
                    newPath = "";

                    for (int i = 0; i <= position; i++) {
                        newPath += paths.get(i).toString() + "/";
//                    steps--;
//                    indexArr.remove(indexArr.size()-1);
//                    topArr.remove(topArr.size()-1);
                    }

                    createListView();
                }
            }
        });
        pathList.setAdapter(pathAdapter);
        pathList.scrollToPosition(pathAdapter.getItemCount() - 1);

        adapter = new CustomFilesList(activity, values, null, new CustomFilesList.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                pos = position;
                newPath = ((CustomFilesList) adapter).names.get(position);
//                if (oldPath.endsWith(File.separator)) {
//                    newPath = oldPath + newPath;
//                } else {
//                    newPath = oldPath + File.separator + newPath;
//                }
                newPath = oldPath + newPath;

                if (newPath.endsWith("/")) {
                    index = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                    View v = list.getChildAt(0);
                    top = (v == null) ? 0 : (v.getTop() - list.getPaddingTop());
                    stack.push(new String[]{oldPath, "" + index, "" + top});
                }
//                getPos();
//                indexArr.add(steps, index);
//                topArr.add(steps++, top);

                createListView();
            }
        } , new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pos = list.getChildAdapterPosition(v);
                if (((CustomFilesList)adapter).names.get(pos).endsWith("/")) {
                    saveFavorite();
                }

                return true;
            }
        }, "sks");
        list.setAdapter(adapter);

    }

    public void saveFavorite(){
        LayoutInflater factory = LayoutInflater.from(ctx);
        final View favDialogView = factory.inflate(R.layout.dialog_favorite, null);
        final AlertDialog favDialog = new AlertDialog.Builder(ctx).create();
        favDialog.setCancelable(true);
        favDialog.setView(favDialogView);
        final TextView favNickTv = (TextView) favDialogView.findViewById(R.id.textViewFav);
        favNickEt = (EditText) favDialogView.findViewById(R.id.fav_nick_et);
        favNickTv.setText("הקלד כינוי לתקית '" + ((CustomFilesList)adapter).names.get(pos).replace("/", "") + "'");
        favDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        favDialogView.findViewById(R.id.add_fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nick = favNickEt.getText().toString();
                String path = values.get(pos);

                SharedPreferences sharedPrefPath = getSharedPreferences(getString(R.string.allFavPaths), MODE_PRIVATE);
                String allPaths = sharedPrefPath.getString(getString(R.string.allFavPaths), "");

                SharedPreferences sharedPrefNicks = getSharedPreferences(getString(R.string.allFavNicks), MODE_PRIVATE);
                String allNicks = sharedPrefNicks.getString(getString(R.string.allFavNicks), "");

                if (ifNotExist(nick, newPath + path, allPaths, allNicks)) {
                    allPaths = newPath + path + "|" + allPaths;

                    allNicks = nick + "|" + allNicks;

                    SharedPreferences.Editor editor = sharedPrefPath.edit();
                    editor.putString(getString(R.string.allFavPaths), allPaths);
                    editor.apply();

                    editor = sharedPrefNicks.edit();
                    editor.putString(getString(R.string.allFavNicks), allNicks);
                    editor.apply();

                    Toast.makeText(ctx, "'" + nick + "'" +" נשמרה למועדפים בהצלחה", Toast.LENGTH_LONG).show();
                    favDialog.dismiss();
                }

//                                Toast.makeText(ctx, allNicks, Toast.LENGTH_LONG).show();
//                                Toast.makeText(ctx, allPaths, Toast.LENGTH_LONG).show();


            }
        });

        favDialogView.findViewById(R.id.cancel_fav).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favDialog.dismiss();
            }
        });
        favDialog.show();
    }

    public Boolean ifNotExist(String nick, String path, String allPaths, String allNicks)
    {
        for(String newnick : allNicks.split("\\|"))
            if(nick.equals(newnick)) {
                favNickEt.setError("קיים מעודף בשם זה, בחר כינוי אחר");
//                Toast.makeText(ctx,"קיים כבר מועדף בשם: '" + nick + "'" , Toast.LENGTH_LONG).show();
                return false;
            }

        for(String newpath : allPaths.split("\\|"))
            if(path.equals(newpath)) {
                Toast.makeText(ctx,"תקיית זו כבר שמורה במועדפים", Toast.LENGTH_LONG).show();
                return false;
            }

        return true;
    }

//    void getPos(){
//        if (adapter != null) {
//            index = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
////        index = data.getFirstVisiblePosition();
//            View v = list.getChildAt(0);
//            top = (v == null) ? 0 : (v.getTop() - list.getPaddingTop());
//            indexArr.add(steps, index);
//            topArr.add(steps++, top);
//        }
//    }

    public void updateSMBCounter()
    {
        String user = getString(R.string.user) + ":" + getString(R.string.pass);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

        //String path = "smb://192.168.1.25/student/19 מחזור כ''ג/עילאי האמיתי חן/lessons.xml";
        String path;
        if (isSks) {
            path = netSksDownloadsCount;
        }
        else {
            path = netFilesDownloadsCount;
        }
        SmbFile sFile = null;
        try {
            sFile = new SmbFile(path,auth);

            sFile.setConnectTimeout(5000);
            sFile.setReadTimeout(5000);
            //SmbFileOutputStream sfos = new SmbFileOutputStream(sFile);
            SmbFileInputStream in = new SmbFileInputStream(sFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(in));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            in.close();

            int num = Integer.parseInt(aBuffer.substring(0, aBuffer.length()-1));
            num++;
            Log.d("MainActivity", "updateSMBCounter: "+aBuffer+" "+ aBuffer.substring(0, aBuffer.length()-1));
            aBuffer = "" + num;
            sFile = new SmbFile(path,auth);

            SmbFileOutputStream out = new SmbFileOutputStream(sFile);
            out.write(aBuffer.getBytes());
            out.flush();
            out.close();
            //sfos.write("Test".getBytes());
            //sfos.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            //Error(e.toString());
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //      Toast.LENGTH_SHORT).show();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            //Error(e.toString());
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (SmbException e) {
            e.printStackTrace();
            //Error(e.toString());
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            //Error(e.toString());
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        }
    }

    private class updateFTPCounter extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                mFTPClient = new FTPClient();
                mFTPClient.setAutodetectUTF8(true);
                mFTPClient.connect("yhy.co.il");

                mFTPClient.enterLocalPassiveMode();
                mFTPClient.login("mp3site@yhy.co.il", ServerPass);

                InputStream in = mFTPClient.retrieveFileStream(netFTPDownloadsCount);
                BufferedReader myReader = new BufferedReader(
                        new InputStreamReader(in));
                String aDataRow = "";
                String aBuffer = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    aBuffer += aDataRow + "\n";
                }
                in.close();
                mFTPClient.disconnect();

                int num = Integer.parseInt(aBuffer.substring(0, aBuffer.length()-1));
                num++;
                aBuffer = "" + num + "\n";


                mFTPClient = new FTPClient();
                mFTPClient.setAutodetectUTF8(true);
                mFTPClient.connect("yhy.co.il");
                mFTPClient.enterLocalPassiveMode();
                mFTPClient.login("mp3site@yhy.co.il", ServerPass);

                Log.d("test", aBuffer);

                InputStream inputStream = new ByteArrayInputStream(aBuffer.getBytes());
                mFTPClient.storeFile(netFTPDownloadsCount, inputStream);
                inputStream.close();

                mFTPClient.disconnect();
            } catch (Exception e) {
                Log.v("test", "errr");
                e.printStackTrace();
            }
            return "done";
        }
    }
}


