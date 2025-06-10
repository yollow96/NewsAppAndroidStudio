package com.red.newsapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.red.newsapp.AddArticleActivity;
import com.red.newsapp.R;
import com.red.newsapp.RegisterActivity;
import com.red.newsapp.Sqlite.DatabaseHelper;
import com.red.newsapp.news_adapters.UserArticlesAdapter;

import java.util.ArrayList;

public class ProfileFragment extends Fragment implements LoginFragment.LoginFragmentListener {

    private ScrollView scrollView;
    private Button loginButton, registerButton, logoutButton, addArticleButton;
    private TextView nameTextView, lastNameTextView, emailTextView, noArticlesTextView;
    private RecyclerView articlesRecyclerView;
    private View loginCard;
    private DatabaseHelper dbHelper;
    private UserArticlesAdapter articleAdapter;
    private int currentUserId = -1; // Track current user ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupUserInterface();

        return view;
    }

    private void initViews(View view) {
        scrollView = view.findViewById(R.id.idProfileScrollView);
        loginCard = view.findViewById(R.id.idProfileLoginCardView);
        loginButton = view.findViewById(R.id.idBtnLogin);
        registerButton = view.findViewById(R.id.idBtnRegister);
        logoutButton = view.findViewById(R.id.idProfileBtnLogout);
        addArticleButton = view.findViewById(R.id.idProfileBtnAddArticle);
        nameTextView = view.findViewById(R.id.idTvProfileName);
        lastNameTextView = view.findViewById(R.id.idTvProfileLastName);
        emailTextView = view.findViewById(R.id.idTvProfileEmail);

        // Tambahan untuk artikel
        articlesRecyclerView = view.findViewById(R.id.idProfileRecyclerViewArticles);
        noArticlesTextView = view.findViewById(R.id.idTvNoArticles);

        dbHelper = new DatabaseHelper(getContext());

        // Setup RecyclerView
        if (articlesRecyclerView != null) {
            articlesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

    private void setupUserInterface() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.red.newsapp", getContext().MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        if (email != null) {
            // User sudah login
            DatabaseHelper.User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                currentUserId = user.getId(); // Store current user ID
                showLoggedInProfile(user, sharedPreferences);
            } else {
                showLoginCard();
                Toast.makeText(getContext(), "User tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        } else {
            // User belum login
            showLoginCard();
        }
    }

    private void showLoggedInProfile(DatabaseHelper.User user, SharedPreferences sharedPreferences) {
        scrollView.setVisibility(View.VISIBLE);
        loginCard.setVisibility(View.GONE);

        nameTextView.setText(user.getName());
        lastNameTextView.setText(user.getLastName());
        emailTextView.setText(user.getEmail());

        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Reset current user ID
            currentUserId = -1;

            // Refresh the profile to show login card
            setupUserInterface();
            Toast.makeText(getContext(), "Logout berhasil", Toast.LENGTH_SHORT).show();
        });

        addArticleButton.setVisibility(View.VISIBLE);
        addArticleButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddArticleActivity.class);
            startActivityForResult(intent, 100); // Request code 100 untuk refresh
        });

        // Load artikel user
        loadUserArticles(user.getId());
    }

    private void showLoginCard() {
        scrollView.setVisibility(View.GONE);
        loginCard.setVisibility(View.VISIBLE);
        addArticleButton.setVisibility(View.GONE);

        loginButton.setOnClickListener(v -> navigateToLoginFragment());
        registerButton.setOnClickListener(v -> navigateToRegisterActivity());
    }

    // Method baru untuk load artikel user - FIXED to match UserArticlesAdapter
    private void loadUserArticles(int userId) {
        // Ambil artikel berdasarkan user ID - using the correct method that returns UserArticle
        ArrayList<DatabaseHelper.UserArticle> userArticles = dbHelper.getArticlesByUserId(userId);

        if (userArticles != null && !userArticles.isEmpty()) {
            // Tampilkan RecyclerView dan sembunyikan pesan kosong
            articlesRecyclerView.setVisibility(View.VISIBLE);
            noArticlesTextView.setVisibility(View.GONE);

            // Setup adapter with correct constructor parameters
            if (articleAdapter == null) {
                articleAdapter = new UserArticlesAdapter(getContext(), userArticles, currentUserId);
                articlesRecyclerView.setAdapter(articleAdapter);
            } else {
                articleAdapter.updateArticles(userArticles);
            }
        } else {
            // Tampilkan pesan tidak ada artikel
            articlesRecyclerView.setVisibility(View.GONE);
            noArticlesTextView.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setLoginFragmentListener(this);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, loginFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToRegisterActivity() {
        startActivity(new Intent(getContext(), RegisterActivity.class));
    }

    // LoginFragment.LoginFragmentListener implementation
    @Override
    public void onLoginSuccess() {
        // Navigate back to profile and refresh
        getParentFragmentManager().popBackStack();
        setupUserInterface();
    }

    @Override
    public void onNavigateToRegister() {
        navigateToRegisterActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            // Refresh artikel setelah kembali dari AddArticleActivity
            if (currentUserId != -1) {
                loadUserArticles(currentUserId);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the interface when returning to this fragment
        if (getView() != null) {
            setupUserInterface();
        }
    }
}