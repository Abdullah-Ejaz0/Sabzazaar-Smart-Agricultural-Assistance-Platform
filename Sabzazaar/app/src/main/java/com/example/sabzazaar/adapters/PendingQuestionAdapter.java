package com.example.sabzazaar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sabzazaar.R;
import com.example.sabzazaar.models.CommunityPost;
import java.util.List;

public class PendingQuestionAdapter extends RecyclerView.Adapter<PendingQuestionAdapter.ViewHolder> {
    private List<CommunityPost> posts;
    private OnQuestionClickListener listener;

    public interface OnQuestionClickListener {
        void onQuestionClick(CommunityPost post);
    }

    public PendingQuestionAdapter(List<CommunityPost> posts, OnQuestionClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityPost post = posts.get(position);
        holder.tvFarmerName.setText(post.getAuthorName());
        holder.tvQuestionSnippet.setText(post.getBody());
        
        // Highlight Urgent (Crop Disease)
        String cat = post.getCategory() != null ? post.getCategory().toLowerCase() : "";
        if (cat.equals("crop_disease") || cat.equals("urgent")) {
            holder.ivQuestionType.setImageResource(R.drawable.ic_volume_up);
            holder.ivQuestionType.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.RED));
            holder.tvFarmerName.setText("⚠️ URGENT: " + post.getAuthorName());
            holder.tvFarmerName.setTextColor(android.graphics.Color.RED);
        } else {
            holder.ivQuestionType.setImageResource(R.drawable.ic_leaf);
            holder.ivQuestionType.setImageTintList(android.content.res.ColorStateList.valueOf(holder.itemView.getContext().getResources().getColor(R.color.green)));
            holder.tvFarmerName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> listener.onQuestionClick(post));
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
        TextView tvFarmerName, tvQuestionSnippet;
        ImageView ivQuestionType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFarmerName = itemView.findViewById(R.id.tvFarmerName);
            tvQuestionSnippet = itemView.findViewById(R.id.tvQuestionSnippet);
            ivQuestionType = itemView.findViewById(R.id.ivQuestionType);
        }
    }
}
