package com.example.timemanagement;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String category;
    private boolean completed;

    private long createdAt;         // thời điểm tạo task
    private long deadlineTimestamp; // deadline

    public Task(String name, long createdAt, String category, long deadlineTimestamp) {
        this.name = name;
        this.createdAt = createdAt;
        this.category = category;
        this.deadlineTimestamp = deadlineTimestamp;
        this.completed = false;
    }

    // Getter & Setter

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getDeadlineTimestamp() { return deadlineTimestamp; }
    public void setDeadlineTimestamp(long deadlineTimestamp) { this.deadlineTimestamp = deadlineTimestamp; }
}