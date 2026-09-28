package com.dinesync.controller;

import com.dinesync.dao.ReservationDAO;
import com.dinesync.dao.RestaurantDAO;
import com.dinesync.dao.TableDAO;
import com.dinesync.dao.UserDAO;
import com.dinesync.model.Restaurant;
import com.dinesync.model.TableModel;
import com.dinesync.service.ReservationService;
import com.dinesync.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminController — REST API for admin dashboard and management operations.
 *
 * Endpoints:
 *   GET  /api/admin/dashboard          — Statistics + recent reservations
 *   GET  /api/admin/reservations       — All reservations (with filters)
 *   DELETE /api/admin/reservations/{id} — Cancel a reservation
 *   GET  /api/admin/customers          — All customers
 *   GET  /api/admin/analytics          — Analytics data
 *
 *   POST   /api/admin/restaurants      — Add restaurant
 *   PUT    /api/admin/restaurants/{id} — Update restaurant
 *   DELETE /api/admin/restaurants/{id} — Delete restaurant
 *
 *   GET    /api/admin/tables           — Get all tables for a restaurant
 *   POST   /api/admin/tables           — Add table
 *   PUT    /api/admin/tables/{id}      — Update table
 *   DELETE /api/admin/tables/{id}      — Delete table
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ReservationDAO reservationDAO;
    private final RestaurantDAO restaurantDAO;
    private final TableDAO tableDAO;
    private final UserDAO userDAO;
    private final ReservationService reservationService;
    private final RestaurantService restaurantService;

    public AdminController(ReservationDAO reservationDAO, RestaurantDAO restaurantDAO,
                           TableDAO tableDAO, UserDAO userDAO,
                           ReservationService reservationService, RestaurantService restaurantService) {
        this.reservationDAO = reservationDAO;
        this.restaurantDAO = restaurantDAO;
        this.tableDAO = tableDAO;
        this.userDAO = userDAO;
        this.reservationService = reservationService;
        this.restaurantService = restaurantService;
    }

    // ============================================================
    // ADMIN AUTH HELPER
    // ============================================================

    /**
     * Verify that the userId (from X-User-Id header) belongs to an ADMIN.
     * Returns a 403 ResponseEntity if not authorized, or null if authorized.
     * Used by all admin endpoints.
     */
    private ResponseEntity<Map<String, Object>> checkAdmin(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.status(403)
                .body(Map.of("success", false, "message", "Not authenticated."));
        }
        try {
            int userId = Integer.parseInt(userIdHeader);
            boolean isAdmin = userDAO.findById(userId)
                .map(u -> "ADMIN".equals(u.getRole()))
                .orElse(false);
            if (!isAdmin) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Admin access required."));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(403)
                .body(Map.of("success", false, "message", "Invalid user ID."));
        }
        return null; // null means authorized
    }

    // ============================================================
    // DASHBOARD
    // ============================================================

    /**
     * GET /api/admin/dashboard
     * Returns summary statistics and recent reservations.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;

        Map<String, Object> data = new HashMap<>();
        data.put("totalReservations", reservationDAO.countTotal());
        data.put("todayReservations", reservationDAO.countToday());
        data.put("totalCustomers", userDAO.countCustomers());
        data.put("totalRestaurants", restaurantDAO.count());
        data.put("recentReservations", reservationDAO.findRecent(10));
        return ResponseEntity.ok(data);
    }

    // ============================================================
    // ANALYTICS
    // ============================================================

    /**
     * GET /api/admin/analytics
     * Returns analytics data using SQL aggregate functions.
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;

        Map<String, Object> data = new HashMap<>();
        data.put("reservationsPerDay", reservationDAO.getReservationsPerDay());
        data.put("mostBookedRestaurants", reservationDAO.getMostBookedRestaurants());
        data.put("averageGuests", reservationDAO.getAverageGuests());
        data.put("popularTimeSlots", reservationDAO.getPopularTimeSlots());
        return ResponseEntity.ok(data);
    }

    // ============================================================
    // RESERVATIONS
    // ============================================================

    /**
     * GET /api/admin/reservations?date=&status=
     */
    @GetMapping("/reservations")
    public ResponseEntity<Object> getReservations(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return ResponseEntity.status(403).body(authError.getBody());
        return ResponseEntity.ok(reservationDAO.findFiltered(date, status));
    }

    /**
     * DELETE /api/admin/reservations/{id}
     * Admin cancels a reservation.
     */
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        // Admin can cancel any reservation (isAdmin = true)
        Map<String, Object> result = reservationService.cancelReservation(id, -1, true);
        return ResponseEntity.ok(result);
    }

    // ============================================================
    // CUSTOMERS
    // ============================================================

    /**
     * GET /api/admin/customers
     */
    @GetMapping("/customers")
    public ResponseEntity<Object> getCustomers(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return ResponseEntity.status(403).body(authError.getBody());

        List<Map<String, Object>> sanitized = userDAO.findAllCustomers().stream()
            .map(u -> {
                Map<String, Object> data = new HashMap<>();
                data.put("userId", u.getUserId());
                data.put("name", u.getName());
                data.put("email", u.getEmail());
                data.put("phone", u.getPhone());
                data.put("role", u.getRole());
                data.put("createdAt", u.getCreatedAt());
                return data;
            })
            .toList();

        return ResponseEntity.ok(sanitized);
    }

    // ============================================================
    // RESTAURANTS
    // ============================================================

    /**
     * POST /api/admin/restaurants
     */
    @PostMapping("/restaurants")
    public ResponseEntity<Map<String, Object>> addRestaurant(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody Map<String, Object> body) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        Restaurant r = buildRestaurantFromBody(null, body);
        return ResponseEntity.ok(restaurantService.addRestaurant(r));
    }

    /**
     * PUT /api/admin/restaurants/{id}
     */
    @PutMapping("/restaurants/{id}")
    public ResponseEntity<Map<String, Object>> updateRestaurant(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id, @RequestBody Map<String, Object> body) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        Restaurant r = buildRestaurantFromBody(id, body);
        return ResponseEntity.ok(restaurantService.updateRestaurant(r));
    }

    /**
     * DELETE /api/admin/restaurants/{id}
     */
    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<Map<String, Object>> deleteRestaurant(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        return ResponseEntity.ok(restaurantService.deleteRestaurant(id));
    }

    // ============================================================
    // TABLES
    // ============================================================

    /**
     * GET /api/admin/tables?restaurantId=1
     */
    @GetMapping("/tables")
    public ResponseEntity<Object> getTables(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam int restaurantId) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return ResponseEntity.status(403).body(authError.getBody());
        return ResponseEntity.ok(tableDAO.findByRestaurant(restaurantId));
    }

    /**
     * POST /api/admin/tables
     * Body: { "restaurantId": 1, "tableNumber": "T07", "capacity": 4, "location": "Patio" }
     */
    @PostMapping("/tables")
    public ResponseEntity<Map<String, Object>> addTable(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody Map<String, Object> body) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        TableModel t = buildTableFromBody(null, body);
        try {
            int id = tableDAO.save(t);
            return ResponseEntity.ok(Map.of("success", true, "message", "Table added.", "tableId", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/tables/{id}
     */
    @PutMapping("/tables/{id}")
    public ResponseEntity<Map<String, Object>> updateTable(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id, @RequestBody Map<String, Object> body) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        TableModel t = buildTableFromBody(id, body);
        try {
            tableDAO.update(t);
            return ResponseEntity.ok(Map.of("success", true, "message", "Table updated."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/tables/{id}
     */
    @DeleteMapping("/tables/{id}")
    public ResponseEntity<Map<String, Object>> deleteTable(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        tableDAO.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Table deleted."));
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private Restaurant buildRestaurantFromBody(Integer id, Map<String, Object> body) {
        Restaurant r = new Restaurant();
        if (id != null) r.setRestaurantId(id);
        r.setName((String) body.get("name"));
        r.setDescription((String) body.get("description"));
        r.setLocation((String) body.get("location"));
        r.setCuisine((String) body.get("cuisine"));
        r.setPriceRange((String) body.getOrDefault("priceRange", "$$"));
        Object rating = body.get("rating");
        r.setRating(rating != null ? new BigDecimal(rating.toString()) : BigDecimal.ZERO);
        r.setPhone((String) body.get("phone"));
        String openTime = (String) body.get("openingTime");
        if (openTime != null) r.setOpeningTime(LocalTime.parse(openTime));
        String closeTime = (String) body.get("closingTime");
        if (closeTime != null) r.setClosingTime(LocalTime.parse(closeTime));
        r.setImageUrl((String) body.get("imageUrl"));
        return r;
    }

    private TableModel buildTableFromBody(Integer id, Map<String, Object> body) {
        TableModel t = new TableModel();
        if (id != null) t.setTableId(id);
        t.setRestaurantId(Integer.parseInt(body.get("restaurantId").toString()));
        t.setTableNumber((String) body.get("tableNumber"));
        t.setCapacity(Integer.parseInt(body.get("capacity").toString()));
        t.setLocation((String) body.getOrDefault("location", "Main Hall"));
        return t;
    }
}
