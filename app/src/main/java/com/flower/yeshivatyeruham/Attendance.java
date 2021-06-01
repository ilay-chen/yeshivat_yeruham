package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;

import static com.flower.yeshivatyeruham.DataClass.AppSavePath;
import static com.flower.yeshivatyeruham.DataClass.ContactPath;
import static com.flower.yeshivatyeruham.DataClass.ServerPass;

/**
 * Created by dell on 25 אוקטובר 2017.
 */

public class Attendance extends AppCompatActivity{
    String name;
    String mGroup;
    String response;
    BufferedReader reader;//should not b used
    String path;
    ArrayList regList=new ArrayList();
    Context context;
    int pos;//the position of the the person on the attendance list



    public  Attendance(Context c, String name, String response) {
    this.name = name;
    this.response = response;
        context=c;
}


    public  boolean writeResponse(){
    //    getListToReg();
    //    String group= searchGroupByName(name);
    //    if(regList.contains(name))
    //        mGroup=group;
    //    else
    //        mGroup="אורחים";
        if(appendFTPTxtFile("attendance.txt", name+","+response)) {
            SharedPreferences sharedPref = context.getSharedPreferences("strings", MODE_PRIVATE);
            if(name==sharedPref.getString("myName", "")){//checks whether you responded to yourself, if yes change the responsed boolean to treu

                SharedPreferences.Editor e = sharedPref.edit();
                e.putBoolean("response", true);
                e.apply();

            }


              return true;
        }
        else return false;

//        if(!uploadFile() ){
//            return false;
//
//        }
//        else
//            return true;


    }

public boolean uploadFile(){
    try{
        FTPClient mFTPClient;
        mFTPClient=connectToFTPServer();
        mFTPClient.enterLocalPassiveMode();
        mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
        String dir =  "מערכת/appdata/Attendance/"+mGroup+"/";
        File mFile=new File(AppSavePath+name+","+response);
        mFile.createNewFile();
        InputStream in=new FileInputStream(mFile);
        mFile.delete();
        mFTPClient.storeFile(dir+"/"+name+","+response, in)  ;
       in.close();
        mFTPClient.logout();
        mFTPClient.disconnect();


        return true;
    } catch(FileNotFoundException e){
        e.printStackTrace();
        return false;
    }
    catch (IOException e) {
        e.printStackTrace();
        return false;
    }
}


    @Nullable
    public String searchGroupByName(String name) {
        try {
            //InputStream is = this.getResources().openRawResource(R.raw.contacts_database);
            FileInputStream is=context.openFileInput(ContactPath);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();
            String groupName;
            while (mLine != null) {
                // process line_divider
                groupName=checkLine(mLine, name);
                if (!mLine.equals("") && !mLine.contains("#") && groupName!=null ) {
                    return groupName;
                }
                mLine = reader.readLine();
            }
            reader.close();
            return null;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
    public String checkLine(String line, String name){
        String[] SplitLine = line.split(",", 4);
        String foundName = SplitLine[0];
        if(foundName.contains(name))
            return SplitLine[2];
        else
            return null;
    }
public boolean getListToReg(){
      // if(readFileFromFTPServer("list_people.txt","מערכת/APPDATA/list_people.txt"))
      //  return false;
    //if (!startReader("list_people.txt"))
        //return false;
    try {

        FileInputStream fis = context.openFileInput("list_people.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));


        // do reading, usually loop until end of file reading

        String mLine = br.readLine();
        while (mLine != null) {
            regList.add(mLine);
            mLine = br.readLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

return false;
}
public boolean startReader(String filePath){
    try {

        FileInputStream is = openFileInput(filePath);
        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        return true;

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }
    return false;
}
     public static FTPClient connectToFTPServer() {
        try {

            FTPClient mFTPClient;
            mFTPClient = new FTPClient();
            mFTPClient.setAutodetectUTF8(true);
            mFTPClient.connect("yhy.co.il");
            mFTPClient.enterLocalPassiveMode();
            mFTPClient.login("mp3site@yhy.co.il", ServerPass);

           return mFTPClient;
            } catch (SocketException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
        }

       public static Boolean appendFTPTxtFile(String fileName, String appendTxt) {

        FTPClient mFTPClient=connectToFTPServer();
           try {
            //   mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);

         //  mFTPClient.enterLocalPassiveMode();
           //mFTPClient.enterLocalActiveMode();
               if (mFTPClient == null) return false;
           mFTPClient.setAutodetectUTF8(true);
           OutputStreamWriter osw= new OutputStreamWriter(mFTPClient.appendFileStream("מערכת/appdata/"+fileName));
           osw.append("\n"+ appendTxt);
               osw.close();
           if (mFTPClient.completePendingCommand())
               return true;

           return false;
           } catch (IOException e) {
               e.printStackTrace();
               return false;
           }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
    public void setmGroup(String group){
        mGroup=group;
    }

}