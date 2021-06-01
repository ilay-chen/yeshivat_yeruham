package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import static com.flower.yeshivatyeruham.AttendanceListDialog.active;

/**
 * Created by dell on 26 אוקטובר 2017.
 * AsyncTask for writing the response in FTP server
 */

public class ResponseWriter extends AsyncTask {
    Context context;

    Attendance attendance;

    public ResponseWriter(Context context){
        this.context=context;

    }

/**
 * gets an Attendance Object and uses it's method
 * to perform and upload of users response to ftp server
  */
@Override
protected Boolean doInBackground(Object[] params) {


    attendance=(Attendance)params[0];
    return attendance.writeResponse();
}

    @Override
    protected void  onPostExecute(Object o) {
        if((Boolean)o) {
            Toast.makeText(context, attendance.getName() + ", " + "התגובה שלך התקבלה במערכת", Toast.LENGTH_SHORT).show();
            //intent is snet to the attendance list if it's running
            if(active) {
                Intent intent = new Intent(context, AttendanceListDialog.class);
                intent.putExtra("name", attendance.getName());
                intent.putExtra("isComing", attendance.getResponse().contains("כן"));
                context.startActivity(intent);
            }
        }
        else         Toast.makeText(context, "אופס!! משהו השתבש, נסה שוב", Toast.LENGTH_SHORT).show();

    }
}
