package com.example.myapplication.Model;

public class ChatModel {

    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private Long timestamp;

   public ChatModel(String sender, String receiver, String message, boolean isseen, Long timestamp){
       this.sender = sender;
       this.receiver = receiver;
       this.message = message;
       this.isseen = isseen;
       this.timestamp = timestamp;
   }

   public ChatModel() {

   }

    public String getSender() {return sender;}
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver( ){
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen(){ return isseen;}
    public void setIsseen(boolean isseen) {this.isseen = isseen;}

    public Long getTimestamp(){return timestamp;}
    public void setTimestamp(Long timestamp) {this.timestamp = timestamp;}

}
