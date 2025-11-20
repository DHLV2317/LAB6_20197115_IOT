package com.example.lab6_20197115.services;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class StorageService {

    private final FirebaseStorage storage;

    public StorageService() {
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Sube un archivo a Firebase Storage y devuelve un Task con la URL de descarga.
     */
    public Task<Uri> uploadFile(String folder, String filename, Uri fileUri) {
        StorageReference ref = storage.getReference()
                .child(folder)
                .child(filename);

        UploadTask uploadTask = ref.putFile(fileUri);

        // Cuando termine la subida, pedimos el downloadUrl
        return uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        });
    }
}