package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.flower.yeshivatyeruham.ContactsActivity.phNums;
import static com.flower.yeshivatyeruham.DataClass.ContactPath;

/**
 * Created by eitanabinovich on 17/10/2017.
 */

public class ContactSelectedActivity extends AppCompatActivity{


        final int RequestPermissionCode = 1;
        SearchView searchView;
        RecyclerView list;
        RecyclerView.Adapter adapter;
        Thread createTrd;
        FileInputStream is;
        static BufferedReader reader;
     Button selectedCount;
//    FloatingActionButton multiSmsBtn;  adds the multissButton,


    //protected static List numArr = new ArrayList();

        //    MenuItem multiSms;
        @Override
        protected void attachBaseContext(Context newBase) {
            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
            super.onCreate(savedInstanceState);
//        searchTask.execute("");
            setContentView(R.layout.activity_contacts);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
//            multiSmsBtn = (FloatingActionButton) findViewById(R.id.multi_sms_button);
//            multiSmsBtn.setVisibility(View.VISIBLE);
//            multiSmsBtn.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    sendMessage();
//                }
//            });
            list = (RecyclerView) findViewById(R.id.data_list);
            phNums = getIntent().getCharSequenceArrayListExtra("phNumsArray");

            RecyclerView.LayoutManager mLayoutManager;
            mLayoutManager = new LinearLayoutManager(this);
            list.setLayoutManager(mLayoutManager);
            list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            createListView();



        }
        public boolean onCreateOptionsMenu(Menu menu) {
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        // Inflate the menu; this adds items to the action bar if it is present.
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contactsselected, menu);

            MenuItem item=menu.findItem(R.id.num_selected);
            MenuItemCompat.setActionView(item, R.layout.feed_update_count);
            selectedCount= (Button) MenuItemCompat.getActionView(item);
            //View count=menu.findItem(R.id.num_selected).getActionView();
            //selectedCount=(Button) count.findViewById(R.id.selcted_count);
            selectedCount.setText(String.valueOf(phNums.size()));

            return super.onCreateOptionsMenu(menu);}
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify contacts_database parent activity in AndroidManifest.xml.
//        if (item.getItemId() == R.id.search) {
//            if (Build.VERSION.SDK_INT < 11) {
//                onSearchRequested();
//            }
//        } else
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;
            }
            if(item.getItemId()== R.id.send_message){
                if(!phNums.isEmpty())
                    sendMessage();
                else
                    Toast.makeText(this, "בחר אנשים ונסה שנית", Toast.LENGTH_SHORT).show();
            }
            if (item.getItemId()== R.id.delete_all) {
                phNums.clear();
                selectedCount.setText(String.valueOf(phNums.size()));
//                multiSmsBtn.hide();
            }

                createListView();


            return super.onOptionsItemSelected(item);
        }
//    @Override public void startActivity(Intent intent) {
//        super.startActivity(intent);
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
//            finish();
//        }
//    }


        private void createListView() {
            if (phNums.isEmpty()) {
                (findViewById(R.id.empty)).setVisibility(View.VISIBLE);
                (findViewById(R.id.ContactsTable1)).setVisibility(View.GONE);
                (findViewById(R.id.data_list)).setVisibility(View.GONE);

                return;
            }
            findViewById(R.id.ContactsTable1).setVisibility(View.VISIBLE);
            removeDupArrayList();
            findViewById(R.id.empty).setVisibility(View.GONE);
                            adapter = new CustomContactsList(this, createNameArr(this, phNums), phNums, createArrGroup(this, phNums), createCheckedList(phNums), checkShowPics(), new CustomContactsList.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        showDetails(position);
                    }
                }, new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        phNums.remove(list.getChildAdapterPosition(v));
                        selectedCount.setText(String.valueOf(phNums.size()));
                        createListView();
//                        if(phNums.isEmpty()) multiSmsBtn.hide();
                        return true;
                    }
                });


            list.setAdapter(adapter);
            list.setLongClickable(true);

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                // Called when a user swipes left or right on a ViewHolder
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    int pos = list.getChildAdapterPosition(viewHolder.itemView);
                    if (swipeDir == ItemTouchHelper.RIGHT) {
                        onCall((String) phNums.get(pos));
                        adapter.notifyItemChanged(pos);
                    } else if (swipeDir == ItemTouchHelper.LEFT) {
                        sms((String) phNums.get(pos));
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
                            p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.call_color));
                            // Draw Rect with varying right side, equal to displacement dX
                            c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                    (float) itemView.getBottom(), p);
                        } else if (dX < 0) {
            /* Set your color for negative displacement */
                            p.setColor(ContextCompat.getColor(getApplicationContext(), R.color.sms_color));
                            // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                            c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                    (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        }

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                }
            }).attachToRecyclerView(list);


