package com.example.ahsanz.instagramfirebase;

public class Comment{
    private String cText;
    private String commenterUserName;
    private String commenterDPurl;

    Comment(){
        cText=null;
        commenterDPurl=null;
        commenterUserName=null;
    }

    Comment(String c, String u, String url){
        cText = c;
        commenterUserName = u;
        commenterDPurl = url;
    }

    public String getcText(){return cText;}
    public String getCommenterUserName() {return commenterUserName;}
    public String getCommenterDPurl() {return commenterDPurl;}
}