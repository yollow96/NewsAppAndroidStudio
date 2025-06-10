package com.red.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.red.newsapp.Models.User;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.red.newsapp.Sqlite.DatabaseHelper;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CardView cardView;
    private ScrollView scrollView;
    private Button loginButton, registerButton, logoutButton;
    private TextView nameTextView, lastNameTextView, emailTextView;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        dbHelper = new DatabaseHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("com.red.newsapp", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.app_bar_home) {
                startActivity(new Intent(this, NewsActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.app_bar_bookmark) {
                startActivity(new Intent(this, SavedArticlesActivity.class));
                finish();
                return true;
            }
            return false;
        });

        if (email != null) {
            // User sudah login
            DatabaseHelper.User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                // Tampilkan profil user
                scrollView.setVisibility(ScrollView.VISIBLE);
                cardView.setVisibility(CardView.GONE);

                nameTextView.setText(user.getName());
                lastNameTextView.setText(user.getLastName());
                emailTextView.setText(user.getEmail());

                logoutButton.setOnClickListener(v -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Restart activity untuk refresh UI
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                });
            } else {
                Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
                // Jika user tidak ditemukan, tampilkan UI belum login
                scrollView.setVisibility(ScrollView.GONE);
                cardView.setVisibility(CardView.VISIBLE);
            }
        } else {
            // User belum login
            scrollView.setVisibility(ScrollView.GONE);
            cardView.setVisibility(CardView.VISIBLE);

            loginButton.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });

            registerButton.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
            });
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.idProfileBottomNavigationBar);
        cardView = findViewById(R.id.idProfileLoginCardView);
        scrollView = findViewById(R.id.idProfileScrollView);
        loginButton = findViewById(R.id.idBtnLogin);
        registerButton = findViewById(R.id.idBtnRegister);
        logoutButton = findViewById(R.id.idProfileBtnLogout);
        nameTextView = findViewById(R.id.idTvProfileName);
        lastNameTextView = findViewById(R.id.idTvProfileLastName);
        emailTextView = findViewById(R.id.idTvProfileEmail);
    }
}
