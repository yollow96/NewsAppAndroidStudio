package com.red.newsapp.news_adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.red.newsapp.NewsDetailsActivity;
import com.red.newsapp.R;
import com.red.newsapp.api_response.Article;
import com.red.newsapp.Sqlite.DatabaseHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SavedArticlesAdapter extends RecyclerView.Adapter<SavedArticlesAdapter.SavedArticlesViewHolder>{
    private ArrayList<Article> articlesArrayList;
    private Context context;
    private DatabaseHelper dbHelper;

    public SavedArticlesAdapter(ArrayList<Article> articlesArrayList, Context context) {
        this.articlesArrayList = articlesArrayList;
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public SavedArticlesAdapter.SavedArticlesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_articles_rv_item, parent, false);
        return new SavedArticlesAdapter.SavedArticlesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedArticlesAdapter.SavedArticlesViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Article article = articlesArrayList.get(position);

        // Set article data
        holder.bottomCardAuthor.setText(article.getAuthor() != null ? article.getAuthor() : "Unknown Author");
        holder.bottomCardTitle.setText(article.getTitle());

        // Load image
        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Picasso.get()
                    .load(article.getUrlToImage())
                    .placeholder(R.color.black)
                    .error(R.color.black)
                    .into(holder.NewsImage);
        } else {
            Picasso.get().load(R.color.black).into(holder.NewsImage);
        }

        // Delete button click listener
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteArticle(article, position);
            }
        });

        // Item click listener to open article details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewsDetailsActivity.class);
                intent.putExtra("title", article.getTitle());
                intent.putExtra("author", article.getAuthor());
                intent.putExtra("description", article.getDescription());
                intent.putExtra("url", article.getUrl());
                intent.putExtra("imageUrl", article.getUrlToImage());
                intent.putExtra("content", article.getContent());

                context.startActivity(intent);
            }
        });
    }

    private void deleteArticle(Article article, int position) {
        // Get current user ID from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.red.newsapp", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);

        if (email != null) {
            DatabaseHelper.User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                // Delete from SQLite database
                boolean deleted = dbHelper.deleteSavedArticleByTitle(user.getId(), article.getTitle());

                if (deleted) {
                    // Remove from local list and update adapter
                    articlesArrayList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, articlesArrayList.size());
                    Toast.makeText(context, "Article removed from saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to remove article", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return articlesArrayList.size();
    }

    // Method to update the entire list
    public void updateArticlesList(ArrayList<Article> newArticles) {
        this.articlesArrayList.clear();
        this.articlesArrayList.addAll(newArticles);
        notifyDataSetChanged();
    }

    public class SavedArticlesViewHolder extends RecyclerView.ViewHolder {
        TextView bottomCardAuthor, bottomCardTitle;
        ImageView NewsImage;
        ImageButton deleteBtn;

        public SavedArticlesViewHolder(@NonNull View itemView) {
            super(itemView);

            bottomCardTitle = itemView.findViewById(R.id.idSavedTvTitle);
            bottomCardAuthor = itemView.findViewById(R.id.idSavedTvAuthor);
            NewsImage = itemView.findViewById(R.id.ivSavedNewsItemBg);
            deleteBtn = itemView.findViewById(R.id.ivDelete);
        }
    }
}