//
        }



        public void sortRows() {
            Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
            int rows = phNums.size();
            // Bubble sort table rows
            for (int i = 0; i < rows; i++) {
                for (int j = 1; j < (rows - i); j++) {
                    String s1 = getName(this, (String) phNums.get(j - 1));
                    String s2 = getName(this, (String) phNums.get(j));
                    if (s2.compareTo(s1) < 0) {
                        //swap the elements!
                        swapRows(i, j);
                    }
                }
            }
        }

        public void swapRows(int i, int j) {


            Object tmp1 = phNums.get(j - 1);
            Object tmp2 = phNums.get(j);
            phNums.set(j - 1, tmp2);
            phNums.set(j, tmp1);


        }


        public void showDetails(int position) {
            Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
            String phNum=(String) phNums.get(position);
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("name", getName(this, phNum));
            intent.putExtra("number", phNum);
            intent.putExtra("group", getGroup(this, phNum));
            startActivity(intent);
        }



        public static boolean checkLine(String line, String query) {
            query = query.trim();
            String[] words = query.split(" ");
//        if (words.length == 0) words = new String[]{query};
            String[] SplitLine = line.split(",", 4);
            String[] names = SplitLine[0].split(" ");

            if (SplitLine[1].contains(query)) {
                return true;
            }
            Boolean found = false;
            for (String word : words) {
                found = false;
                int i = 0;
//            for (; i < names.length; i++){
                for (String name : names) {
//                if (names[i].startsWith(word)) {
                    if (name.startsWith(word)) {
                        found = true;
                    }
                }
                if (!found) break;
            }
            return found;
//        if (SplitLine[0].startsWith(query)){
//            return true;
//        }
//        else if (words.length == 2 && SplitLine[0].startsWith(words[1])
//                && SplitLine[0].startsWith(words[0]))
//            return true;
//        return false;
        }

        public void onCall(String num) {
            Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + num));
            startActivity(callIntent);
        }

        public void sms(String num) {
            Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("sms:" + num));
            startActivity(sendIntent);
        }

        public static void clearAsyncTask(AsyncTask asyncTask) {
            if (asyncTask != null) {
                if (!asyncTask.isCancelled()) {
                    asyncTask.cancel(true);
                }
                asyncTask = null;
            }
        }


        /**
         * this method searches for info about a user using his phone number to identify him.
         * Once it finds him it returns the string with the information about the user

         * @param phoneNum- a phone number
         * @return the information String about the User, "the number doesn't exist" if the number doesn't exist,
         * or "something went wrong" if an error occurred
         *
         */
        public static String  phoneNumSearch(Context c, String phoneNum) {
            try {
                //InputStream is = this.getResources().openRawResource(R.raw.contacts_database);
                FileInputStream is;
                is = c.openFileInput(ContactPath);
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // do reading, usually loop until end of file reading
                String mLine = reader.readLine();

                while (mLine != null) {
                    // process line_divider
                    if (!mLine.equals("") && !mLine.contains("#") && checkLine(mLine, phoneNum)) {
                        return mLine;
                    }
                    mLine = reader.readLine();
                }
                reader.close();
                return "the number doesn't exist";

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "something went wrong";

        }

        /**
         * this method returns a String with the name using a phone number as identifier.
         * @param phNum the phone number
         * @return the groups name
         */
        public static String getName(Context c, String phNum){
            String line=phoneNumSearch(c, phNum);
            String[] data = line.split(",", 4);
            return data[0];//returns the name
        }

        /**
     * get group name using a phone number as identifier
     * @param phNum the phone number
     * @return the groups name
     */
        public  static String getGroup(Context c, String phNum){
            String line=phoneNumSearch(c, phNum);
            String[] data = line.split(",", 4);
            return data[2];
        }


        /**
     * sends sms messages to all people
     */
        public void sendMessage(){
//    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
//    String numbers = "";
//    for (int i = 0; i < phNums.size(); i++) {
//        numbers = numbers + phNums.get(i) + ", ";
//    }
//    sendIntent.setData(Uri.parse("sms:" + numbers));
//    startActivity(sendIntent);


}

        /**
     * creates a nameArray using the phone numbers in numArr
     * @return name array
     */
        public static List createNameArr(Context c, List phNums){
        List nameArr= new ArrayList();
        for (Object num : phNums) {

            nameArr.add(getName(c, (String)num));
        }
        return nameArr;
    }

        /**
     * creates a groupArr using the phone numbers in numArr
     * @return group array
     */
        public static List createArrGroup(Context c, List phNums ){
        List arrGroup= new ArrayList();
        for (Object num : phNums) {
            arrGroup.add(getGroup(c, (String)num));
        }
        return arrGroup;
    }

        /**
     * creates a checkedList with the order of numArr
     * @return name array
     */
        public static ArrayList createCheckedList(List phNums) {
        ArrayList checkedList = new ArrayList();
        for (int i = 0; i < phNums.size(); i++)
            checkedList.add(i);
        return checkedList;
    }

        /**
     * checks whether the user chose to show pictures or not
      * @return boolean for show pictures
     */
        public boolean checkShowPics(){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean showPics = sharedPreferences.getBoolean("show_pics",
                    this.getResources().getBoolean(R.bool.show_pics_default));
        return showPics;
    }

        public void removeDupArrayList(){
        Set<String> hs = new HashSet<>();
        hs.addAll(phNums);
            phNums.clear();
            phNums.addAll(hs);
    }




}


