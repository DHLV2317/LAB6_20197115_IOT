package com.example.lab6_20197115.ui.auth;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20197115.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText inEmail, inPass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        inEmail = findViewById(R.id.inputEmail);
        inPass  = findViewById(R.id.inputPassword);

        ((MaterialButton) findViewById(R.id.btnRegister)).setOnClickListener(v -> {
            String email = text(inEmail), pass = text(inPass);
            if (email.isEmpty() || pass.isEmpty()) { toast("Completa todo"); return; }
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(t -> {
                if (t.isSuccessful()) { toast("Cuenta creada. Inicia sesión"); finish(); }
                else toast("Error: " + t.getException().getMessage());
            });
        });
    }

    private static String text(TextInputEditText e){
        return e.getText()==null ? "" : e.getText().toString().trim();
    }

    private void toast(String s){
        android.widget.Toast.makeText(this, s, android.widget.Toast.LENGTH_SHORT).show();
    }
}