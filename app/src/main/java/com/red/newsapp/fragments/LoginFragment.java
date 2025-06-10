
package com.red.newsapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.red.newsapp.R;
import com.red.newsapp.Sqlite.DatabaseHelper;

public class LoginFragment extends Fragment {

    private Button loginButton;
    private TextView registerTextView;
    private EditText emailEditText, passwordEditText;
    private DatabaseHelper dbHelper;

    public interface LoginFragmentListener {
        void onLoginSuccess();
        void onNavigateToRegister();
    }

    private LoginFragmentListener listener;

    public void setLoginFragmentListener(LoginFragmentListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        loginButton = view.findViewById(R.id.idBtnLogin);
        registerTextView = view.findViewById(R.id.idTvRegister);
        emailEditText = view.findViewById(R.id.idEtEmailLogin);
        passwordEditText = view.findViewById(R.id.idEtPasswordLogin);

        // Initialize database helper
        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
        }

        // Check if email was passed as argument
        Bundle args = getArguments();
        if (args != null && args.containsKey("email")) {
            String email = args.getString("email");
            if (email != null && !email.isEmpty()) {
                emailEditText.setText(email);
            }
        }
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());

        registerTextView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNavigateToRegister();
            }
        });
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (email.isEmpty()) {
            emailEditText.setError("Email tidak boleh kosong");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password tidak boleh kosong");
            passwordEditText.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Format email tidak valid");
            emailEditText.requestFocus();
            return;
        }

        // Check if context and dbHelper are available
        if (getContext() == null || dbHelper == null) {
            Toast.makeText(getContext(), "Terjadi kesalahan sistem", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            boolean validUser = dbHelper.loginUser(email, password);

            if (validUser) {
                DatabaseHelper.User user = dbHelper.getUserByEmail(email);

                if (user != null) {
                    // Save user data to SharedPreferences
                    saveUserSession(user);

                    Toast.makeText(getContext(), "Login berhasil", Toast.LENGTH_SHORT).show();

                    // Notify listener that login was successful
                    if (listener != null) {
                        listener.onLoginSuccess();
                    }
                } else {
                    Toast.makeText(getContext(), "Terjadi kesalahan saat mengambil data user", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Email atau password salah", Toast.LENGTH_SHORT).show();
                passwordEditText.setText(""); // Clear password field
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserSession(DatabaseHelper.User user) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.red.newsapp", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", user.getId());
        editor.putString("email", user.getEmail());
        editor.putString("name", user.getName());
        editor.putString("lastName", user.getLastName());
        editor.putString("role", user.getRole());
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    // Static method to create fragment with email parameter
    public static LoginFragment newInstance(String email) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}