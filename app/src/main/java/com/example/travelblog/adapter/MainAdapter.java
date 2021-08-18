package com.example.travelblog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.travelblog.http.Blog;

import com.example.travelblog.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainAdapter extends ListAdapter<Blog, MainAdapter.MainViewHolder> {

    private List<Blog> originalList = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClicked(Blog blog);
    }

    private OnItemClickListener clickListener;

    public MainAdapter(OnItemClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<Blog> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Blog>() {
                @Override
                public boolean areItemsTheSame(@NonNull Blog oldData,
                                               @NonNull Blog newData) {
                    return oldData.getId().equals(newData.getId());
                }
                @Override
                public boolean areContentsTheSame(@NonNull Blog oldData,
                                                  @NonNull Blog newData) {
                    return oldData.equals(newData);
                }
            };

    @NonNull
    @Override
    public MainAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_main, parent, false);
        return new MainViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.MainViewHolder holder, int position) {
        holder.bindTo(getItem(position));
    }

    static class MainViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textDate;
        private ImageView imageAvatar;
        private Blog blog;

        public MainViewHolder(@NonNull View itemView, OnItemClickListener clickListener) {
            super(itemView);
            itemView.setOnClickListener(v -> clickListener.onItemClicked(blog));
            textTitle = itemView.findViewById(R.id.textTitle);
            textDate = itemView.findViewById(R.id.textDate);
            imageAvatar = itemView.findViewById(R.id.imageAvatar);
        }

        void bindTo(Blog blog) {
            this.blog = blog;
            textTitle.setText(blog.getTitle());
            textDate.setText(blog.getDate());
            Glide.with(itemView)
                    .load(blog.getAuthor().getAvatarURL())
                    .transform(new CircleCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageAvatar);
        }

    }

    public void sortByTitle(){
        Collections.sort(originalList, (o1,o2) -> o1.getTitle().compareTo(o2.getTitle()));
        List<Blog> currentList = new ArrayList<Blog>(originalList);
        submitList(currentList);
    }

    public void sortByDate(){
        Collections.sort(originalList, (o1,o2) -> o1.getDateMillis().compareTo(o2.getDateMillis()));
        List<Blog> currentList = new ArrayList<Blog>(originalList);
        submitList(currentList);
    }

    public void filter(String query) {
        List<Blog> filteredList = new ArrayList<>();
        for (Blog blog : originalList) {
            if (blog.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(blog);
            }
        }
        submitList(filteredList);
    }

    public void setData(List<Blog> blogList){
        originalList = blogList;
    }


}
