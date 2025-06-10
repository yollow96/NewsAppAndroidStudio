package com.red.newsapp.Services.Auth;

public class AuthResponse {
    private String token;
    private boolean success;
    private String message;
    private AuthUser user;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, boolean success, String message, AuthUser user) {
        this.token = token;
        this.success = success;
        this.message = message;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }
}