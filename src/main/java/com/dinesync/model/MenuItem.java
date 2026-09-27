package com.dinesync.model;

import java.math.BigDecimal;

/**
 * MenuItem model — represents a row in the 'menu_items' table.
 * Each food or drink item offered by a restaurant.
 */
public class MenuItem {

    private Integer menuId;
    private Integer restaurantId;
    private String name;
    private String description;
    private String category;        // "Starter", "Main", "Dessert", "Drink"
    private BigDecimal price;
    private String imageUrl;
    private Boolean available;      // FALSE means temporarily unavailable

    // Default constructor
    public MenuItem() {}

    // --- Getters and Setters ---

    public Integer getMenuId() { return menuId; }
    public void setMenuId(Integer menuId) { this.menuId = menuId; }

    public Integer getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Integer restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
