package com.flower.yeshivatyeruham;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.flower.yeshivatyeruham.DataClass.ContactPath;

public class ContactsActivity extends AppCompatActivity {

    protected static String GROUP_NUM = "";
    private List nameArr = new ArrayList();
    private List groupArr = new ArrayList();
    private List numArr = new ArrayList();
    final int RequestPermissionCode = 1;
    SearchView searchView;
    private ArrayList checkedList;
    boolean multiChoose;
    RecyclerView list;
    FloatingActionButton multiSmsBtn;
    static RecyclerView.Adapter adapter;
    Boolean showPics;
    Thread createTrd;
    Task searchTask;
    FileInputStream is;
    static BufferedReader reader;


    MenuItem selectedItem;
    static List phNums= new ArrayList();
    List selectedView=new ArrayList();
    AlertDialog.Builder dialog;

    //    MenuItem multiSms;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dialog =  new AlertDialog.Builder(this);
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        super.onCreate(savedInstanceState);
//        searchTask.execute("");
        setContentView(R.layout.activity_contacts);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        list = (RecyclerView) findViewById(R.id.data_list);
        checkedList = new ArrayList();

        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showPics = sharedPreferences.getBoolean("show_pics",
                this.getResources().getBoolean(R.bool.show_pics_default));



        multiSmsBtn = (FloatingActionButton) findViewById(R.id.multi_sms_button);
        multiSmsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                String numbers = "";
                for (int i = 0; i < checkedList.size(); i++) {
                    numbers = numbers + numArr.get((int) checkedList.get(i)) + ", ";
                }
                sendIntent.setData(Uri.parse("sms:" + numbers));
                startActivity(sendIntent);
            }
        });
        handleIntent(getIntent()); // should the search check the file or the viewable rows?
//        searchResults("בדיקה");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        // Inflate the menu; this adds items to the action bar if it is present.
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contacts, menu);
//checks if there is a multi choose, if yes, sets the selected icon visible, if not set it invisibile



//        if (Build.VERSION.SDK_INT >= 11) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            searchView = (SearchView) menu.findItem(R.id.search).getActionView();
//        }
//        else {
        selectedItem=menu.findItem(R.id.selected);
        if(!phNums.isEmpty()) selectedItem.setVisible(true);

        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
//        }
        searchView.setMaxWidth(Integer.MAX_VALUE);
        // Assumes current activity is the searchable activity
        searchView.setFocusable(true);
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        if (getIntent().getIntExtra(GROUP_NUM, -1) == -1) {
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
        }

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
//                    onSearchRequested();
                if (searchView.getQuery().length() > 1) {
//                        findViewById(R.id.ContactsTable1).setVisibility(View.VISIBLE);
//                        findViewById(R.id.data_list).setVisibility(View.VISIBLE);
//                        findViewById(R.id.empty).setVisibility(View.GONE);
//                        clearAsyncTask(searchTask);
//                        searchTask = new Task();
//                        searchTask.execute(searchView.getQuery().toString());
//                        searchResults(searchView.getQuery().toString());
                    searchView.clearFocus();
                } else {
                    Toast.makeText(getApplicationContext(), "הכנס חיפוש ממוקד יותר", Toast.LENGTH_LONG).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // do your search on change or save the last string in search
                String query = searchView.getQuery().toString();
                if ((!TextUtils.isDigitsOnly(query) && query.length() > 1) || query.length() > 3) {
//                    if (query.length() > 1) {
                    findViewById(R.id.ContactsTable1).setVisibility(View.VISIBLE);
                    findViewById(R.id.data_list).setVisibility(View.VISIBLE);
                    findViewById(R.id.empty).setVisibility(View.GONE);
                    clearAsyncTask(searchTask);
                    searchTask = new Task();
//                        searchTask.execute(searchView.getQuery().toString());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchView.getQuery().toString());
                    else
                        searchTask.execute(searchView.getQuery().toString());
//                        searchResults(query);
                } else {
                    findViewById(R.id.ContactsTable1).setVisibility(View.GONE);
                    findViewById(R.id.data_list).setVisibility(View.GONE);
                    findViewById(R.id.empty).setVisibility(View.GONE);
                }
                return true;
            }
        });

        // you can get query
//            searchView.getQuery();

        //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
//        }
//        if (multiChoose)
//            menu.findItem(R.id.multi_sms).setVisible(true);
//        else menu.findItem(R.id.multi_sms).setVisible(false);
        return true;
    } //create the search button

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

        //DONE send intent to ContactSelectedActivity
        if(item.getItemId()== R.id.selected){
            Intent intent = new Intent(this, ContactSelectedActivity.class);
            intent.putCharSequenceArrayListExtra("phNumsArray", (ArrayList)phNums);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }
