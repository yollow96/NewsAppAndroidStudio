package com.red.newsapp.news_adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.red.newsapp.EditArticleActivity;
import com.red.newsapp.NewsDetailsActivity;
import com.red.newsapp.R;
import com.red.newsapp.Sqlite.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserArticlesAdapter extends RecyclerView.Adapter<UserArticlesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<DatabaseHelper.UserArticle> articles;
    private DatabaseHelper databaseHelper;
    private int currentUserId;

    public UserArticlesAdapter(Context context, ArrayList<DatabaseHelper.UserArticle> articles, int currentUserId) {
        this.context = context;
        this.articles = articles;
        this.currentUserId = currentUserId;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.UserArticle article = articles.get(position);

        holder.titleTextView.setText(article.getTitle());
        holder.descriptionTextView.setText(article.getDescription());
        holder.categoryTextView.setText(article.getCategory());

        String formattedDate = formatDate(article.getCreatedDate());
        holder.dateTextView.setText(formattedDate);

        // Edit artikel
        holder.editImageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditArticleActivity.class);
            intent.putExtra("id", article.getId());
            intent.putExtra("title", article.getTitle());
            intent.putExtra("description", article.getDescription());
            intent.putExtra("content", article.getContent());
            intent.putExtra("url", article.getUrl());
            intent.putExtra("category", article.getCategory());
            context.startActivity(intent);
        });

        // Hapus artikel
        holder.deleteImageView.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Hapus Artikel")
                    .setMessage("Apakah Anda yakin ingin menghapus artikel \"" + article.getTitle() + "\"?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        boolean success = databaseHelper.deleteArticle(article.getId(), currentUserId);
                        if (success) {
                            articles.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, articles.size());
                            Toast.makeText(context, "Artikel berhasil dihapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Gagal menghapus artikel", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // Klik item → buka NewsDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailsActivity.class);
            intent.putExtra("title", article.getTitle());
            intent.putExtra("description", article.getDescription());
            intent.putExtra("content", article.getContent());
            intent.putExtra("url", article.getUrl());
            intent.putExtra("author", "You"); // Penulis artikel adalah user
            intent.putExtra("imageUrl", "");  // Kosongkan jika tidak ada gambar
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    public void updateArticles(ArrayList<DatabaseHelper.UserArticle> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, categoryTextView, dateTextView;
        ImageView editImageView, deleteImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvArticleTitle);
            descriptionTextView = itemView.findViewById(R.id.tvArticleDescription);
            categoryTextView = itemView.findViewById(R.id.tvArticleCategory);
            dateTextView = itemView.findViewById(R.id.tvArticleDate);
            editImageView = itemView.findViewById(R.id.ivEditArticle);
            deleteImageView = itemView.findViewById(R.id.ivDeleteArticle);
        }
    }
}
