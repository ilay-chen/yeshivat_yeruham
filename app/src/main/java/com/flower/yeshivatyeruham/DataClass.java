package com.flower.yeshivatyeruham;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * class of application, contains main variables and functions.
 */
public class DataClass extends MultiDexApplication {

    static protected String AppSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "YeshivatYeruham/",
    AudioSavePath = AppSavePath + "Recording/",PicSavePath = AppSavePath + ".Pic/",DownSavePath = AppSavePath + "Downloads/",
            DataPath = "tagData.xml", ContactPath = "contacts_database.txt",LibraryPath = "library.txt", netDataPath, ServerPass,
            netContactsPath, saveMp3Path, netLogPath, xmlFile, saveSpecialLessons, savePublicLessonsFile, netFilesDownloadsCount,
            netSksDownloadsCount, netFilesUploadsCount, netLessonsUploadsCount, netFTPDownloadsCount, netFTPPics,
            ActPath = "allAct.txt", netAllActPath, netLibraryPath;
    static protected String WIFINAME = "yhy5,yhy2.4,yeshivat-yeruham,yhyoffice";
    static protected String SMBRoot = "smb://192.168.1.25/",FTPRoot = "ספריית קבצי שמע/", studentRoot = "student/", sksRoot =  "mp3/";
    static double version;
    static ArrayList groups;
    static int notificationId = 0;
    static List<String> messagePhoneNumbers = new ArrayList<>();
    static Boolean isUpdating = false;
    final static int NOT_UPDATED=0;
    final static int UPDATING=1;
    final static int UPDATED=2;

    static int updateStat= NOT_UPDATED;
    static ProgressDialog pd;
    static Context cxt;
    static String netPicsPath = SMBRoot + "mp3admin/מערכת/appdata/pictures/";
    static String internalFilePath;
    Boolean makeToast = false;
    final ArrayList<Indexable> indexableNotes = new ArrayList<>();
    int id = 0;
    FTPClient mFTPClient;
    static String netFTPAppDataPath="מערכת/appdata/";
    @Override
    public void onCreate() {
        super.onCreate();
        groups = new ArrayList();
        cxt = getApplicationContext();


        //font initialization.
        //CalligraphyConfig.initDefault("fonts/your-font.ttf");
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("Heebo-Regular.ttf")
//                .setDefaultBoldFontPath("fonts/Roboto-RobotoBold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        makeDir();

        netDataPath = getString(R.string.netDataPath);
        netContactsPath = getString(R.string.netContactsPath);
        netAllActPath = getString(R.string.netAllActPath);
        netLibraryPath = getString(R.string.netLibraryPath);
        saveMp3Path = getString(R.string.saveMp3Path);
        netLogPath = getString(R.string.netLogPath);
        saveSpecialLessons = getString(R.string.saveSpecialLessons);
        savePublicLessonsFile = getString(R.string.netPublicLessonsPath);
        netFilesDownloadsCount = getString(R.string.netFilesDownloadsCountPath);
        netSksDownloadsCount = getString(R.string.netSksDownloadsCountPath);
        netFTPDownloadsCount = getString(R.string.netFTPDownloadsCountPath);
        netLessonsUploadsCount = getString(R.string.netUploadsCountPath);
        netFilesUploadsCount = getString(R.string.netFilesUploadsCountPath);
        netFTPPics = getString(R.string.netTTPPicsPath);
        internalFilePath = getFilesDir().getAbsolutePath();
        ServerPass = getString(R.string.pass);
        version = Double.parseDouble(getString(R.string.version));

    }

    public Boolean isFirstTime()
    {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("strings", MODE_PRIVATE);

        //firstTime = sharedPref.getBoolean("firstTime",false);
        return sharedPref.getBoolean("theFirstTime",true);
    }



