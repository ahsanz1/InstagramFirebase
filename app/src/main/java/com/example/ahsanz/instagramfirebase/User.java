package com.example.ahsanz.instagramfirebase;

/**
 * Created by ahsanz on 8/17/17.
 */

public class User {

    public String fullName;
    public String userName;
    public String emailAddress;
    public String bio;
    public String password;
    public String phoneNo;
    public String imageurl;
    public String userID;

    User(){

        userName = null;
        emailAddress = null;
        bio = null;
        password = null;
        phoneNo = null;
        fullName = null;
        imageurl = null;
        userID = null;
    }

    User(String name, String email, String quote, String userPassword, String phone, String fName, String uId){

        userName = name;
        emailAddress = email;
        bio = quote;
        password = userPassword;
        phoneNo = phone;
        fullName = fName;
        imageurl = null;
        userID = uId;
    }

    public void setUserProfilePicUrl(String s){
        imageurl = s;
    }

}
