package com.example.lab6_20197115.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20197115.R;
import com.example.lab6_20197115.data.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    public interface Callbacks {
        void onClick(Task t);   // editar
        void onLong(Task t);    // eliminar (lo usamos para el tacho)
    }

    private final Callbacks cb;
    private final List<Task> data = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TaskAdapter(Callbacks cb){ this.cb = cb; }

    public void set(List<Task> list){
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Task t = data.get(i);

        h.tTitle.setText(t.title);
        h.tDesc.setText(t.description == null ? "" : t.description);
        h.tState.setText(t.completed ? "completada" : "pendiente");
        h.tDate.setText(t.dueDate > 0 ? fmt.format(new Date(t.dueDate)) : "Sin fecha");

        // Colorcito opcional para estado
        h.tState.setTextColor(t.completed ? 0xFF4CAF50 : 0xFFFF9800);

        // Tap en el item = editar
        h.itemView.setOnClickListener(v -> cb.onClick(t));

        // Click en el tacho = eliminar (reusa cb.onLong)
        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(v -> cb.onLong(t));
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tTitle, tDesc, tState, tDate;
        ImageView btnDelete;

        VH(@NonNull View v){
            super(v);
            tTitle = v.findViewById(R.id.tTitle);
            tDesc  = v.findViewById(R.id.tDesc);
            tState = v.findViewById(R.id.tState);
            tDate  = v.findViewById(R.id.tDate);
            btnDelete = v.findViewById(R.id.btnDelete); // puede ser null si no existe en XML
        }
    }
}