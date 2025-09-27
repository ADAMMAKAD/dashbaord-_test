package com.exam.exam.controller;

import com.exam.exam.entity.Order;
import com.exam.exam.entity.User;
import com.exam.exam.repository.OrderRepository;
import com.exam.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Order management controller
 * Handles basic CRUD operations for orders
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepo; // shorter name for convenience
    
    @Autowired
    private UserRepository userRepo;

    // Get all orders - Admins see all, Users see only their own
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            
            List<Order> orders;
            if (isAdmin) {
                orders = orderRepo.findAll();
            } else {
                // Regular users can only see their own orders
                Optional<User> user = userRepo.findByUname(username);
                if (user.isPresent()) {
                    orders = orderRepo.findByUser(user.get());
                } else {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "User not found");
                    return ResponseEntity.status(404).body(errorResponse);
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            System.err.println("Error fetching orders: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch orders");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Validate ID parameter - basic validation
            if (id == null || id <= 0) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid order ID");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Optional<Order> order = orderRepo.findById(id);
            
            if (order.isPresent()) {
                Order foundOrder = order.get();
                
                // Check if user has ADMIN role
                boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                
                // Admins can access any order, users can only access their own
                if (isAdmin || foundOrder.getUser().getUsername().equals(username)) {
                    return ResponseEntity.ok(foundOrder);
                } else {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Access denied");
                    return ResponseEntity.status(403).body(errorResponse);
                }
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Order not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
        } catch (Exception e) {
            System.err.println("Error fetching order: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch order");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Create new order - quick implementation
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get the authenticated user
            Optional<User> userOptional = userRepo.findByUname(username);
            if (userOptional.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            User authenticatedUser = userOptional.get();
            
            // Check if user has ADMIN role
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
            
            // Basic validation -  improve this
            if (order.getProduct() == null || order.getProduct().getName() == null || order.getProduct().getName().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Product name is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate price - quick check
            if (order.getProduct().getPrice() == null || order.getProduct().getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Valid price is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Set the user to the authenticated user (users can only create orders for themselves)
            // Admins can create orders for any user if specified, otherwise for themselves
            if (order.getUser() == null || (!isAdmin && !order.getUser().equals(authenticatedUser))) {
                order.setUser(authenticatedUser);
            }
            
            // Set default quantity if not provided - quick fix
            if (order.getQuantity() == null || order.getQuantity() <= 0) {
                order.setQuantity(1);
            }
            
            // Calculate total automatically - basic business logic
            order.calculateTotal();
            
            // TODO: Validate stock availability before creating order
            // TODO: Add email notification after order creation
            // TODO: Update product stock after order
            // TODO: Add order status tracking
            // TODO: Generate order number/reference
            // HACK: For now just save it
            
            Order savedOrder = orderRepo.save(order);
            System.out.println("Order created: " + savedOrder.getId() + " by " + username); // temp log
            return ResponseEntity.ok(savedOrder);
            
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create order");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            if (orderRepo.existsById(id)) {
                orderRepo.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Delete failed: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}