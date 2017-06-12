package com.zigic.githubuser.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zigic.githubuser.R;
import com.zigic.githubuser.adapter.listener.OnLoadMoreListener;
import com.zigic.githubuser.model.Item;

import java.util.List;

/**
 * Created by zigic on 10/06/17.
 */

public class GithubUserAdapter extends RecyclerView.Adapter<GithubUserAdapter.ViewHolder> {
    private static final String TAG = GithubUserAdapter.class.getName();
    private Context mContext;
    private List<Item> userList;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_LOADING = 0;
    private OnLoadMoreListener onLoadMoreListener;

    private boolean isLoading;
    int totalItemCount;
    int lastVisibleItem;
    int visibleThreshold=2;

    public GithubUserAdapter(Context mContext, List<Item> userList, final RecyclerView recyclerView) {
        this.mContext = mContext;
        this.userList = userList;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }

            }
        });


    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    // for user item list
    public class UserViewHolder extends ViewHolder {
        public ImageView imgAvatar;
        public TextView txtUser;

        public UserViewHolder(View v) {
            super(v);
            imgAvatar = (ImageView) v.findViewById(R.id.img_avatar);
            txtUser = (TextView) v.findViewById(R.id.tv_name);
        }
    }

    // for bottom loading list
    public class LoadingViewHolder extends ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    public void updateMerchantList(List<Item> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == VIEW_TYPE_ITEM) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_card_layout, parent, false);
            return new UserViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_card_layout, parent, false);
            return new LoadingViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            UserViewHolder viewholder = (UserViewHolder) holder;
            Item user = userList.get(position);
            viewholder.txtUser.setText(user.getLogin());
            if (user.getAvatarUrl() != null) {
                Glide.with(mContext).load(user.getAvatarUrl())
                        .placeholder(R.drawable.github_default).into(viewholder.imgAvatar);
            } else {
                //default image
                Glide.with(mContext).load(R.drawable.github_default)
                        .placeholder(R.drawable.github_default).into(viewholder.imgAvatar);
            }
        } else {
            LoadingViewHolder viewholder = (LoadingViewHolder) holder;
            viewholder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return userList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setUnloaded() {
        isLoading = false;
    }
}
