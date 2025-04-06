package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.Cart;
import com.guitarCommerce.guitar.service.CartService;
import com.guitarCommerce.guitar.service.OrderService;
import com.guitarCommerce.guitar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // Recupera gli elementi del carrello e calcola il totale
    @GetMapping
    public String getCart(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<Cart> cartItems;
        int userId = 0;
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, BigDecimal> subtotals = new HashMap<>();

        if (userDetails == null) {
            // Utente non loggato: restituisci un carrello vuoto
            cartItems = Collections.emptyList();
        } else {
            // Utente loggato: recupera l'ID utente
            userId = getUserIdFromUserDetails(userDetails);
            cartItems = cartService.getCartItems(userId);

            // Calcola i subtotali e il totale
            for (Cart item : cartItems) {
                BigDecimal price = item.getProduct().getPrice();
                BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
                BigDecimal subtotal = price.multiply(quantity);
                subtotals.put(item.getId(), subtotal);
                total = total.add(subtotal);
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotals", subtotals);
        model.addAttribute("total", total);
        model.addAttribute("userId", userId);
        return "cart";
    }

    // Aggiunge un prodotto al carrello
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable int productId, @RequestParam int quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        int userId = getUserIdFromUserDetails(userDetails);
        cartService.addToCart(userId, productId, quantity);
        return "redirect:/cart";
    }

    // Rimuove un prodotto dal carrello
    @GetMapping("/remove")
    public String removeFromCart(@RequestParam int productId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        int userId = getUserIdFromUserDetails(userDetails);
        cartService.removeFromCart(userId, productId);
        return "redirect:/cart";
    }

    // Aggiorna la quantità di un prodotto nel carrello
    @PostMapping("/update")
    public String updateQuantity(@RequestParam int productId, @RequestParam int quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        int userId = getUserIdFromUserDetails(userDetails);
        cartService.updateQuantity(userId, productId, quantity);
        return "redirect:/cart";
    }

    // Conferma l'ordine e pulisce il carrello
    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        int userId = getUserIdFromUserDetails(userDetails);
        orderService.createOrder(userId);
        return "redirect:/orders";
    }

    // Metodo per ottenere l'ID utente da UserDetails
    private int getUserIdFromUserDetails(UserDetails userDetails) {
        String username = userDetails.getUsername();
        com.guitarCommerce.guitar.entity.User user = userService.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("Utente non trovato: " + username);
        }
        return user.getId();
    }
}