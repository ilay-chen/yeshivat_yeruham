package com.flower.yeshivatyeruham;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.flower.yeshivatyeruham.DataClass.AppSavePath;
import static com.flower.yeshivatyeruham.DataClass.ContactPath;
import static com.flower.yeshivatyeruham.DataClass.DataPath;
import static com.flower.yeshivatyeruham.DataClass.LibraryPath;
import static com.flower.yeshivatyeruham.DataClass.PicSavePath;
import static com.flower.yeshivatyeruham.DataClass.ServerPass;
import static com.flower.yeshivatyeruham.DataClass.WIFINAME;
import static com.flower.yeshivatyeruham.DataClass.groups;
import static com.flower.yeshivatyeruham.DataClass.isUpdating;
import static com.flower.yeshivatyeruham.DataClass.makeDir;
import static com.flower.yeshivatyeruham.DataClass.netContactsPath;
import static com.flower.yeshivatyeruham.DataClass.netDataPath;
import static com.flower.yeshivatyeruham.DataClass.netFTPAppDataPath;
import static com.flower.yeshivatyeruham.DataClass.netLibraryPath;
import static com.flower.yeshivatyeruham.DataClass.netPicsPath;
import static com.flower.yeshivatyeruham.DataClass.scanFile;
import static com.flower.yeshivatyeruham.MainActivity.OVERLAY_PERMISSION_REQ_CODE;
import static com.flower.yeshivatyeruham.MainActivity.requestPermission;
import static com.flower.yeshivatyeruham.RecordingActivity.RequestPermissionCode;

/**
 * popup activity show in the application first run.
 * Responsible of download tagXml and contacts file. if the is local network, else on the internet.
 * if there is error with the download the activity say so.
 */
public class StartupActivity extends AppCompatActivity {

