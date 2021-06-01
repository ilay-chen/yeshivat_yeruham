package com.flower.yeshivatyeruham;

/**
 * Created by ilayc on 23 יוני 2017.
 */


/**
 * Object contains the user data
  */

public class AppUser {
    private String name;
    private String pass;
    private int classification;
    private int className;

    public AppUser()
    {
        
    }

    public AppUser(String name, String pass, int classification, int className)
    {
        this.name = name;
        this.pass = pass;
        this.classification = classification;
        this.className = className;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getClassification() {
        return classification;
    }

    public void setClassification(int classification) {
        this.classification = classification;
    }

    public int getClassName() {
        return className;
    }

    public void setClassName(int className) {
        this.className = className;
    }
}
