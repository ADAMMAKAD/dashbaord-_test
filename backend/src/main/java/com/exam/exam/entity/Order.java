package com.exam.exam.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order entitiy for the exam dashbaord
 * Basic implemntation for order managment
 */
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // User who made the order
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    // Product being ordered
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    private Integer quantity;
    private BigDecimal total;
    private LocalDateTime orderDate;
    
    // Added status field for better order tracking
    private String status = "NEW";
    
    public Order() {
        this.orderDate = LocalDateTime.now();
    }
    
    // Constructor 
    public Order(User user, Product product, Integer quantity) {
        this();
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        // Calculate total = price * quantity (simple calculation)
        if (product != null && quantity != null) {
            this.total = product.getPrice().multiply(new BigDecimal(quantity));
        }
    }
    
    // Method to calculate total
    public void calculateTotal() {
        if (product != null && quantity != null) {
            this.total = product.getPrice().multiply(new BigDecimal(quantity));
        }
    }
    
    // Getters and setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public User getUser() { 
        return user; 
    }
    
    public void setUser(User user) { 
        this.user = user; 
    }
    
    public Product getProduct() { 
        return product; 
    }
    
    public void setProduct(Product product) { 
        this.product = product; 
    }
    
    public Integer getQuantity() { 
        return quantity; 
    }
    
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
    }
    
    public BigDecimal getTotal() { 
        return total; 
    }
    
    public void setTotal(BigDecimal total) { 
        this.total = total; 
    }
    
    public LocalDateTime getOrderDate() { 
        return orderDate; 
    }
    
    public void setOrderDate(LocalDateTime orderDate) { 
        this.orderDate = orderDate; 
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}