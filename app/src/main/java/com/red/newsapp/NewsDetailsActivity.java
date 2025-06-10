package com.red.newsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.red.newsapp.api_response.Article;
import com.red.newsapp.Sqlite.DatabaseHelper;
import com.squareup.picasso.Picasso;

public class NewsDetailsActivity extends AppCompatActivity {
    String title, content, imageUrl, newsUrl, author, description;
    private ImageView newsDetailsImage;
    private TextView newsDetailsTitle, newsDetailsAuthor, newsDetailsContent, newsDetailsDescription, newsDetailsUrl;
    private ImageButton backBtn;
    private FloatingActionButton bookmarkBtn;
    private DatabaseHelper dbHelper;
    private boolean isArticleSaved = false;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN, android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();
        getCurrentUser();
        checkIfArticleIsSaved();
        setViews();
        setupClickListeners();
    }

    private void initViews() {
        title = getIntent().getStringExtra("title");
        author = getIntent().getStringExtra("author");
        description = getIntent().getStringExtra("description");
        newsUrl = getIntent().getStringExtra("url");
        imageUrl = getIntent().getStringExtra("imageUrl");
        content = getIntent().getStringExtra("content");

        newsDetailsImage = findViewById(R.id.idDetailsNewsImage);
        newsDetailsTitle = findViewById(R.id.idDetailsNewsTitle);
        newsDetailsAuthor = findViewById(R.id.idDetailsNewsPublisher);
        newsDetailsContent = findViewById(R.id.idDetailsNewsContent);
        newsDetailsUrl = findViewById(R.id.idDetailsNewsUrl);
        newsDetailsDescription = findViewById(R.id.idDetailsNewsDescription);
        backBtn = findViewById(R.id.idBackButton);
        bookmarkBtn = findViewById(R.id.idDetailsNewsBookmark);

        dbHelper = new DatabaseHelper(this);
    }

    private void getCurrentUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.red.newsapp", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        if (email != null) {
            DatabaseHelper.User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                currentUserId = user.getId();
            }
        }
    }

    private void checkIfArticleIsSaved() {
        if (currentUserId != -1 && newsUrl != null) {
            isArticleSaved = dbHelper.isArticleSaved(currentUserId, newsUrl);
            updateBookmarkIcon();
        }
    }

    private void updateBookmarkIcon() {
        if (isArticleSaved) {
            bookmarkBtn.setImageResource(R.drawable.bookmark_filled);
        } else {
            bookmarkBtn.setImageResource(R.drawable.bookmark);
        }
    }

    private void setViews() {
        newsDetailsTitle.setText(title);
        newsDetailsAuthor.setText(author != null ? author : "Unknown Author");
        newsDetailsContent.setText(content);
        newsDetailsUrl.setText(newsUrl);
        newsDetailsDescription.setText(description);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.newspaperbg)
                    .error(R.drawable.newspaperbg)
                    .into(newsDetailsImage);
        } else {
            Picasso.get().load(R.drawable.newspaperbg).into(newsDetailsImage);
        }
    }

    private void setupClickListeners() {
        newsDetailsUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(newsUrl));
                startActivity(browserIntent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleBookmark();
            }
        });
    }

    private void toggleBookmark() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Please login to save articles", Toast.LENGTH_SHORT).show();
            // Redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (isArticleSaved) {
            // Remove from saved articles
            boolean removed = dbHelper.deleteSavedArticle(currentUserId, newsUrl);
            if (removed) {
                isArticleSaved = false;
                updateBookmarkIcon();
                Toast.makeText(this, "Article removed from saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to remove article", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Save article
            Article article = new Article();
            article.setTitle(title);
            article.setAuthor(author);
            article.setDescription(description);
            article.setUrl(newsUrl);
            article.setUrlToImage(imageUrl);
            article.setContent(content);

            boolean saved = dbHelper.saveArticle(currentUserId, article);
            if (saved) {
                isArticleSaved = true;
                updateBookmarkIcon();
                Toast.makeText(this, "Article saved to bookmarks", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Article already saved or failed to save", Toast.LENGTH_SHORT).show();
            }
        }
    }
}