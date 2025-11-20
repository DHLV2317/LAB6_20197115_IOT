package com.example.lab6_20197115.data;

public class RegistroRequest {

    // Estos nombres DEBEN coincidir con el backend:
    // private String nombre;
    // private String dni;
    // private String email;

    private String nombre;
    private String dni;
    private String email;

    public RegistroRequest(String nombre, String dni, String email) {
        this.nombre = nombre;
        this.dni = dni;
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }
}