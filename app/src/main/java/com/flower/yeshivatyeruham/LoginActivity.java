package com.flower.yeshivatyeruham;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
//import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
//import android.app.LoaderManager.LoaderCallbacks;
//import android.content.CursorLoader;
//import android.content.Loader;
//import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcifs.smb.SmbException;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

//import static android.Manifest.permission.READ_PHONE_STATE;
        import static com.flower.yeshivatyeruham.DataClass.ServerPass;
        import static com.flower.yeshivatyeruham.DataClass.cxt;
        import static com.flower.yeshivatyeruham.DataClass.netContactsPath;
import static com.flower.yeshivatyeruham.DataClass.sksRoot;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    public int REQUEST_READ_SMS = 1;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private DatabaseReference mDatabase;
    private List<AppUser> appUsers = new ArrayList<AppUser>();
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private static final int RC_SIGN_IN = 123;
//    private View mLoginFormView;
    static String phNo = "";
    String name = "";
    String group = "";
    String numArr = "";
    String className = "";
    static Boolean wasMyOwnNumber;
    static Boolean workDone;
    final static int SMS_ROUNDTRIP_TIMOUT = 30000;
    final static  int USER_VER = 1;
    Button submit;
    Button guest, secret;
    Button toPassVer, toNumVer;
    static int cnt =0;
    EditText contact;
    static ProgressDialog progress;
    ArrayList groups = new ArrayList();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(relogin()){
        if (isAlreadyConnected()) {
            Intent i = new Intent(getBaseContext(), MainActivity.class);
            startActivity(i);
            finish();
        }}

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        submit = (Button) findViewById(R.id.submit);
        wasMyOwnNumber = false;
        workDone = false;
        submit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                createSignInIntent();


            }
        });
        secret= (Button) findViewById(R.id.button2);
        secret.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cnt==20) {
                    cnt=0;
                    findViewById(R.id.numVerify).setVisibility(View.GONE);
                    findViewById(R.id.passVerify).setVisibility(View.VISIBLE);
                    mEmailView.requestFocus();
                }
                else
                    cnt++;
            }
        });
        guest = (Button) findViewById(R.id.guest);
        guest.setOnClickListener(new OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         Intent i = new Intent(getBaseContext(), GuestActivity.class);
                                         startActivity(i);
                                     }
                                 });

        Button manager= (Button) findViewById(R.id.manager);
        manager.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataClass.isOnline(getBaseContext())) {
                    Intent sksIntent = new Intent(getBaseContext(), ManagerActivity.class);
                    sksIntent.putExtra("rootPath", sksRoot);
                    startActivity(sksIntent);
                }

                }
        });
        Button act = (Button) findViewById(R.id.active);
        act.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getBaseContext(), ActiveActivity.class);
                startActivity(i);
            }
        });





        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();


        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    if (isOnline()) {
                        attemptLogin();
                        return true;
                    }
                }
                return false;
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOnline()) {
                    attemptLogin();
                }
            }
        });

