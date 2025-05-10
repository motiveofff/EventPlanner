package com.example.eventplanner;

public class Event {
    private int id;
    private String title;
    private String datetime;

    public Event(int id, String title, String datetime) {
        this.id = id;
        this.title = title;
        this.datetime = datetime;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDatetime() { return datetime; }
}
