package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.flower.yeshivatyeruham.DataClass.WIFINAME;

/**
 * present list of favorites the user save.
 */
public class ActiveActivity extends AppCompatActivity {

    LinearLayout empty;
    RecyclerView data;
    RecyclerView.Adapter adapter;
    Context ctx;
    int index = 0;
    int top = 0;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList pathsArr = new ArrayList();
    ArrayList nicksArr = new ArrayList();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ctx = this;
        empty = (LinearLayout) findViewById(R.id.empty_favorites);
        data = (RecyclerView) findViewById(R.id.favorites_list);
        data.setLongClickable(true);
        mLayoutManager = new LinearLayoutManager(this);
        data.setLayoutManager(mLayoutManager);
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
                    viewHolder.itemView.performLongClick();
//                    AlertDialog.Builder alertDialog= new AlertDialog.Builder(ActiveActivity.this);
//                    alertDialog
//                            .setMessage("האם אתה בטוח שברצונך למחוק קיצור זה?")
//                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    SharedPreferences sharedPrefPath = getSharedPreferences(getString(R.string.netAllActPathsPath), MODE_PRIVATE);
//                                    String allPaths = sharedPrefPath.getString(getString(R.string.netAllActPathsPath),"");
//                                    String s = pathsArr.get(pos) + "|";
//                                    allPaths = allPaths.replaceAll(pathsArr.get(pos)+ "\\|", "");
//                                    SharedPreferences.Editor editor = sharedPrefPath.edit();
//                                    editor.putString(getString(R.string.netAllActPathsPath), allPaths);
//                                    editor.apply();
//
//                                    sharedPrefPath = getSharedPreferences(getString(R.string.netAllActNicksPath), MODE_PRIVATE);
//                                    String allNicks = sharedPrefPath.getString(getString(R.string.netAllActNicksPath),"");
//                                    s = nicksArr.get(pos) + "|";
//                                    allNicks = allNicks.replaceAll(nicksArr.get(pos)+ "\\|", "");
//
//                                    editor = sharedPrefPath.edit();
//                                    editor.putString(getString(R.string.netAllActNicksPath), allNicks);
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
                        p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.open_fav_color));
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


