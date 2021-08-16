package com.example.travelblog.http;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class BlogHttpClient {

    public static final BlogHttpClient INSTANCE = new BlogHttpClient();
    private static final String BASE_URL =
            "https://bitbucket.org/dmytrodanylyk/travel-blog-resources/raw/";
    private static final String BLOG_ARTICLES_URL =
            BASE_URL + "647f4270e4271fbff28f1d80e2f2d12b3bd4a1cd/blog_articles.json";

    private Executor executor;
    private OkHttpClient client;
    private Gson gson;

    private BlogHttpClient() {
        executor = Executors.newFixedThreadPool(4);
        client = new OkHttpClient();
        gson = new Gson();
    }

    public void loadBlogArticles(BlogArticlesCallback callback) {
        Request request = new Request.Builder() // 1
                .get()
                .url(BLOG_ARTICLES_URL)
                .build();
        executor.execute(() -> { // 2
            try {
                Response response = client.newCall(request).execute(); // 3
                ResponseBody responseBody = response.body();
                if (responseBody != null) { // 4
                    String json = responseBody.string();
                    BlogData blogData = gson.fromJson(json, BlogData.class);
                    if (blogData != null) {
                        callback.onSuccess(blogData.getData());
                        return;
                    }
                }
            } catch (IOException e) { // 5
                Log.e("BlogHttpClient", "Error loading blog articles", e);
            }
            callback.onError();
        });
    }

}


