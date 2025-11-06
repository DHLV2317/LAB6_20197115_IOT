package com.example.lab6_20197115.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20197115.R;
import com.example.lab6_20197115.data.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class TasksFragment extends Fragment implements TaskAdapter.Callbacks {

    private DatabaseReference ref;      // ✅ Realtime DB
    private TaskAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_tasks, c, false);

        RecyclerView rv = v.findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(this);
        rv.setAdapter(adapter);

        v.findViewById(R.id.btnAdd).setOnClickListener(x -> showNewDialog(null));
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // ✅ Ruta Realtime: users/{uid}/tasks
        ref = FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("tasks");

        // Lectura en tiempo real
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                List<Task> list = new ArrayList<>();
                for (DataSnapshot d : snap.getChildren()) {
                    Task t = d.getValue(Task.class);
                    if (t != null) { t.id = d.getKey(); list.add(t); }
                }
                adapter.set(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                toast("Error DB: " + error.getMessage());
            }
        });
    }

    // Tap = editar
    @Override public void onClick(Task t) { showNewDialog(t); }

    // Tacho = eliminar
    @Override public void onLong(Task t) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar tarea")
                .setMessage("¿Deseas eliminar \"" + t.title + "\"?")
                .setPositiveButton("Eliminar", (d, w) ->
                        ref.child(t.id).removeValue()
                                .addOnSuccessListener(x -> snack("Tarea eliminada"))
                                .addOnFailureListener(e -> toast("Error: " + e.getMessage())))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showNewDialog(@Nullable Task editing) {
        View form = LayoutInflater.from(getContext()).inflate(R.layout._dialog_task, null);
        EditText inTitle = form.findViewById(R.id.inTitle);
        EditText inDesc  = form.findViewById(R.id.inDesc);
        EditText inDate  = form.findViewById(R.id.inDate);
        Spinner spEstado = form.findViewById(R.id.spEstado);     // ✅ Spinner en el layout

        // Spinner estado: Pendiente / Completada
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"Pendiente", "Completada"});
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEstado.setAdapter(stateAdapter);

        final long[] chosenDate = { System.currentTimeMillis() };
        inDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                Calendar c = Calendar.getInstance();
                c.set(y, m, d, 0, 0, 0);
                chosenDate[0] = c.getTimeInMillis();
                inDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date(chosenDate[0])));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (editing != null) {
            inTitle.setText(editing.title);
            inDesc.setText(editing.description);
            if (editing.dueDate > 0)
                inDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date(editing.dueDate)));
            spEstado.setSelection(editing.completed ? 1 : 0);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editing == null ? "Nueva tarea" : "Editar tarea")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (d, w) -> {
                    String title = str(inTitle);
                    if (title.isEmpty()) { toast("Título requerido"); return; }
                    boolean completed = spEstado.getSelectedItemPosition() == 1;

                    Task t = editing == null ? new Task() : editing;
                    t.title = title;
                    t.description = str(inDesc);
                    t.dueDate = chosenDate[0];
                    t.completed = completed;

                    if (editing == null) {
                        String key = ref.push().getKey();
                        t.id = key;
                        ref.child(key).setValue(t)
                                .addOnSuccessListener(x -> snack("Tarea creada"))
                                .addOnFailureListener(e -> toast("Error: " + e.getMessage()));
                    } else {
                        ref.child(t.id).setValue(t)
                                .addOnSuccessListener(x -> snack("Tarea actualizada"))
                                .addOnFailureListener(e -> toast("Error: " + e.getMessage()));
                    }
                }).show();
    }

    private String str(EditText e){ return e.getText()==null? "": e.getText().toString().trim(); }
    private void snack(String s){ Snackbar.make(requireView(), s, Snackbar.LENGTH_SHORT).show(); }
    private void toast(String s){ Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show(); }
}