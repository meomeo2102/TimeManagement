package com.example.timemanagement;
import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.content.pm.PackageManager;
import android.os.Build;
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
        NotificationUtil.createChannel(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
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
                new AlertDialog.Builder(this)
                        .setTitle("Giới thiệu ứng dụng")
                        .setMessage("Time Management là ứng dụng giúp bạn tạo, theo dõi và hoàn thành công việc hiệu quả mỗi ngày.\n\nChủ sở hữu: Huỳnh Giao\nPhiên bản: 1.0.0")
                        .setPositiveButton("Đóng", null)
                        .show();

            } else if (itemId == R.id.nav_share) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Time Management App");
                intent.putExtra(Intent.EXTRA_TEXT, "Tải ứng dụng quản lý công việc cực xịn nè: https://drive.google.com/drive/folders/122lZKmY1pC_nJH_M20JA7D_xUzUnVif_");
                startActivity(Intent.createChooser(intent, "Chia sẻ qua..."));

            } else if (itemId == R.id.nav_theme_light) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            } else if (itemId == R.id.nav_theme_dark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            } else if (itemId == R.id.nav_theme_system) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }

            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawers(); // Đóng menu sau khi chọn
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
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không có quyền gửi thông báo. Bạn có thể không nhận được nhắc nhở!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
