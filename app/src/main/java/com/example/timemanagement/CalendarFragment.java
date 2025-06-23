package com.example.timemanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timemanagement.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;
    private CalendarView calendarView;
    private TextView tvTitle, tvNoTask;
    private ImageButton btnToggle;
    private boolean isExpanded = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        tvTitle = view.findViewById(R.id.tvTitleCalendar);
        btnToggle = view.findViewById(R.id.btnToggleCalendar);
        tvNoTask = view.findViewById(R.id.tvNoTask);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerCalendarTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        MainActivity mainActivity = (MainActivity) requireActivity();
        taskViewModel = mainActivity.getTaskViewModel();
        adapter = new TaskAdapter(mainActivity, taskViewModel);
        recyclerView.setAdapter(adapter);

        // Load mặc định tuần hiện tại
        long today = System.currentTimeMillis();
        long[] week = getWeekRangeFrom(today);
        tvTitle.setText("Nhiệm vụ từ " + formatDate(week[0]) + " đến " + formatDate(week[1]));
        loadTasks(week[0], week[1]);

        // Khi chọn ngày → xem tuần chứa ngày đó
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long selectedDate = cal.getTimeInMillis();
            calendarView.setDate(selectedDate, true, true);
            long[] range = getWeekRangeFrom(selectedDate);
            tvTitle.setText("Nhiệm vụ từ " + formatDate(range[0]) + " đến " + formatDate(range[1]));
            loadTasks(range[0], range[1]);
        });

        // Toggle co/giãn lịch
        btnToggle.setOnClickListener(v -> {
            if (isExpanded) {
                calendarView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(150)
                ));
                btnToggle.setImageResource(R.drawable.ic_arrow_drop_up);
                isExpanded = false;
            } else {
                calendarView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                btnToggle.setImageResource(R.drawable.ic_arrow_drop_down);
                isExpanded = true;
            }
        });

        return view;
    }

    private void loadTasks(long start, long end) {
        taskViewModel.getTasksThisWeek(start, end).observe(getViewLifecycleOwner(), tasks -> {
            adapter.setTasks(tasks);
            tvNoTask.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private long[] getWeekRangeFrom(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long end = cal.getTimeInMillis();

        return new long[]{start, end};
    }

    private String formatDate(long millis) {
        return new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                .format(new java.util.Date(millis));
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}