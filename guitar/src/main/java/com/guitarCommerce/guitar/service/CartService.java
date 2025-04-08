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
import java.util.List;

// da commentare  -  rifattorializzato


@Service
public class CartService {

    // dependency 
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;


    // ===================================================================


    // Recupera i dettagli del carrello e popola il model
    public String getCartDetails(UserDetails userDetails, Model model) {
        // sarà la lista nel carrello
        List<Cart> cartItems;
        // sarà id utente
        int userId = 0;

        // totale carrello
        BigDecimal total = BigDecimal.ZERO;
    
        // se non c'è un user viene restituito il carrello vuoto
        if (userDetails == null) {
            cartItems = Collections.emptyList();
        } else {
            // recupero l'id del loggato
            userId = getUserIdFromUserDetails(userDetails);
            // recupero i prodotti salvati
            cartItems = getCartItems(userId);
            // Calcola il subtotale per ogni item
            for (Cart item : cartItems) {
                item.calculateSubtotal();
                // sommo tutto
                total = total.add(item.getSubtotal());
            }
        }
        // restituisco tutto
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("userId", userId);
        return "cart";
    }

    // aggiungo al carrello
    @Transactional
    public void addToCart(UserDetails userDetails, int productId, int quantity) {
        int userId = getUserIdFromUserDetails(userDetails);
        addToCart(userId, productId, quantity);
    }

    // rimuovo dal carrello
    @Transactional
    public void removeFromCart(UserDetails userDetails, int productId) {
        int userId = getUserIdFromUserDetails(userDetails);
        removeFromCart(userId, productId);
    }

    // aggiorno la quantità del prodotto nel carrello
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