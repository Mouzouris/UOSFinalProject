package com.example.myapplication.Model;

import java.util.HashMap;
import java.util.Map;

public class RequestIDsModel {
    private String RequestID;
    private Long timestamp;
    private String status;
    private String sender;
    private String receiver;


    public RequestIDsModel(){}
    public RequestIDsModel(String RequestID, Long timestamp,String status, String sender,String receiver){
        this.RequestID = RequestID;
        this.status = status;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("RequestID", RequestID);
        result.put("timestamp", RequestID);
        result.put("status", status);
        result.put("sender", sender);
        result.put("receiver", receiver);

        return result;
    }

    public String toString() {
        return "Request: " +
                "RequestID= " + RequestID +
                "timestamp= " + timestamp +
                "status= " + status +
                "sender= " + sender+
                "receiver= "+ receiver;

    }

        public String getRequestID() {return RequestID; }
    public void setRequestID(String RequestID) {this.RequestID = RequestID;}

    public String getStatus() {return status; }
    public void setStatus(String status) {this.status = status;}

    public Long getTimestamp() {return timestamp; }
    public void setTimestamp(Long timestamp) {this.timestamp = timestamp;}

    public String getSender() {return sender; }
    public void setSender(String sender) {this.sender = sender;}

    public String getReceiver() {return receiver; }
    public void setReceiver(String receiver) {this.receiver = receiver;}


}
