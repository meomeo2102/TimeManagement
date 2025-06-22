package com.example.timemanagement;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddTaskDialogFragment extends DialogFragment {

    private TextInputEditText edtTaskName, edtDeadlineDate, edtDeadlineTime;
    private MaterialAutoCompleteTextView spinnerCategory;
    private TextView tvCreatedAt;
    private Calendar deadlineCalendar = Calendar.getInstance();

    private Executor executor;
    private TaskDatabase db;

    public static AddTaskDialogFragment newInstance() {
        return new AddTaskDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_task, null);

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }


        // Ánh xạ view
        edtTaskName = dialogView.findViewById(R.id.edtTaskName);
        edtDeadlineDate = dialogView.findViewById(R.id.edtDeadlineDate);
        edtDeadlineTime = dialogView.findViewById(R.id.edtDeadlineTime);
        spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        tvCreatedAt = dialogView.findViewById(R.id.tvCreatedAt);

        executor = Executors.newSingleThreadExecutor();
        db = TaskDatabase.getInstance(requireContext());

        // Danh mục
        String[] categories = {
                "Công việc", "Cá nhân", "Danh sách yêu thích", "Ngày sinh nhật", "Không phân loại"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, categories);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnClickListener(v -> spinnerCategory.showDropDown());

        // Gán ngày tạo
        long createdAt = System.currentTimeMillis();
        tvCreatedAt.setText("Ngày tạo: " + formatDateTime(createdAt));

        // Chọn ngày deadline
        edtDeadlineDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, y, m, d) -> {
                deadlineCalendar.set(Calendar.YEAR, y);
                deadlineCalendar.set(Calendar.MONTH, m);
                deadlineCalendar.set(Calendar.DAY_OF_MONTH, d);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                edtDeadlineDate.setText(sdf.format(deadlineCalendar.getTime()));
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Chọn giờ deadline
        edtDeadlineTime.setOnClickListener(v -> {
            new TimePickerDialog(requireContext(), (view, h, min) -> {
                deadlineCalendar.set(Calendar.HOUR_OF_DAY, h);
                deadlineCalendar.set(Calendar.MINUTE, min);
                deadlineCalendar.set(Calendar.SECOND, 0);
                deadlineCalendar.set(Calendar.MILLISECOND, 0);
                edtDeadlineTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, min));
            }, 14, 0, true).show();
        });

        // Nút
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

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

            Task task = new Task(taskName, createdAt, category, deadlineMillis);

            executor.execute(() -> {
                db.taskDao().insert(task);
                requireActivity().runOnUiThread(() -> {
                    if (requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).reloadTasks();
                    }
                    dialog.dismiss();
                });
            });
        });

        return dialog;
    }

    private String formatDateTime(long millis) {
        return new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                .format(new Date(millis));
    }
}