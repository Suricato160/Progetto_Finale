package com.guitarCommerce.guitar.service;

import com.guitarCommerce.guitar.entity.Cart;
import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

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
}