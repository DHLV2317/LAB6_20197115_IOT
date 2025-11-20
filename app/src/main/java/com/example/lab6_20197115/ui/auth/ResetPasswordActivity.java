package com.example.lab6_20197115.ui.auth;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20197115.R;
import com.example.lab6_20197115.services.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText inEmail;
    private AuthService authService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        inEmail = findViewById(R.id.inputEmail);
        authService = new AuthService();

        MaterialButton btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            String email = inEmail.getText() == null ? "" :
                    inEmail.getText().toString().trim();

            if (email.isEmpty()) {
                toast("Ingresa tu correo PUCP");
                return;
            }

            authService.resetPassword(email, task -> {
                if (task.isSuccessful()) {
                    toast("Se envió un enlace a tu correo");
                    finish();
                } else {
                    String msg = task.getException() != null ?
                            task.getException().getMessage() : "Error";
                    toast("Error: " + msg);
                }
            });
        });
    }

    private void toast(String s) {
        android.widget.Toast.makeText(this, s,
                android.widget.Toast.LENGTH_SHORT).show();
    }
}