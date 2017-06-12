package com.zigic.githubuser.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zigic.githubuser.R;
import com.zigic.githubuser.model.Item;

import java.util.List;

/**
 * Created by zigic on 10/06/17.
 */

public class GithubUserAdapter1 extends RecyclerView.Adapter<GithubUserAdapter1.ViewHolder> {
    private static final String TAG = GithubUserAdapter1.class.getName();
    private Context mContext;
    private List<Item> userList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgAvatar;
        public TextView txtUser;

        public ViewHolder(View v) {
            super(v);
            imgAvatar = (ImageView) v.findViewById(R.id.img_avatar);
            txtUser = (TextView) v.findViewById(R.id.tv_name);
        }
    }

    public GithubUserAdapter1(Context mContext, List<Item> userList) {
        this.mContext = mContext;
        this.userList = userList;
    }

    public void updateMerchantList(List<Item> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_card_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item user = userList.get(position);
        holder.txtUser.setText(user.getLogin());

        if(user.getAvatarUrl()!=null){
            Glide.with(mContext).load(user.getAvatarUrl())
                    .placeholder(R.drawable.github_default).into(holder.imgAvatar);
        }else{
            //default image
            Glide.with(mContext).load(R.drawable.github_default)
                    .placeholder(R.drawable.github_default).into(holder.imgAvatar);
        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
