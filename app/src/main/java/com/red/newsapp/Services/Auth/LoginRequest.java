package com.red.newsapp.Services.Auth;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // getter
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}