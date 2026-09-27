package com.dinesync.dao;

import com.dinesync.model.MenuItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * MenuDAO — handles all database operations for the 'menu_items' table.
 */
@Repository
public class MenuDAO {

    private final JdbcTemplate jdbcTemplate;

    public MenuDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper: converts a database row into a MenuItem object
    private final RowMapper<MenuItem> menuItemRowMapper = (rs, rowNum) -> {
        MenuItem item = new MenuItem();
        item.setMenuId(rs.getInt("menu_id"));
        item.setRestaurantId(rs.getInt("restaurant_id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setCategory(rs.getString("category"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setImageUrl(rs.getString("image_url"));
        item.setAvailable(rs.getBoolean("available"));
        return item;
    };

    /**
     * Get all menu items for a restaurant, grouped by category.
     */
    public List<MenuItem> findByRestaurant(int restaurantId) {
        String sql = "SELECT * FROM menu_items WHERE restaurant_id = ? ORDER BY category, name";
        return jdbcTemplate.query(sql, menuItemRowMapper, restaurantId);
    }

    /**
     * Get all menu items (admin view).
     */
    public List<MenuItem> findAll() {
        String sql = "SELECT * FROM menu_items ORDER BY restaurant_id, category, name";
        return jdbcTemplate.query(sql, menuItemRowMapper);
    }

    /**
     * Find a single menu item by ID.
     */
    public Optional<MenuItem> findById(int menuId) {
        String sql = "SELECT * FROM menu_items WHERE menu_id = ?";
        List<MenuItem> results = jdbcTemplate.query(sql, menuItemRowMapper, menuId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Insert a new menu item.
     */
    public int save(MenuItem item) {
        String sql = "INSERT INTO menu_items (restaurant_id, name, description, category, price, image_url, available) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, item.getRestaurantId());
            ps.setString(2, item.getName());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getCategory());
            ps.setBigDecimal(5, item.getPrice());
            ps.setString(6, item.getImageUrl());
            ps.setBoolean(7, item.getAvailable() != null ? item.getAvailable() : true);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * Update an existing menu item.
     */
    public void update(MenuItem item) {
        String sql = "UPDATE menu_items SET name=?, description=?, category=?, price=?, image_url=?, available=? WHERE menu_id=?";
        jdbcTemplate.update(sql,
            item.getName(), item.getDescription(), item.getCategory(),
            item.getPrice(), item.getImageUrl(), item.getAvailable(), item.getMenuId());
    }

    /**
     * Delete a menu item.
     */
    public void delete(int menuId) {
        jdbcTemplate.update("DELETE FROM menu_items WHERE menu_id = ?", menuId);
    }
}
