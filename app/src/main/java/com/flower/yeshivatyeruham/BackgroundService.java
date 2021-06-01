package com.flower.yeshivatyeruham;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.OpenableColumns;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import static com.flower.yeshivatyeruham.DataClass.AudioSavePath;
import static com.flower.yeshivatyeruham.DataClass.DataPath;
import static com.flower.yeshivatyeruham.DataClass.netDataPath;
import static com.flower.yeshivatyeruham.DataClass.netFilesUploadsCount;
import static com.flower.yeshivatyeruham.DataClass.netLogPath;
import static com.flower.yeshivatyeruham.DataClass.netLessonsUploadsCount;
import static com.flower.yeshivatyeruham.DataClass.notificationId;
import static com.flower.yeshivatyeruham.DataClass.saveMp3Path;
import static com.flower.yeshivatyeruham.DataClass.savePublicLessonsFile;
import static com.flower.yeshivatyeruham.DataClass.scanFile;


/**
 * service responsible to upload the lesson to local sks.
 * 1. get the file tagging
 * 2. read the tagXml file
 * 3. update lesson tag and number
 * 4. write the lesson to local sks
 * 5. log the progress to log file
 * 6. add the lesson path to PublicLesson.txt if necessary (if its define as public lesson in xmlTag file).
 */

