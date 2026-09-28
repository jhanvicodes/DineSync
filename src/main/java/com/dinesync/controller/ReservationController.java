package com.dinesync.controller;

import com.dinesync.dao.UserDAO;
import com.dinesync.model.Reservation;
import com.dinesync.model.TableModel;
import com.dinesync.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ReservationController — REST API for reservation operations.
 *
 * Endpoints:
 *   GET  /api/restaurants/{id}/tables/available — Get available tables
 *   POST /api/reservations                       — Create reservation
 *   GET  /api/reservations/user/{userId}         — Get user's reservations
 *   GET  /api/reservations/{id}                  — Get single reservation
 *   PUT  /api/reservations/{id}                  — Modify reservation
 *   DELETE /api/reservations/{id}                — Cancel reservation
 */
@RestController
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserDAO userDAO;

    public ReservationController(ReservationService reservationService, UserDAO userDAO) {
        this.reservationService = reservationService;
        this.userDAO = userDAO;
    }

    /**
     * GET /api/restaurants/{id}/tables/available?date=2026-09-28&time=19:30&guests=4
     * Returns list of available tables for the given criteria.
     */
    @GetMapping("/api/restaurants/{id}/tables/available")
    public ResponseEntity<List<TableModel>> getAvailableTables(
            @PathVariable int id,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam int guests) {
        List<TableModel> available = reservationService.getAvailableTables(id, date, time, guests);
        return ResponseEntity.ok(available);
    }

    /**
     * POST /api/reservations
     * Body: { "userId": 1, "restaurantId": 1, "tableId": 3, "date": "2026-09-28", "time": "19:30", "guests": 4 }
     */
    @PostMapping("/api/reservations")
    public ResponseEntity<Map<String, Object>> createReservation(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody Map<String, Object> body) {
        try {
            if (userIdHeader == null || userIdHeader.isBlank()) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Not authenticated."));
            }
            if (body == null || body.get("userId") == null || body.get("restaurantId") == null
                    || body.get("tableId") == null || body.get("date") == null
                    || body.get("time") == null || body.get("guests") == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "All reservation fields are required."));
            }

            int currentUserId = Integer.parseInt(userIdHeader);
            int userId = Integer.parseInt(body.get("userId").toString());
            if (currentUserId != userId) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "You can only create reservations for your own account."));
            }

            int restaurantId = Integer.parseInt(body.get("restaurantId").toString());
            int tableId = Integer.parseInt(body.get("tableId").toString());
            String date = (String) body.get("date");
            String time = (String) body.get("time");
            int guests = Integer.parseInt(body.get("guests").toString());

            Map<String, Object> result = reservationService.createReservation(
                userId, restaurantId, tableId, date, time, guests
            );

            if (Boolean.FALSE.equals(result.get("success"))) {
                String msg = (String) result.get("message");
                if (msg != null && msg.contains("reserved")) {
                    return ResponseEntity.status(409).body(result);
                }
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.status(201).body(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Invalid request: " + e.getMessage()));
        }
    }

    /**
     * GET /api/reservations/user/{userId}
     * Returns all reservations for a specific user.
     */
    @GetMapping("/api/reservations/user/{userId}")
    public ResponseEntity<Object> getUserReservations(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int userId) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Not authenticated."));
        }
        try {
            int currentUserId = Integer.parseInt(userIdHeader);
            if (currentUserId != userId) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "You can only view your own reservations."));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Invalid user ID."));
        }

        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    /**
     * GET /api/reservations/{id}
     * Returns a single reservation by ID.
     */
    @GetMapping("/api/reservations/{id}")
    public ResponseEntity<Object> getReservation(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Not authenticated."));
        }

        Optional<Reservation> reservationOpt = reservationService.getReservationById(id);
        if (reservationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            int currentUserId = Integer.parseInt(userIdHeader);
            boolean isAdmin = userDAO.findById(currentUserId).map(u -> "ADMIN".equals(u.getRole())).orElse(false);
            if (!isAdmin && !reservationOpt.get().getUserId().equals(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "You can only access your own reservations."));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Invalid user ID."));
        }

        return ResponseEntity.ok(reservationOpt.get());
    }

    /**
     * PUT /api/reservations/{id}
     * Modify an existing reservation.
     * Body: { "userId": 1, "date": "...", "time": "...", "guests": 4, "tableId": 3 }
     */
    @PutMapping("/api/reservations/{id}")
    public ResponseEntity<Map<String, Object>> modifyReservation(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            if (userIdHeader == null || userIdHeader.isBlank()) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Not authenticated."));
            }
            if (body == null || body.get("userId") == null || body.get("date") == null
                    || body.get("time") == null || body.get("guests") == null
                    || body.get("tableId") == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing required fields for modification."));
            }

            int currentUserId = Integer.parseInt(userIdHeader);
            int userId = Integer.parseInt(body.get("userId").toString());
            if (currentUserId != userId) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "You can only modify your own reservations."));
            }

            String date = (String) body.get("date");
            String time = (String) body.get("time");
            int guests = Integer.parseInt(body.get("guests").toString());
            int tableId = Integer.parseInt(body.get("tableId").toString());

            Map<String, Object> result = reservationService.modifyReservation(id, userId, date, time, guests, tableId);

            if (Boolean.FALSE.equals(result.get("success"))) {
                String msg = (String) result.get("message");
                if (msg != null && msg.contains("not authorized")) {
                    return ResponseEntity.status(403).body(result);
                }
                if (msg != null && msg.contains("not available")) {
                    return ResponseEntity.status(409).body(result);
                }
                if (msg != null && msg.contains("not found")) {
                    return ResponseEntity.status(404).body(result);
                }
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Invalid request: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/reservations/{id}?userId=1
     * Cancel a reservation.
     * The isAdmin status is determined from the DB, not from the request parameter.
     */
    @DeleteMapping("/api/reservations/{id}")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id,
            @RequestParam int userId) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return ResponseEntity.status(403)
                .body(Map.of("success", false, "message", "Not authenticated."));
        }
        try {
            int currentUserId = Integer.parseInt(userIdHeader);
            if (currentUserId != userId) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "You can only cancel your own reservations."));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(403)
                .body(Map.of("success", false, "message", "Invalid user ID."));
        }

        boolean isAdmin = userDAO.findById(userId)
            .map(u -> "ADMIN".equals(u.getRole()))
            .orElse(false);

        Map<String, Object> result = reservationService.cancelReservation(id, userId, isAdmin);

        if (Boolean.FALSE.equals(result.get("success"))) {
            String msg = (String) result.get("message");
            if (msg != null && msg.contains("not authorized")) {
                return ResponseEntity.status(403).body(result);
            }
            if (msg != null && msg.contains("not found")) {
                return ResponseEntity.status(404).body(result);
            }
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }
}
