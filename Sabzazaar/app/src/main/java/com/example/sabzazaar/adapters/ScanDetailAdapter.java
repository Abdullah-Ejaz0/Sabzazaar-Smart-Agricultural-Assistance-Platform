package com.example.sabzazaar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sabzazaar.R;
import java.util.List;

public class ScanDetailAdapter extends RecyclerView.Adapter<ScanDetailAdapter.ViewHolder> {

    public static class DetailItem {
        public String title;
        public String body;

        public DetailItem(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }

    private List<DetailItem> detailItems;

    public ScanDetailAdapter(List<DetailItem> detailItems) {
        this.detailItems = detailItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetailItem item = detailItems.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvBody.setText(item.body);
    }

    @Override
    public int getItemCount() {
        return detailItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public TextView tvBody;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvDetailTitle);
            tvBody = view.findViewById(R.id.tvDetailBody);
        }
    }
}
