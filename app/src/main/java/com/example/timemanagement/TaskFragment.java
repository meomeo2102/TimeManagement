package com.example.timemanagement;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.*;

import java.util.*;

public class TaskFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter adapterToday, adapterCompleted;
    private String mode = "overview";
    private String category = "Tất cả";
    private String owner = "guest";

    private TextView todayTitle, encouragementText, emptyText;
    private ImageView emptyImage;
    private RecyclerView recyclerToday, recyclerCompleted;
    private LinearLayout emptyState;
    private View rootView;

    public static TaskFragment newInstance(String mode, String category, String owner) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString("mode", mode);
        args.putString("category", category);
        args.putString("owner", owner);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.task_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rootView = view;

        if (getArguments() != null) {
            mode = getArguments().getString("mode", "overview");
            category = getArguments().getString("category", "Tất cả");
            owner = getArguments().getString("owner", "guest");
        }

        recyclerToday = view.findViewById(R.id.recyclerToday);
        recyclerCompleted = view.findViewById(R.id.recyclerCompleted);
        emptyState = view.findViewById(R.id.empty_state);
        emptyText = view.findViewById(R.id.empty_text);
        todayTitle = view.findViewById(R.id.today_title);
        encouragementText = view.findViewById(R.id.encouragement_text);
        emptyImage = view.findViewById(R.id.empty_image);

        recyclerToday.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCompleted.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        adapterToday = new TaskAdapter((MainActivity) requireActivity(), viewModel);
        adapterCompleted = new TaskAdapter((MainActivity) requireActivity(), viewModel);

        recyclerToday.setAdapter(adapterToday);
        recyclerCompleted.setAdapter(adapterCompleted);

        loadTasksForOwner(owner);
    }

    private void loadTasksForOwner(String currentUser) {
        TaskDao dao = TaskDatabase.getInstance(requireContext()).taskDao();
        LiveData<List<Task>> liveData = "category".equals(mode) && !"Tất cả".equals(category)
                ? dao.getTasksByCategory(currentUser, category)
                : dao.getTasksByOwner(currentUser);

        liveData.observe(getViewLifecycleOwner(), this::updateTasks);
    }

    private void updateTasks(List<Task> tasks) {
        List<Task> todayTasks = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();

        for (Task task : tasks) {
            if (task.isCompleted()) completedTasks.add(task);
            else todayTasks.add(task);
        }

        adapterToday.setTasks(todayTasks);
        adapterCompleted.setTasks(completedTasks);

        recyclerCompleted.setVisibility(completedTasks.isEmpty() ? View.GONE : View.VISIBLE);
        rootView.findViewById(R.id.completed_title)
                .setVisibility(completedTasks.isEmpty() ? View.GONE : View.VISIBLE);

        todayTitle.setVisibility(todayTasks.isEmpty() ? View.GONE : View.VISIBLE);
        encouragementText.setVisibility(todayTasks.isEmpty() && !completedTasks.isEmpty() ? View.VISIBLE : View.GONE);

        if (todayTasks.isEmpty() && completedTasks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            if (!"Tất cả".equals(category)) {
                emptyText.setText("Không có nhiệm vụ nào trong danh mục này.\nNhấp vào + để tạo nhiệm vụ của bạn");
                switch (category) {
                    case "Công việc": emptyImage.setImageResource(R.drawable.empty_state_work); break;
                    case "Cá nhân": emptyImage.setImageResource(R.drawable.empty_state_personal); break;
                    case "Danh sách yêu thích": emptyImage.setImageResource(R.drawable.empty_state_favorites); break;
                    case "Ngày sinh nhật": emptyImage.setImageResource(R.drawable.empty_state_birthday); break;
                    default: emptyImage.setImageResource(R.drawable.empty_state_image); break;
                }
            } else {
                emptyText.setText("Hôm nay không có lịch trình gì sao?\nBấm vào + để cảm thấy bận rộn hơn nhé!");
                emptyImage.setImageResource(R.drawable.empty_state_image);
            }
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }
}