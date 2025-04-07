package com.guitarCommerce.guitar.service;

import com.guitarCommerce.guitar.entity.Cart;
import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// da commentare  -  rifattorializzato


@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // Rimosso @Autowired per OrderService

    // Recupera i dettagli del carrello e popola il model
    public String getCartDetails(UserDetails userDetails, Model model) {
        List<Cart> cartItems;
        int userId = 0;
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, BigDecimal> subtotals = new HashMap<>();

        if (userDetails == null) {
            cartItems = Collections.emptyList();
        } else {
            userId = getUserIdFromUserDetails(userDetails);
            cartItems = getCartItems(userId);
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

    @Transactional
    public void addToCart(UserDetails userDetails, int productId, int quantity) {
        int userId = getUserIdFromUserDetails(userDetails);
        addToCart(userId, productId, quantity);
    }

    @Transactional
    public void removeFromCart(UserDetails userDetails, int productId) {
        int userId = getUserIdFromUserDetails(userDetails);
        removeFromCart(userId, productId);
    }

    @Transactional
    public void updateQuantity(UserDetails userDetails, int productId, int quantity) {
        int userId = getUserIdFromUserDetails(userDetails);
        updateQuantity(userId, productId, quantity);
    }

    // Rimosso il metodo checkout da CartService

    @Transactional
    public void addToCart(int userId, int productId, int quantity) {
        Cart cartItem = new Cart();
        User user = userService.getUserById(userId);
        cartItem.setUser(user);
        Product product = productService.getProductById(productId);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setAddedAt(LocalDateTime.now());
        cartRepository.save(cartItem);
    }

    public List<Cart> getCartItems(int userId) {
        return cartRepository.findByUserId(userId);
    }

    @Transactional
    public void clearCart(int userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(cartItems);
    }

    @Transactional
    public void removeFromCart(int userId, int productId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        cartItems.stream()
                 .filter(item -> item.getProduct() != null && 
                                 item.getProduct().getId() == productId)
                 .forEach(cartRepository::delete);
    }

    @Transactional
    public void updateQuantity(int userId, int productId, int newQuantity) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        cartItems.stream()
                 .filter(item -> item.getProduct() != null && 
                                 item.getProduct().getId() == productId)
                 .findFirst()
                 .ifPresent(item -> {
                     item.setQuantity(newQuantity);
                     cartRepository.save(item);
                 });
    }

    public int getUserIdFromUserDetails(UserDetails userDetails) {
        String username = userDetails.getUsername();
        com.guitarCommerce.guitar.entity.User user = userService.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("Utente non trovato: " + username);
        }
        return user.getId();
    }
}