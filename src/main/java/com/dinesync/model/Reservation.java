package com.dinesync.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Reservation model — represents a row in the 'reservations' table.
 * This is the core entity of the system — links a user, restaurant, and table
 * for a specific date and time window.
 */
public class Reservation {

    private Integer reservationId;
    private Integer userId;
    private Integer restaurantId;
    private Integer tableId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer guests;
    private String status;          // "CONFIRMED", "CANCELLED", "COMPLETED"
    private LocalDateTime createdAt;

    // Extra fields populated by JOIN queries (not stored in reservations table)
    private String userName;
    private String restaurantName;
    private String tableNumber;
    private Integer tableCapacity;
    private String tableLocation;

    // Default constructor
    public Reservation() {}

    // --- Getters and Setters ---

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Integer restaurantId) { this.restaurantId = restaurantId; }

    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }

    public LocalDate getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Joined fields
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public Integer getTableCapacity() { return tableCapacity; }
    public void setTableCapacity(Integer tableCapacity) { this.tableCapacity = tableCapacity; }

    public String getTableLocation() { return tableLocation; }
    public void setTableLocation(String tableLocation) { this.tableLocation = tableLocation; }
}
