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

        holder.taskTime.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(task.getTimestamp()));

        // Ngăn callback vô hạn
        holder.checkboxDone.setOnCheckedChangeListener(null);
        holder.checkboxDone.setChecked(task.isCompleted());

        // Set màu & gạch nếu hoàn thành
        holder.itemView.setBackgroundResource(task.isCompleted()
                ? R.drawable.bg_task_card_done
                : R.drawable.bg_task_card);

        holder.taskName.setPaintFlags(task.isCompleted()
                ? holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : holder.taskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        //Checkbox hoàn thành
        holder.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            new Thread(() -> {
                taskViewModel.updateTask(task);
                activity.runOnUiThread(activity::reloadTasks); // Reload UI
            }).start();
        });
        if (task.isCompleted()) {
            holder.taskTime.setVisibility(View.VISIBLE);
        } else {
            holder.taskTime.setVisibility(View.GONE);
        }

        //Nút sửa
        holder.btnEdit.setOnClickListener(v -> activity.editTask(task));

        //Nút xóa
        holder.btnDelete.setOnClickListener(v -> {
            new Thread(() -> {
                taskViewModel.deleteTask(task);
                activity.runOnUiThread(activity::reloadTasks); // Reload UI
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskTime;
        CheckBox checkboxDone;
        Button btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskTime = itemView.findViewById(R.id.taskTime);
            checkboxDone = itemView.findViewById(R.id.checkboxDone);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
