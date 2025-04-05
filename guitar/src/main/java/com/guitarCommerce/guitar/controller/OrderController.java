package com.guitarCommerce.guitar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.guitarCommerce.guitar.entity.Order;
import com.guitarCommerce.guitar.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

   

    @GetMapping
    public String getAllOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders/OrderList";
    }

    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable Integer id, Model model) {
        try {
            Order order = orderService.getOrderById(id);
            model.addAttribute("order", order);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "orders/OrderDetail";
    }

    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable Integer id, @RequestParam("status") String status, Model model) {
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