    // make dirs of application if have'nt already.
    public static void makeDir()
    {

//        if(checkSD())
//        {
//            File sdCard = Environment.getExternalStorageDirectory();
//            AudioSavePath = AudioSavePath.replace(Environment.getExternalStorageDirectory().getAbsolutePath()
//                    ,sdCard.getAbsolutePath());
//            PicSavePath = PicSavePath.replace(Environment.getExternalStorageDirectory().getAbsolutePath()
//                    ,sdCard.getAbsolutePath());
//            DownSavePath = DownSavePath.replace(Environment.getExternalStorageDirectory().getAbsolutePath()
//                    ,sdCard.getAbsolutePath());
//        }

        File Dir = new File(Environment.getExternalStorageDirectory(), "YeshivatYeruham");

        if (!Dir.exists()) {
            if (!Dir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }else {
                scanFile(Dir);
            }

        }

        Dir = new File(AudioSavePath);

        if (!Dir.exists()) {
            if (!Dir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }else {
                scanFile(Dir);
            }

        }

        Dir = new File(PicSavePath);

        if (!Dir.exists()) {
            if (!Dir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }else {
                scanFile(Dir);
            }

        }

        Dir = new File(DownSavePath);
        File tempDir = new File(DownSavePath.replace("Downloads","Lessons"));
        if(tempDir.exists())
        {
            tempDir.renameTo(Dir);
        }
        else if (!Dir.exists()) {
            if (!Dir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }else {
                scanFile(Dir);
            }
        }



        makeNoMedia();
    }

    // make leeson show/not show in the phone player (choose in preference).
    static void makeNoMedia (){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        Boolean showFiles = sharedPreferences.getBoolean("files_in_library",
                cxt.getResources().getBoolean(R.bool.files_in_library_default));
        if(!showFiles) {
            File noMediaFile = new File(AppSavePath, ".nomedia");

            if (!noMediaFile.exists()) {
                try {
                    noMediaFile.createNewFile();
                    scanFile(noMediaFile);
                } catch (IOException e) {
                    Log.d("App", "failed to create nomedia");
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.d("App", "failed to create nomedia");
                    e.printStackTrace();
                }
            }
        }
        else{
            File noMediaFile = new File(AppSavePath, ".nomedia");
            if (noMediaFile.exists()) {
                noMediaFile.delete();
                scanFile(noMediaFile);
            }
        }
    }

    static public Boolean checkSD()
    {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable&&mExternalStorageWriteable;
    }

