package com.red.newsapp.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.red.newsapp.Services.Auth.AuthUser;
import com.red.newsapp.api_response.Article;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "news_app.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_SAVED_ARTICLES = "saved_articles";
    private static final String TABLE_ARTICLES = "articles";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "lastName TEXT,"
                + "email TEXT UNIQUE,"
                + "password TEXT,"
                + "role TEXT DEFAULT 'user'"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create saved articles table
        String CREATE_SAVED_ARTICLES_TABLE = "CREATE TABLE " + TABLE_SAVED_ARTICLES + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER,"
                + "title TEXT,"
                + "author TEXT,"
                + "description TEXT,"
                + "url TEXT,"
                + "urlToImage TEXT,"
                + "content TEXT,"
                + "saved_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id),"
                + "UNIQUE(user_id, url)"
                + ")";
        db.execSQL(CREATE_SAVED_ARTICLES_TABLE);

        // Create articles table for user-created articles
        String CREATE_ARTICLES_TABLE = "CREATE TABLE " + TABLE_ARTICLES + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER,"
                + "title TEXT NOT NULL,"
                + "description TEXT NOT NULL,"
                + "content TEXT NOT NULL,"
                + "url TEXT NOT NULL,"
                + "urlToImage TEXT,"
                + "category TEXT NOT NULL,"
                + "created_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "updated_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id)"
                + ")";
        db.execSQL(CREATE_ARTICLES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Create saved articles table for version 2
            String CREATE_SAVED_ARTICLES_TABLE = "CREATE TABLE " + TABLE_SAVED_ARTICLES + "("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER,"
                    + "title TEXT,"
                    + "author TEXT,"
                    + "description TEXT,"
                    + "url TEXT,"
                    + "urlToImage TEXT,"
                    + "content TEXT,"
                    + "saved_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id),"
                    + "UNIQUE(user_id, url)"
                    + ")";
            db.execSQL(CREATE_SAVED_ARTICLES_TABLE);
        }

        if (oldVersion < 3) {
            // Create articles table for version 3
            String CREATE_ARTICLES_TABLE = "CREATE TABLE " + TABLE_ARTICLES + "("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER,"
                    + "title TEXT NOT NULL,"
                    + "description TEXT NOT NULL,"
                    + "content TEXT NOT NULL,"
                    + "url TEXT NOT NULL,"
                    + "urlToImage TEXT,"
                    + "category TEXT NOT NULL,"
                    + "created_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "updated_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id)"
                    + ")";
            db.execSQL(CREATE_ARTICLES_TABLE);
        }
    }

    // Article inner class untuk data hasil query
    public static class UserArticle {
        private int id;
        private int userId;
        private String title;
        private String description;
        private String content;
        private String url;
        private String urlToImage;
        private String category;
        private String createdDate;
        private String updatedDate;

        public UserArticle(int id, int userId, String title, String description, String content,
                           String url, String urlToImage, String category, String createdDate, String updatedDate) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.description = description;
            this.content = content;
            this.url = url;
            this.urlToImage = urlToImage;
            this.category = category;
            this.createdDate = createdDate;
            this.updatedDate = updatedDate;
        }

        // Getters
        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getContent() { return content; }
        public String getUrl() { return url; }
        public String getUrlToImage() { return urlToImage; }
        public String getCategory() { return category; }
        public String getCreatedDate() { return createdDate; }
        public String getUpdatedDate() { return updatedDate; }
    }

    // User inner class untuk data hasil query
    public static class User {
        private int id;
        private String name;
        private String lastName;
        private String email;
        private String role;

        public User(int id, String name, String lastName, String email, String role) {
            this.id = id;
            this.name = name;
            this.lastName = lastName;
            this.email = email;
            this.role = role;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    }

    // Existing user methods
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public boolean registerUser(String name, String lastName, String email, String password, String role) {
        if (isEmailExists(email)) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("lastName", lastName);
        values.put("email", email);
        values.put("password", password);
        values.put("role", role);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        return result != -1;
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_USERS + " WHERE email = ? AND password = ?", new String[]{email, password});
        boolean valid = cursor.moveToFirst();
        cursor.close();
        db.close();
        return valid;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, lastName, email, role FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
            );
            cursor.close();
            db.close();
            return user;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    // SAVED ARTICLES METHODS

    // Save an article for a specific user
    public boolean saveArticle(int userId, Article article) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", article.getTitle());
        values.put("author", article.getAuthor());
        values.put("description", article.getDescription());
        values.put("url", article.getUrl());
        values.put("urlToImage", article.getUrlToImage());
        values.put("content", article.getContent());

        try {
            long result = db.insert(TABLE_SAVED_ARTICLES, null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            // Article already exists for this user
            db.close();
            return false;
        }
    }

    // Check if article is already saved by user
    public boolean isArticleSaved(int userId, String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_SAVED_ARTICLES + " WHERE user_id = ? AND url = ?",
                new String[]{String.valueOf(userId), url});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // Get all saved articles for a specific user
    public ArrayList<Article> getSavedArticlesByUserId(int userId) {
        ArrayList<Article> savedArticles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT title, author, description, url, urlToImage, content FROM "
                        + TABLE_SAVED_ARTICLES + " WHERE user_id = ? ORDER BY saved_date DESC",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Article article = new Article();
                article.setTitle(cursor.getString(0));
                article.setAuthor(cursor.getString(1));
                article.setDescription(cursor.getString(2));
                article.setUrl(cursor.getString(3));
                article.setUrlToImage(cursor.getString(4));
                article.setContent(cursor.getString(5));
                savedArticles.add(article);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return savedArticles;
    }

    // Delete a saved article for a specific user
    public boolean deleteSavedArticle(int userId, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_SAVED_ARTICLES,
                "user_id = ? AND url = ?",
                new String[]{String.valueOf(userId), url});
        db.close();
        return rowsAffected > 0;
    }

    // Delete a saved article by title for a specific user
    public boolean deleteSavedArticleByTitle(int userId, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_SAVED_ARTICLES,
                "user_id = ? AND title = ?",
                new String[]{String.valueOf(userId), title});
        db.close();
        return rowsAffected > 0;
    }

    // Get count of saved articles for a user
    public int getSavedArticlesCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SAVED_ARTICLES + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // NEW METHODS FOR USER ARTICLES

    // Add a new article by user
    public boolean addArticle(int userId, String title, String description, String content,
                              String url, String urlToImage, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("description", description);
        values.put("content", content);
        values.put("url", url);
        values.put("urlToImage", urlToImage);
        values.put("category", category);

        try {
            long result = db.insert(TABLE_ARTICLES, null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            db.close();
            return false;
        }
    }

    // Update an existing article
    public boolean updateArticle(int articleId, String title, String description, String content,
                                 String url, String urlToImage, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("content", content);
        values.put("url", url);
        values.put("urlToImage", urlToImage);
        values.put("category", category);

        // Update timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put("updated_date", sdf.format(new Date()));

        try {
            int rowsAffected = db.update(TABLE_ARTICLES, values, "id = ?",
                    new String[]{String.valueOf(articleId)});
            db.close();
            return rowsAffected > 0;
        } catch (Exception e) {
            db.close();
            return false;
        }
    }

    // Get all articles by a specific user
    public ArrayList<UserArticle> getArticlesByUserId(int userId) {
        ArrayList<UserArticle> articles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT id, user_id, title, description, content, url, urlToImage, category, created_date, updated_date FROM "
                        + TABLE_ARTICLES + " WHERE user_id = ? ORDER BY created_date DESC",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                UserArticle article = new UserArticle(
                        cursor.getInt(0),    // id
                        cursor.getInt(1),    // user_id
                        cursor.getString(2), // title
                        cursor.getString(3), // description
                        cursor.getString(4), // content
                        cursor.getString(5), // url
                        cursor.getString(6), // urlToImage
                        cursor.getString(7), // category
                        cursor.getString(8), // created_date
                        cursor.getString(9)  // updated_date
                );
                articles.add(article);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return articles;
    }

    // Get a specific article by ID
    public UserArticle getArticleById(int articleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, user_id, title, description, content, url, urlToImage, category, created_date, updated_date FROM "
                        + TABLE_ARTICLES + " WHERE id = ?",
                new String[]{String.valueOf(articleId)});

        if (cursor.moveToFirst()) {
            UserArticle article = new UserArticle(
                    cursor.getInt(0),    // id
                    cursor.getInt(1),    // user_id
                    cursor.getString(2), // title
                    cursor.getString(3), // description
                    cursor.getString(4), // content
                    cursor.getString(5), // url
                    cursor.getString(6), // urlToImage
                    cursor.getString(7), // category
                    cursor.getString(8), // created_date
                    cursor.getString(9)  // updated_date
            );
            cursor.close();
            db.close();
            return article;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    // Delete an article
    public boolean deleteArticle(int articleId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_ARTICLES,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(articleId), String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    // Get count of articles by user
    public int getArticlesCountByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ARTICLES + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }



    // Get all articles (for admin or general viewing)
    public ArrayList<UserArticle> getAllArticles() {
        ArrayList<UserArticle> articles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT id, user_id, title, description, content, url, urlToImage, category, created_date, updated_date FROM "
                + TABLE_ARTICLES + " ORDER BY created_date DESC", null);

        if (cursor.moveToFirst()) {
            do {
                UserArticle article = new UserArticle(
                        cursor.getInt(0),    // id
                        cursor.getInt(1),    // user_id
                        cursor.getString(2), // title
                        cursor.getString(3), // description
                        cursor.getString(4), // content
                        cursor.getString(5), // url
                        cursor.getString(6), // urlToImage
                        cursor.getString(7), // category
                        cursor.getString(8), // created_date
                        cursor.getString(9)  // updated_date
                );
                articles.add(article);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return articles;
    }
}