//    @Override public void startActivity(Intent intent) {
//        super.startActivity(intent);
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
//            finish();
//        }
//    }

    public void handleIntent(Intent intent) {
//        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
////            TableSource.setOpened(false);
//            findViewById(R.id.ContactsTable1).setVisibility(View.VISIBLE);
//            // Add Rows that match search
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            searchResults(query);
//        }
//        else
        int num = intent.getIntExtra(GROUP_NUM, -1);
        if (num != -1) {
//            int num = intent.getIntExtra(GROUP_NUM, -1);
            findViewById(R.id.ContactsTable1).setVisibility(View.VISIBLE);
//            findViewById(R.id.data_list).setVisibility(View.VISIBLE);
            showTribe(num);
        }
    } // should the search check the file or the viewable rows?

    public void searchResults(final String query) {
        nameArr.clear();
        numArr.clear();
        groupArr.clear();
        checkedList.clear();
//        createTrd = new Thread(){

//            public void run(){
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
//        if (query.length() > 1) {

        try {
            //InputStream is = this.getResources().openRawResource(R.raw.contacts_database);
            is = openFileInput(ContactPath);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();

            while (mLine != null) {
                // process line_divider
                if (!mLine.equals("") && !mLine.contains("#") && checkLine(mLine, query))
                    processLine(mLine);
                mLine = reader.readLine();
            }
            reader.close();
            sortRows();
//                    createListView();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//            }
//        };
//        createTrd.interrupt();

//        createTrd.run();

//        } else {
//        }
    }

    private void createListView() {
        if (nameArr.isEmpty()) {
            (findViewById(R.id.empty)).setVisibility(View.VISIBLE);
            (findViewById(R.id.ContactsTable1)).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.empty).setVisibility(View.GONE);
        adapter = new CustomContactsList(this, nameArr, numArr, groupArr, checkedList, showPics, new CustomContactsList.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!multiChoose) {
                    showDetails(position);
                } else checkItem(view, position);
            }
        }, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                checkItem(v, list.getChildAdapterPosition(v));
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
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir ) {
                final int pos = list.getChildAdapterPosition(viewHolder.itemView);
                if (swipeDir == ItemTouchHelper.RIGHT) {
                    onCall((String) numArr.get(pos));
                    adapter.notifyItemChanged(pos);
                } else if (swipeDir == ItemTouchHelper.LEFT) {
                    adapter.notifyItemChanged(pos);
                    dialog.setTitle("שליחת הודעה");
                    dialog.setMessage("האם תרצה לשלוח הודעת ס.מ.ס או וואטסאפ?");
                    dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent swipeLeftIntent = new Intent();
                            swipeLeftIntent.setAction(Intent.ACTION_VIEW);
                            swipeLeftIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                            swipeLeftIntent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + convertNum((String) numArr.get(pos))));

                            PackageManager packageManager = getPackageManager();
                            List<ResolveInfo> activities = packageManager.queryIntentActivities(swipeLeftIntent, 0);
                            boolean isIntentSafe = activities.size() > 0;

                            if(isIntentSafe){
                                startActivity(swipeLeftIntent);
                            }
                        }
                    });


                    dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                            sendIntent.setData(Uri.parse("sms:" + numArr.get(pos)));
                            startActivity(sendIntent);
                        }
                    });
                    dialog.show();

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

        multiChoose = false;
//        multiSmsBtn.setVisibility(View.INVISIBLE);
//        multiSmsBtn.hide();
//        invalidateOptionsMenu();

