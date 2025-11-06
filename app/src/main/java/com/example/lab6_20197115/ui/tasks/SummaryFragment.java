package com.example.lab6_20197115.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lab6_20197115.R;
import com.example.lab6_20197115.data.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SummaryFragment extends Fragment {

    private View barDone, barPend, root;
    private TextView tTotal, tDone, tPend;
    private ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_summary, container, false);

        tTotal = root.findViewById(R.id.tTotal);
        tDone  = root.findViewById(R.id.tDone);
        tPend  = root.findViewById(R.id.tPend);
        barDone = root.findViewById(R.id.barDone);
        barPend = root.findViewById(R.id.barPend);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("tasks");

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0, done = 0;
                for (DataSnapshot d : snapshot.getChildren()) {
                    Task t = d.getValue(Task.class);
                    if (t != null) {
                        total++;
                        if (t.completed) done++;
                    }
                }

                int pend = total - done;
                int finalTotal = total;
                int finalDone = done;
                int finalPend = pend;

                tTotal.setText("Total de tareas: " + finalTotal);
                tDone.setText("Completadas: " + finalDone);
                tPend.setText("Pendientes: " + finalPend);

                if (root == null) return;

                root.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        root.getViewTreeObserver().removeOnPreDrawListener(this);
                        int maxW = root.getWidth();
                        float fd = finalTotal == 0 ? 0f : (finalDone / (float) finalTotal);
                        float fp = finalTotal == 0 ? 0f : (finalPend / (float) finalTotal);

                        barDone.getLayoutParams().width = (int) (maxW * fd);
                        barPend.getLayoutParams().width = (int) (maxW * fp);
                        barDone.requestLayout();
                        barPend.requestLayout();
                        return true;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        ref.addValueEventListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listener != null) {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                FirebaseDatabase.getInstance()
                        .getReference("users").child(uid).child("tasks")
                        .removeEventListener(listener);
            }
        }
    }
}