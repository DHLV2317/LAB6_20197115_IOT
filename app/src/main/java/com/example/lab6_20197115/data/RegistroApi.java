package com.example.lab6_20197115.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegistroApi {

    @POST("api/registro")
    Call<RegistroResponse> registrar(@Body RegistroRequest request);
}