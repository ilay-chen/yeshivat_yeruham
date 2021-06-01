package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.flower.yeshivatyeruham.AttendanceDialog.checkDateFormat;
import static com.flower.yeshivatyeruham.DataClass.netFTPAppDataPath;
import static com.flower.yeshivatyeruham.DataClass.readFileFromFTPServer;

/**
 * Created by dell on 24 דצמבר 2017.
 *
 */

public class AttendanceListDialog extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private AttendanceListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Spinner mSpinner;
    private TextView barTextView;
    private ImageButton button;
    Activity context;
    SwipeRefreshLayout swipeRefreshLayout;

    AttendanceResponse ar1;
    static boolean active=false;

    FloatingActionButton multiSmsBtn;//for admins

    private TextView hereTV, notHereTV, didntRespondTV;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_attendance_list);
        mSpinner = (Spinner) findViewById(R.id.attendance_list_spinner);
        barTextView=(TextView)findViewById(R.id.attendance_list_date);
        button=(ImageButton) findViewById(R.id.attendance_list_response);
        mRecyclerView = (RecyclerView) findViewById(R.id.attendance_list_recycler_view);

        mRecyclerView.setHasFixedSize(true);


        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.attendance_list_swiprefresh);

        hereTV=(TextView) findViewById(R.id.att_list_isHere_TV) ;
        notHereTV=(TextView) findViewById(R.id.att_list_notHere_TV);
        didntRespondTV=(TextView) findViewById(R.id.att_list_noResponse_TV);

        barTextView.setVisibility(View.GONE);

        if(Arrays.asList(fileList()).contains(getString(R.string.localStudentsFN))&&
                Arrays.asList(fileList()).contains(getString(R.string.attendance_path)) ) {
            createListFromAR(new AttendanceResponse(context));
            setTimeUpdated();
        }
            swipeRefreshLayout.setRefreshing(true);
            updateList();

