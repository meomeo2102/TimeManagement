package com.example.timemanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RemoteTaskViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(RemoteTaskViewModel.class);
        Fragment defaultFragment = TaskFragment.newInstance("category", "Tất cả");
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, defaultFragment).commit();
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
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, selectedFragment).commit();
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
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, selectedFragment).commit();
                View topNav = findViewById(R.id.bottom_nav);
                topNav.setVisibility(selectedFragment instanceof TaskFragment ? View.VISIBLE : View.GONE);
            }
            return true;
        });
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText edtTaskName = dialogView.findViewById(R.id.edtTaskName);
        MaterialAutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        TextView txtDateTime = dialogView.findViewById(R.id.txtDateTime);
        Button btnPickDateTime = dialogView.findViewById(R.id.btnPickDateTime);
        Calendar calendar = Calendar.getInstance();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category_array));
        spinnerCategory.setAdapter(adapter);
        btnPickDateTime.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Chọn ngày").setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                calendar.setTimeInMillis(selection);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(hour).setMinute(minute).setTitleText("Chọn giờ").build();
                timePicker.addOnPositiveButtonClickListener(t -> {
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    calendar.set(Calendar.MINUTE, timePicker.getMinute());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    txtDateTime.setText(sdf.format(calendar.getTime()));
                });
                timePicker.show(getSupportFragmentManager(), "time_picker");
            });
            datePicker.show(getSupportFragmentManager(), "date_picker");
        });
        new MaterialAlertDialogBuilder(this).setTitle("Thêm công việc").setView(dialogView).setPositiveButton("Lưu", (dialog, which) -> {
            String taskName = edtTaskName.getText().toString().trim();
            String category = spinnerCategory.getText().toString().trim();
            String timestampText = txtDateTime.getText().toString().trim();
            if (!taskName.isEmpty() && !category.isEmpty() && !timestampText.equals("Chưa chọn thời gian")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                try {
                    long timestamp = sdf.parse(timestampText).getTime();
                    Task task = new Task(taskName, timestamp, category);
                    viewModel.insertTask(task, success -> {
                        if (success) runOnUiThread(this::reloadTasks);
                        else
                            runOnUiThread(() -> Toast.makeText(this, "Lưu thất bại!", Toast.LENGTH_SHORT).show());
                    });
                } catch (ParseException e) {
                    Toast.makeText(this, "Lỗi định dạng thời gian!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Hủy", null).show();
    }

    public void editTask(Task task) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText edtTaskName = dialogView.findViewById(R.id.edtTaskName);
        MaterialAutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category_array));
        spinnerCategory.setAdapter(adapter);
        edtTaskName.setText(task.getName());
        spinnerCategory.setText(task.getCategory(), false);
        new MaterialAlertDialogBuilder(this).setTitle("Sửa công việc").setView(dialogView).setPositiveButton("Lưu", (d, which) -> {
            String taskName = edtTaskName.getText().toString().trim();
            String category = spinnerCategory.getText().toString().trim();
            if (!taskName.isEmpty() && !category.isEmpty()) {
                Task updatedTask = new Task(taskName, task.getTimestamp(), category, task.isCompleted());
                updatedTask.setId(task.getId());
                viewModel.updateTask(updatedTask, success -> {
                    if (success) runOnUiThread(this::reloadTasks);
                    else
                        runOnUiThread(() -> Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show());
                });
            } else {
                Toast.makeText(this, "Vui lòng nhập tên công việc và danh mục!", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Hủy", null).show();
    }

    public void reloadTasks() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_frame);
        if (current instanceof TaskFragment) {
            ((TaskFragment) current).refreshTasks();
        }
    }
}