package com.guitarCommerce.guitar.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guitarCommerce.guitar.entity.Cart;
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

    @Autowired
    private CartService cartService;

    // Trova tutti gli ordini
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Trova un ordine per ID
    public Order getOrderById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato"));
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

    // Nuovo metodo: restituisce gli ordini in base al ruolo dell'utente
    public List<Order> getOrdersForUser(UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username);

        // Se l'utente è ADMIN, restituisci tutti gli ordini
        if (user.getRole().equals("ADMIN")) {
            return getAllOrders();
        } else {
            // Se l'utente è USER, restituisci solo i suoi ordini
            return getOrdersByUser(user.getId());
        }
    }

    // Crea un nuovo ordine dal carrello
    @Transactional
    public Order createOrder(Integer userId) {
        User user = userService.getUserById(userId);
        List<Cart> cartItems = cartService.getCartItems(userId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Il carrello è vuoto");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);

        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cartItem : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(cartItem.getProduct());
            detail.setQuantity(cartItem.getQuantity());
            BigDecimal subtotal = cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            orderDetails.add(detail);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        orderDetails.forEach(detail -> {
            detail.setOrder(savedOrder);
            orderDetailService.createOrderDetail(detail);
        });

        // Pulisci il carrello dopo l'ordine
        cartService.clearCart(userId);

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