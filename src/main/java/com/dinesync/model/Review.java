package com.dinesync.model;

import java.time.LocalDateTime;

/**
 * Review model — represents a row in the 'reviews' table.
 * Customers can leave a 1–5 star review for a restaurant they visited.
 */
public class Review {

    private Integer reviewId;
    private Integer userId;
    private Integer restaurantId;
    private Integer rating;          // 1 to 5 (enforced by DB CHECK constraint)
    private String comment;
    private LocalDateTime createdAt;

    // Extra fields populated by JOIN queries
    private String userName;
    private String restaurantName;

    // Default constructor
    public Review() {}

    // --- Getters and Setters ---

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Integer restaurantId) { this.restaurantId = restaurantId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
}
