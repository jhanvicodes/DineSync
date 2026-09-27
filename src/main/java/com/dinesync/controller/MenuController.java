package com.dinesync.controller;

import com.dinesync.dao.UserDAO;
import com.dinesync.service.MenuService;
import com.dinesync.model.MenuItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * MenuController — REST API for menu item management (admin operations).
 *
 * Endpoints:
 *   POST   /api/admin/menu       — Add menu item
 *   PUT    /api/admin/menu/{id}  — Update menu item
 *   DELETE /api/admin/menu/{id}  — Delete menu item
 *   GET    /api/admin/menu       — Get all menu items
 */
@RestController
@RequestMapping("/api/admin/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    private final MenuService menuService;
    private final UserDAO userDAO;

    public MenuController(MenuService menuService, UserDAO userDAO) {
        this.menuService = menuService;
        this.userDAO = userDAO;
    }

    /** Verify admin from X-User-Id header */
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
        return null;
    }

    /**
     * GET /api/admin/menu
     */
    @GetMapping
    public ResponseEntity<Object> getAllMenuItems(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return ResponseEntity.status(403).body(authError.getBody());
        return ResponseEntity.ok(menuService.getAllMenuItems());
    }

    /**
     * POST /api/admin/menu
     * Body: { "restaurantId": 1, "name": "...", "description": "...", "category": "Main", "price": 500.00, "available": true }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addMenuItem(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody Map<String, Object> body) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        MenuItem item = buildMenuItemFromBody(null, body);
        return ResponseEntity.ok(menuService.addMenuItem(item));
    }

    /**
     * PUT /api/admin/menu/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMenuItem(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id, @RequestBody Map<String, Object> body) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        MenuItem item = buildMenuItemFromBody(id, body);
        return ResponseEntity.ok(menuService.updateMenuItem(item));
    }

    /**
     * DELETE /api/admin/menu/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenuItem(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable int id) {
        ResponseEntity<Map<String, Object>> authError = checkAdmin(userIdHeader);
        if (authError != null) return authError;
        return ResponseEntity.ok(menuService.deleteMenuItem(id));
    }

    // Helper method to parse request body into MenuItem object
    private MenuItem buildMenuItemFromBody(Integer menuId, Map<String, Object> body) {
        MenuItem item = new MenuItem();
        if (menuId != null) item.setMenuId(menuId);
        item.setRestaurantId(Integer.parseInt(body.get("restaurantId").toString()));
        item.setName((String) body.get("name"));
        item.setDescription((String) body.get("description"));
        item.setCategory((String) body.get("category"));
        item.setPrice(new BigDecimal(body.get("price").toString()));
        item.setImageUrl((String) body.get("imageUrl"));
        Object avail = body.get("available");
        item.setAvailable(avail == null || Boolean.parseBoolean(avail.toString()));
        return item;
    }
}
