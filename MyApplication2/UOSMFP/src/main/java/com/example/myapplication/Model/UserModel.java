package com.example.myapplication.Model;

import com.google.firebase.database.Exclude;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserModel {
    public String id;
    private String username;
    private String name;
    private String surname;
    private String type;
    private String imageurl;
    private String approved;
    private String status;
    private String search;
    private List<String> registrationTokens ;


    public UserModel() {}
    public UserModel(String id, String email, String name, String surname, String type,String approved,String imageurl, String status, String search, List<String> registrationTokens) {
        this.id = id;
        this.username = email;
        this.name = name;
        this.surname = surname;
        this.type = type;
        this.imageurl = imageurl;
        this.approved=approved;
        this.status= status;
        this.search = search;
        this.registrationTokens = registrationTokens;

    }
    public UserModel(String id, String email, String name, String surname, String type,String approved,String imageurl, List<String> registrationTokens) {
        this.id = id;
        this.username = email;
        this.name = name;
        this.surname = surname;
        this.type = type;
        this.imageurl = imageurl;
        this.approved=approved;
        this.status= "offline";
        this.search = name.toLowerCase();
        this.registrationTokens = null;

    }

    public String getId() { return id; }
    public String getUsername() {
        return username;
    }
    public String getName() {
        return name;
    }
    public String getSurname() {return surname;}
    public String getType() {return type;}
    public String getImageURL () {return imageurl;}
    public String getApproved() {return approved;}
    public String getStatus(){return status;}
    public String getSearch(){return search;}
    public List<String> getRegistrationTokens() { return registrationTokens;}


    public void setId(String id) {this.id = id;}
    public void setUsername(String username) {this.username = username;}
    public void setName(String name) {this.name = name;}
    public void setSurname(String surname) {this.surname = surname;}
    public void setimageURL (String imageurl) {this.imageurl = imageurl;}
    public void setType(String type) {this.type = type;}
    public void setApproved(String approved){this.approved=approved;}
    public void setStatus(String status){this.status = status;}
    public void setSearch(String search) {this.search = search;}
    public void setRegistrationTokens(List<String> registrationTokens) {this.registrationTokens = registrationTokens;}

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("username", username);
        result.put("name",name);
        result.put("imageurl",imageurl);
        result.put("type",type);
        result.put("surname",surname);
        result.put("approved",approved);
        result.put("search", name.toLowerCase());
        result.put("registrationTokens",  (Collections.<String>emptyList()));
        result.put("status", false);
        return result;
    }


}
