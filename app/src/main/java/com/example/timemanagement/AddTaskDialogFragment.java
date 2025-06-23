package com.example.timemanagement;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class AddTaskDialogFragment extends DialogFragment {

    private static final String ARG_TASK = "task_to_edit";

    private TextInputEditText edtTaskName, edtDeadlineDate, edtDeadlineTime;
    private MaterialAutoCompleteTextView spinnerCategory;
    private CheckBox checkboxCompleted;
    private TextView tvCreatedAt;

    private Calendar deadlineCalendar = Calendar.getInstance();
    private Executor executor;
    private TaskDatabase db;
    private Task taskToEdit;

    public static AddTaskDialogFragment newInstance(@Nullable Task task) {
        AddTaskDialogFragment fragment = new AddTaskDialogFragment();
        if (task != null) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_TASK, task);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_task, container, false);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtTaskName = view.findViewById(R.id.edtTaskName);
        edtDeadlineDate = view.findViewById(R.id.edtDeadlineDate);
        edtDeadlineTime = view.findViewById(R.id.edtDeadlineTime);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        checkboxCompleted = view.findViewById(R.id.checkboxCompleted);
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt);

        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);

        executor = Executors.newSingleThreadExecutor();
        db = TaskDatabase.getInstance(requireContext());

        if (getArguments() != null) {
            taskToEdit = (Task) getArguments().getSerializable(ARG_TASK);
        }

        String[] categories = {"Công việc", "Cá nhân", "Danh sách yêu thích", "Ngày sinh nhật", "Không phân loại"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, categories);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnClickListener(v -> spinnerCategory.showDropDown());

        long createdAt;

        if (taskToEdit != null) {
            edtTaskName.setText(taskToEdit.getName());
            spinnerCategory.setText(taskToEdit.getCategory(), false);
            checkboxCompleted.setChecked(taskToEdit.isCompleted());

            deadlineCalendar.setTimeInMillis(taskToEdit.getDeadlineTimestamp());
            edtDeadlineDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(deadlineCalendar.getTime()));
            edtDeadlineTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(deadlineCalendar.getTime()));

            createdAt = taskToEdit.getCreatedAt();
        } else {
            createdAt = System.currentTimeMillis();
        }

        tvCreatedAt.setText("Ngày tạo: " + formatDateTime(createdAt));

        edtDeadlineDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, y, m, d) -> {
                deadlineCalendar.set(Calendar.YEAR, y);
                deadlineCalendar.set(Calendar.MONTH, m);
                deadlineCalendar.set(Calendar.DAY_OF_MONTH, d);
                edtDeadlineDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(deadlineCalendar.getTime()));
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });

        edtDeadlineTime.setOnClickListener(v -> {
            new TimePickerDialog(requireContext(), (view12, h, min) -> {
                deadlineCalendar.set(Calendar.HOUR_OF_DAY, h);
                deadlineCalendar.set(Calendar.MINUTE, min);
                deadlineCalendar.set(Calendar.SECOND, 0);
                deadlineCalendar.set(Calendar.MILLISECOND, 0);
                edtDeadlineTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, min));
            }, 14, 0, true).show();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String taskName = edtTaskName.getText().toString().trim();
            String category = spinnerCategory.getText().toString().trim();

            if (taskName.isEmpty()) {
                edtTaskName.setError("Vui lòng nhập tên công việc");
                return;
            }

            if (edtDeadlineDate.getText().toString().isEmpty()) {
                edtDeadlineDate.setError("Chọn ngày đến hạn");
                return;
            }

            if (edtDeadlineTime.getText().toString().isEmpty()) {
                edtDeadlineTime.setError("Chọn giờ đến hạn");
                return;
            }

            if (category.isEmpty()) {
                category = "Không phân loại";
            }

            long deadlineMillis = deadlineCalendar.getTimeInMillis();

            if (deadlineMillis < createdAt) {
                Toast.makeText(getContext(), "Deadline không được trước ngày tạo", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isCompleted = checkboxCompleted.isChecked();

            if (taskToEdit != null) {
                Task updatedTask = new Task(taskName, createdAt, category, deadlineMillis);
                updatedTask.setId(taskToEdit.getId());
                updatedTask.setCompleted(isCompleted);

                executor.execute(() -> {
                    db.taskDao().update(updatedTask);
                    requireActivity().runOnUiThread(() -> {
                        if (requireActivity() instanceof MainActivity) {
                            ((MainActivity) requireActivity()).reloadTasks();
                        }
                        dismiss();
                    });
                });
            } else {
                Task task = new Task(taskName, createdAt, category, deadlineMillis);
                task.setCompleted(isCompleted);

                executor.execute(() -> {
                    db.taskDao().insert(task);
                    requireActivity().runOnUiThread(() -> {
                        if (requireActivity() instanceof MainActivity) {
                            ((MainActivity) requireActivity()).reloadTasks();
                        }
                        dismiss();
                    });
                });
            }
        });
    }

    private String formatDateTime(long millis) {
        return new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date(millis));
    }
}