//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
//                if (!multiChoose){
//                    showDetails(pos);
//                }
//                else checkItem(view, pos);
//            }
//        });
//        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
//                checkItem(view, pos);
//                return true;
//            }
//        });
    }

    public static String convertNum(String s){
        s = s.substring(1);
        String fullNum = "+972";
        fullNum = fullNum.concat(s);
        return fullNum;
    }

    private void checkItem(View view, int pos) {
//        String num = (String) numArr.get(pos);
        int ind = checkedList.indexOf(pos);
        if (ind < 0) {
            view.setBackgroundResource(R.color.listview_pressed_color);
            checkedList.add(pos);
            phNums.add(numArr.get(pos));//creates a list with all the selected phone numbers. relies on checkList's if and else
            selectedView.add(view);//gets the view id's

            view.setSelected(true);
            if (!multiChoose) {
                multiChoose = true;
                //invalidateOptionsMenu();
//                invalidateOptionsMenu();
//                multiSmsBtn.setVisibility(View.VISIBLE);
                multiSmsBtn.show();
                selectedItem.setVisible(true);
            }
        } else {
            checkedList.remove(ind);
            selectedView.remove(phNums.indexOf(numArr.get(pos)));
            phNums.remove(numArr.get(pos));//removes the number that was disselected, relies on checkedList's if and else

            view.setBackgroundResource(R.drawable.btn_color);
            if (checkedList.isEmpty()) {
                multiChoose = false;
                multiSmsBtn.hide();
                if(phNums.isEmpty()) selectedItem.setVisible(false);

//                multiSmsBtn.setVisibility(View.INVISIBLE);
//                invalidateOptionsMenu();
            }
        }
//        list.setSelection(pos);
//        int i = checkedList.indexOf(numArr.get(pos));
//        if (i < 0) {
//            checkedList.add(numArr.get(pos));
//            if (!multiChoose){
//                multiChoose = true;
//                // TODO change menu btns
//            }
//        }
//        else {
//            checkedList.remove(i);
//            if (checkedList.isEmpty()) {
//                multiChoose = false;
//                // TODO change menu btns
//            }
//        }
//            SetItemChecked(initialSelection, true);
//        if (view.isSelected()) {
//            view.setSelected(false);
//            checkedList.remove(checkedList.indexOf(numArr.get(pos)));
//            if (checkedList.isEmpty()) {
//                multiChoose = false;
//                // TODO change menu btns
//            }
//        }
//        else{
//            list.setSelection(pos);
////            view.setSelected(true);
//            checkedList.add(numArr.get(pos));
//            if (!multiChoose){
//                multiChoose = true;
//                // TODO change menu btns
//            }
//        }
        Log.v("long clicked", "pos: " + pos + "in array: " + checkedList);
    }

    public void sortRows() {
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        int rows = nameArr.size();
        // Bubble sort table rows
        for (int i = 0; i < rows; i++) {
            for (int j = 1; j < (rows - i); j++) {
                String s1 = nameArr.get(j - 1).toString();
                String s2 = nameArr.get(j).toString();
                if (s2.compareTo(s1) < 0) {
                    //swap the elements!
                    swapRows(i, j);
                }
            }
        }
    }

    public void swapRows(int i, int j) {
        Object tmp1 = nameArr.get(j - 1);
        Object tmp2 = nameArr.get(j);
        nameArr.set(j - 1, tmp2);
        nameArr.set(j, tmp1);

        tmp1 = numArr.get(j - 1);
        tmp2 = numArr.get(j);
        numArr.set(j - 1, tmp2);
        numArr.set(j, tmp1);

        tmp1 = groupArr.get(j - 1);
        tmp2 = groupArr.get(j);
        groupArr.set(j - 1, tmp2);
        groupArr.set(j, tmp1);
    }

    public void processLine(String Line) {
        String[] data = Line.split(",", 4);
        nameArr.add(data[0]);
        numArr.add(data[1]);
        groupArr.add(data[2]);
    }

    public void showDetails(int pos) {
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("name", nameArr.get(pos).toString());
        intent.putExtra("number", numArr.get(pos).toString());
        intent.putExtra("group", groupArr.get(pos).toString());
        startActivity(intent);
    }

    public void showTribe(int pos) {
        Log.v("CLog", Thread.currentThread().getStackTrace()[2].getMethodName());
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.allGroups), MODE_PRIVATE);

        String groups = sharedPref.getString(getString(R.string.allGroups), "err");
        //String[] s = getResources().getStringArray(R.array.tribe_array);
        String tribe = groups.split("#")[pos].replace("מחזור ", "");
        //String tribe = getResources().getStringArray(R.array.tribe_array)[pos];
        BufferedReader reader = null;
        try {
            FileInputStream is = openFileInput(ContactPath);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();
            Log.v("Contacts", tribe);
            while (mLine != null) {
                if (!mLine.equals("") && !mLine.contains("#") && mLine.split(",", 4)[2].contentEquals(tribe))
                    processLine(mLine);
                mLine = reader.readLine();
            }
        } catch (Exception e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    createListView();

                } catch (Exception e) {
                }
            }
        }
        sortRows();
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

    public void sms(final String num) {
        dialog.setTitle("שליחת הודעה");
        dialog.setMessage("האם תרצה לשלוח הודעת ס.מ.ס או וואטסאפ?");
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent swipeLeftIntent = new Intent();
                swipeLeftIntent.setAction(Intent.ACTION_VIEW);
                swipeLeftIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                swipeLeftIntent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + convertNum((String) num)));

                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(swipeLeftIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                if(isIntentSafe){
                    startActivity(swipeLeftIntent);
                }
            }
        });


        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + num));
                startActivity(sendIntent);
            }
        });
        dialog.show();

    }

    public static void clearAsyncTask(AsyncTask asyncTask) {
        if (asyncTask != null) {
            if (!asyncTask.isCancelled()) {
                asyncTask.cancel(true);
            }
            asyncTask = null;
        }
    }

    private class Task extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
//            if (params[0].equals(""))return false;
            searchResults(params[0]);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
//            if (result){createListView();}
            createListView();
        }

    }
        public void unselectPhNum() {

            for(int i=0; i<checkedList.size(); i++){
                String phNum=(String) numArr.get((int)checkedList.get(i));

                if(!phNums.contains(numArr.get((int)checkedList.get(i)))){
                    View view = (View)selectedView.get(i);
                    view.setBackgroundResource(R.drawable.btn_color);
                    phNums.remove(phNum);
                    selectedView.remove(i);
                    checkedList.remove(i);
                    i--;


                }

            }
        if(phNums.isEmpty() && selectedItem!=null) {
            selectedItem.setVisible(false);
        }
        if (checkedList.isEmpty()){
            multiSmsBtn.hide();
            multiChoose=false;
        }


        }


    @Override
    public void onStart(){
        unselectPhNum();
        super.onStart();


    }

}
