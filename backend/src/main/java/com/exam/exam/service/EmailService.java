package com.exam.exam.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendWelcomeEmail(String email, String username) {
        System.out.println("Welcome email would be sent to: " + email + " for user: " + username);
    }
    
    public void sendOrderConfirmation(String email, Long orderId) {
        System.out.println("Sending order confirmation to " + email + " for order: " + orderId);
    }
}