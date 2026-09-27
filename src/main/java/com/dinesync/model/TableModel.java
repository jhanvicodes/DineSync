package com.dinesync.model;

import java.time.LocalDateTime;

/**
 * TableModel — represents a row in the 'restaurant_tables' table.
 * Note: named TableModel (not Table) because 'Table' conflicts with java.sql.Table.
 * Each physical table in a restaurant is one row here.
 */
public class TableModel {

    private Integer tableId;
    private Integer restaurantId;
    private String tableNumber;      // e.g. "T01", "T02"
    private Integer capacity;        // Max number of guests
    private String location;         // e.g. "Window", "Patio", "Private"
    private LocalDateTime createdAt;

    // This field is NOT in the database — it's set by the service layer
    // to indicate if this table is available for the requested time slot
    private boolean available = true;

    // Default constructor
    public TableModel() {}

    // --- Getters and Setters ---

    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }

    public Integer getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Integer restaurantId) { this.restaurantId = restaurantId; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
