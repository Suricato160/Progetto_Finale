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
import org.springframework.ui.Model;

import com.guitarCommerce.guitar.entity.Cart;
import com.guitarCommerce.guitar.entity.Order;
import com.guitarCommerce.guitar.entity.OrderDetail;
import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.repository.OrderRepository;


// da commentare  -  rifattorializzato


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

    // Restituisce gli ordini in base al ruolo dell'utente
    public String getOrdersForUser(UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username);

        List<Order> orders;
        if (user.getRole().equals("ADMIN")) {
            orders = getAllOrders();
        } else {
            orders = getOrdersByUser(user.getId());
        }

        model.addAttribute("orders", orders);
        return "orders/OrderList";
    }

    // Nuovo metodo per ottenere i dettagli di un ordine
    public String getOrderDetails(Integer id, UserDetails userDetails, Model model) {
        try {
            Order order = getOrderById(id);
            User user = userService.findByUsername(userDetails.getUsername());

            // Controlla che l'utente abbia il permesso di vedere l'ordine
            if (!user.getRole().equals("ADMIN") && order.getUser().getId() != user.getId()) {
                model.addAttribute("error", "Non hai il permesso di visualizzare questo ordine.");
                return "orders/OrderList";
            }

            model.addAttribute("order", order);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "orders/OrderDetail";
    }

    // Nuovo metodo per aggiornare lo stato di un ordine
    @Transactional
    public String updateOrderStatus(Integer id, String status, UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (!user.getRole().equals("ADMIN")) {
            model.addAttribute("error", "Non hai il permesso di aggiornare lo stato dell'ordine.");
            return "orders/OrderDetail";
        }

        try {
            // Converte la stringa ricevuta dal form nell'enum Status
            Order.Status newStatus = Order.Status.valueOf(status);
            Order updatedOrder = updateOrderStatus(id, newStatus);
            model.addAttribute("success", "Stato dell'ordine aggiornato con successo.");
            model.addAttribute("order", updatedOrder);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Stato non valido.");
            model.addAttribute("order", getOrderById(id));
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "orders/OrderDetail";
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