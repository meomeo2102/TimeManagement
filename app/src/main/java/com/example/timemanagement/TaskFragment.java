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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter adapterToday, adapterCompleted;
    private String mode = "overview";
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
        View view = inflater.inflate(R.layout.task_fragment, container, false);
// TextView userInfo = view.findViewById(R.id.user_info);
// GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
// if (account != null) {
//     String info = "Tên: " + account.getDisplayName() + "\nEmail: " + account.getEmail();
//     userInfo.setText(info);
// }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        if (getArguments() != null) {
            mode = getArguments().getString("mode", "overview");
            category = getArguments().getString("category", "Tất cả");
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

        observeTasks();
    }

    private void observeTasks() {
        if ("category".equals(mode) && !"Tất cả".equals(category)) {
            viewModel.getTasksByCategory(category).observe(getViewLifecycleOwner(), this::updateTasks);
        } else {
            viewModel.getAllTasks().observe(getViewLifecycleOwner(), this::updateTasks);
        }
    }

    private void updateTasks(List<Task> tasks) {
        List<Task> todayTasks = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            } else {
                todayTasks.add(task);
            }
        }

        adapterToday.setTasks(todayTasks);
        adapterCompleted.setTasks(completedTasks);

        boolean hasCompleted = !completedTasks.isEmpty();
        recyclerCompleted.setVisibility(hasCompleted ? View.VISIBLE : View.GONE);
        rootView.findViewById(R.id.completed_title).setVisibility(hasCompleted ? View.VISIBLE : View.GONE);

        boolean hasToday = !todayTasks.isEmpty();
        todayTitle.setVisibility(hasToday ? View.VISIBLE : View.GONE);
        encouragementText.setVisibility(!hasToday && hasCompleted ? View.VISIBLE : View.GONE);

        boolean showEmpty = todayTasks.isEmpty() && completedTasks.isEmpty();
        if (showEmpty) {
            emptyState.setVisibility(View.VISIBLE);

            if (!"Tất cả".equals(category)) {
                emptyText.setText("Không có nhiệm vụ nào trong danh mục này.\nNhấp vào + để tạo nhiệm vụ của bạn");

                switch (category) {
                    case "Công việc":
                        emptyImage.setImageResource(R.drawable.empty_state_work);
                        break;
                    case "Cá nhân":
                        emptyImage.setImageResource(R.drawable.empty_state_personal);
                        break;
                    case "Danh sách yêu thích":
                        emptyImage.setImageResource(R.drawable.empty_state_favorites);
                        break;
                    case "Ngày sinh nhật":
                        emptyImage.setImageResource(R.drawable.empty_state_birthday);
                        break;
                    default:
                        emptyImage.setImageResource(R.drawable.empty_state_image);
                        break;
                }

            } else {
                emptyText.setText("Hôm nay không có lịch trình gì sao?\nBấm vào + để cảm thấy bận rộn hơn nhé!");
                emptyImage.setImageResource(R.drawable.empty_state_image);
            }
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }
    public void refreshTasks() {
        observeTasks();
    }
}
