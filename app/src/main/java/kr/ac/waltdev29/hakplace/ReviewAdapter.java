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
    // mealTypeMapping is now deprecated as we get it directly from API

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

        // Use meal_type directly from API
        holder.tvMealType.setText(review.meal_type != null ? review.meal_type : holder.itemView.getContext().getString(R.string.meal_label));
        
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f", review.rating));
        holder.tvComment.setText(review.review_comment);
        holder.tvAuthor.setText(review.student_name);
        
        // Display meal_date instead of created_at
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(review.meal_date);
            SimpleDateFormat outputFormat = new SimpleDateFormat("M/d (E)", Locale.KOREAN);
            holder.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.tvDate.setText(review.meal_date);
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

        // Like count is not in the API yet, removed for now to fix build error
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealType, tvRating, tvComment, tvAuthor, tvDate;
        ImageView ivReviewPhoto;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivReviewPhoto = itemView.findViewById(R.id.ivReviewPhoto);
        }
    }
}
