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

public class EditArticleActivity extends AppCompatActivity {

    private static final String TAG = "EditArticleActivity";
    private int articleId, currentUserId;
    private String originalTitle, originalDescription, originalContent, originalUrl, originalCategory;

    private EditText titleEditText, descriptionEditText, contentEditText, urlEditText;
    private Spinner categorySpinner;
    private Button updateArticleButton, cancelUpdateArticleButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_article);

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
        getIntentData();
        loadArticleData();
        setupClickListeners();
    }

    private void initViews() {
        titleEditText = findViewById(R.id.idEtUpdateArticleTitle);
        descriptionEditText = findViewById(R.id.idEtUpdateArticleDescription);
        contentEditText = findViewById(R.id.idEtUpdateArticleContent);
        urlEditText = findViewById(R.id.idEtUpdateArticleUrl);
        categorySpinner = findViewById(R.id.idSpUpdateCategory);
        updateArticleButton = findViewById(R.id.idBtnUpdateArticle);
        cancelUpdateArticleButton = findViewById(R.id.idBtnUpdateArticleCancel);
    }

    private void getIntentData() {
        // Ambil data dari Intent
        articleId = getIntent().getIntExtra("id", -1);
        originalTitle = getIntent().getStringExtra("title");
        originalDescription = getIntent().getStringExtra("description");
        originalContent = getIntent().getStringExtra("content");
        originalUrl = getIntent().getStringExtra("url");
        originalCategory = getIntent().getStringExtra("category");

        Log.d(TAG, "Article ID: " + articleId);
        Log.d(TAG, "Original Title: " + originalTitle);

        if (articleId == -1) {
            Toast.makeText(this, "ID artikel tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void loadArticleData() {
        // Jika data tidak lengkap dari Intent, ambil dari database
        if (originalTitle == null || originalDescription == null) {
            DatabaseHelper.UserArticle article = databaseHelper.getArticleById(articleId);
            if (article != null) {
                // Verifikasi bahwa artikel ini milik user yang sedang login
                if (article.getUserId() != currentUserId) {
                    Toast.makeText(this, "Anda tidak memiliki izin untuk mengedit artikel ini", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                originalTitle = article.getTitle();
                originalDescription = article.getDescription();
                originalContent = article.getContent();
                originalUrl = article.getUrl();
                originalCategory = article.getCategory();
            } else {
                Toast.makeText(this, "Artikel tidak ditemukan", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        setValues();
    }

    private void setValues() {
        if (originalTitle != null) titleEditText.setText(originalTitle);
        if (originalDescription != null) descriptionEditText.setText(originalDescription);
        if (originalContent != null) contentEditText.setText(originalContent);
        if (originalUrl != null) urlEditText.setText(originalUrl);

        // Set kategori di spinner
        if (originalCategory != null && categorySpinner.getAdapter() != null) {
            for (int i = 0; i < categorySpinner.getCount(); i++) {
                if (categorySpinner.getItemAtPosition(i).toString().equals(originalCategory)) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupClickListeners() {
        updateArticleButton.setOnClickListener(v -> updateArticle());

        cancelUpdateArticleButton.setOnClickListener(v -> {
            // Kembali ke activity/fragment sebelumnya
            finish();
        });
    }

    private void updateArticle() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String url = urlEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem() != null ?
                categorySpinner.getSelectedItem().toString().trim() : "";

        // Debug: Log semua input
        Log.d(TAG, "Updated Title: " + title);
        Log.d(TAG, "Updated Description: " + description);
        Log.d(TAG, "Updated URL: " + url);
        Log.d(TAG, "Updated Content: " + content);
        Log.d(TAG, "Updated Category: " + category);

        // Validasi input
        if (!validateInput(title, description, url, content, category)) {
            return;
        }

        // Disable button saat sedang update
        updateArticleButton.setEnabled(false);
        updateArticleButton.setText("Memperbarui...");

        try {
            // Update artikel di database
            boolean isSuccess = databaseHelper.updateArticle(
                    articleId,
                    title,
                    description,
                    content,
                    url,
                    "", // urlToImage - kosong untuk saat ini
                    category
            );

            // Enable kembali button
            updateArticleButton.setEnabled(true);
            updateArticleButton.setText("Perbarui Artikel");

            if (isSuccess) {
                Log.d(TAG, "Artikel berhasil diperbarui di database");
                Toast.makeText(this, "Artikel berhasil diperbarui", Toast.LENGTH_SHORT).show();

                // Kembali ke activity/fragment sebelumnya
                finish();
            } else {
                Log.e(TAG, "Gagal memperbarui artikel di database");
                Toast.makeText(this, "Gagal memperbarui artikel. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            // Enable kembali button jika terjadi exception
            updateArticleButton.setEnabled(true);
            updateArticleButton.setText("Perbarui Artikel");

            Log.e(TAG, "Exception saat memperbarui artikel", e);
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
        Intent intent = new Intent(EditArticleActivity.this, MainActivity.class);
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