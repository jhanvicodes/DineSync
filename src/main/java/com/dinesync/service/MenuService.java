package com.dinesync.service;

import com.dinesync.dao.MenuDAO;
import com.dinesync.model.MenuItem;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MenuService — business logic for menu item operations.
 */
@Service
public class MenuService {

    private final MenuDAO menuDAO;

    public MenuService(MenuDAO menuDAO) {
        this.menuDAO = menuDAO;
    }

    /**
     * Get all menu items for a restaurant.
     */
    public List<MenuItem> getMenuByRestaurant(int restaurantId) {
        return menuDAO.findByRestaurant(restaurantId);
    }

    /**
     * Get all menu items (admin use).
     */
    public List<MenuItem> getAllMenuItems() {
        return menuDAO.findAll();
    }

    /**
     * Add a new menu item (admin only).
     */
    public Map<String, Object> addMenuItem(MenuItem item) {
        Map<String, Object> response = new HashMap<>();
        try {
            int id = menuDAO.save(item);
            response.put("success", true);
            response.put("message", "Menu item added successfully.");
            response.put("menuId", id);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add menu item: " + e.getMessage());
        }
        return response;
    }

    /**
     * Update a menu item (admin only).
     */
    public Map<String, Object> updateMenuItem(MenuItem item) {
        Map<String, Object> response = new HashMap<>();
        Optional<MenuItem> existing = menuDAO.findById(item.getMenuId());
        if (existing.isEmpty()) {
            response.put("success", false);
            response.put("message", "Menu item not found.");
            return response;
        }
        menuDAO.update(item);
        response.put("success", true);
        response.put("message", "Menu item updated successfully.");
        return response;
    }

    /**
     * Delete a menu item (admin only).
     */
    public Map<String, Object> deleteMenuItem(int menuId) {
        Map<String, Object> response = new HashMap<>();
        menuDAO.delete(menuId);
        response.put("success", true);
        response.put("message", "Menu item deleted successfully.");
        return response;
    }
}
