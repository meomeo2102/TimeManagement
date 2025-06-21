package com.example.timemanagement;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public long timestamp;
    public String category;
    public boolean completed;

    @Ignore
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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public long getTimestamp() { return timestamp; }
    public String getCategory() { return category; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
