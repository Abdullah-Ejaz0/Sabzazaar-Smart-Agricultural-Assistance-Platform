package com.example.sabzazaar.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.sabzazaar.R;
import com.example.sabzazaar.models.ScanHistoryItem;

import java.util.List;

public class RecentScanAdapter extends RecyclerView.Adapter<RecentScanAdapter.ViewHolder> {

    public interface OnScanClickListener {
        void onScanClick(ScanHistoryItem item);
    }

    private List<ScanHistoryItem> scans;
    private final OnScanClickListener listener;

    public RecentScanAdapter(List<ScanHistoryItem> scans, OnScanClickListener listener) {
        this.scans = scans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_scan, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanHistoryItem item = scans.get(position);
        Context ctx = holder.itemView.getContext();

        // Disease name
        holder.tvDiseaseName.setText(item.diseaseName != null ? item.diseaseName : "Unknown");

        // Crop name
        holder.tvCropName.setText(item.cropName != null ? item.cropName : "");

        // Risk badge color
        String risk = item.riskLevel != null ? item.riskLevel.toLowerCase() : "low";
        holder.tvRiskBadge.setText(risk.toUpperCase());
        switch (risk) {
            case "high":
                holder.tvRiskBadge.setBackgroundColor(Color.parseColor("#FFCDD2"));
                holder.tvRiskBadge.setTextColor(Color.parseColor("#C62828"));
                break;
            case "medium":
                holder.tvRiskBadge.setBackgroundColor(Color.parseColor("#FFF9C4"));
                holder.tvRiskBadge.setTextColor(Color.parseColor("#F57F17"));
                break;
            default:
                holder.tvRiskBadge.setBackgroundColor(Color.parseColor("#C8E6C9"));
                holder.tvRiskBadge.setTextColor(Color.parseColor("#2E7D32"));
                break;
        }

        // Date — just show first 10 chars of ISO date (YYYY-MM-DD)
        String date = item.createdAt != null && item.createdAt.length() >= 10
                ? item.createdAt.substring(0, 10) : "";
        holder.tvScanDate.setText(date);

        // Image — load from signed URL via Glide
        String url = item.getDisplayUrl();
        if (url != null && !url.isEmpty()) {
            holder.ivScanThumb.clearColorFilter();
            Glide.with(ctx)
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_camera)
                            .error(R.drawable.ic_camera)
                            .transform(new RoundedCorners(40)))
                    .into(holder.ivScanThumb);
        } else {
            holder.ivScanThumb.setImageResource(R.drawable.ic_camera);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onScanClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return scans != null ? scans.size() : 0;
    }

    public void updateData(List<ScanHistoryItem> newScans) {
        this.scans = newScans;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivScanThumb;
        TextView tvDiseaseName, tvCropName, tvRiskBadge, tvScanDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivScanThumb   = itemView.findViewById(R.id.ivScanThumb);
            tvDiseaseName = itemView.findViewById(R.id.tvDiseaseName);
            tvCropName    = itemView.findViewById(R.id.tvCropName);
            tvRiskBadge   = itemView.findViewById(R.id.tvRiskBadge);
            tvScanDate    = itemView.findViewById(R.id.tvScanDate);
        }
    }
}
