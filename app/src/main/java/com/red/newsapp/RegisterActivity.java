package com.red.newsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.red.newsapp.Sqlite.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, firstNameEditText, lastNameEditText;
    private Button registerButton;
    private TextView loginTextView;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        initViews();

        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        registerUser();
    }

    private void registerUser() {
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String name = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Silakan isi semua kolom", Toast.LENGTH_SHORT).show();
            } else {
                boolean isRegistered = dbHelper.registerUser(name, lastName, email, password, "user");
                if (isRegistered) {
                    Toast.makeText(this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initViews() {
        emailEditText = findViewById(R.id.idEtRegisterEmail);
        passwordEditText = findViewById(R.id.idEtRegisterPassword);
        firstNameEditText = findViewById(R.id.idEtRegisterName);
        lastNameEditText = findViewById(R.id.idEtRegisterLastName);
        registerButton = findViewById(R.id.idBtnRegister);
        loginTextView = findViewById(R.id.idTvLogin);
    }
}
