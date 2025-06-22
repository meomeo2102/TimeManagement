package com.example.timemanagement;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final MainActivity activity;
    private final RemoteTaskViewModel taskViewModel;

    public TaskAdapter(MainActivity activity, RemoteTaskViewModel taskViewModel) {
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

        // ✅ Sửa lỗi format timestamp chuỗi
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(task.getTimestamp());
            String formatted = outputFormat.format(date);
            holder.taskTime.setText(formatted);
        } catch (ParseException e) {
            holder.taskTime.setText(task.getTimestamp()); // fallback nếu lỗi
        }

        holder.checkboxDone.setOnCheckedChangeListener(null);
        holder.checkboxDone.setChecked(task.isCompleted());

        holder.itemView.setBackgroundResource(task.isCompleted()
                ? R.drawable.bg_task_card_done
                : R.drawable.bg_task_card);

        holder.taskName.setPaintFlags(task.isCompleted()
                ? holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : holder.taskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        holder.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            taskViewModel.updateTask(task, success -> {
                if (!success) {
                    Log.e("TaskAdapter", "Cập nhật trạng thái task thất bại");
                }
            });
        });

        holder.taskTime.setVisibility(task.isCompleted() ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v -> activity.editTask(task));
        holder.btnDelete.setOnClickListener(v -> taskViewModel.deleteTask(task));
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