//CODE FOR AN UPDATE BUTTON
        //button.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {

       //         if(!swipeRefreshLayout.isRefreshing()){
      //         swipeRefreshLayout.setRefreshing(true);
      //          updateList();}
      //      }
    //    });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(context, AttendanceDialog.class) ;
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();

            }
        });

        multiSmsBtn = (FloatingActionButton) findViewById(R.id.att_list_multi_sms_button);
        multiSmsBtn.setVisibility(View.VISIBLE);//todo change to gone on app publish

    }

    /**
     * creates an adapter  with the values extracted from @param-groups
     * and puts it in th Spinner
     * the method also changes all the people who are in shiur ח and higher and puts them together in one group
     * @param groups a List with all the yearGrops
     */
    private void addItemsToSpinner(List<String> groups) {

        int i = gimatria(groups.get(0));
        boolean isYear6Plus = false;

        Iterator<String> iter = groups.iterator();
        while (iter.hasNext()) {
            String year = iter.next();
            if (!year.equals("אורח") && i - gimatria(year) >= 5) {
                iter.remove();
                isYear6Plus = true;
            }
        }
        if (isYear6Plus)
            groups.add("שיעור ו+");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(dataAdapter);
    }

    private void updateList(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new updateProperties().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
            else
                new updateProperties().execute(context, swipeRefreshLayout);

    }

    /**
     * sets the text view the time that the list was update
     */
    private void setTimeUpdated(){
        barTextView.setVisibility(View.INVISIBLE);
        List info = new ArrayList(MainActivity.getInfo("נוכחות", this));

        SharedPreferences sharedPreferences = getSharedPreferences("strings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("attLastDate", (String) info.get(1));
        editor.apply();

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.files_version), MODE_PRIVATE);
        Calendar c = Calendar.getInstance();
        Calendar now=Calendar.getInstance();
        c.setTimeInMillis(sharedPref.getLong(context.getString(R.string.attendance_path)+"updated", 0));
        now.setTime(new Date());
        boolean sameDay=c.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                c.get(Calendar.DAY_OF_YEAR)==now.get(Calendar.DAY_OF_YEAR);
        DateFormat df=new SimpleDateFormat("dd.MM HH:mm");
     //   boolean sameHour=c.get(Calendar.HOUR_OF_DAY)==now.get(Calendar.HOUR_OF_DAY);
     //   String stSameHour= sameHour?(now.get(Calendar.MINUTE)-c.get(Calendar.MINUTE))+" דקות":(now.get(Calendar.HOUR_OF_DAY)-c.get(Calendar.HOUR_OF_DAY))+" שעות";

    //    String howMuchTimePassed="עודכן לפני: "+ stSameHour;

        SharedPreferences sharedPref1 = getApplicationContext().getSharedPreferences("strings", MODE_PRIVATE);
        String date=sharedPref1.getString("attLastDate", "");
        DateFormat dateFormat=checkDateFormat(date);
        boolean beforeLastDate=false;
        try {
           beforeLastDate=new Date().before(dateFormat.parse(date));  //checks whether the person already responded to this attendance
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(!beforeLastDate) {
            barTextView.setText("ההרשמה סגורה!");
            barTextView.setTextColor(Color.RED);
            barTextView.setVisibility(View.VISIBLE);
        }
        else if(!sameDay) {
            barTextView.setText("עודכן: " + df.format(c.getTime()));
            barTextView.setVisibility(View.VISIBLE);

        }

    }

    private void createListFromAR(final AttendanceResponse ar) {
        {
            addItemsToSpinner(ar.getGroups());

            //gets called when a user clicks an item on the list, currently opnes an AttendanceDialog
            // for the clicked person
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                    parent.getItemAtPosition(position);

                      ar1=new AttendanceResponse(ar, (String) parent.getItemAtPosition(position));
                        updateResponseCounter();

                    mAdapter = new AttendanceListAdapter(ar1, context, new AttendanceListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(context, AttendanceDialog.class);
                            intent.putExtra("name", ar1.getName(position) );
                            intent.putExtra("attListPos", position);
                            startActivityForResult(intent, 1);

//                            mAdapter.addToList(position);
                            mAdapter.notifyItemChanged(position);
                            mAdapter.notifyDataSetChanged();

                            updateResponseCounter();
                        }
                    });

                    mRecyclerView.setAdapter(mAdapter);
                    new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
                    {
                        @Override
                        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                            return false;
                        }

                        // Called when a user swipes left or right on a ViewHolder
                        @Override
                        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                            int pos = mRecyclerView.getChildAdapterPosition(viewHolder.itemView);
                            if (swipeDir == ItemTouchHelper.RIGHT) {

                                Intent intent=new Intent(context, AttendanceDialog.class);
                                intent.putExtra("name", ar1.getName(pos) );
                                intent.putExtra("listResponse", "כן");
                                startActivityForResult(intent, 1);
//                                mAdapter.addToList(pos);
                                mAdapter.notifyItemChanged(pos);
                                mAdapter.notifyDataSetChanged();

                                updateResponseCounter();

                            } else if (swipeDir == ItemTouchHelper.LEFT) {

                                Intent intent=new Intent(context, AttendanceDialog.class);
                                intent.putExtra("name", ar1.getName(pos) );
                                intent.putExtra("listResponse", "לא");
                                startActivityForResult(intent, 1);

//                                mAdapter.addToList(pos);
                                mAdapter.notifyItemChanged(pos);//todo check if it is needed
                                mAdapter.notifyDataSetChanged();

                                updateResponseCounter();
                            }
                        }

                        //this is what is drawn when swiped
                        @Override
                        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                // Get RecyclerView item from the ViewHolder
                                View itemView = viewHolder.itemView;

                                Paint p = new Paint();
                                if (dX > 0) {
            /* Set your color for positive displacement */
                                    p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.attendance_here));

                                    // Draw Rect with varying right side, equal to displacement dX
                                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                            (float) itemView.getBottom(), p);

                                    p.setColor(Color.BLACK);
                                    p.setTextSize(100);
                                    c.drawText("\uD83D\uDC4D",  (((float) itemView.getLeft())+dX)/(float)10, (float) itemView.getBottom()- (float)itemView.getHeight()/2+ p.getTextSize()/2-25, p  );
                                } else if (dX < 0) {
            /* Set your color for negative displacement */
                                    p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.attendance_not_here));
                                    // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                                    c.drawRect((float) itemView.getRight()+ dX, (float) itemView.getTop(),
                                            (float) itemView.getRight(), (float) itemView.getBottom(), p);

                                    p.setColor(Color.BLACK);
                                    p.setTextSize(100);
                                    p.setTextSkewX((float)-0.75);

                                    c.drawText("\uD83D\uDC4E",  (((float) itemView.getRight()*2 + dX))/(float)2, (float) itemView.getBottom()- (float)itemView.getHeight()/2+ p.getTextSize()/2-25, p  );

                                }

                                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            }
                        }
                    })
                            .attachToRecyclerView(mRecyclerView);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            //mAdapter = new AttendanceListAdapter(ar, context);
            // mRecyclerView.setAdapter(mAdapter);


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==1){
            if(resultCode==RESULT_OK) {
                String resultName = data.getStringExtra("name");
                int pos=ar1.getPosByName(resultName);
                if(pos>=0){
                    mAdapter.addToList(pos);
                    mAdapter.notifyItemChanged(pos);
                    mAdapter.notifyDataSetChanged();

                    updateResponseCounter();

                }
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        active=true;
    }

    @Override
    public void onStop(){
        super.onStop();
        active=false;
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        int pos = ar1.getPosByName(intent.getStringExtra("name"));
        boolean isComing=intent.getBooleanExtra("isComing", false);
        if(pos>=0) {
            mAdapter.removeFromList(pos);
            ar1.setIsHere(pos, isComing);
            mAdapter.notifyItemChanged(pos);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * created an AttendanceResponse object in the background(as it has functions of reading from the server(might change)
     *
     * than creates a list for the user with the information(including a dropdown list with the yearGroups)
     */
     class updateProperties extends AsyncTask {

        //creates an attendResponse Object in background(as it reads from server(might be changed in the future)
        @Override
        protected AttendanceResponse doInBackground(Object[] params) {
            readFileFromFTPServer(context.getString(R.string.attendance_path), netFTPAppDataPath + context.getString(R.string.attendance_path),
                    true, context);
            readFileFromFTPServer(context.getString(R.string.localStudentsFN),  netFTPAppDataPath+context.getString(R.string.localStudentsFN),
                    true, context);

            final AttendanceResponse ar = new AttendanceResponse(context);

            return ar;
        }

        @Override
        protected void onPostExecute(final Object o) {
            int spinPos=mSpinner.getSelectedItemPosition();
            createListFromAR((AttendanceResponse) o);
            mSpinner.setSelection(spinPos);
            setTimeUpdated();
            swipeRefreshLayout.setRefreshing(false);
            //mAdapter = new AttendanceListAdapter(ar, context);
           // mRecyclerView.setAdapter(mAdapter);

            multiSmsBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    sendMessage(((AttendanceResponse) o).getPhNumsDidntRespond((String)mSpinner.getSelectedItem()));
                }
            });



            super.onPostExecute(o);

        }

    }

    /**
     * this method gets a hebrew charsequence and returns the int value of that sequence
     * this method does not check if the input is valid
     * @param s the CharSequence
     * @return the Gimatria value
     */
    public static int gimatria(CharSequence s) {
        int sum=0;
        if(s==null)
            return -1;
        if (s.length()==0)
            return 0;
        for(int i=0; i<s.length(); i++)
            sum+=hCharVal(s.charAt(i));

        return sum;

    }

    /**
     * gets a a hebrew char and finds it's gimatria value
     * this method does not check validity of the input
     * @param a the hebrew char
     * @return the Gimatria value of the char
     */
    private static int hCharVal(char a) {
        if(a<'י')
            return a-'א'+1;
        if (a > 'צ')
            return (a - 'ק' + 1) * 10;
        if (a == 'י')
            return 10;
        if (a == 'כ')
            return 20;
        if (a == 'ך')
            return 20;
        if (a == 'ל')
            return 30;
        if (a == 'מ')
            return 40;
        if (a == 'ם')
            return 40;
        if (a == 'נ')
            return 50;
        if (a == 'ן')
            return 50;
        if (a == 'ס')
            return 60;
        if (a == 'ע')
            return 70;
        if (a == 'פ')
            return 80;
        if (a == 'ף')
            return 80;
        if (a == 'צ')
            return 90;
        if (a == 'ץ')
            return 90;
        return 0;
    }

    private void sendMessage(List phNums){
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        String numbers = "";
        for (int i = 0; i < phNums.size(); i++) {
            numbers = numbers + phNums.get(i) + ", ";
        }
        sendIntent.setData(Uri.parse("sms:" + numbers));
        startActivity(sendIntent);
    }

    private void updateResponseCounter(){
        int[] responseCounter=ar1.countResponse();
        hereTV.setText(Integer.toString(responseCounter[0]));
        notHereTV.setText(Integer.toString(responseCounter[1]));
        didntRespondTV.setText(Integer.toString(responseCounter[2]));



    }



    }




