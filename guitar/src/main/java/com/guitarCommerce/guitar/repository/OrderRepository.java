package com.guitarCommerce.guitar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.guitarCommerce.guitar.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    //QUERY per trovare ordini di un utente
    List<Order> findByUserId(Integer userId);

    //Query per trovare ordini per stato (pedding, ecc.)
    List<Order> findByStatus(Order.Status status);
    
}
