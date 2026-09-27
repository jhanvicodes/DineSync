package com.dinesync.dao;

import com.dinesync.model.Restaurant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * RestaurantDAO — handles all database operations for the 'restaurants' table.
 */
@Repository
public class RestaurantDAO {

    private final JdbcTemplate jdbcTemplate;

    public RestaurantDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper: converts a database row into a Restaurant object
    private final RowMapper<Restaurant> restaurantRowMapper = (rs, rowNum) -> {
        Restaurant r = new Restaurant();
        r.setRestaurantId(rs.getInt("restaurant_id"));
        r.setName(rs.getString("name"));
        r.setDescription(rs.getString("description"));
        r.setLocation(rs.getString("location"));
        r.setCuisine(rs.getString("cuisine"));
        r.setPriceRange(rs.getString("price_range"));
        r.setRating(rs.getBigDecimal("rating"));
        r.setPhone(rs.getString("phone"));
        Time openTime = rs.getTime("opening_time");
        if (openTime != null) r.setOpeningTime(openTime.toLocalTime());
        Time closeTime = rs.getTime("closing_time");
        if (closeTime != null) r.setClosingTime(closeTime.toLocalTime());
        r.setImageUrl(rs.getString("image_url"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    };

    /**
     * Get all restaurants, ordered by rating descending.
     */
    public List<Restaurant> findAll() {
        String sql = "SELECT * FROM restaurants ORDER BY rating DESC";
        return jdbcTemplate.query(sql, restaurantRowMapper);
    }

    /**
     * Search restaurants by name, location, or cuisine (case-insensitive).
     * Supports optional filters: cuisine, location, priceRange.
     */
    public List<Restaurant> search(String query, String cuisine, String location, String priceRange) {
        StringBuilder sql = new StringBuilder(
            "SELECT * FROM restaurants WHERE 1=1"
        );
        // Build dynamic WHERE clauses
        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE LOWER(?) OR LOWER(cuisine) LIKE LOWER(?) OR LOWER(location) LIKE LOWER(?))");
        }
        if (cuisine != null && !cuisine.isBlank()) {
            sql.append(" AND LOWER(cuisine) = LOWER(?)");
        }
        if (location != null && !location.isBlank()) {
            sql.append(" AND LOWER(location) LIKE LOWER(?)");
        }
        if (priceRange != null && !priceRange.isBlank()) {
            sql.append(" AND price_range = ?");
        }
        sql.append(" ORDER BY rating DESC");

        // Build parameters list
        List<Object> params = new java.util.ArrayList<>();
        if (query != null && !query.isBlank()) {
            String like = "%" + query + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (cuisine != null && !cuisine.isBlank()) params.add(cuisine);
        if (location != null && !location.isBlank()) params.add("%" + location + "%");
        if (priceRange != null && !priceRange.isBlank()) params.add(priceRange);

        return jdbcTemplate.query(sql.toString(), restaurantRowMapper, params.toArray());
    }

    /**
     * Find a single restaurant by ID.
     */
    public Optional<Restaurant> findById(int id) {
        String sql = "SELECT * FROM restaurants WHERE restaurant_id = ?";
        List<Restaurant> results = jdbcTemplate.query(sql, restaurantRowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Get all distinct cuisine types (for filter dropdowns).
     */
    public List<String> findAllCuisines() {
        String sql = "SELECT DISTINCT cuisine FROM restaurants ORDER BY cuisine";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * Get all distinct locations.
     */
    public List<String> findAllLocations() {
        String sql = "SELECT DISTINCT location FROM restaurants ORDER BY location";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * Count total restaurants.
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM restaurants";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Insert a new restaurant.
     */
    public int save(Restaurant r) {
        String sql = "INSERT INTO restaurants (name, description, location, cuisine, price_range, rating, phone, opening_time, closing_time, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.getName());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getLocation());
            ps.setString(4, r.getCuisine());
            ps.setString(5, r.getPriceRange() != null ? r.getPriceRange() : "$$");
            ps.setBigDecimal(6, r.getRating() != null ? r.getRating() : BigDecimal.ZERO);
            ps.setString(7, r.getPhone());
            ps.setTime(8, r.getOpeningTime() != null ? Time.valueOf(r.getOpeningTime()) : null);
            ps.setTime(9, r.getClosingTime() != null ? Time.valueOf(r.getClosingTime()) : null);
            ps.setString(10, r.getImageUrl());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * Update an existing restaurant.
     */
    public void update(Restaurant r) {
        String sql = "UPDATE restaurants SET name=?, description=?, location=?, cuisine=?, price_range=?, phone=?, opening_time=?, closing_time=?, image_url=? WHERE restaurant_id=?";
        jdbcTemplate.update(sql,
            r.getName(), r.getDescription(), r.getLocation(), r.getCuisine(),
            r.getPriceRange(), r.getPhone(),
            r.getOpeningTime() != null ? Time.valueOf(r.getOpeningTime()) : null,
            r.getClosingTime() != null ? Time.valueOf(r.getClosingTime()) : null,
            r.getImageUrl(), r.getRestaurantId());
    }

    /**
     * Delete a restaurant by ID (cascades to tables, menu, reservations).
     */
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM restaurants WHERE restaurant_id = ?", id);
    }

    /**
     * Recalculate and update the average rating from reviews.
     */
    public void updateRating(int restaurantId) {
        String sql = "UPDATE restaurants SET rating = COALESCE(" +
                     "(SELECT ROUND(AVG(rating)::NUMERIC, 1) FROM reviews WHERE restaurant_id = ?), 0) " +
                     "WHERE restaurant_id = ?";
        jdbcTemplate.update(sql, restaurantId, restaurantId);
    }
}
