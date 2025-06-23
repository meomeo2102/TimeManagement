package com.example.timemanagement;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskViewModel extends AndroidViewModel {
    private final TaskDao taskDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        taskDao = TaskDatabase.getInstance(application).taskDao();
    }

    // ===== Truy vấn theo người dùng =====

    public LiveData<List<Task>> getTasksByOwner(String owner) {
        return taskDao.getTasksByOwner(owner);
    }

    public LiveData<List<Task>> getTasksByCategory(String owner, String category) {
        return taskDao.getTasksByCategory(owner, category);
    }

    public LiveData<List<Task>> getFavoriteTasks(String owner) {
        return taskDao.getFavoriteTasks(owner);
    }

    public LiveData<List<Task>> getBirthdayTasks(String owner) {
        return taskDao.getBirthdayTasks(owner);
    }

    public LiveData<List<Task>> getCompletedTasks(String owner) {
        return taskDao.getCompletedTasks(owner);
    }

    public LiveData<List<Task>> getPendingTasks(String owner) {
        return taskDao.getPendingTasks(owner);
    }

    public LiveData<List<Task>> getTasksThisWeek(String owner, long start, long end) {
        return taskDao.getTasksThisWeek(owner, start, end);
    }


    // ===== Truy vấn tổng quát (tuỳ chọn) =====

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    // ===== Cập nhật / Xoá =====

    public void updateTask(Task task) {
        executor.execute(() -> taskDao.update(task));
    }

    public void deleteTask(Task task) {
        executor.execute(() -> taskDao.delete(task));
    }
}