package com.dinesync.dao;

import com.dinesync.model.Reservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ReservationDAO — handles all database operations for the 'reservations' table.
 * Uses JOIN queries to fetch related user/restaurant/table names.
 */
@Repository
public class ReservationDAO {

    private final JdbcTemplate jdbcTemplate;

    public ReservationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper: converts a database row (with joins) into a Reservation object
    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> {
        Reservation res = new Reservation();
        res.setReservationId(rs.getInt("reservation_id"));
        res.setUserId(rs.getInt("user_id"));
        res.setRestaurantId(rs.getInt("restaurant_id"));
        res.setTableId(rs.getInt("table_id"));

        Date date = rs.getDate("reservation_date");
        if (date != null) res.setReservationDate(date.toLocalDate());

        Time startTime = rs.getTime("start_time");
        if (startTime != null) res.setStartTime(startTime.toLocalTime());

        Time endTime = rs.getTime("end_time");
        if (endTime != null) res.setEndTime(endTime.toLocalTime());

        res.setGuests(rs.getInt("guests"));
        res.setStatus(rs.getString("status"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) res.setCreatedAt(ts.toLocalDateTime());

        // Joined fields (may be null if simple query without JOIN)
        try { res.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        try { res.setRestaurantName(rs.getString("restaurant_name")); } catch (SQLException ignored) {}
        try { res.setTableNumber(rs.getString("table_number")); } catch (SQLException ignored) {}
        try { res.setTableCapacity(rs.getInt("capacity")); } catch (SQLException ignored) {}
        try { res.setTableLocation(rs.getString("table_location")); } catch (SQLException ignored) {}

        return res;
    };

    // SQL for joined reservation query (used in multiple places)
    private static final String JOIN_SQL =
        "SELECT r.*, " +
        "       u.name AS user_name, " +
        "       rest.name AS restaurant_name, " +
        "       rt.table_number, rt.capacity, rt.location AS table_location " +
        "FROM reservations r " +
        "JOIN users u ON r.user_id = u.user_id " +
        "JOIN restaurants rest ON r.restaurant_id = rest.restaurant_id " +
        "JOIN restaurant_tables rt ON r.table_id = rt.table_id ";

    /**
     * Get all reservations for a specific user (for My Reservations page).
     * Uses JOIN to include restaurant and table info.
     */
    public List<Reservation> findByUser(int userId) {
        String sql = JOIN_SQL + "WHERE r.user_id = ? ORDER BY r.reservation_date DESC, r.start_time DESC";
        return jdbcTemplate.query(sql, reservationRowMapper, userId);
    }

    /**
     * Get a single reservation by ID.
     */
    public Optional<Reservation> findById(int reservationId) {
        String sql = JOIN_SQL + "WHERE r.reservation_id = ?";
        List<Reservation> results = jdbcTemplate.query(sql, reservationRowMapper, reservationId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Get all reservations (for admin dashboard).
     */
    public List<Reservation> findAll() {
        String sql = JOIN_SQL + "ORDER BY r.reservation_date DESC, r.start_time DESC";
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    /**
     * Get reservations filtered by date and/or status (for admin).
     */
    public List<Reservation> findFiltered(String date, String status) {
        StringBuilder sql = new StringBuilder(JOIN_SQL + "WHERE 1=1 ");
        List<Object> params = new java.util.ArrayList<>();
        if (date != null && !date.isBlank()) {
            sql.append("AND r.reservation_date = ? ");
            params.add(java.sql.Date.valueOf(date));
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND r.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY r.reservation_date DESC, r.start_time DESC");
        return jdbcTemplate.query(sql.toString(), reservationRowMapper, params.toArray());
    }

    /**
     * Count today's reservations (for admin dashboard).
     */
    public int countToday() {
        String sql = "SELECT COUNT(*) FROM reservations WHERE reservation_date = CURRENT_DATE AND status = 'CONFIRMED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Count total reservations (for admin dashboard).
     */
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM reservations WHERE status != 'CANCELLED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Get analytics: reservations per day (last 7 days).
     * Demonstrates GROUP BY and COUNT.
     */
    public List<Map<String, Object>> getReservationsPerDay() {
        String sql =
            "SELECT reservation_date, COUNT(*) AS count " +
            "FROM reservations " +
            "WHERE reservation_date >= CURRENT_DATE - 7 " +
            "AND status != 'CANCELLED' " +
            "GROUP BY reservation_date " +
            "ORDER BY reservation_date";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Get analytics: most booked restaurant.
     * Demonstrates JOIN, GROUP BY, ORDER BY, COUNT.
     */
    public List<Map<String, Object>> getMostBookedRestaurants() {
        String sql =
            "SELECT r.name AS restaurant_name, COUNT(res.reservation_id) AS booking_count " +
            "FROM restaurants r " +
            "LEFT JOIN reservations res ON r.restaurant_id = res.restaurant_id AND res.status != 'CANCELLED' " +
            "GROUP BY r.restaurant_id, r.name " +
            "ORDER BY booking_count DESC " +
            "LIMIT 5";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Get analytics: average guests per reservation.
     * Demonstrates AVG aggregate function.
     */
    public double getAverageGuests() {
        String sql = "SELECT AVG(guests) FROM reservations WHERE status != 'CANCELLED'";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    /**
     * Get analytics: most popular time slots.
     */
    public List<Map<String, Object>> getPopularTimeSlots() {
        String sql =
            "SELECT start_time, COUNT(*) AS count " +
            "FROM reservations " +
            "WHERE status != 'CANCELLED' " +
            "GROUP BY start_time " +
            "ORDER BY count DESC " +
            "LIMIT 5";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Create a new reservation.
     * Returns the auto-generated reservation_id.
     */
    public int save(Reservation res) {
        String sql =
            "INSERT INTO reservations (user_id, restaurant_id, table_id, reservation_date, start_time, end_time, guests, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'CONFIRMED') RETURNING reservation_id";

        Integer reservationId = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            res.getUserId(),
            res.getRestaurantId(),
            res.getTableId(),
            java.sql.Date.valueOf(res.getReservationDate()),
            java.sql.Time.valueOf(res.getStartTime()),
            java.sql.Time.valueOf(res.getEndTime()),
            res.getGuests()
        );

        if (reservationId == null) {
            throw new IllegalStateException("Reservation creation succeeded but no reservation_id was returned from the database.");
        }

        return reservationId;
    }

    /**
     * Update reservation (date, time, guests, table).
     */
    public void update(Reservation res) {
        String sql =
            "UPDATE reservations SET reservation_date=?, start_time=?, end_time=?, guests=?, table_id=? " +
            "WHERE reservation_id=?";
        jdbcTemplate.update(sql,
            java.sql.Date.valueOf(res.getReservationDate()),
            java.sql.Time.valueOf(res.getStartTime()),
            java.sql.Time.valueOf(res.getEndTime()),
            res.getGuests(),
            res.getTableId(),
            res.getReservationId()
        );
    }

    /**
     * Cancel a reservation by updating status to 'CANCELLED'.
     */
    public void cancel(int reservationId) {
        jdbcTemplate.update("UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?", reservationId);
    }

    /**
     * Get recent 10 reservations for admin dashboard.
     */
    public List<Reservation> findRecent(int limit) {
        String sql = JOIN_SQL + "ORDER BY r.created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, reservationRowMapper, limit);
    }
}
