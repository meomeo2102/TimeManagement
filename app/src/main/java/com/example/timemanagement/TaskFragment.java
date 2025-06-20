package com.example.timemanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class TaskFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private String category = "Tất cả";
    private View emptyStateView;

    public static TaskFragment newInstance(String category) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_fragment, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        emptyStateView = view.findViewById(R.id.empty_state);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category", "Tất cả");
        }
        refreshTasks();
    }

    public void refreshTasks() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Task> tasks;
            if (category.equals("Tất cả")) {
                tasks = TaskDatabase.getInstance(getContext()).taskDao().getAllTasks();
            } else if (category.equals("Danh sách yêu thích")) {
                tasks = TaskDatabase.getInstance(getContext()).taskDao().getFavoriteTasks();
            } else if (category.equals("Ngày sinh nhật")) {
                tasks = TaskDatabase.getInstance(getContext()).taskDao().getBirthdayTasks();
            } else {
                tasks = TaskDatabase.getInstance(getContext()).taskDao().getTasksByCategory(category);
            }
            requireActivity().runOnUiThread(() -> {
                adapter.setTasks(tasks);
                updateEmptyStateVisibility(tasks.isEmpty());
            });
        });
    }

    private void updateEmptyStateVisibility(boolean isEmpty) {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }
}