package com.red.newsapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.red.newsapp.R;
import com.red.newsapp.Sqlite.DatabaseHelper;
import com.red.newsapp.api_response.Article;
import com.red.newsapp.news_adapters.SavedArticlesAdapter;

import java.util.ArrayList;

public class SavedArticlesFragment extends Fragment {
    private static final String TAG = "SavedArticlesFragment";

    private RecyclerView savedArticlesRV;
    private ScrollView scrollView;
    private TextView emptyStateText;
    private ArrayList<Article> savedArticlesArrayList;
    private SavedArticlesAdapter savedArticlesAdapter;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;

    public interface SavedArticlesFragmentListener {
        void onNavigateToLogin();
        void onUserNotLoggedIn();
    }

    private SavedArticlesFragmentListener listener;

    public void setSavedArticlesFragmentListener(SavedArticlesFragmentListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_articles, container, false);

        initViews(view);
        setupRecyclerView();
        loadSavedArticles();

        return view;
    }

    private void initViews(View view) {
        savedArticlesRV = view.findViewById(R.id.rvSavedArticles);
        progressBar = view.findViewById(R.id.progressBar);
        scrollView = view.findViewById(R.id.idSCSavedArticles);
        emptyStateText = view.findViewById(R.id.idEmptyStateText);

        // Initialize database helper
        if (getContext() != null) {
            dbHelper = new DatabaseHelper(getContext());
        }
    }

    private void setupRecyclerView() {
        savedArticlesArrayList = new ArrayList<>();

        if (getContext() != null) {
            savedArticlesAdapter = new SavedArticlesAdapter(savedArticlesArrayList, getContext());
            savedArticlesRV.setLayoutManager(new LinearLayoutManager(getContext()));
            savedArticlesRV.setAdapter(savedArticlesAdapter);
        }
    }

    private void loadSavedArticles() {
        showLoading(true);

        // Check if context and dbHelper are available
        if (getContext() == null || dbHelper == null) {
            showError("Terjadi kesalahan sistem");
            return;
        }

        try {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.red.newsapp", getContext().MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
            String email = sharedPreferences.getString("email", null);

            if (!isLoggedIn || email == null || email.isEmpty()) {
                handleUserNotLoggedIn();
                return;
            }

            DatabaseHelper.User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                loadUserSavedArticles(user.getId());
            } else {
                handleUserNotFound();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading saved articles: " + e.getMessage(), e);
            showError("Terjadi kesalahan saat memuat artikel tersimpan");
        }
    }

    private void loadUserSavedArticles(int userId) {
        try {
            ArrayList<Article> userSavedArticles = dbHelper.getSavedArticlesByUserId(userId);

            savedArticlesArrayList.clear();
            savedArticlesArrayList.addAll(userSavedArticles);

            if (savedArticlesAdapter != null) {
                savedArticlesAdapter.notifyDataSetChanged();
            }

            showLoading(false);
            showContent(true);

            if (savedArticlesArrayList.isEmpty()) {
                showEmptyState();
            } else {
                showArticlesList();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user saved articles: " + e.getMessage(), e);
            showError("Terjadi kesalahan saat memuat artikel");
        }
    }

    private void handleUserNotLoggedIn() {
        showError("Silakan login untuk melihat artikel tersimpan");

        // Notify listener that user is not logged in
        if (listener != null) {
            listener.onUserNotLoggedIn();
            listener.onNavigateToLogin();
        }
    }

    private void handleUserNotFound() {
        showError("Data pengguna tidak ditemukan. Silakan login ulang");

        // Clear invalid session
        clearUserSession();

        if (listener != null) {
            listener.onNavigateToLogin();
        }
    }

    private void clearUserSession() {
        if (getContext() != null) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.red.newsapp", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showContent(boolean show) {
        if (scrollView != null) {
            scrollView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState() {
        if (emptyStateText != null && savedArticlesRV != null) {
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("Belum ada artikel tersimpan.\nMulai simpan artikel untuk melihatnya di sini!");
            savedArticlesRV.setVisibility(View.GONE);
        }
    }

    private void showArticlesList() {
        if (emptyStateText != null && savedArticlesRV != null) {
            emptyStateText.setVisibility(View.GONE);
            savedArticlesRV.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        showLoading(false);
        showContent(true);

        if (emptyStateText != null && savedArticlesRV != null) {
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(message);
            savedArticlesRV.setVisibility(View.GONE);
        }

        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Method to refresh saved articles (can be called from parent activity/fragment)
    public void refreshSavedArticles() {
        loadSavedArticles();
    }

    // Method to check if user is logged in
    public boolean isUserLoggedIn() {
        if (getContext() != null) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.red.newsapp", getContext().MODE_PRIVATE);
            return sharedPreferences.getBoolean("isLoggedIn", false);
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh articles when fragment becomes visible
        if (isUserLoggedIn()) {
            refreshSavedArticles();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}