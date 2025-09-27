package com.exam.exam.config;

import com.exam.exam.entity.Order;
import com.exam.exam.entity.Product;
import com.exam.exam.entity.Role;
import com.exam.exam.entity.User;
import com.exam.exam.repository.OrderRepository;
import com.exam.exam.repository.ProductRepository;
import com.exam.exam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        // Initialize default users for the application
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole(Role.ADMIN); // Set admin role explicitly
            userRepository.save(adminUser);
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            User usr = new User(); 
            usr.setUsername("user");
            usr.setPassword(passwordEncoder.encode("user123")); //  password for demo
            usr.setRole(Role.USER); // Set user role explicitly
            userRepository.save(usr);
        }
        
        // Quick test user 
        if (userRepository.findByUsername("test").isEmpty()) {
            User testUser = new User();
            testUser.setUsername("test");
            testUser.setPassword(passwordEncoder.encode("123456")); // weak password for testing
            testUser.setRole(Role.USER); // Set user role explicitly
            userRepository.save(testUser);
        }

        if (productRepository.count() == 0) {
            Product prod1 = new Product();
            prod1.setName("phone");
            prod1.setDescription("phone");
            prod1.setPrice(new BigDecimal("1999.99"));
            productRepository.save(prod1);

            Product prod2 = new Product();
            prod2.setName("mango");
            prod2.setDescription("mango");
            prod2.setPrice(new BigDecimal("19.99"));
            productRepository.save(prod2);
        }

        if (orderRepository.count() == 0) {
            User adminUser = userRepository.findByUsername("admin").orElse(null);
        User regularUser = userRepository.findByUsername("user").orElse(null);
            
            if (adminUser != null && regularUser != null) {
                Product phone = productRepository.findAll().stream()
                    .filter(p -> "phone".equals(p.getName()))
                    .findFirst().orElse(null);
                Product mango = productRepository.findAll().stream()
                    .filter(p -> "mango".equals(p.getName()))
                    .findFirst().orElse(null);

                if (phone != null && mango != null) {
                    Order order1 = new Order();
                    order1.setUser(adminUser);
                    order1.setProduct(phone);
                    order1.setQuantity(1);
                    order1.setTotal(phone.getPrice());
                    orderRepository.save(order1);

                    // Order 2
                    Order order2 = new Order();
                    order2.setUser(regularUser);
                    order2.setProduct(mango);
                    order2.setQuantity(2);
                    order2.setTotal(mango.getPrice().multiply(new BigDecimal("2")));
                    orderRepository.save(order2);

                    // Order 3
                    Order order3 = new Order();
                    order3.setUser(adminUser);
                    order3.setProduct(mango);
                    order3.setQuantity(1);
                    order3.setTotal(mango.getPrice());
                    orderRepository.save(order3);
                }
            }
        }
    }
}