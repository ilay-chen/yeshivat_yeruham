package com.flower.yeshivatyeruham;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static com.flower.yeshivatyeruham.Attendance.connectToFTPServer;
import static com.flower.yeshivatyeruham.DataClass.AppSavePath;
import static com.flower.yeshivatyeruham.FTPSearch.listDirectory;

public class connectToFTP extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] objects) {
        FTPClient ftpClient = connectToFTPServer();
        String dirToList = "";
        Writer writer;




            try {
                File mFile=new File(AppSavePath+"ftpSearch.txt");
                mFile.createNewFile();
                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(mFile), "utf-8"));

            ftpClient.connect("yhy.co.il");
            int replyCode = ftpClient.getReplyCode();
            FTPReply.isPositiveCompletion(replyCode);


            ftpClient.login("mp3site@yhy.co.il", DataClass.ServerPass);


            listDirectory(ftpClient, dirToList, "", 0, writer);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // logs out and disconnects from server
            try {
                if (ftpClient != null && ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return true;

    }
    }

