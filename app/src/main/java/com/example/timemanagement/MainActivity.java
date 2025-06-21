package com.example.timemanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    TaskDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

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
                // ✅ bottom_nav luôn hiển thị trong TaskFragment
                findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
            }
            return true;
        });

        BottomNavigationView bottomNavSecondary = findViewById(R.id.bottom_nav_secondary);
        bottomNavSecondary.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_menu) {
                selectedFragment = new MenuFragment();
            } else if (itemId == R.id.nav_tasks) {
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText edtTaskName = dialogView.findViewById(R.id.edtTaskName);
        MaterialAutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.category_array));
        spinnerCategory.setAdapter(adapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Thêm công việc")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, which) -> {
                    String taskName = edtTaskName.getText().toString().trim();
                    String category = spinnerCategory.getText().toString().trim();
                    if (!taskName.isEmpty() && !category.isEmpty()) {
                        Task task = new Task(taskName, System.currentTimeMillis(), category);
                        executor.execute(() -> {
                            db.taskDao().insert(task);
                            runOnUiThread(this::reloadTasks);
                        });
                    } else {
                        Toast.makeText(this, "Vui lòng nhập tên công việc và danh mục!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    public void editTask(Task task) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText edtTaskName = dialogView.findViewById(R.id.edtTaskName);
        MaterialAutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.category_array));
        spinnerCategory.setAdapter(adapter);

        edtTaskName.setText(task.getName());
        spinnerCategory.setText(task.getCategory(), false);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Sửa công việc")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, which) -> {
                    String taskName = edtTaskName.getText().toString().trim();
                    String category = spinnerCategory.getText().toString().trim();
                    if (!taskName.isEmpty() && !category.isEmpty()) {
                        executor.execute(() -> {
                            Task updatedTask = new Task(taskName, task.getTimestamp(), category, task.isCompleted());
                            updatedTask.setId(task.getId());
                            db.taskDao().update(updatedTask);
                            runOnUiThread(this::reloadTasks);
                        });
                    } else {
                        Toast.makeText(this, "Vui lòng nhập tên công việc và danh mục!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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
}
