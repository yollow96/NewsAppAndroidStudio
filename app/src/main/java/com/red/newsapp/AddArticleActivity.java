package com.red.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.red.newsapp.Sqlite.DatabaseHelper;

public class AddArticleActivity extends AppCompatActivity {

    private static final String TAG = "AddArticleActivity";
    private EditText titleEditText, descriptionEditText, urlEditText, contentEditText;
    private Spinner categorySpinner;
    private Button addArticleButton, cancelAddArticleButton;
    private DatabaseHelper databaseHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get current user ID from SharedPreferences - mengikuti pola ProfileFragment
        SharedPreferences sharedPreferences = getSharedPreferences("com.red.newsapp", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null); // ✅ Menggunakan kunci "email" seperti ProfileFragment

        if (email != null) {
            DatabaseHelper.User user = databaseHelper.getUserByEmail(email);
            if (user != null) {
                currentUserId = user.getId();
                Log.d(TAG, "Current User ID: " + currentUserId);
                Log.d(TAG, "Current User Email: " + email);
            } else {
                Toast.makeText(this, "User tidak ditemukan. Silakan login kembali.", Toast.LENGTH_SHORT).show();
                redirectToLogin();
                return;
            }
        } else {
            Toast.makeText(this, "Silakan login terlebih dahulu.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        titleEditText = findViewById(R.id.idEtArticleTitle);
        descriptionEditText = findViewById(R.id.idEtArticleDescription);
        urlEditText = findViewById(R.id.idEtArticleUrl);
        contentEditText = findViewById(R.id.idEtArticleContent);
        categorySpinner = findViewById(R.id.idSpCategory);
        addArticleButton = findViewById(R.id.idBtnAddArticle);
        cancelAddArticleButton = findViewById(R.id.idBtnAddArticleCancel);
    }

    private void setupClickListeners() {
        addArticleButton.setOnClickListener(v -> addArticle());

        cancelAddArticleButton.setOnClickListener(v -> {
            // Kembali ke fragment sebelumnya atau main activity
            finish();
        });
    }

    private void addArticle() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String url = urlEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem() != null ?
                categorySpinner.getSelectedItem().toString().trim() : "";

        // Debug: Log semua input
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Description: " + description);
        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "Content: " + content);
        Log.d(TAG, "Category: " + category);

        // Validasi input
        if (!validateInput(title, description, url, content, category)) {
            return;
        }

        // Disable button saat sedang menyimpan
        addArticleButton.setEnabled(false);
        addArticleButton.setText("Menambahkan...");

        try {
            // Simpan artikel ke database
            boolean isSuccess = databaseHelper.addArticle(
                    currentUserId,
                    title,
                    description,
                    content,
                    url,
                    "", // urlToImage - kosong untuk saat ini
                    category
            );

            // Enable kembali button
            addArticleButton.setEnabled(true);
            addArticleButton.setText("Tambah Artikel");

            if (isSuccess) {
                Log.d(TAG, "Artikel berhasil ditambahkan ke database");
                Toast.makeText(this, "Artikel berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                // Kembali ke activity/fragment sebelumnya
                finish();
            } else {
                Log.e(TAG, "Gagal menambahkan artikel ke database");
                Toast.makeText(this, "Gagal menambahkan artikel. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            // Enable kembali button jika terjadi exception
            addArticleButton.setEnabled(true);
            addArticleButton.setText("Tambah Artikel");

            Log.e(TAG, "Exception saat menambahkan artikel", e);
            Toast.makeText(this, "Terjadi kesalahan sistem", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String title, String description, String url, String content, String category) {
        if (title.isEmpty()) {
            Toast.makeText(this, "Judul artikel harus diisi", Toast.LENGTH_SHORT).show();
            titleEditText.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Deskripsi artikel harus diisi", Toast.LENGTH_SHORT).show();
            descriptionEditText.requestFocus();
            return false;
        }

        if (url.isEmpty()) {
            Toast.makeText(this, "URL artikel harus diisi", Toast.LENGTH_SHORT).show();
            urlEditText.requestFocus();
            return false;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Konten artikel harus diisi", Toast.LENGTH_SHORT).show();
            contentEditText.requestFocus();
            return false;
        }

        if (category.isEmpty() || category.equals("Pilih Kategori")) {
            Toast.makeText(this, "Kategori harus dipilih", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validasi format URL
        if (!isValidUrl(url)) {
            Toast.makeText(this, "Format URL tidak valid", Toast.LENGTH_SHORT).show();
            urlEditText.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void redirectToLogin() {
        // Redirect ke login activity jika diperlukan
        Intent intent = new Intent(AddArticleActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}