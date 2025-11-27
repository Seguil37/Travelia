package com.proyecto.travelia.ui.reviews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyecto.travelia.R;
import com.proyecto.travelia.data.local.ReviewEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.VH> {

    private final List<ReviewEntity> data = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    public void submit(List<ReviewEntity> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ReviewEntity item = data.get(position);
        holder.tvName.setText(item.userName != null ? item.userName : "Invitado");
        holder.ratingBar.setRating(item.rating);
        holder.tvComment.setText(item.comment != null ? item.comment : "");
        holder.tvDate.setText(sdf.format(new Date(item.createdAt)));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvComment, tvDate;
        RatingBar ratingBar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_reviewer_name);
            tvComment = itemView.findViewById(R.id.tv_review_comment);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            ratingBar = itemView.findViewById(R.id.rating_review);
        }
    }
}
