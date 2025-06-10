package com.red.newsapp.news_adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.red.newsapp.NewsDetailsActivity;
import com.red.newsapp.R;
import com.red.newsapp.api_response.Article;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EverythingNewsAdapter extends RecyclerView.Adapter<EverythingNewsAdapter.ViewHolder>{
    private ArrayList<Article> articlesArrayList;
    private Context context;

    public EverythingNewsAdapter(ArrayList<Article> articlesArrayList, Context context) {
        this.articlesArrayList = articlesArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public EverythingNewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.everything_news_rv_item, parent, false);
        return new EverythingNewsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EverythingNewsAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Article article = articlesArrayList.get(position);

        // Handle null author like in SavedArticlesAdapter
        holder.bottomCardAuthor.setText(article.getAuthor() != null ? article.getAuthor() : "Unknown Author");
        holder.bottomCardTitle.setText(article.getTitle());

        // Load image with placeholder and error handling like in SavedArticlesAdapter
        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Picasso.get()
                    .load(article.getUrlToImage())
                    .placeholder(R.color.black)
                    .error(R.color.black)
                    .into(holder.NewsImage);
        } else {
            Picasso.get().load(R.color.black).into(holder.NewsImage);
        }

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

    @Override
    public int getItemCount() {
        return articlesArrayList.size();
    }

    // Method to update the entire list (consistent with SavedArticlesAdapter)
    public void updateArticlesList(ArrayList<Article> newArticles) {
        this.articlesArrayList.clear();
        this.articlesArrayList.addAll(newArticles);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView bottomCardAuthor, bottomCardTitle;
        ImageView NewsImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bottomCardTitle = itemView.findViewById(R.id.idTvTitle);
            bottomCardAuthor = itemView.findViewById(R.id.idTvAuthor);
            NewsImage = itemView.findViewById(R.id.ivNewsItemBg);
        }
    }
}