    Context cxt;
    ProgressDialog pd;
    RelativeLayout startUpFailed, startUpSuccess;
    Button retry, ok;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_startup);

        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        cxt = this;
        startUpFailed = (RelativeLayout)findViewById(R.id.failed);
        startUpSuccess = (RelativeLayout)findViewById(R.id.success);
        retry = (Button) findViewById(R.id.retry);
        ok = (Button) findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryGetFiles();
                startUpFailed.setVisibility(View.GONE);
            }
        });

        tryGetFiles();
    }

    public void tryGetFiles()
    {
        if(!checkPermission())
            startRequestPermission();
        else {
            makeDir();
//            if(!getContactsFile())
            new readFromServer().execute("");
        }
    }

    private void saveToInternalStorage(SmbFile sFile){

        String fileName = sFile.getName().substring(0,10)+".jpeg";
//        String fileName = sFile.getName();
        File myFile = new File(PicSavePath + fileName);

        byte myByte[] = new byte[0];

        SmbFileInputStream fis= null;
        FileOutputStream out = null;

        try {
            fis = new SmbFileInputStream(sFile);
            myByte = new byte[(int)sFile.length()];
            //out = new FileOutputStream(myFile);
            out = new FileOutputStream(myFile);
            //out = this.openFileOutput(fileName, Context.MODE_PRIVATE);
            fis.read(myByte,0,myByte.length);
            out.write(myByte);
            out.flush();
            out.close();
            fis.close();
            scanFile(myFile);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPics(){
        String path = PicSavePath;
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.savedPics), MODE_PRIVATE);
        String allPics = sharedPref.getString(getString(R.string.savedPics), "");
        try {
            ArrayList<String> pics = new ArrayList<>();
            File deviceDir = new File(path);
            File[] deviceFiles = deviceDir.listFiles();
            if (deviceFiles == null)
                deviceFiles = new File[0];
            for (int i = 0; i < deviceFiles.length; i++) {
                if (deviceFiles[i].getName().contains(".jpeg"))
                    pics.add(deviceFiles[i].getName().replace(".jpeg",""));
            }

            String user = getString(R.string.user) + ":" + getString(R.string.pass);
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);
            SmbFile netDir = new SmbFile(netPicsPath, auth);
            netDir.setConnectTimeout(10000);
            SmbFile[] netFiles = netDir.listFiles();
            for (int i = 0; i < netFiles.length; i++) {

                String num = netFiles[i].getName().split("-")[0].trim();
                String ver = netFiles[i].getName().split("-")[1].trim();

                if (allPics.contains(num)) { //exist
                    String contact = allPics.substring(allPics.indexOf(num)).split(",")[0];
                    String tmp = contact.split("-")[1];
                    if (tmp.equals(ver)) //do noting
                    {
                        pics.remove(num);
                    } else { //update
                        String newContact = contact.replace(tmp, ver);
                        allPics = allPics.replace(contact, newContact);
//                        File f = new File(path + num + ".jpeg");
//                        f.delete();
//                        scanFile(f);
                        saveToInternalStorage(netFiles[i]);
                        pics.remove(num);
                    }
                } else { //not exist
                    allPics = allPics + num + "-" + ver + ",";

                    saveToInternalStorage(netFiles[i]);
                    pics.remove(num);
                }
            }
            for(int i = 0; i < pics.size(); i++){
                File delFile = new File(path + pics.get(i) + ".jpeg");
                delFile.delete();
                String contact = allPics.substring(allPics.indexOf(pics.get(i))).split(",")[0] + ",";
                allPics = allPics.replace(contact,"");
                scanFile(delFile);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
//                Boolean fileExist = false;
//                for (int j = 0; j < deviceFiles.length; j++){
////                    String num = netFiles[i].getName().split("-")[0].trim();
////                    if(deviceFiles[j].getName().contains(".jpeg") && deviceFiles[j].getName().contains(num)) { //if is exist
////                        if (netFiles[i].getName().equals(deviceFiles[j].getName())) { // if its fine
////                            fileExist = true;
////                            //                        long netLast = netFiles[i].lastModified() - System.currentTimeMillis();
////                            //                        long devLast = deviceFiles[j].lastModified()- System.currentTimeMillis() - (1000*60*60*2);
////                            //                        if (netLast < devLast){
////                            //                            // if need update
////                            //                            long last = netFiles[i].lastModified();
////                            //                            saveToInternalStorage(netFiles[i]);
////                            //                            //deviceFiles[j].setLastModified(last);
////                            //                            // replace pic netFiles[i]
////                            //                        }
////                        }else { //if its had to update
////                            deviceFiles[j].delete();
////                            scanFile(deviceFiles[j]);
////                            saveToInternalStorage(netFiles[i]);
////                        }
////                        pics.remove(deviceFiles[j]);
////                        break;
////                    }
//                    if (deviceFiles[j].getName().contains(num)){
//                        String allPics = sharedPref.getString(getString(R.string.savedPics),"");
//                        if (allPics.contains(num)){
//
//                        }
//                        if (netFiles[i].getName().equals(deviceFiles[j])){
//                            fileExist = true;
//                        }
//                    }else {
//                        String allPics = sharedPref.getString(getString(R.string.savedPics),"");
//                        allPics = allPics + num + "-" + ver + "," ;
//
//                        SharedPreferences.Editor editor = sharedPref.edit();
//                        editor.putString(getString(R.string.savedPics), allPics);
//                        editor.commit();
//
//                    }
//                }
//                if(!fileExist) //if isnt exist
//                {
//                    saveToInternalStorage(netFiles[i]);
//                    //addFile
//                }
//            }
//            //delete unused pics
//            for (int i = 0; i< pics.size(); i++)  {
//                File from  = pics.get(i);
//                from.delete();
//                scanFile(from);
//            }
//
//            SharedPreferences sharedPref = getSharedPreferences(
//                    getString(R.string.savedPics),
//                    MODE_PRIVATE);
//            String allPics = sharedPref.getString(getString(R.string.savedPics),"");
//            allPics = allPics + num + "-" + ver + "," ;
//
//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putString(getString(R.string.savedPics), allPics);
//            editor.commit();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.savedPics), allPics);
        editor.apply();

    }

    public boolean getContactsFile() {
        String path = AppSavePath;

//        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files==null)
            files = new File[0];

        for (int i = 0; i < files.length; i++)        {
            if (files[i].getName().contains(".contacts")){
                try {
                    FileInputStream in = new FileInputStream(files[i]);
                    BufferedReader myReader = new BufferedReader(
                            new InputStreamReader(in));
                    String aDataRow = "";
                    String aBuffer = "";
                    while ((aDataRow = myReader.readLine()) != null) {
                        aBuffer += aDataRow + "\n";
                    }
                    in.close();
                    FileOutputStream out;
                    out = this.openFileOutput(ContactPath, Context.MODE_PRIVATE);
                    out.write(aBuffer.getBytes());
                    out.flush();
                    out.close();
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("strings", MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("theFirstTime", false);
                    editor.apply();
                    files[i].delete();
                    scanFile(files[i]);
                    Toast.makeText(cxt, "אנשי קשר עודכנו באופן ידני", Toast.LENGTH_SHORT).show();
                    startUpFailed.setVisibility(View.GONE);
                    startUpSuccess.setVisibility(View.VISIBLE);
                    updateGroups();
                    return true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return  false;
    }

    //read file given from local network and save it with given name
    public Boolean readFileFromSMBServer(String Filename, String path) {
        String user = getString(R.string.user) + ":" + getString(R.string.pass);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

        SmbFile sFile = null;
        try {
            sFile = new SmbFile(path,auth);
            sFile.setConnectTimeout(1000);
            SmbFileInputStream in = new SmbFileInputStream(sFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(in));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            in.close();

            FileOutputStream out;// = new FileOutputStream(DataPath);
            out = this.openFileOutput(Filename, Context.MODE_PRIVATE);
            out.write(aBuffer.getBytes());
            out.flush();
            out.close();

            return true;
            //sfos.write("Test".getBytes());
            //sfos.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //      Toast.LENGTH_SHORT).show();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (SmbException e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //Toast.makeText(getBaseContext(), "done!!",
        //      Toast.LENGTH_SHORT).show();
    }

    //read file given from internet network and save it with given name
    public Boolean readFileFromFTPServer(String Filename, String path) {

        FTPClient mFTPClient;
        mFTPClient = new FTPClient();

        path = path.replace("smb://192.168.1.25/mp3admin/","");

        try {
            mFTPClient.setAutodetectUTF8(true);

            mFTPClient.connect("yhy.co.il");
            mFTPClient.enterLocalPassiveMode();
            mFTPClient.login("mp3site@yhy.co.il", ServerPass);

            InputStream in = mFTPClient.retrieveFileStream(path);

            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(in));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            in.close();

            FileOutputStream out;// = new FileOutputStream(DataPath);
            out = this.openFileOutput(Filename, Context.MODE_PRIVATE);
            out.write(aBuffer.getBytes());
            out.flush();
            out.close();
            mFTPClient.disconnect();

            return true;
            //sfos.write("Test".getBytes());
            //sfos.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //      Toast.LENGTH_SHORT).show();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (SmbException e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
    }
        //Toast.makeText(getBaseContext(), "done!!",
        //      Toast.LENGTH_SHORT).show();
    }

    //responsible for download from the server
    public class readFromServer extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            //refreshPics();
            if(isWIFIAvailable())
                return readFileFromSMBServer(DataPath, netDataPath)
                        && readFileFromSMBServer(ContactPath, netContactsPath)
                        && readFileFromSMBServer(LibraryPath, netLibraryPath);

            else if (isNetworkAvailable())
                return readFileFromFTPServer(DataPath, netDataPath)
                    &&readFileFromFTPServer(ContactPath, netContactsPath)
                    && readFileFromFTPServer(LibraryPath, netLibraryPath)
                    && readFileFromFTPServer(getString(R.string.properties), netFTPAppDataPath + getString(R.string.properties));

            else return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pd != null)
                pd.cancel();

            isUpdating = false;

            if(!result) {
                //Toast.makeText(cxt, "לא עודכנו קבצים", Toast.LENGTH_SHORT).show();
                startUpFailed.setVisibility(View.VISIBLE);
                startUpSuccess.setVisibility(View.GONE);
            }
            else {
                updateGroups();

                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("strings", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("theFirstTime", false);
                    editor.apply();

                startUpFailed.setVisibility(View.GONE);
                startUpSuccess.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPreExecute() {

                pd = new ProgressDialog(cxt);
                pd.setMessage("מעדכן קבצים ראשוניים, אנא המתן...");
                pd.setCancelable(false);
                pd.show();

            isUpdating = true;
            //Toast.makeText(cxt, "מעדכן קבצים ברקע", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private void updateGroups(){
        BufferedReader reader = null;
        try {
            //InputStream is = this.getResources().openRawResource(R.raw.contacts_database);
            FileInputStream is = openFileInput(ContactPath);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();

            while (mLine != null) {
                // process line_divider
                if (!mLine.equals("") && !mLine.contains("#"))
                    checkLine(mLine);
                mLine = reader.readLine();
            }
            reader.close();

            String allGroups = "";
            for (int i = 0; i < groups.size(); i++){
                allGroups = groups.get(i) + "#" + allGroups;
            }
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.allGroups), MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.allGroups), allGroups);
            editor.commit();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "שגיאה בעדכון אנשי קשר", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkLine(String line) {
        String[] SplitLine = line.split(",", 4);
        String group = SplitLine[2];
        if (group.equals("?"))
            return;
        if (!group.equals("צוות")){
            group = "מחזור "+group;
        }
        if (!groups.contains(group)){
            groups.add(group);
        }
    }

    private void startRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(cxt)){
                //requestFloatingPermission();
            }
            else {
                requestPermission(this);
            }
        }
    }

    public void requestFloatingPermission() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this);
        alertDialog
                .setMessage("על מנת לאפשר שימוש מלא באפליקציה אנא אשר את ההרשאות הבאות")
                .setPositiveButton("סבבה, הבנתי", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this,"הרשאה אושרה, תודה",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this,"ההרשאה נדחתה",Toast.LENGTH_LONG).show();
                }
                requestPermission(this);
            }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        makeDir();
//                        if(!getContactsFile())
                        new readFromServer().execute("");
                    } else {
                        Toast.makeText(this,"הרשאה נדחתה",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        Boolean floating = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            floating = Settings.canDrawOverlays(this);
        }
        int result = ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(this,
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED
                && result1 == PackageManager.PERMISSION_GRANTED
                && floating;
    }

    public Boolean isWIFIAvailable() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            wifiInfo.getSSID();
            String name = networkInfo.getExtraInfo();
            String ssid = "";
            ssid = "\"" + wifiInfo.getSSID() + "\"";

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
}
