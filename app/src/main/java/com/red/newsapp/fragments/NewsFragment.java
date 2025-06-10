package com.red.newsapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.red.newsapp.R;
import com.red.newsapp.SearchResultActivity;
import com.red.newsapp.Services.RetrofitInitialize;
import com.red.newsapp.api_response.API;
import com.red.newsapp.api_response.Articles;
import com.red.newsapp.api_response.Article;
import com.red.newsapp.categories.CategoryRVAdapter;
import com.red.newsapp.categories.CategoryRVModal;
import com.red.newsapp.news_adapters.EverythingNewsAdapter;
import com.red.newsapp.news_adapters.TopHeadlinesNewsRVAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NewsFragment extends Fragment implements CategoryRVAdapter.CategoryClickInterface {

    private RecyclerView categoryRV, topHeadlinesNewsRV, bottomNewsRV;
    private ArrayList<Article> articlesArrayList = new ArrayList<>();
    private ArrayList<Article> topHeadlinesArticlesArrayList = new ArrayList<>();
    private ArrayList<CategoryRVModal> categoryModelArrayList = new ArrayList<>();
    private TopHeadlinesNewsRVAdapter topHeadlinesNewsRVAdapter;
    private CategoryRVAdapter categoryRVAdapter;
    private EverythingNewsAdapter everythingNewsAdapter;
    private EditText etSearch;
    private ImageButton searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        etSearch = view.findViewById(R.id.etSearch);
        searchButton = view.findViewById(R.id.btnSearch);
        categoryRV = view.findViewById(R.id.rvCategory);
        topHeadlinesNewsRV = view.findViewById(R.id.rvTopNews);
        bottomNewsRV = view.findViewById(R.id.rvEverythingNews);

        topHeadlinesNewsRVAdapter = new TopHeadlinesNewsRVAdapter(topHeadlinesArticlesArrayList, getContext());
        categoryRVAdapter = new CategoryRVAdapter(categoryModelArrayList, getContext(), this);
        everythingNewsAdapter = new EverythingNewsAdapter(articlesArrayList, getContext());

        topHeadlinesNewsRV.setAdapter(topHeadlinesNewsRVAdapter);
        categoryRV.setAdapter(categoryRVAdapter);
        bottomNewsRV.setAdapter(everythingNewsAdapter);

        getTopHeadlinesNews();
        getCategories();
        getBottomNews("General");

        searchButton.setOnClickListener(view1 -> {
            String searchInput = etSearch.getText().toString();
            if (!searchInput.isEmpty()) {
                Intent intent = new Intent(getContext(), SearchResultActivity.class);
                intent.putExtra("searchInput", searchInput);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Silakan masukkan istilah pencarian.", Toast.LENGTH_SHORT).show();
            }
        });

        etSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            String searchedInput = etSearch.getText().toString();
            if (!searchedInput.isEmpty() && i == EditorInfo.IME_ACTION_DONE) {
                Intent intent = new Intent(getContext(), SearchResultActivity.class);
                intent.putExtra("searchInput", searchedInput);
                startActivity(intent);
            }
            return false;
        });

        return view;
    }

    private void getBottomNews(String category) {
        articlesArrayList.clear();
        Retrofit retrofit = RetrofitInitialize.BaslangicAdim();
        API api = retrofit.create(API.class);
        Call<Articles> call = api.getNewsArticles("us", category, 20, getString(R.string.api_key));
        call.enqueue(new Callback<Articles>() {
            @Override
            public void onResponse(Call<Articles> call, Response<Articles> response) {
                if (response.isSuccessful() && response.body() != null) {
                    articlesArrayList.addAll(response.body().getArticles());
                    everythingNewsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Articles> call, Throwable t) {
                Toast.makeText(getContext(), "Gagal memuat artikel.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTopHeadlinesNews() {
        topHeadlinesArticlesArrayList.clear();
        Retrofit retrofit = RetrofitInitialize.BaslangicAdim();
        API api = retrofit.create(API.class);
        Call<Articles> call = api.getNewsArticles("us", "technology", 5, getString(R.string.api_key));
        call.enqueue(new Callback<Articles>() {
            @Override
            public void onResponse(Call<Articles> call, Response<Articles> response) {
                if (response.isSuccessful() && response.body() != null) {
                    topHeadlinesArticlesArrayList.addAll(response.body().getArticles());
                    topHeadlinesNewsRVAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Articles> call, Throwable t) {
                Toast.makeText(getContext(), "Gagal memuat headline.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCategories() {
        categoryModelArrayList.add(new CategoryRVModal("General", true));
        categoryModelArrayList.add(new CategoryRVModal("Business", false));
        categoryModelArrayList.add(new CategoryRVModal("Entertainment", false));
        categoryModelArrayList.add(new CategoryRVModal("Health", false));
        categoryModelArrayList.add(new CategoryRVModal("Science", false));
        categoryModelArrayList.add(new CategoryRVModal("Sports", false));
        categoryModelArrayList.add(new CategoryRVModal("Technology", false));
    }

    @Override
    public void onCategoryClick(int position) {
        String category = categoryModelArrayList.get(position).getCategory();
        getBottomNews(category);
    }
}
