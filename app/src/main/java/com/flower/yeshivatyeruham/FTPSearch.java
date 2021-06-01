package com.flower.yeshivatyeruham;//package com.flower.yeshivatyeruham.Backups;


  //Created by ERabinovich on 21/10/2017.


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.Writer;

public class FTPSearch {


       static void listDirectory(FTPClient ftpClient, String parentDir,
                                  String currentDir, int level, Writer writer) throws IOException {

               String dirToList = parentDir;
               if (!currentDir.equals("")) {
                   dirToList += "/" + currentDir;
               }
               FTPFile[] subFiles = ftpClient.listFiles(dirToList);
               if (subFiles != null && subFiles.length > 0) {
                   for (FTPFile aFile : subFiles) {
                       String currentFileName = aFile.getName();
                       if (currentFileName.equals(".")
                               || currentFileName.equals("..")) {
                           // skip parent directory and directory itself
                           continue;
                       }
                      // for (int i = 0; i < level; i++)
                      //     writer.write("/t");

                       if (aFile.isDirectory()) {
                           //System.out.println("[" + currentFileName + "]");
                           writer.write("\n"+dirToList+currentFileName+"/");
                           listDirectory(ftpClient, dirToList, currentFileName, level + 1, writer);
                       } else {
                           writer.write("/n" + dirToList+ currentFileName);
                       }
                   }
               }
            {
               try {
                   if (writer!=null)
                   writer.close();
               } catch (Exception ex) {/*ignore*///}
               }
           }
       }
             }



