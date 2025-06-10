package com.red.newsapp.Services.Auth;

public class AuthUser {
    private int id;
    private String name;
    private String lastName;
    private String email;
    private String role;

    public AuthUser(int id, String name, String lastName, String email, String role) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}