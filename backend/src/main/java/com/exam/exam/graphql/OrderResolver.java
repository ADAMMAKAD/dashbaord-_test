package com.exam.exam.graphql;

import com.exam.exam.entity.Order;
import com.exam.exam.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class OrderResolver {

    @Autowired
    private OrderRepository orderRepository;

    @QueryMapping
    public List<Order> orders() {
        return orderRepository.findAll();
    }

    @QueryMapping
    public Order order(@Argument Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @MutationMapping
    public Order createOrder(@Argument Integer quantity) {
        Order newOrder = new Order();
        newOrder.setQuantity(quantity);
        return orderRepository.save(newOrder);
    }
}