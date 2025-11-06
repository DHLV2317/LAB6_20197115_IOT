package com.example.lab6_20197115.ui.tasks;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.lab6_20197115.R;
import com.example.lab6_20197115.ui.auth.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_SELECTED_ITEM = "selected_item";
    private MaterialToolbar toolbar;
    private BottomNavigationView bottom;
    private int selectedItemId = R.id.m_tasks;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);

        // Bloquea si no hay sesión
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goLoginAndFinish();
            return;
        }

        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottom = findViewById(R.id.bottom);

        // Restaurar tab seleccionado si rotó la pantalla
        if (b != null) {
            selectedItemId = b.getInt(KEY_SELECTED_ITEM, R.id.m_tasks);
        }

        bottom.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.m_tasks) {
                selectedItemId = id;
                show(new TasksFragment(), getString(R.string.title_tasks));
            } else if (id == R.id.m_summary) {
                selectedItemId = id;
                show(new SummaryFragment(), getString(R.string.title_summary));
            } else if (id == R.id.m_logout) {
                // Cerrar sesión y volver a Login
                FirebaseAuth.getInstance().signOut();
                goLoginAndFinish();
                return false; // no dejar marcado "logout"
            }
            return true;
        });

        // Selección inicial (o restaurada)
        bottom.setSelectedItemId(selectedItemId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Seguridad extra: si perdió sesión, volver a login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            goLoginAndFinish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_ITEM, selectedItemId);
    }

    private void show(Fragment f, String title){
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
        else if (toolbar != null) toolbar.setTitle(title);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.container, f)
                .commit();
    }

    private void goLoginAndFinish() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}