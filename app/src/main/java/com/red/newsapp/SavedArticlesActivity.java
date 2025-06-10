package com.red.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.red.newsapp.api_response.Article;
import com.red.newsapp.news_adapters.SavedArticlesAdapter;
import com.red.newsapp.Sqlite.DatabaseHelper;

import java.util.ArrayList;

public class SavedArticlesActivity extends AppCompatActivity {
    private RecyclerView savedArticlesRV;
    private ScrollView scrollView;
    private TextView emptyStateText;
    private ArrayList<Article> savedArticlesArrayList;
    private SavedArticlesAdapter savedArticlesAdapter;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigationView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_articles);
        initViews();
        setupRecyclerView();
        loadSavedArticles();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the saved articles when returning to this activity
        loadSavedArticles();
    }

    private void initViews() {
        try {
            savedArticlesRV = findViewById(R.id.rvSavedArticles);
            progressBar = findViewById(R.id.progressBar);
            scrollView = findViewById(R.id.idSCSavedArticles);
            bottomNavigationView = findViewById(R.id.idSavedBottomNavigationBar);

            // PENTING: Inisialisasi emptyStateText - ini yang menyebabkan crash
            emptyStateText = findViewById(R.id.idEmptyStateText); // Pastikan ID ini ada di XML

            // Jika emptyStateText tidak ditemukan di XML, buat secara programmatik
            if (emptyStateText == null) {
                Log.w("SavedArticlesActivity", "emptyStateText not found in XML, creating programmatically");
                emptyStateText = new TextView(this);
                emptyStateText.setTextSize(16);
                emptyStateText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emptyStateText.setPadding(20, 20, 20, 20);
                // Anda perlu menambahkan TextView ini ke layout jika tidak ada di XML
            }

            dbHelper = new DatabaseHelper(this);
            savedArticlesArrayList = new ArrayList<>();
            savedArticlesAdapter = new SavedArticlesAdapter(savedArticlesArrayList, this);

        } catch (Exception e) {
            Log.e("SavedArticlesActivity", "Error in initViews: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        try {
            savedArticlesRV.setLayoutManager(new LinearLayoutManager(this));
            savedArticlesRV.setAdapter(savedArticlesAdapter);
        } catch (Exception e) {
            Log.e("SavedArticlesActivity", "Error in setupRecyclerView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupBottomNavigation() {
        try {
            bottomNavigationView.setSelectedItemId(R.id.app_bar_bookmark);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.app_bar_home) {
                    startActivity(new Intent(SavedArticlesActivity.this, NewsActivity.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.app_bar_profile) {
                    startActivity(new Intent(SavedArticlesActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                } else if (item.getItemId() == R.id.app_bar_bookmark) {
                    return true; // Already on this page
                }
                return false;
            });
        } catch (Exception e) {
            Log.e("SavedArticlesActivity", "Error in setupBottomNavigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSavedArticles() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);

            // Get current user from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("com.red.newsapp", MODE_PRIVATE);
            String email = sharedPreferences.getString("email", null);

            if (email != null) {
                DatabaseHelper.User user = dbHelper.getUserByEmail(email);
                if (user != null) {
                    // Load saved articles for this user
                    ArrayList<Article> userSavedArticles = dbHelper.getSavedArticlesByUserId(user.getId());

                    savedArticlesArrayList.clear();
                    savedArticlesArrayList.addAll(userSavedArticles);
                    savedArticlesAdapter.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);

                    // Show/hide empty state - dengan null check
                    if (savedArticlesArrayList.isEmpty()) {
                        if (emptyStateText != null) {
                            emptyStateText.setVisibility(View.VISIBLE);
                            emptyStateText.setText("No saved articles yet.\nStart saving articles to see them here!");
                        }
                        savedArticlesRV.setVisibility(View.GONE);
                    } else {
                        if (emptyStateText != null) {
                            emptyStateText.setVisibility(View.GONE);
                        }
                        savedArticlesRV.setVisibility(View.VISIBLE);
                    }

                } else {
                    showError("User not found. Please login again.");
                }
            } else {
                // User not logged in
                showError("Please login to view saved articles.");
                // Redirect to login
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } catch (Exception e) {
            Log.e("SavedArticlesActivity", "Error in loadSavedArticles: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading saved articles: " + e.getMessage());
        }
    }

    private void showError(String message) {
        try {
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.VISIBLE);
                emptyStateText.setText(message);
            }

            savedArticlesRV.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("SavedArticlesActivity", "Error in showError: " + e.getMessage());
            e.printStackTrace();
        }
    }
}