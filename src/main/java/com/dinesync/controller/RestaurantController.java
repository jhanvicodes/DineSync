package com.dinesync.controller;

import com.dinesync.model.Restaurant;
import com.dinesync.service.MenuService;
import com.dinesync.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * RestaurantController — REST API for restaurant data.
 *
 * Endpoints:
 *   GET  /api/restaurants                     — List all / search restaurants
 *   GET  /api/restaurants/{id}                — Get single restaurant + reviews
 *   GET  /api/restaurants/{id}/menu           — Get restaurant menu
 *   GET  /api/restaurants/cuisines            — List all cuisines
 *   GET  /api/restaurants/locations           — List all locations
 *   POST /api/restaurants/{id}/reviews        — Post a review
 */
@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;

    public RestaurantController(RestaurantService restaurantService, MenuService menuService) {
        this.restaurantService = restaurantService;
        this.menuService = menuService;
    }

    /**
     * GET /api/restaurants?query=&cuisine=&location=&priceRange=
     * Returns all restaurants, with optional filtering.
     */
    @GetMapping
    public ResponseEntity<List<Restaurant>> getRestaurants(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String priceRange) {
        return ResponseEntity.ok(restaurantService.getRestaurants(query, cuisine, location, priceRange));
    }

    /**
     * GET /api/restaurants/cuisines
     * Returns all distinct cuisine types.
     */
    @GetMapping("/cuisines")
    public ResponseEntity<List<String>> getCuisines() {
        return ResponseEntity.ok(restaurantService.getCuisines());
    }

    /**
     * GET /api/restaurants/locations
     */
    @GetMapping("/locations")
    public ResponseEntity<List<String>> getLocations() {
        return ResponseEntity.ok(restaurantService.getLocations());
    }

    /**
     * GET /api/restaurants/{id}
     * Returns restaurant details + reviews.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRestaurant(@PathVariable int id) {
        Map<String, Object> result = restaurantService.getRestaurantDetails(id);
        if (Boolean.FALSE.equals(result.get("success"))) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/restaurants/{id}/menu
     * Returns all menu items for a restaurant.
     */
    @GetMapping("/{id}/menu")
    public ResponseEntity<Object> getMenu(@PathVariable int id) {
        return ResponseEntity.ok(menuService.getMenuByRestaurant(id));
    }

    /**
     * POST /api/restaurants/{id}/reviews
     * Body: { "userId": 1, "rating": 5, "comment": "..." }
     */
    @PostMapping("/{id}/reviews")
    public ResponseEntity<Map<String, Object>> postReview(
            @PathVariable int id, @RequestBody Map<String, Object> body) {
        if (body == null || body.get("userId") == null || body.get("rating") == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "User ID and rating are required."));
        }
        try {
            com.dinesync.model.Review review = new com.dinesync.model.Review();
            review.setRestaurantId(id);
            review.setUserId(Integer.parseInt(body.get("userId").toString()));
            review.setRating(Integer.parseInt(body.get("rating").toString()));
            review.setComment(body.get("comment") != null ? body.get("comment").toString().trim() : "");
            Map<String, Object> result = restaurantService.addReview(review);
            if (Boolean.FALSE.equals(result.get("success"))) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Invalid rating or user ID format."));
        }
    }
}
