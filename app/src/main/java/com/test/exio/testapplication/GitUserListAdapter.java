package com.test.exio.testapplication;

import android.content.Context;
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
 * Создано: exio Дата: 25.10.2015.
 */
public class GitUserListAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private Context context;
    private List<GitUser> users;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading=false;
    private OnLoadMoreListener onLoadMoreListener;

    GitUserListAdapter(RecyclerView  recyclerView, List<GitUser> users){
        this.context=recyclerView.getContext();
        this.users = users;

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
        return users.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return users.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.user_list_view_item,viewGroup, false);
            holder = new GitUserViewHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.progress_item, viewGroup, false);
            holder = new ProgressViewHolder(v);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof GitUserViewHolder) {
            ((GitUserViewHolder) viewHolder).userName.setText(users.get(i).login);

               Picasso.with(context)
                        .load(users.get(i).avatar_url)
                        .resize(50, 50)
                        .centerCrop()
                        .placeholder(R.drawable.git_unknown_user)
                        .error(R.drawable.git_error_image)
                        .into(((GitUserViewHolder) viewHolder).userPhoto);

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

    public static class GitUserViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView userName;
        ImageView userPhoto;

        GitUserViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            userName = (TextView)itemView.findViewById(R.id.user_name);
            userPhoto = (ImageView)itemView.findViewById(R.id.user_photo);
        }
    }
}
