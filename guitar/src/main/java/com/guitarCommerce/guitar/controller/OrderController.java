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

import com.guitarCommerce.guitar.service.OrderService;

// ======================================= ok


@Controller
@RequestMapping("/orders")
public class OrderController {

    // dipendenze
    @Autowired
    private OrderService orderService;

    // ===============================================
    // recupero gli ordini dall'utente autenticato
    @GetMapping 
    public String getOrders(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) { // se non c'è un utente autenticato
            return "redirect:/login"; // vai al login
        }
        return orderService.getOrdersForUser(userDetails, model);
    }

    // ===============================================
    // recupero il dettaglio ordine dell'utente autenticato
    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable Integer id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return orderService.getOrderDetails(id, userDetails, model);
    }

    // ===============================================
    // aggiorna lo stato di un ordine specifico per l'utente autenticato
    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable Integer id, @RequestParam("status") String status, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return orderService.updateOrderStatus(id, status, userDetails, model);
    }
}