package com.example.timemanagement;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTask(int taskId);

    @Query("UPDATE tasks SET completed = :completed WHERE id = :taskId")
    void markCompleted(int taskId, boolean completed);

    // ✅ Các truy vấn danh sách phải trả về LiveData
    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY timestamp DESC")
    LiveData<List<Task>> getTasksByCategory(String category);

    @Query("SELECT * FROM tasks WHERE category = 'Danh sách yêu thích' ORDER BY timestamp DESC")
    LiveData<List<Task>> getFavoriteTasks();

    @Query("SELECT * FROM tasks WHERE category = 'Ngày sinh nhật' ORDER BY timestamp DESC")
    LiveData<List<Task>> getBirthdayTasks();

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY timestamp DESC")
    LiveData<List<Task>> getCompletedTasks();

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY timestamp DESC")
    LiveData<List<Task>> getPendingTasks();

    // ✅ Các truy vấn đếm vẫn để nguyên là int
    @Query("SELECT COUNT(*) FROM tasks")
    int getTotalTaskCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1")
    int getCompletedTaskCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE category = :category")
    int getTaskCountByCategory(String category);
}
