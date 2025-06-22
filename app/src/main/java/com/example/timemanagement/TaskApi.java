package com.example.timemanagement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface TaskApi {
    @GET("get_tasks.php")
    Call<List<Task>> getTasks();

    @FormUrlEncoded
    @POST("insert_task.php")
    Call<Void> insertTask(
            @Field("name") String name,
            @Field("timestamp") String timestamp,
            @Field("category") String category
    );

    @FormUrlEncoded
    @POST("update_task.php")
    Call<Void> updateTask(
            @Field("id") int id,
            @Field("name") String name,
            @Field("timestamp") String timestamp,
            @Field("category") String category,
            @Field("completed") boolean completed
    );

    @FormUrlEncoded
    @POST("delete_task.php")
    Call<Void> deleteTask(
            @Field("id") int id
    );
}
