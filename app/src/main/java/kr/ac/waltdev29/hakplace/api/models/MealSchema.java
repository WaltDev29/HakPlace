package kr.ac.waltdev29.hakplace.api.models;

import java.util.List;

public class MealSchema {
    public int meal_id;
    public String served_date;
    public String meal_type;
    public double avg_rating;
    public int review_count;
    public List<String> foods;
}
