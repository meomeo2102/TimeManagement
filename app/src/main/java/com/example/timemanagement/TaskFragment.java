package com.example.timemanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment {

    private RemoteTaskViewModel viewModel;
    private TaskAdapter adapterToday, adapterCompleted;
    private String category = "Tất cả";
    private TextView todayTitle, encouragementText, emptyText;
    private ImageView emptyImage;
    private RecyclerView recyclerToday, recyclerCompleted;
    private LinearLayout emptyState;
    private View rootView;

    public static TaskFragment newInstance(String mode, String category) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString("mode", mode);
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.task_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        if (getArguments() != null) {
            category = getArguments().getString("category", "Tất cả");
        }

        todayTitle = view.findViewById(R.id.today_title);
        encouragementText = view.findViewById(R.id.encouragement_text);
        emptyText = view.findViewById(R.id.empty_text);
        emptyImage = view.findViewById(R.id.empty_image);
        emptyState = view.findViewById(R.id.empty_state);

        recyclerToday = view.findViewById(R.id.recyclerToday);
        recyclerCompleted = view.findViewById(R.id.recyclerCompleted);
        recyclerToday.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCompleted.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel = new ViewModelProvider(this).get(RemoteTaskViewModel.class);
        adapterToday = new TaskAdapter((MainActivity) requireActivity(), viewModel);
        adapterCompleted = new TaskAdapter((MainActivity) requireActivity(), viewModel);

        recyclerToday.setAdapter(adapterToday);
        recyclerCompleted.setAdapter(adapterCompleted);

        observeTasks();
    }

    private void observeTasks() {
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            List<Task> today = new ArrayList<>();
            List<Task> done = new ArrayList<>();

            for (Task task : tasks) {
                if ("Tất cả".equals(category) || category.equals(task.getCategory())) {
                    if (task.isCompleted()) done.add(task); else today.add(task);
                }
            }

            adapterToday.setTasks(today);
            adapterCompleted.setTasks(done);

            todayTitle.setVisibility(today.isEmpty() ? View.GONE : View.VISIBLE);
            encouragementText.setVisibility(today.isEmpty() && !done.isEmpty() ? View.VISIBLE : View.GONE);

            boolean hasDone = !done.isEmpty();
            recyclerCompleted.setVisibility(hasDone ? View.VISIBLE : View.GONE);
            rootView.findViewById(R.id.completed_title).setVisibility(hasDone ? View.VISIBLE : View.GONE);

            if (today.isEmpty() && done.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                emptyText.setText("Không có nhiệm vụ nào trong mục này.\nBấm + để tạo công việc mới!");
                emptyImage.setImageResource(R.drawable.empty_state_image); // hoặc tùy theo danh mục
            } else {
                emptyState.setVisibility(View.GONE);
            }
        });
    }

    public void refreshTasks() {
        viewModel.fetchAllTasks();
    }
}
