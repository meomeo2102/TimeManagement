package com.example.timemanagement;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final MainActivity activity;
    private final TaskViewModel taskViewModel;

    public TaskAdapter(MainActivity activity, TaskViewModel taskViewModel) {
        this.activity = activity;
        this.taskViewModel = taskViewModel;
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskName.setText(task.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

        String createdText = "📅 Ngày tạo: " + sdf.format(new Date(task.getCreatedAt()));
        String deadlineText = "⏰ Deadline: " + sdf.format(new Date(task.getDeadlineTimestamp()));

        holder.textCreatedAt.setText(createdText);
        holder.textDeadline.setText(deadlineText);

        // Gạch ngang nếu đã hoàn thành
        holder.taskName.setPaintFlags(task.isCompleted()
                ? holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : holder.taskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        holder.itemView.setBackgroundResource(task.isCompleted()
                ? R.drawable.bg_task_card_done
                : R.drawable.bg_task_card);

        // CheckBox
        holder.checkboxDone.setOnCheckedChangeListener(null);
        holder.checkboxDone.setChecked(task.isCompleted());
        holder.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            new Thread(() -> {
                taskViewModel.updateTask(task);
                activity.runOnUiThread(activity::reloadTasks);
            }).start();
        });

        // Nút sửa
        holder.btnEdit.setOnClickListener(v -> activity.editTask(task));

        // Nút xoá
        holder.btnDelete.setOnClickListener(v -> {
            new Thread(() -> {
                taskViewModel.deleteTask(task);
                activity.runOnUiThread(activity::reloadTasks);
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, textCreatedAt, textDeadline;
        CheckBox checkboxDone;
        Button btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            textCreatedAt = itemView.findViewById(R.id.textCreatedAt);
            textDeadline = itemView.findViewById(R.id.textDeadline);
            checkboxDone = itemView.findViewById(R.id.checkboxDone);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}