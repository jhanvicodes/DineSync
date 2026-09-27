package com.dinesync.service;

import com.dinesync.dao.RestaurantDAO;
import com.dinesync.dao.ReviewDAO;
import com.dinesync.dao.UserDAO;
import com.dinesync.model.Restaurant;
import com.dinesync.model.Review;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * RestaurantService — business logic for restaurant operations.
 * Controllers call this service, which in turn calls DAOs.
 */
@Service
public class RestaurantService {

    private final RestaurantDAO restaurantDAO;
    private final ReviewDAO reviewDAO;
    private final UserDAO userDAO;

    public RestaurantService(RestaurantDAO restaurantDAO, ReviewDAO reviewDAO, UserDAO userDAO) {
        this.restaurantDAO = restaurantDAO;
        this.reviewDAO = reviewDAO;
        this.userDAO = userDAO;
    }

    /**
     * Get all restaurants with optional search and filter parameters.
     */
    public List<Restaurant> getRestaurants(String query, String cuisine, String location, String priceRange) {
        return restaurantDAO.search(query, cuisine, location, priceRange);
    }

    /**
     * Get restaurant details including reviews.
     */
    public Map<String, Object> getRestaurantDetails(int restaurantId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Restaurant> restaurantOpt = restaurantDAO.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Restaurant not found.");
            return response;
        }

        List<Review> reviews = reviewDAO.findByRestaurant(restaurantId);

        response.put("success", true);
        response.put("restaurant", restaurantOpt.get());
        response.put("reviews", reviews);
        return response;
    }

    /**
     * Get all distinct cuisine types for filter dropdown.
     */
    public List<String> getCuisines() {
        return restaurantDAO.findAllCuisines();
    }

    /**
     * Get all distinct locations for filter dropdown.
     */
    public List<String> getLocations() {
        return restaurantDAO.findAllLocations();
    }

    /**
     * Add a new restaurant (admin only).
     */
    public Map<String, Object> addRestaurant(Restaurant restaurant) {
        Map<String, Object> response = new HashMap<>();
        try {
            int id = restaurantDAO.save(restaurant);
            response.put("success", true);
            response.put("message", "Restaurant added successfully.");
            response.put("restaurantId", id);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add restaurant: " + e.getMessage());
        }
        return response;
    }

    /**
     * Update restaurant details (admin only).
     */
    public Map<String, Object> updateRestaurant(Restaurant restaurant) {
        Map<String, Object> response = new HashMap<>();
        Optional<Restaurant> existing = restaurantDAO.findById(restaurant.getRestaurantId());
        if (existing.isEmpty()) {
            response.put("success", false);
            response.put("message", "Restaurant not found.");
            return response;
        }
        restaurantDAO.update(restaurant);
        response.put("success", true);
        response.put("message", "Restaurant updated successfully.");
        return response;
    }

    /**
     * Delete a restaurant (admin only).
     */
    public Map<String, Object> deleteRestaurant(int restaurantId) {
        Map<String, Object> response = new HashMap<>();
        restaurantDAO.delete(restaurantId);
        response.put("success", true);
        response.put("message", "Restaurant deleted successfully.");
        return response;
    }

    /**
     * Post a review for a restaurant.
     * After saving the review, recalculate the restaurant's average rating.
     */
    public Map<String, Object> addReview(Review review) {
        Map<String, Object> response = new HashMap<>();

        if (review.getUserId() == null || userDAO.findById(review.getUserId()).isEmpty()) {
            response.put("success", false);
            response.put("message", "Must be logged in to leave a review.");
            return response;
        }

        if (restaurantDAO.findById(review.getRestaurantId()).isEmpty()) {
            response.put("success", false);
            response.put("message", "Restaurant not found.");
            return response;
        }

        // Validate rating is 1–5
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            response.put("success", false);
            response.put("message", "Rating must be between 1 and 5.");
            return response;
        }

        int reviewId = reviewDAO.save(review);

        // Recalculate restaurant rating from all reviews
        restaurantDAO.updateRating(review.getRestaurantId());

        response.put("success", true);
        response.put("message", "Review posted successfully.");
        response.put("reviewId", reviewId);
        return response;
    }
}
