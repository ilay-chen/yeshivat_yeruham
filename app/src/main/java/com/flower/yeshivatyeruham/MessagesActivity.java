package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MessagesActivity extends AppCompatActivity {

    LinearLayout empty;
    RecyclerView data;
    RecyclerView.Adapter adapter;
    Context ctx;
    int index = 0;
    int top = 0;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList titlesArr = new ArrayList();
    ArrayList textsArr = new ArrayList();
    ArrayList linksArr = new ArrayList();


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ctx = this;
        empty = (LinearLayout) findViewById(R.id.empty_msgs);
        data = (RecyclerView) findViewById(R.id.msgs_list);
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
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder(MessagesActivity.this);
                    alertDialog
                            .setMessage("האם אתה בטוח שברצונך למחוק הודעה זו?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.savedNtf), MODE_PRIVATE);

                                    String titles = sharedPref.getString(getString(R.string.savedNtf_titles), "");
                                    String texts = sharedPref.getString(getString(R.string.savedNtf_texts), "");
                                    String links = sharedPref.getString(getString(R.string.savedNtf_links), "");

                                    titles = titles.replace(titlesArr.get(pos) + "<^>", "");
                                    texts = texts.replace(textsArr.get(pos) + "<^>", "");
                                    links = links.replace(linksArr.get(pos) + "<^>", "");

                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString(getString(R.string.savedNtf_titles), titles);
                                    editor.putString(getString(R.string.savedNtf_texts), texts);
                                    editor.putString(getString(R.string.savedNtf_links), links);
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

        if(getMessages()) {
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
        adapter = new CustomFilesList(this, titlesArr, textsArr, new CustomFilesList.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MessagesActivity.this, MessageDialog.class);
                intent.putExtra("data", textsArr.get(position).toString());
                intent.putExtra("link", linksArr.get(position).toString());
                intent.putExtra("title", titlesArr.get(position).toString());
                intent.putExtra("massege", true);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }, null, "messages");
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

    public boolean getMessages() {

        titlesArr.clear();
        textsArr.clear();
        linksArr.clear();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.savedNtf), MODE_PRIVATE);
        String allTitles = sharedPref.getString(getString(R.string.savedNtf_titles), "");
        String allTexts = sharedPref.getString(getString(R.string.savedNtf_texts), "");
        String allLinks = sharedPref.getString(getString(R.string.savedNtf_links), "");

        List<String> titlesList= new LinkedList(Arrays.asList(allTitles.split("<\\^>", -1)));
        titlesList.remove(titlesList.size()-1);
        List<String> textsList= new LinkedList(Arrays.asList(allTexts.split("<\\^>", -1)));
        textsList.remove(textsList.size()-1);
        List<String> linksList= new LinkedList(Arrays.asList(allLinks.split("<\\^>", -1)));
        linksList.remove(linksList.size()-1);

        String[] titles = titlesList.toArray(new String[titlesList.size()]);
        String[] texts =  textsList.toArray(new String[textsList.size()]);
        String[] links = linksList.toArray(new String[linksList.size()]);

        if (!allTitles.equals("")){
            Collections.addAll(titlesArr, titles);
            Collections.addAll(textsArr, texts);
            Collections.addAll(linksArr, links);
        }


        if (titlesArr.isEmpty())
            return false;
        else return true;
    }
}
