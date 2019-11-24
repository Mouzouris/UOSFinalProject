package com.example.myapplication.Model;

import java.util.HashMap;
import java.util.Map;

public class MyElevationModel {
    private String date;
    private String user;
    private int status;
    private Long timestamp;

    public MyElevationModel(){}
    public MyElevationModel(String user, String date, int status, Long timestamp) {
        this.date =date;
        this.timestamp =timestamp;
        this.user= user;
        this.status =status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() { return date;  }

    public void setDate(String date) {  this.date = date;  }

    public String getUser() { return user;  }

    public void setUser(String user) {  this.user = user;  }


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("timestamp", timestamp);
        result.put("status", status);
        result.put("user",user);
        result.put("date",date);
        return result;
    }
    @Override
    public String toString() {
        return "Elevation: " +
                "timestamp= " + timestamp +
                ", user= " + user +
                ", date= " + date +
                ", status= " + status;
    }



}
