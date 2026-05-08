package com.example.sabzazaar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sabzazaar.R;
import com.example.sabzazaar.models.CommunityPost;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {
    private List<CommunityPost> posts;
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(CommunityPost post);
    }

    public CommunityAdapter(List<CommunityPost> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityPost post = posts.get(position);
        holder.tvQuestionText.setText(post.getBody());
        holder.itemView.setOnClickListener(v -> listener.onPostClick(post));
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public void updateData(List<CommunityPost> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
        }
    }
}