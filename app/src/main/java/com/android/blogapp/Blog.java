package com.android.blogapp;


public class Blog {

    String desc, imageurl,userUid ;

    public Blog(String desc, String imageurl,String userUid) {
        this.desc = desc;
        this.imageurl = imageurl;
        this.userUid = userUid;
    }

    public Blog() {
        // Needed for Firebase
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }


    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
}
