package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class RecordsFragment extends Fragment {


    int top = 0;
    int index = 0;
    ListView data;
    TextView empty;
    View rootView;
    CustomRecordsList adapter;
    String AudioSavePath = DataClass.AudioSavePath;
    FloatingActionButton addRecord;
    List<String> rootNames = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public RecordsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
        data.setSelectionFromTop(index, top);
    }

    @Override
    public void onPause() {
        super.onPause();
        index = data.getFirstVisiblePosition();
        View v = data.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - data.getPaddingTop());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_records, container, false);
        data = (ListView)rootView.findViewById(R.id.lesson_list);
        empty = (TextView) rootView.findViewById(R.id.empty);

        updateData();

        data.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                    Intent saveRecord = new Intent(getContext(), UploadDialog.class);
                    saveRecord.putExtra("tempName",rootNames.get(position));
                    saveRecord.putExtra("upload", 0);
                    startActivity(saveRecord);

            }

        });

//        data.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
//                Intent saveRecord = new Intent(getContext(), UploadDialog.class);
//                saveRecord.putExtra("tempName",rootNames.get(position));
//                startActivity(saveRecord);
//                return false;
//            }
//        });

        addRecord = (FloatingActionButton)rootView.findViewById(R.id.add_record_btn);

        addRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(checkFileExist()) {
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder(getActivity());
                    alertDialog
                            .setMessage("למען בטיחות ההקלטה, אנא וודא שהינך במצב טיסה. תודה!")
                            .setPositiveButton("אחלה, אינני הטייס!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent saveRecord = new Intent(getContext(), RecordingActivity.class);
                                    startActivity(saveRecord);
                                }
                            })
                            .show();
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateData()
    {
        List<String> allRecords = getAllRecordsName();
        if(allRecords.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            data.setVisibility(View.GONE);
        }
        else {
            empty.setVisibility(View.GONE);
            data.setVisibility(View.VISIBLE);

            adapter = new CustomRecordsList(getActivity(), getAllRecordsName(), data);
            data.setAdapter(adapter);
        }
    }

    public Boolean checkFileExist()
    {
        File f = getContext().getFileStreamPath(DataClass.DataPath);
        if(f == null || !f.exists()){
            Toast.makeText(getContext(), "חסרים קבצים, אנא פתח את האפליקציה כאשר המכשיר מחובר לרשת", Toast.LENGTH_LONG).show(); return false;}
        return true;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public List<String> getAllRecordsName() {
        String path = AudioSavePath;

//        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files==null)
            files = new File[0];

        Arrays.sort(files, new Comparator<File>(){
            public int compare(File f2, File f1)
            {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            } });
        List<String> filesNames = new ArrayList<>();
//        Log.d("Files", "Size: "+ files.length);
        int j = 0;
        for (int i = 0; i < files.length; i++)
        {
            if(files[i].getName().contains(".mp3")) {
                rootNames.add(j, files[i].getName());

                if (rootNames.get(j).contains(" - ")) {
                    String []parts = rootNames.get(j).split(" - ");
                    //get album
                    String lesson = parts[1];
                    //get name
                    String name = "";
                    for(int k = 2; k < parts.length-1; k++)
                        name += parts[k] + " - ";

                    //get date
                    String date = parts[parts.length-1];
                    if(date.split(" ").length>1)
                        date = date.split(" ")[0] + " " + date.split(" ")[1];
                    else date = parts[parts.length-1];

                    //add this lesson to the list
                    filesNames.add(j, lesson + " - " + name + date);
//                    filesNames[j] = rootNames[j].split(" - ")[1] + " - " +
//                            rootNames[j].split(" - ")[2] + " - " + rootNames[j].split(" - ")[3];
                } else
                    filesNames.add(j, rootNames.get(j));

                filesNames.set(j, filesNames.get(j).replace(".mp3", ""));
//                Log.d("Files", "FileName:" + files[i].getName());
                j++;
            }
        }
        return filesNames;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
