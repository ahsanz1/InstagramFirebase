package com.example.ahsanz.instagramfirebase;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class Photo {

    public String photoURL;
    public String Caption;
    public int totalLikes;
    public String photoKey;
    public ArrayList<String> listOfPeopleWhoLikeThis;
    public String ownerID;

    Photo(){
        photoURL = null;
        Caption = null;
        totalLikes = 0;
        photoKey = null;
        ownerID = null;
        listOfPeopleWhoLikeThis = new ArrayList<>();
    }

    Photo(String url, String caption, int likes, String key, String id){
        photoURL = url;
        Caption = caption;
        totalLikes = likes;
        photoKey = key;
        ownerID = id;
        listOfPeopleWhoLikeThis = new ArrayList<>();
    }


}
