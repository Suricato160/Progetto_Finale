package com.guitarCommerce.guitar.repository;

import com.guitarCommerce.guitar.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    List<Cart> findByUserId(int userId);

    Cart findByUserIdAndProductId(int userId, int productId);
}