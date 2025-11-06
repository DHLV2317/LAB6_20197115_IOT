package com.example.lab6_20197115.data;

public class Task {
    public String id;
    public String title;
    public String description;
    public long dueDate;
    public boolean completed;

    // Necesario para Firebase
    public Task() { }

    // Útil para crear/editar desde el diálogo
    public Task(String title, String description, long dueDate, boolean completed) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
    }
}