package kr.ac.waltdev29.hakplace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import kr.ac.waltdev29.hakplace.api.models.ReviewResponse;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<ReviewResponse> reviews = new ArrayList<>();

    public void setReviews(List<ReviewResponse> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewResponse review = reviews.get(position);

        // Meal Type - API doesn't provide this directly, using meal_id as placeholder or generic
        // In a real app, you might want to fetch meal info or have the API include the type.
        holder.tvMealType.setText("식단 #" + review.meal_id);
        
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", review.rating));
        holder.tvComment.setText(review.review_comment);
        holder.tvAuthor.setText(review.student_name);
        
        // Date formatting: 2024-05-14T08:36:11.000Z -> 5/14 (화)
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(review.created_at);
            SimpleDateFormat outputFormat = new SimpleDateFormat("M/d (E)", Locale.KOREAN);
            holder.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.tvDate.setText(review.created_at);
        }

        if (review.photo_url != null && !review.photo_url.isEmpty()) {
            holder.ivReviewPhoto.setVisibility(View.VISIBLE);
            // Assuming photo_url is a relative path or full URL
            // If it's relative, you might need to prepend the base URL
            Glide.with(holder.itemView.getContext())
                    .load(review.photo_url)
                    .into(holder.ivReviewPhoto);
        } else {
            holder.ivReviewPhoto.setVisibility(View.GONE);
        }

        // Like count is not in the API yet, using a placeholder
        holder.tvLikeCount.setText("0");
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealType, tvRating, tvComment, tvAuthor, tvDate, tvLikeCount;
        ImageView ivReviewPhoto;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            ivReviewPhoto = itemView.findViewById(R.id.ivReviewPhoto);
        }
    }
}
