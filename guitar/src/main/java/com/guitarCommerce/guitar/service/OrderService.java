package com.guitarCommerce.guitar.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guitarCommerce.guitar.entity.Order;
import com.guitarCommerce.guitar.entity.OrderDetail;
import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDetailService orderDetailService;

    // Trova tutti gli ordini
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Trova un ordine per ID
    public Order getOrderById(Integer id) {
    Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Ordine non trovato"));
    Hibernate.initialize(order.getOrderDetails());
    order.getOrderDetails().forEach(detail -> Hibernate.initialize(detail.getProduct()));
    return order;
}

    // Trova ordini per utente
    public List<Order> getOrdersByUser(Integer userId) {
        return orderRepository.findByUserId(userId);
    }

    // Trova ordini per stato
    public List<Order> getOrdersByStatus(Order.Status status) {
        return orderRepository.findByStatus(status);
    }

    // Crea un nuovo ordine
    @Transactional
    public Order createOrder(Integer userId, List<OrderDetail> orderDetails) {
        User user = userService.getUserById(userId);
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now()); // Ora funziona con LocalDateTime
        order.setStatus(Order.Status.PENDING);

        // Calcola il totale sommando i dettagli dell'ordine
        BigDecimal totalAmount = orderDetails.stream()
                .map(detail -> detail.getProduct().getPrice() // Ora BigDecimal
                        .multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        orderDetails.forEach(detail -> {
            detail.setOrder(savedOrder);
            orderDetailService.createOrderDetail(detail);
        });

        return savedOrder;
    }


    // Aggiorna lo stato di un ordine
    @Transactional
    public Order updateOrderStatus(Integer id, Order.Status status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    // Elimina un ordine
    @Transactional
    public void deleteOrder(Integer id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }
}