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

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getTasksByCategory(String category) {
        return taskDao.getTasksByCategory(category);
    }

    public void updateTask(Task task) {
        executor.execute(() -> taskDao.update(task));
    }

    public void deleteTask(Task task) {
        executor.execute(() -> taskDao.delete(task));
    }
}
