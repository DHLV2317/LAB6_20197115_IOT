package com.example.lab6_20197115.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroClient {

    private static Retrofit retrofit;

    // 🔹 Opción 1: si el backend corre en TU PC y usas el emulador de Android Studio:
    // private static final String BASE_URL = "http://10.0.2.2:8082/";

    // 🔹 Opción 2: si quieres usar la IP LAN de tu PC (la que probaste en Postman):
    private static final String BASE_URL = "http://192.168.1.151:8082/";

    public static RegistroApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)              // http://192.168.1.151:8082/
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(RegistroApi.class);
    }
}