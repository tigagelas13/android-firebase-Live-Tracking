package com.example.androidrealtimelocation2019.Model;

import java.util.HashMap;

public class User {
    private String uid,username,imageURL,status,search,email;
    private HashMap<String,User> acceptList; // List user friend

    public User() {
    }

    public User(String uid, String username, String imageURL, String status, String search, String email) {
//    public User(String uid,String email) {
        this.uid = uid;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.email = email;
        acceptList = new HashMap<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) { this.username = username; }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public HashMap<String,User> getAcceptList() {
        return acceptList;
    }

    public void setAcceptList(HashMap<String,User> acceptList) {
        this.acceptList = acceptList;
    }
}
