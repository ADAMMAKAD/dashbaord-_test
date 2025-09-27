package com.exam.exam.controller;

import com.exam.exam.entity.Order;
import com.exam.exam.entity.Role;
import com.exam.exam.entity.User;
import com.exam.exam.repository.OrderRepository;
import com.exam.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin-only controller for administrative operations
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    /**
     * Get all users in the system
     * Admin-only endpoint
     */
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     * Admin-only endpoint
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> ResponseEntity.ok(user))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete user by ID
     * Admin-only endpoint
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get comprehensive system statistics
     * Admin-only endpoint
     */
    @GetMapping("/stats")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // User statistics
        long totalUsers = userRepository.count();
        long adminUsers = userRepository.findAll().stream()
            .mapToLong(user -> Role.ADMIN.equals(user.getRole()) ? 1 : 0)
            .sum();
        long regularUsers = totalUsers - adminUsers;
        
        // Order statistics
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        BigDecimal totalRevenue = allOrders.stream()
            .map(Order::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Build response
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("total", totalUsers);
        userStats.put("admins", adminUsers);
        userStats.put("users", regularUsers);
        
        Map<String, Object> orderStats = new HashMap<>();
        orderStats.put("total", totalOrders);
        orderStats.put("revenue", totalRevenue);
        
        stats.put("users", userStats);
        stats.put("orders", orderStats);
        
        return stats;
    }

    /**
     * Get all orders with full details
     * Admin-only endpoint
     */
    @GetMapping("/orders")
    public List<Order> getAllOrdersAdmin() {
        return orderRepository.findAll();
    }

    /**
     * Delete order by ID
     * Admin-only endpoint
     */
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}