public class BackgroundService extends Service {
    private String fileName, ALBUM, ARTIST, TITLE, TRACK, YEAR, COMMENT;
    private Uri uploadFile;
    private String uploadPath;
    private long uploadSize;
    private DataClass.allTeacher allData;
    SmbFile []fileList;
    Boolean pickedFile = false;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent,int flag, int startId)
    {
        Intent upload = intent;
        fileName = upload.getStringExtra("tempName");
        if (fileName == null){
            pickedFile = true;
            uploadFile = upload.getParcelableExtra("uploadFile");
            uploadPath = upload.getStringExtra("uploadPath");
            uploadSize = upload.getLongExtra("uploadSize", 0);
            Log.d("test", "path " + uploadPath);
            Log.d("test", "file " + uploadFile);
            Log.d("test", "size " + uploadSize);

            new writeToServer().execute("");
        }
//        uploadFile = upload.getStringExtra("uploadFile");
//        if (uploadFile != null){
//            pickedFile = true;
//            new writeToServer().execute("");
//        }
        else {
            IntialazeTag();
            tryGetFile();
        }

        return Service.START_NOT_STICKY;
    }

    public String readFile() throws IOException {
        FileInputStream fis = openFileInput("tagData.xml");
        BufferedReader myReader = new BufferedReader(
                new InputStreamReader(fis));
        String aDataRow = "";
        String aBuffer = "";
        while ((aDataRow = myReader.readLine()) != null) {
            aBuffer += aDataRow + "\n";
        }
        return aBuffer;
    }

    public void tryGetFile()
    {
        try {
            readFromServer bs = new readFromServer();
            bs.execute("true");
        } catch (Exception e) {
            e.printStackTrace();
            //Error(e.toString());
            Toast.makeText(this, "קובץ מידע חסר.. או שגיאה בקריאת קובץ", Toast.LENGTH_SHORT).show();
            //new TaggingActivity.readFromServer().execute("false");
        }
    }

    //get xmlTag file to objects
    @Test
    public void Taging(String xml) throws Exception {

        Serializer serializer = new Persister();
        xml = xml.substring(xml.indexOf("<all>"),xml.indexOf("</all>")+6);
        allData = serializer.read(DataClass.allTeacher.class, xml);
        Log.d("test",allData.toString());
    }


    public void upLoadDataFile() {
            Serializer serializer = new Persister();
            File newData = new File(DataPath);

            try {
                //Tagging(xmlFile);
                setTag();
                String data = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + allData.toString();

                new writeToServer().execute(data);
            } catch (Exception e) {
                e.printStackTrace();
                Error(e.toString());
            }
    }

    //get file tags
    public void IntialazeTag()
    {
        File myFile = new File(AudioSavePath + fileName);

        MP3File f = null;
        try {
            f = (MP3File) AudioFileIO.read(myFile);
            //f = new MP3File(myFile);
            Tag tag = f.getTag();
            ID3v23Tag v23tag = (ID3v23Tag) tag;

            ARTIST = v23tag.getValue(FieldKey.ARTIST, 0);
            ALBUM = v23tag.getValue(FieldKey.ALBUM, 0);
            TITLE = v23tag.getValue(FieldKey.TITLE, 0);
            YEAR = v23tag.getValue(FieldKey.YEAR, 0);
            COMMENT = v23tag.getValue(FieldKey.COMMENT, 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //update the file gat (number)
    public void setTag()
    {

        File myFile = new File(AudioSavePath + fileName);

        MP3File f = null;
        try {
            f = (MP3File) AudioFileIO.read(myFile);
            //f = new MP3File(myFile);
            Tag tag = f.getTag();
            ID3v23Tag v23tag = (ID3v23Tag)tag;

            TRACK = v23tag.getValue(FieldKey.TRACK,0);

            if(TRACK == null || TRACK.equals("0") || fileName.contains("0")){
                TRACK = getTrackNumber();
            }
            else
                return;

            v23tag.setField(FieldKey.TRACK, TRACK);
            f.setTag(v23tag);
            //f.commit();
            AudioFileIO.write(f);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        } catch (CannotWriteException e) {
            e.printStackTrace();
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //tag.getFields(FieldKey.ARTIST);

        fileName = TRACK + " " + ARTIST + " - " + ALBUM + " - " + TITLE + " - " + COMMENT + ".mp3";
        File newName = new File(AudioSavePath, fileName);
        myFile.renameTo(newName);
        scanFile(newName);
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public String getTrackNumber()
    {
        int bigestTrack = 0, addUnknownFiles = 0;
        for(SmbFile file : fileList)
        {
            if(file.getName()!=null && file.getName().contains(" ")) {
                String track = file.getName().substring(0, file.getName().indexOf(" "));
                if(isNumeric(track)) {
                    //int Track = Integer.parseInt(track);
                    Double Track = Double.parseDouble(track);
                    if (Track > bigestTrack)
                        bigestTrack = Track.intValue();
                }
                else addUnknownFiles++;
            }
        }
            /*
            if(file.getName().toString().contains("mp3")||file.getName().toString().contains("MP3") ||
                    file.getName().toString().contains("wma") || file.getName().toString().contains("WMA"))
                bigestTrack++;
        }
        */
        /*
        for(DataClass.myTeacher teacher : allData.teachers)
            if(teacher.name.equals(teacherName)) {
                for(DataClass.myLesson lesson : teacher.lessons)
                    if(lesson.lessonname.equals(lessonName)) {
                        lesson.track = ""+(Integer.parseInt(lesson.track)+1);
                        return lesson.track;
                    }
            }
         */
        return ++bigestTrack +"";
    }

    public String getFilePath(String teacherName, String lessonName)
    {
        try {
            Taging(readFile());

            if(lessonName.contains("שיעור מיוחד"))
                return allData.specialLessons;
            for(DataClass.myTeacher teacher : allData.teachers)
                if(teacher.name.equals(teacherName)) {
                    for(DataClass.myLesson lesson : teacher.lessons)
                        if(lesson.lessonname.equals(lessonName))
                            return "smb:" + lesson.folder.replace("\\", "/");
                }

        } catch (Exception e) {
            e.printStackTrace();
            Error("cant read DataFile");
        }

        Error("no path");
        return saveMp3Path;
    }

    //check if lesson is public
    public Boolean isFilePublic(String teacherName, String lessonName)
    {
            if(lessonName.contains("שיעור מיוחד"))
                return false;

            for(DataClass.myTeacher teacher : allData.teachers)
                if(teacher.name.equals(teacherName)) {
                    for(DataClass.myLesson lesson : teacher.lessons)
                        if(lesson.lessonname.equals(lessonName))
                            return lesson.isPublic;
                }

        Error("no path");
        return false;
    }

    public void updateCounter()
    {
        String user = getString(R.string.user) + ":" + getString(R.string.pass);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

        //String path = "smb://192.168.1.25/student/19 מחזור כ''ג/עילאי האמיתי חן/lessons.xml";
        String path;
        if (pickedFile){
            path = netFilesUploadsCount;
        }
        else{
            path = netLessonsUploadsCount;
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
        } catch (IOException e) {
            e.printStackTrace();
            //Error(e.toString());
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //add file path to PublicLessons file to upload to internet sks
    public void publicLessonsFile(String filePath)
    {
        String user = getString(R.string.user) + ":" + getString(R.string.pass);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

        //String path = "smb://192.168.1.25/student/19 מחזור כ''ג/עילאי האמיתי חן/lessons.xml";
        String path = savePublicLessonsFile;
        SmbFile sFile = null;
        try {
            sFile = new SmbFile(path,auth);

            if(!sFile.exists())
            {
                sFile.mkdir();
                updateLog("יצירת קובץ שיעורים שיש לפרסם",path);
            }

            sFile.setConnectTimeout(5000);
            sFile.setReadTimeout(5000);

            SmbFileInputStream in = new SmbFileInputStream(sFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(in));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            in.close();

            //aBuffer += "\n" + filePath + fileName + "," +
                    //filePath.replace("\\\\192.168.1.25\\mp3admin\\ספריית קבצי שמע","") + fileName;

            aBuffer += filePath + "\n";

            sFile = new SmbFile(path,auth);

            SmbFileOutputStream outFile = new SmbFileOutputStream(sFile);

            outFile.write(aBuffer.getBytes());
            outFile.flush();
            outFile.close();
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
        } catch (IOException e) {
            e.printStackTrace();
            //Error(e.toString());
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readFileFromServer(String upLoad) {

        String user = getString(R.string.user) + ":" + getString(R.string.pass);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

        updateLog("קורא קובץ נתונים","");

        //String path = "smb://192.168.1.25/student/19 מחזור כ''ג/עילאי האמיתי חן/lessons.xml";
        String path;
        SmbFile sFile = null;
        try {
            sFile = new SmbFile(netDataPath,auth);

            sFile.setConnectTimeout(5000);
            sFile.setReadTimeout(5000);

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
            out = this.openFileOutput(DataPath, Context.MODE_PRIVATE);
            out.write(aBuffer.getBytes());
            out.flush();
            out.close();

            path = getFilePath(ARTIST,ALBUM);

            sFile.setConnectTimeout(5000);
            sFile.setReadTimeout(5000);

            sFile = new SmbFile(path,auth);

            sFile.setConnectTimeout(5000);
            sFile.setReadTimeout(5000);

            if(!sFile.exists())
            {
                //sFile.mkdir();
                Error("לא קיים מיקום");
            }

            fileList = sFile.listFiles();

            return upLoad;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Error(e.toString());
            return "err";
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //      Toast.LENGTH_SHORT).show();
        }catch (SmbException e) {
            e.printStackTrace();
            Error(e.toString());
            return "err";
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Error(e.toString());
            return "err";
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            return "err";
        }
        //Toast.makeText(getBaseContext(), "done!!",
        //      Toast.LENGTH_SHORT).show();
    }

    public class readFromServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return readFileFromServer(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            if(result.equals("err")) {
                offLine();
                Toast.makeText(BackgroundService.this, "תקלה בהעלאה! הקובץ לא עלה בשלמות, או לא עלה כלל. אנא בדוק את החיבור לרשת, או פנה לא. סק''ש", Toast.LENGTH_LONG).show();
                //Intent i = new Intent("com.Flower.ProgressReceiver");
                Intent i = new Intent("Finish");
                i.putExtra("action",1);
                sendBroadcast(i);
                stopSelf();
            }
            else if(result.equals("true")) {
                upLoadDataFile();
            }
            //Intent saveRecord = new Intent(getBaseContext(), LessonsList.class);
            //startActivity(saveRecord);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public void offLine()
    {
        Toast.makeText(this,"שגיאה, אין חיבור", Toast.LENGTH_SHORT).show();
    }

    public String writeFileToServer() {

        //String [] text = getFavoritesNames();
        String user = getString(R.string.user) + ":" + getString(R.string.pass);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);
        String newName = fileName;

//        if(fileName.contains("#"))
//            fileName = fileName.replace("#","");

        SmbFile sFile = null;
        String path;
        try {
//            updateLog("מעדכן קובץ נתונים",path);
//            sFile = new SmbFile(path,auth);
//            sFile.setConnectTimeout(5000);
//            SmbFileOutputStream out = new SmbFileOutputStream(sFile);
//            out.write(data.getBytes());
//            out.flush();
            FileInputStream inputStream;
            File myFile;
            long size;
            if (!pickedFile) {
                path = getFilePath(ARTIST,ALBUM) + newName;
                updateLog("מתחיל להעלות קובץ שמע",path);
                myFile = new File(AudioSavePath + fileName);
                inputStream = new FileInputStream(myFile);
                size = myFile.length();
            }
            else {

//                path = uploadFile;
//                myFile = new File(fileName);
                inputStream = (FileInputStream) getContentResolver().openInputStream(uploadFile);

//                Cursor returnCursor =
//                        getContentResolver().query(uploadFile, null, null, null, null);
    /*
     * Get the column indexes of the data in the Cursor,
     * move to the first row in the Cursor, get the data,
     * and display it.
     */
//                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
//                returnCursor.moveToFirst();
//                name = returnCursor.getString(nameIndex);
//                size = returnCursor.getLong(sizeIndex);
                size = uploadSize;
                path = uploadPath;

//                FileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(uploadFile, "r").getFileDescriptor();
//                fileDescriptor
//                returnCursor.close();
            }

            SmbFileOutputStream out;

            sFile = new SmbFile(path,auth);
            sFile.setConnectTimeout(5000);
            sFile.setReadTimeout(5000);



            out = new SmbFileOutputStream(sFile);
            int totalSize = (int) size;
            byte[] buffer = new byte[totalSize/100];
            double downloadedSize = 0;
            int bufferLength = 0;
            while ( (bufferLength = inputStream.read(buffer)) > 0 )
            {
                out.write(buffer, 0, bufferLength);
                //Intent i = new Intent("com.Flower.ProgressReceiver");
                Intent i = new Intent("Finish");
                i.putExtra("action",2);
                i.putExtra("percent",((double)downloadedSize/(double)totalSize)*100);
                sendBroadcast(i);
                downloadedSize += bufferLength;
                Log.i("Progress:","downloadedSize:"+downloadedSize+"totalSize:"+ totalSize);
            }
            out.flush();
            out.close();
            inputStream.close();

            if (!pickedFile) {
                updateLog("העלאת הקובץ הסתיימה", path);

                if (isFilePublic(ARTIST, ALBUM))
                    publicLessonsFile(path);
            }
            updateCounter();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Error(e.toString());
            return "err";
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //      Toast.LENGTH_SHORT).show();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Error(e.toString());
            return "err";
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (SmbException e) {
            e.printStackTrace();
            Error(e.toString());
            return "err";
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Error(e.toString());
            return "err";
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            return "err";
        }
        //Toast.makeText(getBaseContext(), "done!!",
        //      Toast.LENGTH_SHORT).show();
        return "OK";
    }
    public void updateLog(String action, String filePath)
    {
        String user = getString(R.string.user) + ":" + getString(R.string.pass);
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(user);

        //String path = "smb://192.168.1.25/student/19 מחזור כ''ג/עילאי האמיתי חן/lessons.xml";
        String path = netLogPath;
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

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:MM:SS");

            aBuffer += "\n" + sdf.format(new Date()) + " - " + action + ": " + fileName + ", אל התקיה: " + filePath;

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
        } catch (IOException e) {
            e.printStackTrace();
            //Error(e.toString());
            //Toast.makeText(getBaseContext(), e.getMessage(),
            //Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Error(String e)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("SKS_CHANNEL", "רשת וסק\"ש", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "SKS_CHANNEL")
                .setContentTitle("שגיאה!")
                .setContentText("שים לב! הקובץ לא עלה כראוי, או לא עלה כלל!")
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true);
                NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId++, mBuilder.build());

//        Intent i = new Intent("com.Flower.ProgressReceiver");
//        i.putExtra("action",1);
//        sendBroadcast(i);
//        stopSelf();
        //updateLog(e,"");
        //Toast.makeText(getApplicationContext(), "אירעה תקלה, אנא בדוק שהקובץ עלה כראוי/פנה לאחראי סק''ש", Toast.LENGTH_LONG).show();
    }

    //upload the lesson, and update necessary files
    class writeToServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            return writeFileToServer();
            //return readFile();
        }

        @Override
        protected void onPostExecute(String result) {

            if (result.equals("OK")) {
                if (!pickedFile) {
                    File myFile = new File(AudioSavePath + fileName);
                    fileName = TRACK + " " + ARTIST + " - " + "~" + ALBUM + " - " + TITLE + " - " + COMMENT + ".mp3";
                    File newName = new File(AudioSavePath, fileName);
                    myFile.renameTo(newName);
                    scanFile(newName);

                    String p = getFilePath(ARTIST, ALBUM).replace("smb://192.168.1.25/mp3admin/ספריית קבצי שמע", "mp3");
                    Intent intent = new Intent(getApplicationContext(), SksActivity.class);
                    intent.putExtra("rootPath", p);
                    intent.putExtra("rollList", p);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("SKS_CHANNEL", "רשת וסק\"ש", NotificationManager.IMPORTANCE_DEFAULT);
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(BackgroundService.this, "SKS_CHANNEL")
                            .setContentTitle("השיעור '" + TITLE + "' עלה בהצלחה")
                            .setContentText("אנא לחץ לבדיקה שהוא ברשת")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setAutoCancel(true);
                    NotificationManager notificationManager =
                            (NotificationManager) BackgroundService.this.getSystemService(Context.NOTIFICATION_SERVICE);

                    notificationManager.notify(notificationId++, mBuilder.build());
                }

                if (!pickedFile) Toast.makeText(BackgroundService.this, "הקובץ עלה בהצלחה, תודה!", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(BackgroundService.this, "הקובץ עלה בהצלחה", Toast.LENGTH_LONG).show();
                }
                //Intent i = new Intent("com.Flower.ProgressReceiver");
                Intent i = new Intent("Finish");
                i.putExtra("action",1);
                sendBroadcast(i);
                stopSelf();
            }
            else
            {
                if (!pickedFile) Toast.makeText(BackgroundService.this, "תקלה בהעלאה! הקובץ לא עלה בשלמות, או לא עלה כלל. אנא בדוק את החיבור לרשת, או פנה לא. סק''ש", Toast.LENGTH_LONG).show();
                else Toast.makeText(BackgroundService.this, "תקלה בהעלאה! הקובץ לא עלה בשלמות, או לא עלה כלל. אנא בדוק את החיבור לרשת", Toast.LENGTH_LONG).show();

                //Intent i = new Intent("com.Flower.ProgressReceiver");
                Intent i = new Intent("Finish");
                i.putExtra("action",1);
                sendBroadcast(i);
                stopSelf();
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
