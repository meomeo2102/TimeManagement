package com.example.timemanagement;

import com.google.gson.annotations.SerializedName;

public class Task {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("category")
    private String category;

    @SerializedName("completed")
    private boolean completed;

    // Constructors
    public Task() {}

    public Task(String name, long timestamp, String category) {
        this.name = name;
        this.timestamp = timestamp;
        this.category = category;
        this.completed = false;
    }

    public Task(String name, long timestamp, String category, boolean completed) {
        this.name = name;
        this.timestamp = timestamp;
        this.category = category;
        this.completed = completed;
    }

    public Task(int id, String name, long timestamp, String category, boolean completed) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.category = category;
        this.completed = completed;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCategory() {
        return category;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