    // update tagXml file, contacts file and photos (check if there is a connection, and which.)
    public void updateFile(Boolean makeToast, Boolean forceUpdate)
    {
        this.makeToast = makeToast;
        if(!isUpdating && !isFirstTime()) {
            updateStat=NOT_UPDATED;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                new readFromServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, forceUpdate);
            else
                new readFromServer().execute(forceUpdate);
//            new readFromServer().execute(forceUpdate);
        }
    }

    public boolean isOnline()
    {
        WifiManager connManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //NetworkInfo myWiFi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiInfo mWiFi = connManager.getConnectionInfo();
        if(mWiFi != null && mWiFi.getSSID()!=null &&
                (mWiFi.getSSID().contains(WIFINAME.split(",")[0]) ||
                        mWiFi.getSSID().contains(WIFINAME.split(",")[1])))
        {
            return true;
        }
        else {
            if (makeToast)
                Toast.makeText(cxt, "לא ניתן לעדכן קבצים, אין חיבור לרשת הישיבה", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

//    public boolean getContactsFile() {
//        String path = AudioSavePath;
//
//        Log.d("Files", "Path: " + path);
//        File directory = new File(path);
//        File[] files = directory.listFiles();
//        if(files!=null)
//        for (int i = 0; i < files.length; i++)        {
//            if (files[i].getName().contains(".contacts")){
//                try {
//                    FileInputStream in = new FileInputStream(files[i]);
//                    BufferedReader myReader = new BufferedReader(
//                            new InputStreamReader(in));
//                    String aDataRow = "";
//                    String aBuffer = "";
//                    while ((aDataRow = myReader.readLine()) != null) {
//                        aBuffer += aDataRow + "\n";
//                    }
//                    in.close();
//                    FileOutputStream out;
//                    out = this.openFileOutput("contacts_database.txt", Context.MODE_PRIVATE);
//                    out.write(aBuffer.getBytes());
//                    out.flush();
//                    out.close();
//                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
//                            "strings",
//                            MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPref.edit();
//                    editor.putBoolean("firstTime", false);
//                    editor.apply();
//                    files[i].delete();
//                    scanFile(files[i]);
//                    Toast.makeText(cxt, "אנשי קשר עודכנו באופן ידני", Toast.LENGTH_SHORT).show();
//                    return true;
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return  false;
//    }

    private void saveToInternalStorage(FTPFile fFile, String subFolder){
        String fileName = fFile.getName().toLowerCase().replace(".jpg", ".jpeg");
        File myFile = new File(PicSavePath + fileName);
        try {
//                InputStream inputStream = new ByteArrayInputStream(aBuffer.getBytes());
//                File f = new File(resultUri.getPath());
//                InputStream inputStream = f;
//                InputStream inputStream = new FileInputStream(f);
//                InputStream fis = new FileInputStream(new File(context.getCacheDir(), resultUri.getLastPathSegment()));

//                File file = new File(context.getCacheDir(), resultUri.getLastPathSegment());

//                Uri uri = FileProvider.getUriForFile(context, cxt.getApplicationContext().getPackageName() + ".provider", file);

//                InputStream fis = context.openFileInput(resultUri.getLastPathSegment());
//            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
            mFTPClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            OutputStream os = new FileOutputStream(myFile);
            Boolean result = mFTPClient.retrieveFile(netFTPPics + "/" + subFolder+ "/" + fFile.getName(), os);
//                inputStream.close();
//                fis.close();
//            mFTPClient.disconnect();
            os.close();
            scanFile(myFile);
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //save the pic from local sks
    private void saveToInternalStorage(SmbFile sFile){

        String fileName = sFile.getName().toLowerCase().replace(".jpg", ".jpeg");
//        String fileName = sFile.getName();
        File myFile = new File(PicSavePath + fileName);

        byte myByte[] = new byte[0];

        try {
            SmbFileInputStream fis= null;
            FileOutputStream out = null;

            fis = new SmbFileInputStream(sFile);
            myByte = new byte[(int)sFile.length()];

            out = new FileOutputStream(myFile);
            //out = this.openFileOutput(fileName, Context.MODE_PRIVATE);
            fis.read(myByte,0,myByte.length);
            out.write(myByte);
            out.flush();
            out.close();
            fis.close();
            myFile.setLastModified(sFile.getLastModified());
            scanFile(myFile);
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //update the pics. look on evey pics on sks, and in local phone - if need to download(pic exsist in
    // sks but not on the phone), or to update(if the pic exsist in the sks, and the phone - but
    // there are newer version of the pic in sks) - delete and it.
    //else - if needed to delete pic (ic is in the phone, but not in sks).
    public Boolean refreshPics(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        Boolean showPics = sharedPreferences.getBoolean("show_pics",
                cxt.getResources().getBoolean(R.bool.show_pics_default));
        if (showPics){
            String path = PicSavePath;
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.savedPics), MODE_PRIVATE);
            String allPics = sharedPref.getString(getString(R.string.savedPics), "");
            try {
                ArrayList<String> pics = new ArrayList<>();
                File deviceDir = new File(path);
                File[] deviceFiles = deviceDir.listFiles();
                if (deviceFiles == null)
                    deviceFiles = new File[0];
                for (File deviceFile : deviceFiles) {
                    if (deviceFile.getName().contains(".jpeg"))
                        pics.add(deviceFile.getName().replace(".jpeg", ""));
                }

//                String user = getString(R.string.user) + ":" + getString(R.string.pass);
//                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);
//                SmbFile netRootDir = new SmbFile(netPicsPath, auth);
//                netRootDir.setConnectTimeout(10000);
//                SmbFile[] netFolders = netRootDir.listFiles();


//                FTPClient mFTPClient;
                mFTPClient = new FTPClient();
                mFTPClient.setAutodetectUTF8(true);
                mFTPClient.connect("yhy.co.il");
                mFTPClient.enterLocalPassiveMode();
                mFTPClient.login("mp3site@yhy.co.il", ServerPass);


                FTPFile[] ftpFolders = mFTPClient.listFiles(netFTPPics);

                for (FTPFile ftpFolder : ftpFolders){
                    if (!ftpFolder.getName().startsWith(".")) {
                        FTPFile[] ftpFiles = mFTPClient.listFiles(netFTPPics + ftpFolder.getName());
                        for (FTPFile ftpFile : ftpFiles) {
                            if (!ftpFile.getName().startsWith(".")) {
                                String num = ftpFile.getName().toLowerCase().replace(".jpg", "").replace(".jpeg", "");
//                                String date = mFTPClient.getModificationTime(ftpFile.toString());
                                String date = String.valueOf(ftpFile.getTimestamp().getTimeInMillis());
                                if (allPics.contains(num + "-")) { //exist
                                    String contact = allPics.substring(allPics.indexOf(num)).split(",")[0];
                                    String oldDate = contact.split("-")[1];
                                    if (oldDate.equals(date)) //same - do noting
                                    {
                                        pics.remove(num);
                                    } else { //different - update
                                        String newContact = contact.replace(oldDate, date);
                                        allPics = allPics.replace(contact, newContact);
//                        File f = new File(path + num + ".jpeg");
//                        f.delete();
//                        scanFile(f);
                                        saveToInternalStorage(ftpFile, ftpFolder.getName());
                                        pics.remove(num);
                                    }
                                } else { //not exist - add
                                    allPics = allPics + num + "-" + date + ",";

                                    saveToInternalStorage(ftpFile, ftpFolder.getName());
                                    pics.remove(num);
                                }
                            }
                        }
                    }
                }
                for(int i = 0; i < pics.size(); i++){ // delete old pics
                    File delFile = new File(path + pics.get(i) + ".jpeg");
                    delFile.delete();
                    String contact;
                    try{
                        contact = allPics.substring(allPics.indexOf(pics.get(i))).split(",")[0] + ",";
                        allPics = allPics.replace(contact, "");
                    }catch (Exception e){
                    }
                    scanFile(delFile);
                }
                mFTPClient.disconnect();
//                Snackbar.make("שמנו לב שאין לך תמונה עדיין...", Snackbar.LENGTH_LONG)
//                        .setAction("עדכן תמונה", new View.OnClickListener() {
//                            @Override
////                        @TargetApi(Build.VERSION_CODES.M)
//                            public void onClick(View v) {
//                                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
//                                startActivity(settings);
//                            }
//                        });
////                for (SmbFile netFolder : netFolders) {
////                    if (netFolder.getName().endsWith("/")) {
////                        SmbFile[] netFiles = netFolder.listFiles();
////                        for (SmbFile netFile : netFiles) {
////
////                            String num = netFile.getName().toLowerCase().replace(".jpg", "").replace(".jpeg", "");
////                            String date = netFile.getLastModified() + "";
////
////                            if (allPics.contains(num + "-")) { //exist
////                                String contact = allPics.substring(allPics.indexOf(num)).split(",")[0];
////                                String oldDate = contact.split("-")[1];
////                                if (oldDate.equals(date)) //same - do noting
////                                {
////                                    pics.remove(num);
////                                } else { //different - update
////                                    String newContact = contact.replace(oldDate, date);
////                                    allPics = allPics.replace(contact, newContact);
//////                        File f = new File(path + num + ".jpeg");
//////                        f.delete();
//////                        scanFile(f);
////                                    saveToInternalStorage(netFile);
////                                    pics.remove(num);
////                                }
////                            } else { //not exist - add
////                                allPics = allPics + num + "-" + date + ",";
////
////                                saveToInternalStorage(netFile);
////                                pics.remove(num);
////                            }
////                        }
////                    }
////                }
////                for(int i = 0; i < pics.size(); i++){ // delete old pics
////                    File delFile = new File(path + pics.get(i) + ".jpeg");
////                    delFile.delete();
////                    String contact = allPics.substring(allPics.indexOf(pics.get(i))).split(",")[0] + ",";
////                    allPics = allPics.replace(contact,"");
////                    scanFile(delFile);
////                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SmbException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
        return true;
    }

    // read tag and contacts files from local sks
    public Boolean readFileFromSMBServer(String Filename, String path, Boolean forceUpdate) {
        String user = getString(R.string.user) + ":" + getString(R.string.pass);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

        //String path = "smb://192.168.1.25/student/19 מחזור כ''ג/עילאי האמיתי חן/lessons.xml";
        SmbFile sFile = null;
        try {
            sFile = new SmbFile(path,auth);

            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.files_version), MODE_PRIVATE);
            String curFileVers = sharedPref.getString(Filename, "");
            String newFileVers = sFile.getLastModified() + "";
            if (forceUpdate || !newFileVers.equals(curFileVers)) {
                Log.d("test", Filename + " update");
                sFile.setConnectTimeout(10000);
                //SmbFileOutputStream sfos = new SmbFileOutputStream(sFile);
                SmbFileInputStream in = new SmbFileInputStream(sFile);
                BufferedReader myReader = new BufferedReader(
                        new InputStreamReader(in));
                String aDataRow = "";
                String aBuffer = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    aBuffer += aDataRow + "\n";
                }


                FileOutputStream out;// = new FileOutputStream(DataPath);
                out = this.openFileOutput(Filename, Context.MODE_PRIVATE);
                out.write(aBuffer.getBytes());
                out.flush();
                out.close();

                File f = getFilesDir();

                FileInputStream fis = openFileInput(Filename);
                myReader = new BufferedReader(
                        new InputStreamReader(fis));
                aDataRow = "";
                aBuffer = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    aBuffer += aDataRow + "\n";
                }
                fis.close();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(Filename, newFileVers);
                editor.apply();

                xmlFile = aBuffer;
            }
            Log.d("test", Filename + " ended");
//            updateGroups();

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
        //Toast.makeText(getBaseContext(), "donee!!",
        //      Toast.LENGTH_SHORT).show();
    }

    // read tag and contacts files from internet sks
    public static Boolean readFileFromFTPServer(String Filename, String path, Boolean forceUpdate, Context context) {

        FTPClient mFTPClient;
        mFTPClient = new FTPClient();


        path = path.replace("smb://192.168.1.25/mp3admin/","");

        try {
            mFTPClient.setAutodetectUTF8(true);

            mFTPClient.connect("yhy.co.il");
            mFTPClient.enterLocalPassiveMode();
            mFTPClient.login("mp3site@yhy.co.il", ServerPass);

            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.files_version), MODE_PRIVATE);
            String curFileVers = sharedPref.getString(Filename, "");
            String newFileVers = mFTPClient.getModificationTime(path);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(Filename+"updated", Calendar.getInstance().getTimeInMillis());
            editor.apply();

            if (forceUpdate || !newFileVers.equals(curFileVers)) {
                Log.d("test", Filename + " update");
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
                out = context.openFileOutput(Filename, Context.MODE_PRIVATE);
                out.write(aBuffer.getBytes());
                out.flush();
                out.close();

                editor.putString(Filename, newFileVers);
                editor.apply();

                if(Filename.equals(context.getString(R.string.properties)) && !newFileVers.equals(curFileVers)){
                    SharedPreferences sharedPrefs = context.getSharedPreferences("strings", MODE_PRIVATE);
                    SharedPreferences.Editor e = sharedPrefs.edit();
                    e.putBoolean("response", false);
                    e.apply();


                }

            }
            Log.d("test", Filename + " ended");
            mFTPClient.disconnect();

//            updateGroups();

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
            //Toast.LENGTH_SHORT).show(///////////////////);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //Toast.makeText(getBaseContext(), "done!!",
        //      Toast.LENGTH_SHORT).show();
    }

    public class readFromServer extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            isUpdating = true;

            if(makeToast)
                Toast.makeText(cxt, "מעדכן קבצים ברקע", Toast.LENGTH_SHORT).show();
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(cxt)
//                    .setContentTitle("בדיקה")
//                    .setContentText("מעדכן קבצים")
//                    .setSmallIcon(R.drawable.ic_notification)
//                    .setAutoCancel(true);
//            NotificationManager notificationManager =
//                    (NotificationManager) cxt.getSystemService(cxt.NOTIFICATION_SERVICE);
//
//            notificationManager.notify(notificationId++, mBuilder.build());
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
//            if(isWIFIAvailable())

            if(isWIFIAvailable())
                return readFileFromSMBServer(ContactPath, netContactsPath, params[0]) &&
                    readFileFromSMBServer(DataPath, netDataPath, params[0]) &&
                    readFileFromSMBServer(ActPath, netAllActPath, params[0]) &&
                    readFileFromSMBServer(LibraryPath, netLibraryPath, params[0]);
            else if (isNetworkAvailable()) {
                Context context=getApplicationContext();

                //this sets the update status of properties.txt
                updateStat= UPDATING;
                if(readFileFromFTPServer(getString(R.string.properties), netFTPAppDataPath + getString(R.string.properties),
                        params[0],context))
                    updateStat=UPDATED;
                else updateStat=NOT_UPDATED;

                return readFileFromFTPServer(context.getString(R.string.attendance_path), netFTPAppDataPath + context.getString(R.string.attendance_path), true, context)
                        && readFileFromFTPServer(ContactPath, netContactsPath, params[0], context)
                        && readFileFromFTPServer(DataPath, netDataPath, params[0], context)
                        && readFileFromFTPServer(LibraryPath, netLibraryPath, params[0], context)
                        && refreshPics()
                        && readFileFromFTPServer(context.getString(R.string.localStudentsFN), netFTPAppDataPath + context.getString(R.string.localStudentsFN), true, context)
                        && updateStat==UPDATED;

            }

            else return false;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

        @Override
        protected void onPostExecute(Boolean result) {

            isUpdating = false;

            if(!result) {

//                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(cxt)
//                        .setContentTitle("בדיקה")
//                        .setContentText("לא עודכנו קבצים")
//                        .setSmallIcon(R.drawable.ic_notification)
//                        .setAutoCancel(true);
//                NotificationManager notificationManager =
//                        (NotificationManager) cxt.getSystemService(cxt.NOTIFICATION_SERVICE);
//                notificationManager.notify(notificationId++, mBuilder.build());
                if(makeToast)
                Toast.makeText(cxt, "לא עודכנו קבצים", Toast.LENGTH_SHORT).show();
            }
            else {
                updateGroups();

//                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(cxt)
//                        .setContentTitle("בדיקה")
//                        .setContentText("הקבצים עודכנו בהצלחה")
//                        .setSmallIcon(R.drawable.ic_notification)
//                        .setAutoCancel(true);
//                NotificationManager notificationManager =
//                        (NotificationManager) cxt.getSystemService(cxt.NOTIFICATION_SERVICE);
//                notificationManager.notify(notificationId++, mBuilder.build());
                if(makeToast)
                Toast.makeText(cxt, "הקבצים עודכנו בהצלחה", Toast.LENGTH_SHORT).show();
            }
        }



    }

    public DataClass()
    {

    }

    //tagXml object.
    @Root(name = "all")
    static class allTeacher {

        @ElementList(required=false, name = "teachers")
        List<myTeacher> teachers;
        @Element(required=false, name = "specialLessons")
        String specialLessons;

        public String getList()
        {
            String ret = "";
            for(myTeacher teach : teachers)
                ret += teach.toString();
            return ret;
        }

        @Override
        public String toString() {
            return "<all>\n<specialLessons>"+specialLessons+"</specialLessons>\n<teachers>\n"+getList()+"\n</teachers>\n</all>";
            //return "test";
        }
    }

    //tagXml object child
    @Root(name = "teacher")
    static class myTeacher {
        @ElementList (required=false, name = "lessons")
        List<myLesson> lessons;
        @Element(required=false, name = "name")
        String name;


        public String getList()
        {
            String ret = "";
            for(myLesson less : lessons)
                ret += less.toString();
            return ret;
        }

        @Override
        public String toString() {
            return "\n<teacher>\n" + "<name>" + name + "</name>\n<lessons>\n" + getList() + "\n</lessons>\n</teacher>\n";
            //return "test";
        }
    }

    //tagXml object teacher child
    @Root(name = "Lesson")
    static class myLesson {
        @Element (required=false,name = "lessonname")
        String lessonname;
        @Element (required=false,name = "folder")
        String folder;
        @Element (required=false,name = "track")
        String track;
        @Element (required=false,name = "isPublic")
        Boolean isPublic;

        @Override
        public String toString() {
            return "<lesson>\n<lessonname>"+lessonname+"</lessonname>\n"+
                    "<track>"+track+"</track>\n<folder>"+folder+"</folder>\n" +
                    "<isPublic>" + isPublic + "</isPublic>\n</lesson>\n";
        }
    }

    private void updateGroups(){
        BufferedReader reader = null;
        groups.clear();
        indexableNotes.clear();
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
            editor.apply();

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "שגיאה בעדכון אנשי קשר", Toast.LENGTH_SHORT).show();
        }


//        Indexable noteToIndex = Indexables.personBuilder()
//                .setName("try3")
//                .setTelephone("0524471730")
//                .setEmail("מחזור כג")
//                .setDescription("bal bal")
//                .setUrl("yeshivatyeruham://0544338658/שם של אדם/שג")
//                .build();
//
//
//        indexableNotes.add(noteToIndex);
////        FirebaseAppIndex.getInstance().update(noteToIndex);
//
//        noteToIndex = Indexables.personBuilder()
//                .setName("try2")
//                .setTelephone("0524471730")
//                .setEmail("מחזור כג")
//                .setDescription("bal bal")
//                .setUrl("yeshivatyeruham://05443528658/שם של אדם/שג")
//                .build();
//
//        indexableNotes.add(noteToIndex);
////        FirebaseAppIndex.getInstance().update(noteToIndex);
//
//        noteToIndex = Indexables.personBuilder()
//                .setName("try1")
//                .setTelephone("0524471730")
//                .setEmail("מחזור כג")
//                .setDescription("bal bal")
//                .setUrl("yeshivatyeruham://0544308658/שם של אדם/שג")
//                .build();

//        indexableNotes.add(noteToIndex);

        Task<Void> reset = FirebaseAppIndex.getInstance().removeAll();
//
        reset.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("test", "App Indexing API: Successfully reset");
                if (indexableNotes.size() > 0) {
                    int size = indexableNotes.size();
                    int total = 0;
                    int i;
                    Indexable[] notesArr = new Indexable[size];
                    ArrayList<Indexable> indexableNotesSmall = new ArrayList<>();
                    notesArr = indexableNotes.toArray(notesArr);

                    while (total < size) {
//                        notesArrSmall = new Indexable[600];
                    indexableNotesSmall.clear();
                        for (i = 0; i < 500; i++) {
                            if (i + total >= size) {
                                break;
                            }
                            indexableNotesSmall.add( notesArr[i + total]);
                        }
                        total += i;
                        // batch insert indexable notes into index
                    Indexable[] notesArrSmall = new Indexable[indexableNotesSmall.size()];
                        notesArrSmall = indexableNotesSmall.toArray(notesArrSmall);
                        Task<Void> update = FirebaseAppIndex.getInstance().update(notesArrSmall);
                        update.addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
//                            Toast.makeText(cxt, "עודכן", Toast.LENGTH_SHORT).show();
                                Log.d("test", "App Indexing API: Successfully added note to index");
                            }
                        });
                        update.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception exception) {
                                Log.e("test", "App Indexing API: Failed to add note to index. " + exception
                                        .getMessage());
                            }
                        });
                    }

                }
            }
        });
    }

    public void checkLine(String line) {
//        Log.d("test", line);
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

        String name = SplitLine[0];
        String num = SplitLine[1];
        String[] words = group.split(" ");
        String shortGroup = words[words.length - 1];
        Indexable noteToIndex = Indexables.personBuilder()
                .setName(name)
                .setTelephone(num)
                .setDescription(group)
                .setUrl("yeshivatyeruham://contact/" + num + "/" + name + "/" + shortGroup)
//                .setUrl("yeshivatyeruham://" + id++)
                .build();

        indexableNotes.add(noteToIndex);
    }

    static public void scanFile(File file){
        MediaScannerConnection.scanFile(cxt, new String[]{file.getAbsolutePath()}, null, null);
    }

    //check if yeshiva WIFI available
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

    protected static boolean isOnline(Context ctx) {
        android.net.wifi.WifiManager connManager = (android.net.wifi.WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //NetworkInfo myWiFi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiInfo mWiFi = connManager.getConnectionInfo();
        if (mWiFi != null && mWiFi.getSSID() != null &&
                (mWiFi.getSSID().contains(WIFINAME.split(",")[0]) ||
                        mWiFi.getSSID().contains(WIFINAME.split(",")[1]))) {
            return true;
        } else {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
                return true;
            else {
                return false;
            }
        }
    }
//???? why is that here?
    protected static Boolean isWIFIAvailable(Context ctx) {
        android.net.wifi.WifiManager connManager = (android.net.wifi.WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWiFi = connManager.getConnectionInfo();
        if(mWiFi != null && mWiFi.getSSID()!=null && (mWiFi.getSSID().contains(WIFINAME.split(",")[0]) || mWiFi.getSSID().contains(WIFINAME.split(",")[1]))) {
            return true;
        }
        else {
            return false;
        }
    }
    //check if internet connection available
    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


