package com.example.timemanagement;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;

public class AuthUtils {

    // ====== SESSION MANAGER ======
    public static class SessionManager {
        private static final String PREF_NAME = "user_session";
        private static final String KEY_USERNAME = "current_username";

        public static void login(Context context, String username) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_USERNAME, username).apply();
        }

        public static void logout(Context context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit().remove(KEY_USERNAME).apply();
        }

        public static String getLoggedInUsername(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_USERNAME, null);
        }
    }

    // ====== PASSWORD UTIL ======
    public static class PasswordUtil {
        public static String hash(String plainText) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] bytes = md.digest(plainText.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes)
                    sb.append(String.format("%02x", b));
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }
    }
}