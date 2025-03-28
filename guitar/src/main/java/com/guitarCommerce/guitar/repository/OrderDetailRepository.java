package com.guitarCommerce.guitar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.guitarCommerce.guitar.entity.OrderDetail;
import java.util.List;


@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    // QUERY per trovare un ordine specifico con l'id
    List<OrderDetail> findByOrderId(Integer orderId);

    // Query personalizzata per trovare i dettagli per prodotto
    List<OrderDetail> findByProductId(Integer productId);
    
} 
