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

// ======================================= ok
// pulire dopo controllo

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

    // -----
    public int getUserIdFromUserDetails(UserDetails userDetails) {
        String username = userDetails.getUsername();
        com.guitarCommerce.guitar.entity.User user = userService.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("Utente non trovato: " + username);
        }
        return user.getId();
    }

    // -----
    // recupero il carrello dall'utente
    public List<Cart> getCartItems(int userId) {
        return cartRepository.findByUserId(userId);
    }

    // recupero id del loggato per poi passare il tutto al add carrello
    @Transactional
    public void addToCart(UserDetails userDetails, int productId, int quantity) {
        int userId = getUserIdFromUserDetails(userDetails);
        User user = userService.getUserById(userId);
        Product product = productService.getProductById(productId);

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Stock insufficiente per il prodotto: " + product.getName());
        }

        Cart existingItem = cartRepository.findByUserIdAndProductId(userId, productId);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setAddedAt(LocalDateTime.now());
            cartRepository.save(existingItem);
        } else {
            Cart newItem = new Cart();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setAddedAt(LocalDateTime.now());
            cartRepository.save(newItem);
        }

        // Aggiorna stock
        product.setStock(product.getStock() - quantity);
        productService.updateProduct(product);
    }

    // rimuovo dal carrello
    @Transactional
    public void removeFromCart(UserDetails userDetails, int productId) {
        int userId = getUserIdFromUserDetails(userDetails);
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        for (Cart item : cartItems) {
            if (item.getProduct() != null && item.getProduct().getId() == productId) {
                // Ripristina stock
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productService.updateProduct(product);

                cartRepository.delete(item);
                break;
            }
        }
    }

    // cancello tutto il carrello
    @Transactional
    public void clearCart(int userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(cartItems);
    }

    // ===============================================

    // aggiorno la quantità del prodotto nel carrello
    @Transactional
    public void updateQuantity(UserDetails userDetails, int productId, int newQuantity) {
        int userId = getUserIdFromUserDetails(userDetails);
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        for (Cart item : cartItems) {
            if (item.getProduct() != null && item.getProduct().getId() == productId) {
                int oldQuantity = item.getQuantity();
                int diff = newQuantity - oldQuantity;

                Product product = item.getProduct();
                if (product.getStock() < diff) {
                    throw new IllegalArgumentException("Stock insufficiente per aggiornare la quantità.");
                }

                item.setQuantity(newQuantity);
                cartRepository.save(item);

                product.setStock(product.getStock() - diff);
                productService.updateProduct(product);
                break;
            }
        }
    }

}