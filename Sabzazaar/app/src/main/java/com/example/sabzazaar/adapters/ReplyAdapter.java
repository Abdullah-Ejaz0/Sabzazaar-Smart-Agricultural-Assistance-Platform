package com.example.sabzazaar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sabzazaar.R;
import com.example.sabzazaar.models.CommunityReply;
import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder> {
    private List<CommunityReply> replies;

    public ReplyAdapter(List<CommunityReply> replies) {
        this.replies = replies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityReply reply = replies.get(position);
        holder.tvAuthor.setText(reply.getAuthorName());
        holder.tvTime.setText(reply.getCreatedAt());
        holder.tvBody.setText(reply.getBody());
        
        if (reply.isVerified()) {
            holder.ivVerifiedBadge.setVisibility(View.VISIBLE);
            holder.container.setBackgroundResource(R.drawable.bg_expert_answer);
        } else {
            holder.ivVerifiedBadge.setVisibility(View.GONE);
            holder.container.setBackgroundResource(R.drawable.bg_lang_box);
        }
    }

    @Override
    public int getItemCount() {
        return replies != null ? replies.size() : 0;
    }

    public void updateData(List<CommunityReply> newReplies) {
        this.replies = newReplies;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvTime, tvBody;
        ImageView ivVerifiedBadge;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvReplyAuthor);
            tvTime = itemView.findViewById(R.id.tvReplyTime);
            tvBody = itemView.findViewById(R.id.tvReplyBody);
            ivVerifiedBadge = itemView.findViewById(R.id.tvVerifiedBadge);
            container = itemView.findViewById(R.id.containerReply);
        }
    }
}