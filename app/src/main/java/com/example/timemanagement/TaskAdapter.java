package com.example.timemanagement;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timemanagement.databinding.TaskItemBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList != null ? taskList : new ArrayList<>();
    }

    public void setTasks(List<Task> newList) {
        this.taskList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        TaskItemBinding binding = TaskItemBinding.inflate(inflater, parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.taskName.setText(task.name);

        String formattedTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(task.timestamp));
        holder.binding.taskTime.setText(formattedTime);

        updateUIState(holder, task.completed, context);

        holder.binding.checkboxDone.setOnCheckedChangeListener(null);
        holder.binding.checkboxDone.setChecked(task.completed);
        holder.binding.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.completed = isChecked;
            executor.execute(() ->
                    TaskDatabase.getInstance(context).taskDao().markCompleted(task.id, isChecked));
            updateUIState(holder, isChecked, context);
        });

        holder.binding.btnEdit.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).editTask(task);
            }
        });

        holder.binding.btnDelete.setOnClickListener(v -> {
            executor.execute(() ->
                    TaskDatabase.getInstance(context).taskDao().deleteTask(task.id));
            if (context instanceof MainActivity) {
                ((MainActivity) context).reloadTasks();
            }
        });
    }

    private void updateUIState(TaskViewHolder holder, boolean isCompleted, Context context) {
        if (isCompleted) {
            holder.binding.taskName.setPaintFlags(holder.binding.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.binding.taskName.setAlpha(0.5f);
            holder.binding.getRoot().setBackground(ContextCompat.getDrawable(context, R.drawable.bg_task_card_done));
        } else {
            holder.binding.taskName.setPaintFlags(holder.binding.taskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.binding.taskName.setAlpha(1.0f);
            holder.binding.getRoot().setBackground(ContextCompat.getDrawable(context, R.drawable.bg_task_card));
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TaskItemBinding binding;

        public TaskViewHolder(@NonNull TaskItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}