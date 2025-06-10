package com.red.newsapp.Services.Auth;

import android.content.Context;
import com.red.newsapp.Models.User;

import com.red.newsapp.Sqlite.DatabaseHelper;

public class AuthService {
    private DatabaseHelper dbHelper;

    public AuthService(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean isEmailExists(String email) {
        return dbHelper.isEmailExists(email);
    }

    public boolean register(RegisterRequest request) {
        String role = "user";
        return dbHelper.registerUser(request.getName(), request.getLastName(),
                request.getEmail(), request.getPassword(), role);
    }

    public AuthUser login(LoginRequest request) {
        boolean isValid = dbHelper.loginUser(request.getEmail(), request.getPassword());
        if (isValid) {
            DatabaseHelper.User user = dbHelper.getUserByEmail(request.getEmail());
            return convertToAuthUser(user);
        }
        return null;
    }

    private AuthUser convertToAuthUser(DatabaseHelper.User user) {
        if (user == null) return null;
        return new AuthUser(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
