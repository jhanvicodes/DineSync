package com.dinesync.service;

import com.dinesync.dao.ReservationDAO;
import com.dinesync.dao.RestaurantDAO;
import com.dinesync.dao.TableDAO;
import com.dinesync.model.Reservation;
import com.dinesync.model.Restaurant;
import com.dinesync.model.TableModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ReservationService — the core business logic of the entire application.
 *
 * This service handles:
 * 1. Finding available tables for a requested date/time
 * 2. Creating new reservations (with server-side availability & operating hours check)
 * 3. Modifying existing reservations (excluding current booking from overlap check)
 * 4. Cancelling reservations (with server-enforced 2-hour cancellation rule)
 */
@Service
public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final TableDAO tableDAO;
    private final RestaurantDAO restaurantDAO;

    public ReservationService(ReservationDAO reservationDAO, TableDAO tableDAO, RestaurantDAO restaurantDAO) {
        this.reservationDAO = reservationDAO;
        this.tableDAO = tableDAO;
        this.restaurantDAO = restaurantDAO;
    }

    /**
     * STEP 1: Get available tables for a date/time/guests combination.
     * Checks restaurant operating hours and table capacity.
     */
    public List<TableModel> getAvailableTables(int restaurantId, String dateStr, String timeStr, int guests) {
        if (guests <= 0) return List.of();

        LocalDate date = LocalDate.parse(dateStr);
        LocalTime startTime = LocalTime.parse(timeStr);
        LocalTime endTime = startTime.plusHours(2); // Standard 2-hour reservation window

        // Validate date/time is not in the past
        if (date.isBefore(LocalDate.now())) {
            return List.of();
        }
        if (date.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            return List.of();
        }

        // Validate restaurant operating hours
        Optional<Restaurant> restOpt = restaurantDAO.findById(restaurantId);
        if (restOpt.isPresent()) {
            Restaurant r = restOpt.get();
            if (r.getOpeningTime() != null && r.getClosingTime() != null) {
                if (startTime.isBefore(r.getOpeningTime()) || endTime.isAfter(r.getClosingTime())) {
                    return List.of();
                }
            }
        }

        return tableDAO.findAvailableTables(restaurantId, date, startTime, endTime, guests);
    }

    /**
     * STEP 2: Create a reservation.
     *
     * Comprehensive server-side validations:
     * - Date not in the past
     * - Restaurant exists
     * - Operating hours respected
     * - Guest count positive
     * - Table exists, belongs to restaurant, and capacity >= guests
     * - Final real-time availability check to prevent double booking
     */
    public Map<String, Object> createReservation(int userId, int restaurantId, int tableId,
                                                   String dateStr, String timeStr, int guests) {
        Map<String, Object> response = new HashMap<>();

        if (guests <= 0) {
            response.put("success", false);
            response.put("message", "Number of guests must be at least 1.");
            return response;
        }

        LocalDate date;
        LocalTime startTime;
        try {
            date = LocalDate.parse(dateStr);
            startTime = LocalTime.parse(timeStr);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Invalid date or time format.");
            return response;
        }
        LocalTime endTime = startTime.plusHours(2);

        // Validate date/time is not in the past
        if (date.isBefore(LocalDate.now())) {
            response.put("success", false);
            response.put("message", "Reservations cannot be made for past dates.");
            return response;
        }
        if (date.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            response.put("success", false);
            response.put("message", "Reservations cannot be made for a past time.");
            return response;
        }

        // Validate restaurant and operating hours
        Optional<Restaurant> restOpt = restaurantDAO.findById(restaurantId);
        if (restOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Restaurant not found.");
            return response;
        }
        Restaurant restaurant = restOpt.get();
        if (restaurant.getOpeningTime() != null && restaurant.getClosingTime() != null) {
            if (startTime.isBefore(restaurant.getOpeningTime()) || endTime.isAfter(restaurant.getClosingTime())) {
                response.put("success", false);
                response.put("message", "Selected time is outside operating hours (" 
                    + restaurant.getOpeningTime() + " - " + restaurant.getClosingTime() + ").");
                return response;
            }
        }

        // Validate table belongs to restaurant and has enough capacity
        Optional<TableModel> tableOpt = tableDAO.findById(tableId);
        if (tableOpt.isEmpty() || !tableOpt.get().getRestaurantId().equals(restaurantId)) {
            response.put("success", false);
            response.put("message", "Selected table does not belong to this restaurant.");
            return response;
        }
        TableModel table = tableOpt.get();
        if (table.getCapacity() < guests) {
            response.put("success", false);
            response.put("message", "Selected table capacity (" + table.getCapacity() 
                + ") is insufficient for " + guests + " guests.");
            return response;
        }

        // SERVER-SIDE availability check — prevent double-booking
        boolean available = tableDAO.isTableAvailable(tableId, date, startTime, endTime);
        if (!available) {
            response.put("success", false);
            response.put("message", "This table has just been reserved. Please choose another table.");
            return response;
        }

        // Create and save the reservation
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setRestaurantId(restaurantId);
        reservation.setTableId(tableId);
        reservation.setReservationDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setGuests(guests);

        int reservationId = reservationDAO.save(reservation);

        // Fetch the saved reservation with all joined info for the confirmation page
        Optional<Reservation> savedOpt = reservationDAO.findById(reservationId);

        response.put("success", true);
        response.put("message", "Reservation confirmed!");
        response.put("reservationId", reservationId);
        if (savedOpt.isPresent()) {
            response.put("reservation", savedOpt.get());
        }
        return response;
    }

    /**
     * Get all reservations for a user (My Reservations page).
     */
    public List<Reservation> getUserReservations(int userId) {
        return reservationDAO.findByUser(userId);
    }

    /**
     * Get a single reservation by ID.
     */
    public Optional<Reservation> getReservationById(int reservationId) {
        return reservationDAO.findById(reservationId);
    }

    /**
     * Modify an existing reservation.
     * Enforces:
     * - Only owner can modify
     * - Only confirmed reservations can be modified
     * - Server-side 2-hour window check
     * - Operating hours check
     * - Table capacity check
     * - Excludes current reservation from availability overlap check
     */
    public Map<String, Object> modifyReservation(int reservationId, int userId,
                                                   String dateStr, String timeStr, int guests, int tableId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Reservation> existingOpt = reservationDAO.findById(reservationId);
        if (existingOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Reservation not found.");
            return response;
        }

        Reservation existing = existingOpt.get();

        // Security check: user can only modify their own reservations
        if (!existing.getUserId().equals(userId)) {
            response.put("success", false);
            response.put("message", "You are not authorized to modify this reservation.");
            return response;
        }

        if (!"CONFIRMED".equals(existing.getStatus())) {
            response.put("success", false);
            response.put("message", "Only confirmed reservations can be modified.");
            return response;
        }

        // Server-side 2-hour rule: cannot modify within 2 hours of existing reservation time
        LocalDateTime existingDateTime = LocalDateTime.of(existing.getReservationDate(), existing.getStartTime());
        if (existingDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            response.put("success", false);
            response.put("message", "Reservations can only be modified up to 2 hours before the scheduled time.");
            return response;
        }

        if (guests <= 0) {
            response.put("success", false);
            response.put("message", "Number of guests must be at least 1.");
            return response;
        }

        LocalDate newDate;
        LocalTime newStartTime;
        try {
            newDate = LocalDate.parse(dateStr);
            newStartTime = LocalTime.parse(timeStr);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Invalid date or time format.");
            return response;
        }
        LocalTime newEndTime = newStartTime.plusHours(2);

        if (newDate.isBefore(LocalDate.now())) {
            response.put("success", false);
            response.put("message", "Please select a valid future date.");
            return response;
        }
        if (newDate.isEqual(LocalDate.now()) && newStartTime.isBefore(LocalTime.now())) {
            response.put("success", false);
            response.put("message", "Please select a valid future time.");
            return response;
        }

        // Validate restaurant operating hours
        Optional<Restaurant> restOpt = restaurantDAO.findById(existing.getRestaurantId());
        if (restOpt.isPresent()) {
            Restaurant r = restOpt.get();
            if (r.getOpeningTime() != null && r.getClosingTime() != null) {
                if (newStartTime.isBefore(r.getOpeningTime()) || newEndTime.isAfter(r.getClosingTime())) {
                    response.put("success", false);
                    response.put("message", "Selected time is outside operating hours (" 
                        + r.getOpeningTime() + " - " + r.getClosingTime() + ").");
                    return response;
                }
            }
        }

        // Validate table belongs to the restaurant and capacity
        Optional<TableModel> tableOpt = tableDAO.findById(tableId);
        if (tableOpt.isEmpty() || !tableOpt.get().getRestaurantId().equals(existing.getRestaurantId())) {
            response.put("success", false);
            response.put("message", "Invalid table selected.");
            return response;
        }
        if (tableOpt.get().getCapacity() < guests) {
            response.put("success", false);
            response.put("message", "Selected table capacity (" + tableOpt.get().getCapacity() 
                + ") is insufficient for " + guests + " guests.");
            return response;
        }

        // Check availability, EXCLUDING the current reservation being modified
        boolean available = tableDAO.isTableAvailableExcluding(tableId, newDate, newStartTime, newEndTime, reservationId);
        if (!available) {
            response.put("success", false);
            response.put("message", "The selected table is not available for the new time. Please choose another.");
            return response;
        }

        existing.setReservationDate(newDate);
        existing.setStartTime(newStartTime);
        existing.setEndTime(newEndTime);
        existing.setGuests(guests);
        existing.setTableId(tableId);

        reservationDAO.update(existing);

        response.put("success", true);
        response.put("message", "Reservation updated successfully.");
        return response;
    }

    /**
     * Cancel a reservation.
     * Enforces:
     * - Only owner or admin can cancel
     * - Server-side 2-hour cancellation rule for customers (admin can cancel anytime)
     */
    public Map<String, Object> cancelReservation(int reservationId, int requestingUserId, boolean isAdmin) {
        Map<String, Object> response = new HashMap<>();

        Optional<Reservation> existingOpt = reservationDAO.findById(reservationId);
        if (existingOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Reservation not found.");
            return response;
        }

        Reservation existing = existingOpt.get();

        // Check authorization: user can only cancel their own, admin can cancel any
        if (!isAdmin && !existing.getUserId().equals(requestingUserId)) {
            response.put("success", false);
            response.put("message", "You are not authorized to cancel this reservation.");
            return response;
        }

        if ("CANCELLED".equals(existing.getStatus())) {
            response.put("success", false);
            response.put("message", "This reservation is already cancelled.");
            return response;
        }

        // Server-enforced 2-hour cancellation rule for non-admin users
        if (!isAdmin) {
            LocalDateTime reservationDateTime = LocalDateTime.of(existing.getReservationDate(), existing.getStartTime());
            if (reservationDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                response.put("success", false);
                response.put("message", "Reservations can only be cancelled up to 2 hours before the scheduled time.");
                return response;
            }
        }

        reservationDAO.cancel(reservationId);

        response.put("success", true);
        response.put("message", "Reservation cancelled successfully.");
        return response;
    }
}
