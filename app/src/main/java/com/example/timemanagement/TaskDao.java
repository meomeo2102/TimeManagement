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

    // Thêm, sửa, xóa
    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM Task WHERE id = :taskId")
    void deleteTask(int taskId);

    @Query("UPDATE Task SET completed = :completed WHERE id = :taskId")
    void markCompleted(int taskId, boolean completed);

    // Danh sách task
    @Query("SELECT * FROM Task ORDER BY createdAt DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM Task WHERE category = :category ORDER BY createdAt DESC")
    LiveData<List<Task>> getTasksByCategory(String category);

    @Query("SELECT * FROM Task WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt ASC")
    LiveData<List<Task>> getTasksThisWeek(long start, long end);

    @Query("SELECT * FROM Task WHERE category = 'Danh sách yêu thích' ORDER BY createdAt DESC")
    LiveData<List<Task>> getFavoriteTasks();

    @Query("SELECT * FROM Task WHERE category = 'Ngày sinh nhật' ORDER BY createdAt DESC")
    LiveData<List<Task>> getBirthdayTasks();

    @Query("SELECT * FROM Task WHERE completed = 1 ORDER BY createdAt DESC")
    LiveData<List<Task>> getCompletedTasks();
    @Query("SELECT COUNT(*) FROM Task WHERE completed = :status")
    int countCompleted(boolean status);

    @Query("SELECT * FROM Task WHERE completed = 0 ORDER BY createdAt DESC")
    LiveData<List<Task>> getPendingTasks();

    // Thống kê
    @Query("SELECT COUNT(*) FROM Task")
    int getTotalTaskCount();

    @Query("SELECT COUNT(*) FROM Task WHERE completed = 1")
    int getCompletedTaskCount();

    @Query("SELECT COUNT(*) FROM Task WHERE category = :category")
    int getTaskCountByCategory(String category);
}