//        data.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> arg0, final View view, final int pos, long id) {
//                AlertDialog.Builder alertDialog= new AlertDialog.Builder(ActiveActivity.this);
//                alertDialog
//                        .setMessage("האם אתה בטוח שברצונך למחוק קיצור זה?")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                SharedPreferences sharedPrefPath = getSharedPreferences(getString(R.string.netAllActPathsPath), MODE_PRIVATE);
//                                String allPaths = sharedPrefPath.getString(getString(R.string.netAllActPathsPath),"");
//                                String s = adapter.paths.get(pos) + "|";
//                                allPaths = allPaths.replaceAll(adapter.paths.get(pos)+ "\\|", "");
//                                SharedPreferences.Editor editor = sharedPrefPath.edit();
//                                editor.putString(getString(R.string.netAllActPathsPath), allPaths);
//                                editor.apply();
//
//                                sharedPrefPath = getSharedPreferences(getString(R.string.netAllActNicksPath), MODE_PRIVATE);
//                                String allNicks = sharedPrefPath.getString(getString(R.string.netAllActNicksPath),"");
//                                s = adapter.names.get(pos) + "|";
//                                allNicks = allNicks.replaceAll(adapter.names.get(pos)+ "\\|", "");
//
//                                editor = sharedPrefPath.edit();
//                                editor.putString(getString(R.string.netAllActNicksPath), allNicks);
//                                editor.apply();
//                                onResume();
//
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // do nothing
//                            }
//                        })
//                        .show();
//                return true;
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
    public boolean isOnline(Boolean isSks) {
        WifiManager connManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWiFi = connManager.getConnectionInfo();

        if (isSks) {
            if (mWiFi != null && mWiFi.getSSID() != null &&
                    (mWiFi.getSSID().contains(WIFINAME.split(",")[0]) ||
                            mWiFi.getSSID().contains(WIFINAME.split(",")[1]))) {
                return true;
            } else {
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
                    return true;
                else {
                    Toast.makeText(ctx, "לא ניתן להציג, אין חיבור לרשת", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        else {
            if (mWiFi != null && mWiFi.getSSID() != null && (mWiFi.getSSID().contains(WIFINAME.split(",")[0]) || mWiFi.getSSID().contains(WIFINAME.split(",")[1]))) {
                return true;
            } else {
                Toast.makeText(ctx, "לא ניתן להציג, אין חיבור לרשת הישיבה", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
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

        if(getFavoritesNames()) {
            empty.setVisibility(View.GONE);
            data.setVisibility(View.VISIBLE);
        }
        else {
            empty.setVisibility(View.VISIBLE);
            data.setVisibility(View.GONE);
        }
//        adapter = new CustomRecordsList(this, allDownloads, data);
        setAdapter();
    }

    void setAdapter(){
        getPos();
        ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(index, top);
        adapter = new CustomFilesList(this, nicksArr, pathsArr, new CustomFilesList.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String path = pathsArr.get(position).toString();
                if(isOnline(path.startsWith("mp3/"))) {
                    Intent sks = new Intent(ctx, SksActivity.class);
                    sks.putExtra("rootPath", path);
                    startActivity(sks);
                    finish();
                }
            }
        }, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int pos = data.getChildAdapterPosition(v);
                AlertDialog.Builder alertDialog= new AlertDialog.Builder(ActiveActivity.this);
                alertDialog
                        .setMessage("האם אתה בטוח שברצונך למחוק קיצור זה?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPrefPath = getSharedPreferences(getString(R.string.netAllActPath), MODE_PRIVATE);
                                String allPaths = sharedPrefPath.getString(getString(R.string.netAllActPath),"");
                                String s = pathsArr.get(pos) + "|";
                                allPaths = allPaths.replaceAll(pathsArr.get(pos)+ "\\|", "");
                                SharedPreferences.Editor editor = sharedPrefPath.edit();
                                editor.putString(getString(R.string.netAllActPath), allPaths);
                                editor.apply();

                                sharedPrefPath = getSharedPreferences(getString(R.string.netAllActPath), MODE_PRIVATE);
                                String allNicks = sharedPrefPath.getString(getString(R.string.netAllActPath),"");
                                s = nicksArr.get(pos) + "|";
                                allNicks = allNicks.replaceAll(nicksArr.get(pos)+ "\\|", "");

                                editor = sharedPrefPath.edit();
                                editor.putString(getString(R.string.netAllActPath), allNicks);
                                editor.apply();
                                onResume();

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
        }, "favorites");
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

    public boolean getFavoritesNames() {

        pathsArr.clear();
        SharedPreferences sharedPrefPath = getSharedPreferences(getString(R.string.netAllActPath), MODE_PRIVATE);
        String allPaths = sharedPrefPath.getString(getString(R.string.netAllActPath),"");
        String[] paths = allPaths.split("\\|");
        if (!allPaths.equals("")){
            for (String path: paths) {
                pathsArr.add(path);
            }
        }

        nicksArr.clear();
        SharedPreferences sharedPrefNicks = getSharedPreferences(getString(R.string.netAllActPath), MODE_PRIVATE);
        String allNicks = sharedPrefNicks.getString(getString(R.string.netAllActPath),"");
        String[] nicks = allNicks.split("\\|");
        if (!allNicks.equals("")){
            for (String nick: nicks) {
                nicksArr.add(nick);
            }
        }

        if (pathsArr.isEmpty())
            return false;
        else return true;

//        Arrays.sort(files, new Comparator<File>(){
//            public int compare(File f2, File f1)
//            {
//                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
//            } });
////        List<String> filesNames = new ArrayList<>();
//        Log.d("Files", "Size: "+ files.length);
//        for (int i = 0; i < files.length; i++)
//        {
//            filesNames.add(i ,files[i].getName());
////            if(isAudioFile(files[i].getName())) {
////                rootNames.add(j, files[i].getName());
////                filesNames.add(j, rootNames.get(j));
////                filesNames.set(j, filesNames.get(j).substring(0,filesNames.get(j).lastIndexOf(".") ));
////                Log.d("Files", "FileName:" + files[i].getName());
////                j++;
////            }
//        }
    }
}
