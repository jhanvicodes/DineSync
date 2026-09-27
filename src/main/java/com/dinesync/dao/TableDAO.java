package com.dinesync.dao;

import com.dinesync.model.TableModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * TableDAO — handles all database operations for the 'restaurant_tables' table.
 * Most importantly: the availability query that prevents double-booking.
 */
@Repository
public class TableDAO {

    private final JdbcTemplate jdbcTemplate;

    public TableDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper: converts a database row into a TableModel object
    private final RowMapper<TableModel> tableRowMapper = (rs, rowNum) -> {
        TableModel t = new TableModel();
        t.setTableId(rs.getInt("table_id"));
        t.setRestaurantId(rs.getInt("restaurant_id"));
        t.setTableNumber(rs.getString("table_number"));
        t.setCapacity(rs.getInt("capacity"));
        t.setLocation(rs.getString("location"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    };

    /**
     * Get all tables for a specific restaurant.
     */
    public List<TableModel> findByRestaurant(int restaurantId) {
        String sql = "SELECT * FROM restaurant_tables WHERE restaurant_id = ? ORDER BY table_number";
        return jdbcTemplate.query(sql, tableRowMapper, restaurantId);
    }

    /**
     * Find a table by its ID.
     */
    public Optional<TableModel> findById(int tableId) {
        String sql = "SELECT * FROM restaurant_tables WHERE table_id = ?";
        List<TableModel> results = jdbcTemplate.query(sql, tableRowMapper, tableId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * ============================================================
     * CORE RESERVATION LOGIC: Get Available Tables
     * ============================================================
     *
     * This is the most important DBMS query in the project.
     *
     * It finds tables that:
     *   1. Belong to the selected restaurant
     *   2. Have enough capacity for the requested number of guests
     *   3. Are NOT already booked during the requested time window
     *
     * The NOT IN subquery finds table IDs that have CONFLICTING reservations:
     *   A conflict exists when the requested time overlaps with an existing booking.
     *   Overlap condition: startA < endB AND endA > startB
     *
     * @param restaurantId  The restaurant to check
     * @param date          The requested reservation date
     * @param requestedStart The start time the customer wants
     * @param requestedEnd   The end time (typically start + 2 hours)
     * @param guests         Minimum table capacity required
     * @return List of available tables
     */
    public List<TableModel> findAvailableTables(int restaurantId, LocalDate date,
                                                  LocalTime requestedStart, LocalTime requestedEnd,
                                                  int guests) {
        String sql =
            "SELECT * FROM restaurant_tables " +
            "WHERE restaurant_id = ? " +
            "AND capacity >= ? " +
            "AND table_id NOT IN ( " +
            "    SELECT table_id FROM reservations " +
            "    WHERE reservation_date = ? " +
            "    AND status = 'CONFIRMED' " +
            "    AND start_time < ? " +
            "    AND end_time > ? " +
            ") " +
            "ORDER BY capacity";

        return jdbcTemplate.query(sql, tableRowMapper,
            restaurantId,       // restaurant_id = ?
            guests,             // capacity >= ?
            java.sql.Date.valueOf(date),           // reservation_date = ?
            java.sql.Time.valueOf(requestedEnd),   // start_time < our end
            java.sql.Time.valueOf(requestedStart)  // end_time > our start
        );
    }

    /**
     * Check if a specific table is available (used during final booking to prevent race conditions).
     */
    public boolean isTableAvailable(int tableId, LocalDate date, LocalTime start, LocalTime end) {
        String sql =
            "SELECT COUNT(*) FROM reservations " +
            "WHERE table_id = ? " +
            "AND reservation_date = ? " +
            "AND status = 'CONFIRMED' " +
            "AND start_time < ? " +
            "AND end_time > ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
            tableId,
            java.sql.Date.valueOf(date),
            java.sql.Time.valueOf(end),
            java.sql.Time.valueOf(start)
        );
        return count != null && count == 0;
    }

    /**
     * Check if a specific table is available, EXCLUDING a specific reservation.
     * Used during modification so the user's own existing booking is not treated as a conflict.
     */
    public boolean isTableAvailableExcluding(int tableId, LocalDate date, LocalTime start, LocalTime end, int excludeReservationId) {
        String sql =
            "SELECT COUNT(*) FROM reservations " +
            "WHERE table_id = ? " +
            "AND reservation_date = ? " +
            "AND status = 'CONFIRMED' " +
            "AND reservation_id != ? " +
            "AND start_time < ? " +
            "AND end_time > ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
            tableId,
            java.sql.Date.valueOf(date),
            excludeReservationId,
            java.sql.Time.valueOf(end),
            java.sql.Time.valueOf(start)
        );
        return count != null && count == 0;
    }

    /**
     * Insert a new table.
     */
    public int save(TableModel t) {
        String sql = "INSERT INTO restaurant_tables (restaurant_id, table_number, capacity, location) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, t.getRestaurantId());
            ps.setString(2, t.getTableNumber());
            ps.setInt(3, t.getCapacity());
            ps.setString(4, t.getLocation() != null ? t.getLocation() : "Main Hall");
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * Update table details.
     */
    public void update(TableModel t) {
        String sql = "UPDATE restaurant_tables SET table_number=?, capacity=?, location=? WHERE table_id=?";
        jdbcTemplate.update(sql, t.getTableNumber(), t.getCapacity(), t.getLocation(), t.getTableId());
    }

    /**
     * Delete a table.
     */
    public void delete(int tableId) {
        jdbcTemplate.update("DELETE FROM restaurant_tables WHERE table_id = ?", tableId);
    }
}
