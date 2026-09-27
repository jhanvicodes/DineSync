package com.dinesync.dao;

import com.dinesync.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * UserDAO — handles all database operations for the 'users' table.
 * Uses JdbcTemplate (simple SQL — no Hibernate/JPA).
 */
@Repository
public class UserDAO {

    // JdbcTemplate is injected by Spring Boot automatically
    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- RowMapper: converts a database row into a User object ---
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) user.setCreatedAt(ts.toLocalDateTime());
        return user;
    };

    /**
     * Find a user by their email address.
     * Used during login — email is unique.
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /**
     * Find a user by their ID.
     */
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, userId);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /**
     * Get all users with CUSTOMER role (for admin dashboard).
     */
    public List<User> findAllCustomers() {
        String sql = "SELECT * FROM users WHERE role = 'CUSTOMER' ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    /**
     * Count total number of customers.
     */
    public int countCustomers() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Insert a new user into the database.
     * Returns the auto-generated user_id.
     */
    public int save(User user) {
        String sql = "INSERT INTO users (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole() != null ? user.getRole() : "CUSTOMER");
            return ps;
        }, keyHolder);

        // Return the generated primary key
        return keyHolder.getKey().intValue();
    }

    /**
     * Update user profile information.
     */
    public void update(User user) {
        String sql = "UPDATE users SET name = ?, phone = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getPhone(), user.getUserId());
    }

    /**
     * Update user password.
     */
    public void updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, hashedPassword, userId);
    }

    /**
     * Check if an email already exists in the database.
     * Used during signup to prevent duplicate accounts.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}
