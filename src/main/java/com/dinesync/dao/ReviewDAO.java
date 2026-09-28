package com.dinesync.dao;

import com.dinesync.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

/**
 * ReviewDAO — handles database operations for the 'reviews' table.
 */
@Repository
public class ReviewDAO {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper: converts a database row (with join) into a Review object
    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setRestaurantId(rs.getInt("restaurant_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) review.setCreatedAt(ts.toLocalDateTime());
        try { review.setUserName(rs.getString("user_name")); } catch (Exception ignored) {}
        return review;
    };

    /**
     * Get all reviews for a restaurant, most recent first.
     * JOIN to include the reviewer's name.
     */
    public List<Review> findByRestaurant(int restaurantId) {
        String sql =
            "SELECT rv.*, u.name AS user_name " +
            "FROM reviews rv " +
            "JOIN users u ON rv.user_id = u.user_id " +
            "WHERE rv.restaurant_id = ? " +
            "ORDER BY rv.created_at DESC";
        return jdbcTemplate.query(sql, reviewRowMapper, restaurantId);
    }

    /**
     * Save a new review and return its generated ID.
     */
    public int save(Review review) {
        String sql = "INSERT INTO reviews (user_id, restaurant_id, rating, comment) VALUES (?, ?, ?, ?) RETURNING review_id";

        Integer reviewId = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            review.getUserId(),
            review.getRestaurantId(),
            review.getRating(),
            review.getComment()
        );

        if (reviewId == null) {
            throw new IllegalStateException("Review creation succeeded but no review_id was returned from the database.");
        }

        return reviewId;
    }
}
