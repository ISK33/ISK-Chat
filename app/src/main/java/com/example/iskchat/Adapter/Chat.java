package com.example.iskchat.Adapter;

public class Chat {

    private String  sender, reciver,message;
    private boolean seen;
    private String time;

    public Chat( String sender, String reciver,String message,boolean seen,String time) {
        this.message = message;
        this.sender = sender;
        this.reciver = reciver;
        this.seen = seen;
        this.time=time;
    }

    public Chat() {

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciver() {
        return reciver;
    }

    public void setReciver(String reciver) {
        this.reciver = reciver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}