//        mLoginFormView = findViewById(R.id.login_form);
//        mProgressView = findViewById(R.id.login_progress);
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            phNo = response.getPhoneNumber();
            phNo = convertBroadToIsrael(phNo);
            if (resultCode == RESULT_OK) {

                if (isOnline()) {
                    verifyNum();

                }
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private void populateAutoComplete() {

        List<String> emails = new ArrayList<>();

        SharedPreferences sharedPref = getSharedPreferences("strings", MODE_PRIVATE);
        String[] users = sharedPref.getString("users", "").split(",");
        for (String email : users) {
            emails.add(email);
        }
        addUsersToAutoComplete(emails);

//        if (VERSION.SDK_INT >= 14) {
//            // Use ContactsContract.Profile (API 14+)
//            getSupportLoaderManager().initLoader(0, null, this);
//
//
//        } else if (VERSION.SDK_INT >= 8) {
//            // Use AccountManager (API 8+)
//            new SetupEmailAutoCompleteTask().execute(null, null);
//        }
    }

    private void verifyNum(){
        new CheckOwnMobileNumber().execute();
//        new CheckOwnMobileNumber().execute();
    }

//    private void saveNotification(Intent notification) {
//יייייייייייייייייי
//        String text = notification.getStringExtra("data");
//        String link = notification.getStringExtra("link");
//        String title = notification.getStringExtra("title");
//        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.savedNtf), MODE_PRIVATE);
//
//        String titles = sharedPref.getString(getString(R.string.savedNtf_titles), "");
//        String texts = sharedPref.getString(getString(R.string.savedNtf_texts), "");
//        String links = sharedPref.getString(getString(R.string.savedNtf_links), "");
//
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString(getString(R.string.savedNtf_titles), title + "<^>" + titles);
//        editor.putString(getString(R.string.savedNtf_texts), text + "<^>" + texts);
//        editor.putString(getString(R.string.savedNtf_links), link + "<^>" + links);
//
//        editor.apply();
//    }
    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
//            }
//        }
        if (requestCode == REQUEST_READ_SMS) {
            if (grantResults.length > 0) {
                boolean ReceivePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean ReadPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean SendPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                if (ReceivePermission && ReadPermission && SendPermission) {
                    new CheckOwnMobileNumber().execute();
                } else {
                    Toast.makeText(this, "לא ניתן להתחבר ללא אישור ההרשאות", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if ((!TextUtils.isEmpty(password) && !isPasswordValid(password))||TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isUserValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don'createTrd attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
//            try {
//                Integer.parseInt(mEmailView.getText().toString());
//                if (mEmailView.getText().length() == 10) {
//                    checkNum();
//                }
//            }
//            catch (Exception e){}
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserValid(String email) {
        return true;
        //return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
        if (show) {
            progress = ProgressDialog.show(LoginActivity.this, "", "מאמת ומתחבר\nאנא המתן...");
        } else {
            progress.dismiss();
        }

    }

    private void addUsersToAutoComplete (List<String> collection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, collection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("Login", "http://[ENTER-YOUR-URL-HERE]");
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
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            //finishLogin(true, "yhy24", "yhy12345", 2, 124);

            mDatabase.child("allData").child("users").addListenerForSingleValueEvent(//todo check why gets stuck
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                //<Event> ev = dataSnapshot.getValue();
                                GenericTypeIndicator<List<AppUser>> t = new GenericTypeIndicator<List<AppUser>>() {
                                };
                                appUsers = dataSnapshot.getValue(t);
                            }

                            Boolean succes = false;

                            for (AppUser user : appUsers) {
                                String userName = user.getName();
                                if (userName.equals(mEmail)) {
                                    // Account exists, return true if the password matches.
                                    if (user.getPass().equals(mPassword)) {
                                        succes = true;
                                        //todo
                                        finishLogin(true, user.getName(), user.getPass(), user.getClassification(), user.getClassName());
                                        saveEmail(mEmail);
                                        break;
                                    }
                                }
                            }

                            if (!succes)
                                finishLogin(false, "", "", -1, -1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            // ...
                        }
                    });

            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private void saveEmail(String email) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("strings", MODE_PRIVATE);

            String users = sharedPref.getString("users", "");

            if (!users.contains(email)) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("users", sharedPref.getString("users", "") + "," + email);
                editor.apply();
            }
        }

    }

    public void finishLogin(final Boolean success, String name, String pass, int classification, int className) {
        mAuthTask = null;

        if (success) {
            String user = name + ":" + pass + ":" + classification + ":" + className;
            logIn(user, this);

            Intent i = new Intent(getBaseContext(), MainActivity.class);
            startActivity(i);
            showProgress(false);
            finish();
        } else {
            showProgress(false);
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }
    }

    public static void logIn(String user, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("strings", MODE_PRIVATE);

        String oldUser = sharedPref.getString("user", "null");
        if (!oldUser.equals("null"))
            FirebaseMessaging.getInstance().unsubscribeFromTopic(getNumberAsString(sharedPref.getInt("myGroupNum", -1)));

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("loggedIn", true);
        editor.putBoolean("relogin", true);
        editor.putString("user", user);
        editor.putString("myName", (user.split(":")[0]));
        editor.putString("myPhoneNum", phNo);
        editor.putInt("userVer", USER_VER);
        editor.putInt("myClassificationNum", Integer.parseInt(user.split(":")[2]));
        editor.putString("myGroupName", (user.split(":")[3]));
        editor.putInt("myGroupNum", Integer.parseInt(user.split(":")[3]));
        editor.apply();

        FirebaseMessaging.getInstance().subscribeToTopic(getNumberAsString(sharedPref.getInt("myGroupNum", -1)));
    }

    public static String getNumberAsString(int num) {
        int i = num;

            switch (i) {
                case 1:
                    return "admin";
                case 2:
                    return "staff";
            }

        return i+"";
    }

    public String convertBroadToIsrael(String phNum){
        phNum = phNum.substring(4);
        String fullNum = "0" + phNum;
        return fullNum;
    }

    public Boolean isAlreadyConnected() {
        SharedPreferences sharedPref = getSharedPreferences("strings", MODE_PRIVATE);
        if (sharedPref.getString("user", "null").equals("null"))
            return false;

        return sharedPref.getBoolean("loggedIn", false);
    }

    private class CheckOwnMobileNumber extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
//            if(progress.isShowing())
//            {
//                checkNum();
            //if (wasMyOwnNumber) {
                if (workDone) {
                    Toast.makeText(getApplicationContext(), name + " המספר זוהה בהצלחה", Toast.LENGTH_LONG).show();
                    String userName;
                    int className, classification = 2;
                    if (!group.equals("צוות")) {
                        userName = "yhy" + (groups.indexOf(group) + 1);
                        className = (groups.indexOf(group) + 1);
                        classification = 1;
                    } else {
                        userName = "yhy0";
                        className = 0;
                        classification = 0;
                    }
                    //mAuthTask = new UserLoginTask(userName, "yhy12345");
                    //mAuthTask.execute((Void) null);

                    finishLogin(workDone, userName, "yhy12345", classification, className);
                    //logIn(group, name, phNo);
                    wasMyOwnNumber = false;
                    workDone = false;

                } else {
                    Toast.makeText(getApplicationContext(), "המספר זוהה אך לא נמצא במאגר הנתונים או שאין לך חיבור אינטרנט, פנה לא. אפליקציה", Toast.LENGTH_LONG).show();
                    finishLogin(workDone, "", "", -1, -1);
//                    contact.setError("המספר לא נמצא במאגר הנתונים");
                    wasMyOwnNumber = false;
                    workDone = false;
//                        progress.dismiss();
                    showProgress(false);
                }
/*
            } else {
                Toast.makeText(getApplicationContext(), "לא ניתן לאמת מספר זה", Toast.LENGTH_LONG).show();
                contact.setError("בטוח שזה המספר שלך?");
                wasMyOwnNumber = false;
                workDone = false;
                finishLogin(false, "", "", -1, -1);
                showProgress(false);
                return;
            }
            */
//            }
            super.onPostExecute(result);
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                workDone = checkNum();
                //timeout();
            } catch (Exception ex) {
                Log.v("Exception :", "" + ex);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            showProgress(true);
//            progress = ProgressDialog.show(this, "","מאמת מספר פלאפון...");
//            progress.setIndeterminate(true);
//            progress.getWindow().setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
            super.onPreExecute();
        }

        private boolean timeout() {
            int waited = 0;
            while (waited < SMS_ROUNDTRIP_TIMOUT ) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                waited += 100;

                if (workDone) {
                    waited = SMS_ROUNDTRIP_TIMOUT;
                }
            }
            return workDone;
        }

        private boolean phoneNumberConfirmationReceived() {
            if (wasMyOwnNumber && checkNum()) {
                workDone = true;
            }
            return workDone;
        }

        private Boolean checkNum() {

            String path = netContactsPath;
            FTPClient mFTPClient;
            mFTPClient = new FTPClient();
            Boolean found = false;
            groups.clear();

            path = path.replace("smb://192.168.1.25/mp3admin/", "");

            try {
                mFTPClient.setAutodetectUTF8(true);

                mFTPClient.connect("yhy.co.il");
                mFTPClient.enterLocalPassiveMode();
                mFTPClient.login("mp3site@yhy.co.il", ServerPass);

                InputStream in = mFTPClient.retrieveFileStream(path);

                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                // do reading, usually loop until end of file reading
                String mLine = reader.readLine();

                while (mLine != null) {
                    // process line_divider
                    if (!mLine.equals("") && !mLine.contains("#") && checkLine(mLine, phNo)) {
                        processLine(mLine);
                        found = true;
                    }
                    mLine = reader.readLine();
                }
                in.close();
                reader.close();
                mFTPClient.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return false;
            } catch (SmbException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return found;
        }

        private boolean checkLine(String line, String num) {
            String[] SplitLine = line.split(",", 4);
            String group = SplitLine[2];

            if (!group.equals("?") && !group.equals("צוות") && !groups.contains(group)) {
                groups.add(group);
            }

            return SplitLine[1].equals(num);
        }

        private void processLine(String Line) {
            String[] data = Line.split(",", 4);
            name = data[0];
            numArr = data[1];
            group = data[2];
        }
    }

    public boolean isOnline() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
            return true;
        else {
            Toast.makeText(cxt, "אין חיבור אינטרנט", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean relogin(){
        SharedPreferences sharedPref = getSharedPreferences("strings", MODE_PRIVATE);
        if(!sharedPref.getBoolean("relogin", false)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("loggedIn", false);
            editor.apply();

        }
        return sharedPref.getBoolean("relogin", false);


    }

}