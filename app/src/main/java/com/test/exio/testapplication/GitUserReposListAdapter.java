package com.test.exio.testapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Создано: exio Дата: 26.10.2015.
 */
public class GitUserReposListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private Context context;
    private List<GitUserRepo> repos;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading=false;
    private OnLoadMoreListener onLoadMoreListener;

    GitUserReposListAdapter(RecyclerView recyclerView, List<GitUserRepo> repos){
        this.context=recyclerView.getContext();
        this.repos = repos;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView
                    .addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading
                                    && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                if (onLoadMoreListener != null) {
                                    onLoadMoreListener.onLoadMore();
                                }
                                loading = true;
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return repos.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return repos.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.user_repos_list_view_item,viewGroup, false);
            holder = new ItemHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.progress_item, viewGroup, false);
            holder = new ProgressViewHolder(v);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof ItemHolder) {
            ItemHolder itemHolder = (ItemHolder)viewHolder;
            itemHolder.name.setText(repos.get(i).name);
            itemHolder.html_url.setText(repos.get(i).html_url);
            itemHolder.owner.setText(repos.get(i).owner.html_url);
            itemHolder.description.setText(repos.get(i).description);
            itemHolder.imageForked.setVisibility(repos.get(i).fork?ImageView.VISIBLE:ImageView.INVISIBLE);
        } else {
            ((ProgressViewHolder) viewHolder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.itemProgressBar);
        }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView name;
        TextView html_url;
        TextView owner;
        TextView description;
        ImageView imageForked;

        ItemHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            name = (TextView)itemView.findViewById(R.id.repos_name);
            html_url = (TextView)itemView.findViewById(R.id.repos_html_url);
            owner = (TextView)itemView.findViewById(R.id.repos_owner);
            description = (TextView)itemView.findViewById(R.id.repos_desc);
            imageForked = (ImageView)itemView.findViewById(R.id.imageForked);
        }
    }
}
