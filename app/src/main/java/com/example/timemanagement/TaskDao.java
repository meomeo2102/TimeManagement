package com.example.timemanagement;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface TaskDao {

    // ===== Thêm, sửa, xóa =====
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

    // ===== Danh sách task theo người dùng =====

    @Query("SELECT * FROM Task WHERE owner = :owner ORDER BY deadlineTimestamp")
    LiveData<List<Task>> getTasksByOwner(String owner);

    @Query("SELECT * FROM Task WHERE owner = :owner AND category = :category ORDER BY createdAt DESC")
    LiveData<List<Task>> getTasksByCategory(String owner, String category);

    @Query("SELECT * FROM Task WHERE owner = :owner AND category = 'Danh sách yêu thích' ORDER BY createdAt DESC")
    LiveData<List<Task>> getFavoriteTasks(String owner);

    @Query("SELECT * FROM Task WHERE owner = :owner AND category = 'Ngày sinh nhật' ORDER BY createdAt DESC")
    LiveData<List<Task>> getBirthdayTasks(String owner);

    @Query("SELECT * FROM Task WHERE owner = :owner AND completed = 1 ORDER BY createdAt DESC")
    LiveData<List<Task>> getCompletedTasks(String owner);

    @Query("SELECT * FROM Task WHERE owner = :owner AND completed = 0 ORDER BY createdAt DESC")
    LiveData<List<Task>> getPendingTasks(String owner);

    @Query("SELECT * FROM Task WHERE owner = :owner AND createdAt BETWEEN :start AND :end ORDER BY createdAt ASC")
    LiveData<List<Task>> getTasksThisWeek(String owner, long start, long end);

    // ===== Thống kê theo người dùng =====

    @Query("SELECT COUNT(*) FROM Task WHERE owner = :owner")
    int getTotalTaskCount(String owner);

    @Query("SELECT COUNT(*) FROM Task WHERE owner = :owner AND completed = 1")
    int getCompletedTaskCount(String owner);

    @Query("SELECT COUNT(*) FROM Task WHERE owner = :owner AND completed = :isDone")
    int countCompletedByOwner(String owner, boolean isDone);

    @Query("SELECT COUNT(*) FROM Task WHERE owner = :owner AND category = :category")
    int getTaskCountByCategory(String owner, String category);

    // ===== (Tuỳ chọn) Dành cho admin hoặc debug =====

    @Query("SELECT * FROM Task ORDER BY createdAt DESC")
    LiveData<List<Task>> getAllTasks();

}