package com.red.newsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.red.newsapp.Sqlite.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private TextView registerTextView;
    private EditText emailEditText, passwordEditText;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        initViews();

        login();

        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void login() {
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Silakan isi semua kolom", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean validUser = dbHelper.loginUser(email, password);

            if (validUser) {
                DatabaseHelper.User user = dbHelper.getUserByEmail(email);

                SharedPreferences sharedPreferences = getSharedPreferences("com.red.newsapp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("id", user.getId());
                editor.putString("email", user.getEmail());
                editor.putString("name", user.getName());
                editor.putString("lastName", user.getLastName());
                editor.putString("role", user.getRole());
                editor.apply();

                Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        loginButton = findViewById(R.id.idBtnLogin);
        registerTextView = findViewById(R.id.idTvRegister);
        emailEditText = findViewById(R.id.idEtEmailLogin);
        passwordEditText = findViewById(R.id.idEtPasswordLogin);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        if (email != null) {
            emailEditText.setText(email);
        }
    }
}
