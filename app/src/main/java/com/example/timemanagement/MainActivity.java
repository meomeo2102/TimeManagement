package com.example.timemanagement;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.timemanagement.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    TaskDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = TaskDatabase.getInstance(this);

        // Hiển thị TaskFragment mặc định
        Fragment defaultFragment = TaskFragment.newInstance("category", "Tất cả");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, defaultFragment)
                .commit();

        findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);

        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_all) {
                selectedFragment = TaskFragment.newInstance("category", "Tất cả");
            } else if (itemId == R.id.nav_work) {
                selectedFragment = TaskFragment.newInstance("category", "Công việc");
            } else if (itemId == R.id.nav_personal) {
                selectedFragment = TaskFragment.newInstance("category", "Cá nhân");
            } else if (itemId == R.id.nav_favorites) {
                selectedFragment = TaskFragment.newInstance("category", "Danh sách yêu thích");
            } else if (itemId == R.id.nav_birthdays) {
                selectedFragment = TaskFragment.newInstance("category", "Ngày sinh nhật");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_frame, selectedFragment)
                        .commit();
                findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
            }
            return true;
        });
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_about) {
                        Toast.makeText(this, "Giới thiệu app", Toast.LENGTH_SHORT).show();
                    } else if (itemId == R.id.nav_share) {
                        Toast.makeText(this, "Chia sẻ ứng dụng", Toast.LENGTH_SHORT).show();
                    } else if (itemId == R.id.nav_theme_light) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (itemId == R.id.nav_theme_dark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else if (itemId == R.id.nav_theme_system) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }


            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawers(); // đóng menu sau khi chọn

            return true;
        });

        BottomNavigationView bottomNavSecondary = findViewById(R.id.bottom_nav_secondary);
        bottomNavSecondary.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_tasks) {
                selectedFragment = TaskFragment.newInstance("category", "Tất cả");
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_frame, selectedFragment)
                        .commit();

                //Hiển thị bottom_nav nếu là TaskFragment
                View topNav = findViewById(R.id.bottom_nav);
                if (selectedFragment instanceof TaskFragment) {
                    topNav.setVisibility(View.VISIBLE);
                } else {
                    topNav.setVisibility(View.GONE);
                }
            }

            return true;
        });
    }

    private void showAddTaskDialog() {
        AddTaskDialogFragment.newInstance(null).show(getSupportFragmentManager(), "AddTaskDialog");
    }

    public void editTask(Task task) {
        AddTaskDialogFragment.newInstance(task)
                .show(getSupportFragmentManager(), "EditTaskDialog");
    }

    public void reloadTasks() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_frame);
        if (current instanceof TaskFragment) {
            ((TaskFragment) current).refreshTasks();
        }
    }

    public Executor getExecutor() {
        return executor;
    }

    public TaskViewModel getTaskViewModel() {
        return new ViewModelProvider(this).get(TaskViewModel.class);
    }
}
