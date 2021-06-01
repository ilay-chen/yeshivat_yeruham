package com.flower.yeshivatyeruham.Backups;
/*
import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.test.espresso.core.deps.guava.collect.Sets;
import android.support.v7.preference.PreferenceManager;

import com.flower.yeshivatyeruham.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.flower.yeshivatyeruham.AttendanceListDialog.gimatria;
import static com.flower.yeshivatyeruham.DataClass.readFileFromFTPServer;

/
 * This backup includes and AR object that can perform when the names of the attendees in yeshiva
 * do not 100% correspond to the names in the contacts data base
 * due to long performance tie decided to make a seperate program to change the names in the attendees
 * list to correspond to the names in the contacts DB
 * Created by dell on 24 דצמבר 2017.


public class AttendanceResponsee {

    private LinkedList<Student> studentLS;
    List notGuests;
    private  Activity context;
    private Boolean showPics;

    private String ContactPath;
    private String netFTPAppDataPath;

    //private int posChangeNotHere;
    // private LinkedList<String> people;
    // private LinkedList<String[]>arrNumYear;


    //made so that you always need to vreate an object containing all the information
    // adn  from it you cna create object containing information only about a specific year
    // for saving time, and not having to run the code multiple times if choosing different years
    public AttendanceResponsee(Activity context) {
        this.context=context;
        this.notGuests=readCurrentInYeshiva(context);
        //posChangeNotHere=0;
        //people=new LinkedList<>();
        //arrNumYear= new LinkedList<>();

        studentLS=new LinkedList<>();

        //Set<String> set= hm.keySet();
        //HashMap<String, Student> namePhoneStatMap= getNamePhoneStatMap();

        readAttendanceResponse(getNamePhoneStatMap());
        //for(String name: set){
//
//            if(hm.get(name))
//                if(namePhoneStatMap.get(name)!=null)
//                    namePhoneStatMap.get(name).setIsHere(true);
//
//                else
//                    namePhoneStatMap.put(name, new Student(name, null, "אורח", true) );
//
//
//            else
//                if (namePhoneStatMap.get(name) != null)
//                    namePhoneStatMap.get(name).setIsHere(false);
//
//                else
//                    namePhoneStatMap.put(name, new Student(name, null, "אורח", true) );
//
//
//            studentLS.add(namePhoneStatMap.get(name));
//        }

        Collections.sort(studentLS);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        showPics = sharedPreferences.getBoolean("show_pics",
                context.getResources().getBoolean(R.bool.show_pics_default));

    }

    public AttendanceResponsee(AttendanceResponsee ar, String year){
        context=ar.context;
        showPics=ar.showPics;
        studentLS=new LinkedList<>();

        List<String> groups=ar.getGroups();

        if(year.equals("שיעור ו+")){
            int i=gimatria(groups.get(0));
            for(Student st: ar.studentLS){
                if (i-gimatria(st.getYear())>=5)
                    studentLS.add(st);
            }}
        else
            for(Student st: ar.studentLS){
                if(st.getYear().equals(year))
                    studentLS.add(st);
            }
        Collections.sort(studentLS);
        //  people=new LinkedList<>();
        //  arrNumYear= new LinkedList<>();
        //posChangeNotHere=0;
        //for(int i=0; i<ar.people.size(); i++){
        //    if(year.equals(ar.getStat(i))) {
        //         people.add(ar.people.get(i));
        //        arrNumYear.add(ar.arrNumYear.get(i));
        //        if(i<ar.posChangeNotHere)
        //            posChangeNotHere++;

        //     }

        //   }
    }


    private  HashMap<String, Student> getNamePhoneStatMap( )  {
        HashMap<String, Student> map=new HashMap<>();

        //HashMap<String, String[]> map=new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput(ContactPath), "UTF-8"));


            //InputStream is = this.getResources().openRawResource(R.raw.contacts_database);

            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();

            while (mLine != null) {
                // process line_divider
                if (!mLine.equals("") && !mLine.contains("#")) {

                    String[] line = mLine.split(",");

                    //map.put(line[0], new String[]{line[1], line[2]});
                    map.put(line[0], new Student(line[0], line[1], line[2], null ) );
                }
                mLine = reader.readLine();

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    private boolean findStudentStat(String name){//TODO ADD SHTOOSELS CONDITIONS FOR MORE ACCURATE RESULTS
        {//cheking if student A and B are the same person
            int count = 0;
            String[] s = name.split(" ");
            Iterator<String> iter = notGuests.iterator();
            while (iter.hasNext()) {
                String[] v = iter.next().split(" ");
                for (int a = 0; a <= s.length - 1; a++) {
                    for (int b = 0; b <= v.length - 1; b++) {//the levenshtein returns the amount of changes between two names, so yonathan and yonatan will count as the same person (נועם נעם)
                        if (levenshteinDistance(s[a], (v[b])) < 2)
                            count++;
                    }
                }
                if (count > 1) {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }
    }

    public void readAttendanceResponse(HashMap<String, Student> namePhoneStatMap){

        try{
            readFileFromFTPServer(context.getString(R.string.attendance_path), netFTPAppDataPath + context.getString(R.string.attendance_path),
                    true, context);
            FileInputStream fis= context.openFileInput(context.getString(R.string.attendance_path));
            BufferedReader br= new BufferedReader(new InputStreamReader(fis, "UTF-8"));



            List<String> ans=new ArrayList<>();
            String rl=br.readLine();
            while(rl!=null){
                try {
                    ans.add(rl);
                    rl=br.readLine();
                } catch (IOException e) {}
            }

            HashSet<String> hashSet=new HashSet<>();
            String[] info;//i use it for splitting the read info
            for(int i=ans.size()-1; i>=0; i--){//splits all the read strings to get i nfo and adds them to the map
                info=ans.get(i).split(",");
                try{
                    Student st=namePhoneStatMap.get(info[0])!=null?namePhoneStatMap.get(info[0]):new Student(info[0], null, "אורח", false);
                    if(!findStudentStat(info[0]))
                        st.setYear("אורח");


                    if(hashSet.add(info[0])){
                        st.setIsHere(info[1].contains("כן"));


                        studentLS.add(st);
                    }
                }
                catch( java.lang.ArrayIndexOutOfBoundsException e){
                }
            }

            Iterator<String> iter = notGuests.iterator();
            Student stu;
            while (iter.hasNext()) {
                Set<Set<java.lang.String>> fullSet=Sets.powerSet(Sets.newHashSet(iter.next().split(" ")));
                for (Set<java.lang.String> set: fullSet ) {
                    StringBuilder b=new StringBuilder();
                    for (String st: set)
                        b.append(st+" ");

                    stu=set.size()>1?namePhoneStatMap.get(b.deleteCharAt(b.length()-1)):null;
                    if(stu!=null){
                        studentLS.add(stu);
                        break;
                    }

                }
            }
        }
        catch (java.io.IOException ioe){
            ioe.printStackTrace();
        }

    }

    public static List<String> readCurrentInYeshiva(Activity context){
        List<String> notGuest=new LinkedList<>();

        try {

            FileInputStream fis = context.openFileInput(context.getString(R.string.localStudentsFN));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

            String rl=br.readLine();
            while(rl!=null){
                try {
                    if(!rl.isEmpty())
                        notGuest.add(rl);
                    rl=br.readLine();

                } catch (IOException e) {}
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //todo read from ftp the list of people that are here in yeshiva
        return notGuest;
    }

    public Activity getContext(){
        return context;
    }

    public String getNum(int pos){
        return studentLS.get(pos).getNum();
    }

    // this methods take O(n), to reduce complexity can try using iterator to mark where in list
    // are the people that didn't respond (and if needed add also the people that did't respond)
    public Boolean isHere(int pos){

        return studentLS.get(pos).getHere();

    }

    public String getName(int pos){
        return studentLS.get(pos).getName();
    }

    public int getItemCount(){
        return studentLS.size();
    }

    public Boolean getShowPics() {
        return showPics;
    }

    public String getStat(int pos){
        return studentLS.get(pos).getYear();
    }

    public List<String> getGroups(){
        HashSet<String> hs=new HashSet<>();

        for (Student st: studentLS)
            hs.add(st.getYear());

        ArrayList arrayList=new ArrayList<>(hs);
        Collections.sort(arrayList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if(o1.equals(o2))
                    return 0;
                if(o1.equals("אורח"))
                    return  1;
                if(o2.equals("אורח"))
                    return -1;
                else if(gimatria(o1)>gimatria(o2))
                    return -1;
                else return 1;
            }
        });
        return arrayList;
    }



    private class Student implements Comparable<Student>   {


        String name, num, year;
        Boolean isHere;



        private Student (String name, String num, String year, Boolean isHere){
            this.name=name;
            if(num!=null)
                this.num=num;
            else
                this.num="00000000";
            this.year=year;
            this.isHere=isHere;
        }

        public String getNum() {
            return num;
        }

        public String getName(){
            return name;
        }

        public Boolean getHere(){
            return isHere;
        }

        public String getYear(){
            return year;
        }

        public void setIsHere(Boolean isHere){
            this.isHere=isHere;
        }

        public void setYear(String year){
            this.year=year;
        }

        @Override
        public int compareTo(@NonNull Student st) {
            if(name.equals(st.name))
                return 0;

            if(isHere==null) {
                if (st.getHere() == null)
                    if (name.compareTo(st.name) > 0)
                        return -1;
                    else
                        return 1;
                else
                    return 1;
            }
            else if(st.getHere()==null)
                return -1;


            if(isHere && !st.getHere() || (isHere==st.getHere() && name.compareTo(st.name)<0) )
                return -1;
            else
                return 1;
        }

        @Override
        public boolean equals(Object o) {
            return((o instanceof Student) && (name.equals(((Student) o).getName() )));

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }




    public static int levenshteinDistance (CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }


}

*/