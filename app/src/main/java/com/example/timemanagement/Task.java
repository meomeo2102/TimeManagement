package com.example.timemanagement;

public class Task {
    private int id;
    private String name;
    private String timestamp; // dạng chuỗi: "yyyy-MM-dd HH:mm:ss"
    private String category;
    private boolean completed;

    // Constructor mặc định (cần cho Retrofit hoặc Firebase)
    public Task() {}

    // Constructor đầy đủ
    public Task(int id, String name, String timestamp, String category, boolean completed) {
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.category = category;
        this.completed = completed;
    }

    // ✅ Constructor rút gọn: name, timestamp, category
    public Task(String name, String timestamp, String category) {
        this.name = name;
        this.timestamp = timestamp;
        this.category = category;
        this.completed = false; // mặc định chưa hoàn thành
    }

    // Getter và Setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
