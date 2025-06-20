package com.example.timemanagement;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY timestamp DESC")
    List<Task> getTasksByCategory(String category);

    @Query("SELECT * FROM tasks WHERE category = 'Danh sách yêu thích' ORDER BY timestamp DESC")
    List<Task> getFavoriteTasks();

    @Query("SELECT * FROM tasks WHERE category = 'Ngày sinh nhật' ORDER BY timestamp DESC")
    List<Task> getBirthdayTasks();

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTask(int taskId);

    @Query("UPDATE tasks SET name = :newName WHERE id = :taskId")
    void updateTaskName(int taskId, String newName);

    @Query("UPDATE tasks SET completed = :status WHERE id = :taskId")
    void markCompleted(int taskId, boolean status);
}