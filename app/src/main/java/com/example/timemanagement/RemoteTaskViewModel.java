package com.example.timemanagement;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemoteTaskViewModel extends ViewModel {

    private final MutableLiveData<List<Task>> taskList = new MutableLiveData<>();
    private final TaskApi taskApi;

    public RemoteTaskViewModel() {
        taskApi = RetrofitClient.getInstance().create(TaskApi.class);
        fetchAllTasks();
    }

    public LiveData<List<Task>> getTasks() {
        return taskList;
    }

    public void fetchAllTasks() {
        taskApi.getTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.setValue(response.body());
                } else {
                    Log.e("API_FETCH", "Không lấy được dữ liệu: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Log.e("API_FETCH", "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void insertTask(Task task, ResultCallback<Boolean> callback) {
        Log.d("DEBUG_TASK", "Đang gửi: name = " + task.getName()
                + ", timestamp = " + task.getTimestamp()
                + ", category = " + task.getCategory());

        taskApi.insertTask(task.getName(), task.getTimestamp(), task.getCategory())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d("API_INSERT", "Response code: " + response.code());
                        callback.onResult(response.isSuccessful());
                        if (response.isSuccessful()) fetchAllTasks();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API_INSERT", "Lỗi kết nối: " + t.getMessage());
                        callback.onResult(false);
                    }
                });
    }


    public void updateTask(Task task, ResultCallback<Boolean> callback) {
        taskApi.updateTask(task.getId(), task.getName(), task.getTimestamp(), task.getCategory(), task.isCompleted())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        boolean success = response.isSuccessful();
                        callback.onResult(success);
                        if (success) fetchAllTasks();
                        else Log.e("API_UPDATE", "Lỗi cập nhật: " + response.code());
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API_UPDATE", "Lỗi mạng khi cập nhật: " + t.getMessage());
                        callback.onResult(false);
                    }
                });
    }

    public void deleteTask(Task task) {
        taskApi.deleteTask(task.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            fetchAllTasks();
                        } else {
                            Log.e("API_DELETE", "Xóa thất bại: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API_DELETE", "Lỗi kết nối khi xóa: " + t.getMessage());
                    }
                });
    }
}
