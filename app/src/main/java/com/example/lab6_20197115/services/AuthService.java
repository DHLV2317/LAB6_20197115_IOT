package com.example.lab6_20197115.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.lab6_20197115.data.RegistroApi;
import com.example.lab6_20197115.data.RegistroClient;
import com.example.lab6_20197115.data.RegistroRequest;
import com.example.lab6_20197115.data.RegistroResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthService {

    private static final String TAG = "AuthService";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    public void registerUser(String nombre,
                             String dni,
                             String email,
                             String password,
                             @NonNull SimpleCallback cb) {

        try {
            RegistroApi api = RegistroClient.getApi();
            RegistroRequest body = new RegistroRequest(nombre, dni, email);

            api.registrar(body).enqueue(new Callback<RegistroResponse>() {
                @Override
                public void onResponse(@NonNull Call<RegistroResponse> call,
                                       @NonNull Response<RegistroResponse> response) {

                    if (!response.isSuccessful() || response.body() == null) {
                        // Caso HTTP != 200
                        String errorMsg = "Error de validación";

                        try {
                            if (response.errorBody() != null) {
                                String bodyError = response.errorBody().string();
                                Log.e(TAG, "Respuesta error raw: " + bodyError);

                                try {
                                    JSONObject json = new JSONObject(bodyError);
                                    if (json.has("message")) {
                                        errorMsg = json.getString("message");
                                    } else if (json.has("error")) {
                                        errorMsg = json.getString("error");
                                    } else if (!bodyError.isEmpty()) {
                                        errorMsg = bodyError.length() > 120
                                                ? bodyError.substring(0, 120) + "..."
                                                : bodyError;
                                    }
                                } catch (JSONException je) {
                                    if (!bodyError.isEmpty()) {
                                        errorMsg = bodyError.length() > 120
                                                ? bodyError.substring(0, 120) + "..."
                                                : bodyError;
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            Log.e(TAG, "Error leyendo errorBody", ex);
                        }

                        cb.onError(errorMsg);
                        return;
                    }

                    // Aquí ya tenemos 200 OK con un RegistroResponse
                    RegistroResponse res = response.body();

                    if (res != null && res.isSuccess()) {
                        // ✅ válido → pasamos a Firebase
                        crearEnFirebase(nombre, dni, email, password, cb);
                    } else {
                        // ❌ backend dijo que no es válido
                        String msg = (res != null && res.getMessage() != null && !res.getMessage().isEmpty())
                                ? res.getMessage()
                                : "Registro inválido";
                        cb.onError(msg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RegistroResponse> call,
                                      @NonNull Throwable t) {
                    Log.e(TAG, "Fallo en llamada a /api/registro", t);
                    cb.onError("No se pudo contactar al servidor: "
                            + (t.getMessage() != null ? t.getMessage() : "Error de red"));
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error completo al registrar usuario", e);
            cb.onError(e.getMessage() != null ? e.getMessage() : "Error inesperado");
        }
    }

    private void crearEnFirebase(String nombre,
                                 String dni,
                                 String email,
                                 String password,
                                 @NonNull SimpleCallback cb) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Error en registro";
                        cb.onError(msg);
                        return;
                    }

                    String uid = auth.getCurrentUser() != null
                            ? auth.getCurrentUser().getUid()
                            : null;

                    if (uid == null) {
                        cb.onError("No se pudo obtener UID");
                        return;
                    }

                    Map<String, Object> profile = new HashMap<>();
                    profile.put("nombre", nombre);
                    profile.put("dni", dni);
                    profile.put("email", email);

                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("profile")
                            .setValue(profile)
                            .addOnSuccessListener(unused -> cb.onSuccess())
                            .addOnFailureListener(e -> cb.onError(
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Error guardando perfil"));
                });
    }

    public void resetPassword(String email,
                              @NonNull OnCompleteListener<Void> listener) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(listener);
    }
}