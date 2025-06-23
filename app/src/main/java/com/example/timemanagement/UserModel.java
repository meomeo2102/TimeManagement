package com.example.timemanagement;

import androidx.room.*;

public class UserModel {

    // ===== ENTITY =====
    @Entity
    public static class User {
        @PrimaryKey(autoGenerate = true)
        public int id;

        public String username;
        public String passwordHash;
        public long createdAt;

        public User(String username, String passwordHash, long createdAt) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.createdAt = createdAt;
        }
    }

    // ===== DAO =====
    @Dao
    public static interface UserDao {
        @Insert
        void insert(User user);
        @Query("UPDATE User SET passwordHash = :newHash WHERE username = :username")
        void updatePassword(String username, String newHash);
        @Query("SELECT * FROM User WHERE username = :username LIMIT 1")
        User findByUsername(String username);
    }
}