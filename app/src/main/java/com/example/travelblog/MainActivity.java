package com.example.travelblog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.travelblog.adapter.MainAdapter;
import com.example.travelblog.database.AppDatabase;
import com.example.travelblog.database.BlogDAO;
import com.example.travelblog.database.DatabaseProvider;
import com.example.travelblog.http.Blog;
import com.example.travelblog.http.BlogArticlesCallback;
import com.example.travelblog.http.BlogHttpClient;
import com.example.travelblog.repository.BlogRepository;
import com.example.travelblog.repository.DataFromNetworkCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainAdapter mainAdapter;
    private SwipeRefreshLayout refreshLayout;
    private MaterialToolbar toolbar;
    private BlogRepository repository;

    private static final int SORT_TITLE = 0;
    private static final int SORT_DATE = 1;

    private int currentSort = SORT_DATE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new BlogRepository(getApplicationContext());

        mainAdapter = new MainAdapter(blog -> BlogDetailsActivity.startBlogDetailsActivity(this, blog));

        toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.sort){
                onSortClicked();
            }
            return false;
        });

        MenuItem searchItem = toolbar.getMenu().findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mainAdapter.filter(newText);
                return true;
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mainAdapter);

        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this::loadDataFromNetwork);

        loadDataFromDatabase();
        loadDataFromNetwork();
    }

    private void loadDataFromDatabase() {
        repository.loadDataFromDatabase(blogList -> runOnUiThread(() -> {
            showData(blogList);
        }));
    }

    private void loadDataFromNetwork(){
        refreshLayout.setRefreshing(true);
        repository.loadDataFromNetwork(new DataFromNetworkCallback() {
            @Override
            public void onSuccess(List<Blog> blogList) {
                runOnUiThread(() ->{
                    showData(blogList);
                    refreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onError() {
                runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);
                    showErrorSnackbar();
                });
            }
        });
    }

    private void showData(List<Blog> blogList){
        mainAdapter.setData(blogList);
        sortData();
    }


    private void showErrorSnackbar() {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, "Error during loading blog articles", Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.orange500));
        snackbar.setAction("Retry", v -> {
            loadDataFromNetwork();
            snackbar.dismiss();
        });
        snackbar.show();
    }

    private void onSortClicked(){
        String[] items = {"Title", "Date"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sort order")
                .setSingleChoiceItems(items, currentSort, ((dialog, which) -> {
                    dialog.dismiss();
                    currentSort = which;
                    sortData();
                })).show();
    }

    private void sortData(){
        if(currentSort == SORT_TITLE){
            mainAdapter.sortByTitle();
        }
        else if(currentSort == SORT_DATE){
            mainAdapter.sortByDate();
        }
    }

}