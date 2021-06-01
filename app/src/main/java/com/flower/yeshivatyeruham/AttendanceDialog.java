package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.flower.yeshivatyeruham.DataClass.ContactPath;
import static com.flower.yeshivatyeruham.DataClass.NOT_UPDATED;
import static com.flower.yeshivatyeruham.DataClass.UPDATED;
import static com.flower.yeshivatyeruham.DataClass.UPDATING;
import static com.flower.yeshivatyeruham.DataClass.netFTPAppDataPath;
import static com.flower.yeshivatyeruham.DataClass.readFileFromFTPServer;
import static com.flower.yeshivatyeruham.DataClass.updateStat;


/**
 * Created by dell on 26 אוקטובר 2017.
 * creates a dialog with the event's name and gives the user an option to respond if he'll attend the event or not
 * the name of the responder is taken from his login(phNum), he can click a Button and enter a name instead of using his own
 * if he logged with user name and password he'll  be asked to enter a name before entering a response
 */

public class AttendanceDialog extends AppCompatActivity {
    AutoCompleteTextView act;
    Button yes, no, changeNameBtt;
    TextView question, shabbatName;
    String name = "";
    private ResponseWriter mResponseWriter;
    List info;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isNetworkAvailable()) {
            Toast.makeText(getBaseContext(), "התחבר לאינטרנט ועדכן קבצים", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!isPropertiesUpdated(getApplicationContext())) {
            finish();
            return;
        }

        info = new ArrayList(MainActivity.getInfo("נוכחות", this));

        SharedPreferences sharedPref = getSharedPreferences("strings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("attLastDate", (String) info.get(1));
        editor.apply();

        if (!isOnDate(getBaseContext(), info)) {
            finish();
            return;
        }

        //sends a response for swipe  response list
        Intent intent = getIntent();
        String listResponse = intent.getStringExtra("listResponse");
        if (listResponse != null) {
            name=intent.getStringExtra("name");
            Toast.makeText(getBaseContext(),  "חכה רגע, התגובה נרשמת...", Toast.LENGTH_SHORT).show();
            Attendance attendance = new Attendance(getBaseContext(), name, listResponse);
            runTask(getBaseContext(), attendance);
            setResult(RESULT_OK, intent);
            finish();
        }

        setContentView(R.layout.dialog_attendance);


        changeNameBtt = (Button) findViewById(R.id.change_name_button);
        act = (AutoCompleteTextView) findViewById(R.id.autocomplete_names);
        yes = (Button) findViewById(R.id.positive_ans);
        no = (Button) findViewById(R.id.negative_ans);
        question = (TextView) findViewById(R.id.attQuestion);
        shabbatName = (TextView) findViewById(R.id.shabbatName);

        question.setText((String) info.get(2));
        shabbatName.setText((String) info.get(3));

        //this opens the dialog with the name send by the intent
        // (used when selecting a name in the responded list)
        if(intent.getStringExtra("name")!=null)
            name=intent.getStringExtra("name");

        //gets user's name if available
        else if (!sharedPref.getString("myName", "").equals(""))
            name = sharedPref.getString("myName", "");

        if (name.isEmpty())
            showAutocomplete();

        changeNameBtt.setText("לא " + name + "?");


        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Toast.makeText(getBaseContext(),  "חכה רגע, התגובה נרשמת...", Toast.LENGTH_SHORT).show();
                getNameInput();
                    //if(mResponseWriter!=null)
                     //   try {
                    //        Thread.sleep(4000);
                    //    } catch (InterruptedException e) {
                    //        e.printStackTrace();
                    //    }

                if (name != null && !name.isEmpty()) {
                    Attendance attendance = new Attendance(getBaseContext(), name, "כן");
                    runTask(getBaseContext(), attendance);
                    if(intent.getIntExtra("attListPos", -1)!=-1){
                        Intent returnIntent= new Intent();
                        returnIntent.putExtra("resultPos", intent.getIntExtra("attListPos", -1));
                        setResult(RESULT_OK, intent);
                    }
                    finish();
                }
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Toast.makeText(getBaseContext(),  "חכה רגע, התגובה נרשמת...", Toast.LENGTH_SHORT).show();
                getNameInput();
                if (name != null && !name.isEmpty()) {
                    Attendance attendance = new Attendance(getBaseContext(), name, "לא");
                    runTask(getBaseContext(), attendance);
                    if(intent.getIntExtra("attListPos", -1)!=-1){
                        Intent returnIntent= new Intent();
                        returnIntent.putExtra("resultPos", intent.getIntExtra("attListPos", -1));
                        setResult(RESULT_OK, intent);
                    }
                    finish();
                }
            }
        });

        changeNameBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAutocomplete();
            }
        });

    }

    /**
     * runs the Asynctask to communicate with the server and send the response
     * @param ctx the context
     * @param attendance Attendace object that has the name and response
     */
    private void runTask(Context ctx, Attendance attendance) {
        if (mResponseWriter != null)
            return;
        mResponseWriter = new ResponseWriter(ctx);
        Object[] params = {attendance};
        mResponseWriter.execute(params);
    }

    /**
     * creates a ArrayList wrapped in a list containing all the names of students
     * @param context
     * @return return a arrayList(wrapped in a List) including all the names
     */
    private static List<String> createFullNameArrayList(Context context) {
        List<String> nameArray = new ArrayList();
        try {
            //InputStream is = this.getResources().openRawResource(R.raw.contacts_database);
            FileInputStream is = context.openFileInput(ContactPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();

            while (mLine != null) {
                // process line_divider
                if (!mLine.equals("") && !mLine.contains("#")) {
                    String[] data = mLine.split(",", 4);
                    nameArray.add(data[0]);

                }
                mLine = reader.readLine();
            }
            reader.close();
            return nameArray;
//                    createListView();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void showAutocomplete() {
        changeNameBtt.setVisibility(View.GONE);
        act.setVisibility(View.VISIBLE);
        //create the autocomplete text for writing names
        String[] namesArr = createFullNameArrayList(getBaseContext()).toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, namesArr);
        act.setAdapter(adapter);
    }

    public List getInfo(String key) {
        try {
            FileInputStream fis = getBaseContext().openFileInput(getString(R.string.properties));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line = br.readLine();
            List<String> info = new ArrayList<>();
            while (line != null) {
                if (line.contains("#"))
                    if (line.contains(key)) {
                        line = br.readLine();
                        while (line != null && !line.isEmpty()) {
                            info.add(line);
                            line = br.readLine();
                        }
                        return info;
                    }
                br.readLine();
            }
            return info;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * checks whether theres an internet connection
     * @return true if there is a connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("AttendanceDialog", "http://[ENTER-YOUR-URL-HERE]");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().start(getIndexApiAction());
    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();
    }

    /**
     * checks if there is a an attendance check for the current date and time
     *
     * @param context
     * @param info List containing the info extracted from the properties file about the attendance
     * @return true or false
     */
    public static boolean isOnDate(Context context, List info){

        DateFormat dateFormat=checkDateFormat((String)info.get(0));////

        try {
            if (new Date().before(dateFormat.parse((String) info.get(0))) ||
                    new Date().after(dateFormat.parse((String) info.get(1)))) {
                Toast.makeText(context, "אין טופס למילוי", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(context, "אופס!! קרתה שגיאה", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    public static DateFormat checkDateFormat(String date){
        DateFormat dateFormat=new SimpleDateFormat();
        if(date.indexOf("/")==2)
            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//done changed from dd//MM//yyyy
        else if(date.indexOf(".")==2)
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        else if(date.indexOf("-")==2)
            dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return dateFormat;
    }

    /**
     * checks if the properties.txt is updated if not updates it
     * @param context
     * @return
     */
    public static boolean isPropertiesUpdated(Context context){
        if (DataClass.updateStat == NOT_UPDATED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new updateProperties().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context, context.getString(R.string.properties));
            else
                new updateProperties().execute(context,context.getString(R.string.properties));


        }
        int count=0;
        while (updateStat != UPDATED  && count!=200 ) {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        if (updateStat == UPDATING || updateStat==NOT_UPDATED) {
            Toast.makeText(context, "אופס, משהו השתבש!!", Toast.LENGTH_SHORT).show();
            return false;
        }
    return true;
    }

    /**
     * gets the name input fro the EditText.
     * checks whether the input is empty or has invalid characters
     */
    private void getNameInput(){

        if (act.getVisibility() != View.GONE) {
            if (!act.getText().toString().isEmpty()){
                if(act.getText().toString().contains("#") || act.getText().toString().contains(","))
                    Toast.makeText(getBaseContext(), "השתמש רק בתווים תקניים", Toast.LENGTH_SHORT).show();
                else
                    name = act.getText().toString();}
            else {
                Toast.makeText(getBaseContext(), "נא הכנס נתונים", Toast.LENGTH_SHORT).show();
                name = null;
            }
        }
    }
}

/**
 * updates the _properties.txt file
 */
       class updateProperties extends AsyncTask {

        protected Void doInBackground(Object[] params) {
            updateStat = UPDATING;
            if (readFileFromFTPServer((String)params[1], netFTPAppDataPath + params[1],
                    true, (Context)params[0])) {
                updateStat = UPDATED;
            }
            else {
                updateStat = NOT_UPDATED;
                //return new Object[]{params[0], false};
            }
            return null;
        }




}

