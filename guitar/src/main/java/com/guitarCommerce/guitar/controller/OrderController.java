package com.guitarCommerce.guitar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.guitarCommerce.guitar.entity.Order;
import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.service.OrderService;
import com.guitarCommerce.guitar.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String getOrders(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersForUser(userDetails);
        model.addAttribute("orders", orders);
        return "orders/OrderList";
    }

    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable Integer id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.getOrderById(id);
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

    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable Integer id, @RequestParam("status") String status, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (!user.getRole().equals("ADMIN")) {
            model.addAttribute("error", "Non hai il permesso di aggiornare lo stato dell'ordine.");
            return "orders/OrderDetail";
        }

        try {
            // Converte la stringa ricevuta dal form nell'enum Status
            Order.Status newStatus = Order.Status.valueOf(status);
            Order updatedOrder = orderService.updateOrderStatus(id, newStatus);
            model.addAttribute("success", "Stato dell'ordine aggiornato con successo.");
            model.addAttribute("order", updatedOrder);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Stato non valido.");
            model.addAttribute("order", orderService.getOrderById(id));
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "orders/OrderDetail";
    }
}