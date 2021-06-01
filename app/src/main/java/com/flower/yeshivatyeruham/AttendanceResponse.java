package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.flower.yeshivatyeruham.AttendanceListDialog.gimatria;
import static com.flower.yeshivatyeruham.DataClass.ContactPath;

/**
 * Created by dell on 24 דצמבר 2017.
 */

public class AttendanceResponse {

    private LinkedList<Student> studentLS;
    private  Activity context;
    private Boolean showPics;

     final int didntRespondInYehsiva=5;
     final int notHereNotInYeshiva=1;
     final int notHereInYeshiva=2;
     final int HereNotInYeshiva=3;
     final int HereInYeshiva=4;




    //private int posChangeNotHere;
    // private LinkedList<String> people;
    // private LinkedList<String[]>arrNumYear;


    //made so that you always need to vreate an object containing all the information
    // adn  from it you cna create object containing information only about a specific year
    // for saving time, and not having to run the code multiple times if choosing different years
    public AttendanceResponse(Activity context) {
        this.context=context;
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

    AttendanceResponse(AttendanceResponse ar, String year){
        context=ar.context;
        showPics=ar.showPics;
        studentLS=new LinkedList<>();

        if(year.equals("שיעור ו+")){
            int i=gimatria(ar.getGroups().get(0));
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
                    line[2]=line[2].equals("צוות")?"אורח":line[2];

                    //map.put(line[0], new String[]{line[1], line[2]});
                    map.put(line[0], new Student(line[0], line[1], line[2], notHereNotInYeshiva ) );
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

    public void readAttendanceResponse(HashMap<String, Student> namePhoneStatMap){

        try {
            FileInputStream fileInputStream = context.openFileInput(context.getString(R.string.localStudentsFN));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));

            Student st;
            ArrayList<Student> notGuest=new ArrayList<>();
            String s=reader.readLine();
            while(s!=null){
                try {
                    if(!s.isEmpty()) {
                        st=namePhoneStatMap.get(s);
                        if(st==null) {
                            st=new Student(s, null, "אורח", didntRespondInYehsiva);
                            namePhoneStatMap.put(s, st);
                        }
                        else
                                st.setIsHere(didntRespondInYehsiva);

                        notGuest.add(st);
                    }
                    s=reader.readLine();

                } catch (IOException e) {}
            }

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
            for(int i=ans.size()-1; i>=0; i--){//splits all the read strings to get info and adds them to the map
                info=ans.get(i).split(",");
                try{
                     st=namePhoneStatMap.get(info[0])!=null?
                             namePhoneStatMap.get(info[0]):
                             new Student(info[0], null, "אורח", notHereNotInYeshiva);

                        if(hashSet.add(info[0])){
                            if(st.getHere()==notHereNotInYeshiva)
                                st.setIsHere(info[1].contains("כן")?HereNotInYeshiva:notHereNotInYeshiva);
                            else
                                st.setIsHere(info[1].contains("כן")?HereInYeshiva:notHereInYeshiva);

                        studentLS.add(st);
                    }
                }
                catch( java.lang.ArrayIndexOutOfBoundsException e){
                }
            }
            for(Student a: studentLS)
                for (int i=0; i<notGuest.size(); i++)
                    if (a.equals(notGuest.get(i)))
                        notGuest.remove(i);
            studentLS.addAll(notGuest);

        }
        catch (java.io.IOException ioe){
            ioe.printStackTrace();
        }

    }

    public Activity getContext(){
        return context;
    }

    public String getNum(int pos){
        return studentLS.get(pos).getNum();
    }

//this method return boolean and not int, might want to change to int
   public Boolean isHere(int pos){
        int here=studentLS.get(pos).getHere();
        if(here==didntRespondInYehsiva)
            return null;
        return (here==HereInYeshiva|| here==HereNotInYeshiva);

    }

    public String getName(int pos){
        return studentLS.get(pos).getName();
    }

    public void setIsHere(int pos, boolean isComing){
        int isHereStat=studentLS.get(pos).isHere;
            if(isComing) {
                if (isHereStat == notHereNotInYeshiva)
                    studentLS.get(pos).setIsHere(HereNotInYeshiva);
                else if (isHereStat == notHereInYeshiva)
                    studentLS.get(pos).setIsHere(HereInYeshiva);
                else if (isHereStat == didntRespondInYehsiva)
                    studentLS.get(pos).setIsHere(HereInYeshiva);
            }
                else{
                    if (isHereStat == HereNotInYeshiva)
                        studentLS.get(pos).setIsHere(notHereNotInYeshiva);
                    else if (isHereStat == HereInYeshiva)
                        studentLS.get(pos).setIsHere(notHereInYeshiva);
                    else if (isHereStat == didntRespondInYehsiva)
                        studentLS.get(pos).setIsHere(notHereInYeshiva);
            }
    }

    /**
     * get a name and returns the pos of that name in the array.
     * in case that name is not found returns -1
     * @param name -the name to be searched
     * @return the pos in the list or -1 if not found
     */
    public int getPosByName(String name){
        ListIterator iterator= studentLS.listIterator();
        String temp;
        int index;
        while(iterator.hasNext()){
            index=iterator.nextIndex();
            temp=((Student)(iterator.next())).getName();
            if(name.equals(temp))
                return index;
        }
        return -1;

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

    public List<String> getPhNumsDidntRespond(String group){
        List<String> phNums=new ArrayList();
        List<Student> students=new LinkedList<>();

        //to get Shiur VavPlus
        if(group.equals("שיעור ו+")){
            int i=gimatria(getGroups().get(0));
            for(Student st: studentLS){
                if (i-gimatria(st.getYear())>=5)
                    students.add(st);
            }}
        else
            students=studentLS;

        for(Student st:students)
            if(st.getHere()==didntRespondInYehsiva && (st.getYear().equals(group)|| group.equals("שיעור ו+"))){
                if(!st.getNum().equals("00000000"))
                    phNums.add(st.getNum());
            }
        return phNums;

    }

    public int[] countResponse(){
        int[] response= new int[]{0, 0, 0};
        for(Student st: studentLS){
            if(st.isHere())
                response[0]++;
            else if(st.getHere()==didntRespondInYehsiva)
                response[2]++;
            else
                response[1]++;
        }
        return response;
    }




    private class Student implements Comparable<Student>   {


        String name, num, year;
        int isHere;


        private Student (String name, String num, String year, int isHereShabbat ){
            this.name=name;
            if(num!=null)
                this.num=num;
            else
                this.num="00000000";
            this.year=year;
            this.isHere=isHereShabbat;
        }


        public String getNum() {
            return num;
        }

        public String getName(){
            return name;
        }

        public int getHere(){
            return isHere;
        }
        public boolean isHere(){
            return (isHere==HereInYeshiva|| isHere==HereNotInYeshiva);

        }

        public String getYear(){
            return year;
        }

        public void setIsHere(int isHere){
            this.isHere=isHere;
        }

        public void setYear(String year){
            this.year=year;
        }

        @Override
        public int compareTo(@NonNull Student st) {
            if(name.equals(st.name))
                return 0;

           if(isHere==didntRespondInYehsiva) {
               if (st.getHere() == didntRespondInYehsiva)
                   if (name.compareTo(st.name) > 0)
                       return 1;
                   else
                       return -1;
               else
                   return -1;
           }
           else if(st.getHere()==didntRespondInYehsiva)
               return 1;


            if(isHere() && !st.isHere() || (isHere()==st.isHere() && name.compareTo(st.name)<0) )
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

