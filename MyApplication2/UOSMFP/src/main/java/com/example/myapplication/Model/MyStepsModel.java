package com.example.myapplication.Model;

import java.util.HashMap;
import java.util.Map;

public class MyStepsModel {
    private String user;
    private int stepsmade;
    private String date;


    public MyStepsModel(){}
    public MyStepsModel(String user, int stepsmade, String date) {
        this.date =date;
        this.user= user;
        this.stepsmade =stepsmade;
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getStepsmade() {
        return stepsmade;
    }

    public void setStepsmade(int stepsmade) {
        this.stepsmade = stepsmade;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("stepsmade", stepsmade);
        result.put("user",user);
        return result;
    }
    @Override
    public String toString() {
        return "Steps: " +
                "date= " + date +
                ", user= " + user +
                ", stepsmade= " + stepsmade;
    }


}
