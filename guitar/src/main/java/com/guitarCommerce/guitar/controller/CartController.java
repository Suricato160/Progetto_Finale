package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.service.CartService;
import com.guitarCommerce.guitar.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



// ======================================= ok


@Controller
@RequestMapping("/cart")
public class CartController {

    // dipendenze
    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

// =========================================================
// recupera i dettagli del carrello per l'utente autenticato
    @GetMapping
    public String getCart(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        return cartService.getCartDetails(userDetails, model);
    }

// =========================================================
// aggiunge un prodotto al carrello dell'utente autenticato
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable int productId, @RequestParam int quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        cartService.addToCart(userDetails, productId, quantity);
        return "redirect:/cart";
    }

// =========================================================
// rimuove un elemento dal carrello
    @GetMapping("/remove")
    public String removeFromCart(@RequestParam int productId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        cartService.removeFromCart(userDetails, productId);
        return "redirect:/cart";
    }

// =========================================================
// aggiorna la quantità di un prodotto nel carrello
    @PostMapping("/update")
    public String updateQuantity(@RequestParam int productId, @RequestParam int quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        cartService.updateQuantity(userDetails, productId, quantity);
        return "redirect:/cart";
    }

// =========================================================
// prende gli elementi nel carrelo e crea un ordine associato all'utente
    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        int userId = cartService.getUserIdFromUserDetails(userDetails); // Metodo privato reso pubblico o chiamato indirettamente
        orderService.createOrder(userId);
        return "redirect:/orders";
    }
}