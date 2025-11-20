package com.example.lab6_20197115.ui.storage;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.lab6_20197115.R;
import com.example.lab6_20197115.services.StorageService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class UploadActivity extends AppCompatActivity {

    private ImageView preview;
    private TextView tvNombre, tvDni, tvEmail;
    private Uri selectedUri = null;
    private StorageService storageService;
    private ActivityResultLauncher<String> pickImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        preview   = findViewById(R.id.imgPreview);
        tvNombre  = findViewById(R.id.tvNombre);
        tvDni     = findViewById(R.id.tvDni);
        tvEmail   = findViewById(R.id.tvEmail);

        storageService = new StorageService();

        // Cargar perfil de BD
        loadProfileData();
        loadProfileImage();

        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .transform(new CircleCrop())
                                .into(preview);
                    }
                }
        );

        MaterialButton btnPick = findViewById(R.id.btnPickImage);
        MaterialButton btnUpload = findViewById(R.id.btnUpload);

        btnPick.setOnClickListener(v -> pickImage.launch("image/*"));

        btnUpload.setOnClickListener(v -> {
            if (selectedUri == null) {
                toast("Selecciona una imagen");
                return;
            }

            String filename = "perfil_" + System.currentTimeMillis() + ".jpg";

            storageService
                    .uploadFile("imagenes", filename, selectedUri)
                    .addOnSuccessListener(url -> {
                        toast("Imagen subida correctamente");
                        saveUrlToDatabase(url.toString());
                    })
                    .addOnFailureListener(e -> toast("Error: " + e.getMessage()));
        });
    }

    private void loadProfileData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("profile");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvNombre.setText("Nombre: " + snapshot.child("nombre").getValue(String.class));
                tvDni.setText("DNI: " + snapshot.child("dni").getValue(String.class));
                tvEmail.setText("Correo: " + snapshot.child("email").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast("Error cargando datos: " + error.getMessage());
            }
        });
    }

    private void loadProfileImage() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("profileImageUrl");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = snapshot.getValue(String.class);
                if (url != null && !url.isEmpty()) {
                    Glide.with(UploadActivity.this)
                            .load(url)
                            .placeholder(R.drawable.ic_person)
                            .transform(new CircleCrop())
                            .into(preview);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveUrlToDatabase(String url) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("profileImageUrl")
                .setValue(url)
                .addOnSuccessListener(unused -> {
                    toast("Foto actualizada");
                    loadProfileImage();
                })
                .addOnFailureListener(e -> toast("Error: " + e.getMessage()));
    }

    private void toast(String m){
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}