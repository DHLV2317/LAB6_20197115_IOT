package com.example.lab6_20197115.ui.auth;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20197115.R;
import com.example.lab6_20197115.services.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private AuthService authService;
    private TextInputEditText inName, inDni, inEmail, inPass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = new AuthService();

        inName  = findViewById(R.id.inputName);
        inDni   = findViewById(R.id.inputDni);
        inEmail = findViewById(R.id.inputEmail);
        inPass  = findViewById(R.id.inputPassword);

        ((MaterialButton) findViewById(R.id.btnRegister)).setOnClickListener(v -> {
            String nombre = text(inName);
            String dni    = text(inDni);
            String email  = text(inEmail);
            String pass   = text(inPass);

            if (nombre.isEmpty()) { toast("Nombre requerido"); return; }
            if (dni.isEmpty() || dni.length() != 8) {
                toast("DNI debe tener 8 dígitos"); return;
            }
            if (email.isEmpty()) { toast("Correo requerido"); return; }
            if (pass.isEmpty())  { toast("Contraseña requerida"); return; }

            authService.registerUser(
                    nombre, dni, email, pass,
                    new AuthService.SimpleCallback() {
                        @Override public void onSuccess() {
                            toast("Usuario creado correctamente");
                            finish(); // volver a Login
                        }

                        @Override public void onError(String message) {
                            toast("Error registro: " + message);
                        }
                    }
            );
        });
    }

    private static String text(TextInputEditText e){
        return e.getText()==null ? "" : e.getText().toString().trim();
    }

    private void toast(String s){
        android.widget.Toast.makeText(this, s, android.widget.Toast.LENGTH_SHORT).show();
    }
}