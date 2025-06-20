package com.example.timemanagement;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TaskDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = TaskDatabase.getInstance(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, TaskFragment.newInstance("Tất cả"))
                .commit();

        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_all) {
                selectedFragment = TaskFragment.newInstance("Tất cả");
            } else if (itemId == R.id.nav_work) {
                selectedFragment = TaskFragment.newInstance("Công việc");
            } else if (itemId == R.id.nav_personal) {
                selectedFragment = TaskFragment.newInstance("Cá nhân");
            } else if (itemId == R.id.nav_favorites) {
                selectedFragment = TaskFragment.newInstance("Danh sách yêu thích");
            } else if (itemId == R.id.nav_birthdays) {
                selectedFragment = TaskFragment.newInstance("Ngày sinh nhật");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_frame, selectedFragment)
                        .commit();
            }
            return true;
        });

        BottomNavigationView bottomNavSecondary = findViewById(R.id.bottom_nav_secondary);
        bottomNavSecondary.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_menu) {
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_tasks) {
                Toast.makeText(this, "Nhiệm vụ clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_calendar) {
                Toast.makeText(this, "Lịch clicked", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Của tôi clicked", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void showAddTaskDialog() {
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        final EditText edtTaskName = dialogView.findViewById(R.id.edtTaskName);
        final Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        new AlertDialog.Builder(this)
                .setTitle("📝 Thêm công việc")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String taskName = edtTaskName.getText().toString().trim();
                    String category = spinnerCategory.getSelectedItem().toString();
                    if (!taskName.isEmpty()) {
                        Task task = new Task(taskName, System.currentTimeMillis(), category);
                        executor.execute(() -> {
                            db.taskDao().insert(task);
                            runOnUiThread(this::reloadTasks);
                        });
                    } else {
                        Toast.makeText(this, "Vui lòng nhập tên công việc!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    public void editTask(Task task) {
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        final EditText edtTaskName = dialogView.findViewById(R.id.edtTaskName);
        final Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);

        // Pre-fill dialog with task data
        edtTaskName.setText(task.getName());
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
        int categoryPosition = adapter.getPosition(task.getCategory());
        spinnerCategory.setSelection(categoryPosition);

        new AlertDialog.Builder(this)
                .setTitle("📝 Sửa công việc")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String taskName = edtTaskName.getText().toString().trim();
                    String category = spinnerCategory.getSelectedItem().toString();
                    if (!taskName.isEmpty()) {
                        executor.execute(() -> {
                            Task updatedTask = new Task(taskName, task.getTimestamp(), category, task.isCompleted());
                            updatedTask.id = task.id;
                            db.taskDao().update(updatedTask);
                            runOnUiThread(this::reloadTasks);
                        });
                    } else {
                        Toast.makeText(this, "Vui lòng nhập tên công việc!", Toast.LENGTH_SHORT).show();
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
}