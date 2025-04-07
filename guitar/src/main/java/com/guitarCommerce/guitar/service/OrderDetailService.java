package com.guitarCommerce.guitar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guitarCommerce.guitar.entity.OrderDetail;
import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.repository.OrderDetailRepository;

import java.util.List;

// da commentare  -  rifattorializzato

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductService productService;

    // Trova tutti i dettagli degli ordini
    public List<OrderDetail> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    // Trova i dettagli di un ordine specifico
    public List<OrderDetail> getOrderDetailsByOrderId(Integer orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    // Trova i dettagli per un prodotto
    public List<OrderDetail> getOrderDetailsByProductId(Integer productId) {
        return orderDetailRepository.findByProductId(productId);
    }

    // Crea un nuovo dettaglio dell'ordine
    @Transactional
    public OrderDetail createOrderDetail(OrderDetail orderDetail) {
        Product product = productService.getProductById(orderDetail.getProduct().getId());
        orderDetail.setProduct(product);
        return orderDetailRepository.save(orderDetail);
    }

    // Aggiorna un dettaglio dell'ordine
    @Transactional
    public OrderDetail updateOrderDetail(Integer id, OrderDetail updatedDetail) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id: " + id));
        orderDetail.setQuantity(updatedDetail.getQuantity());
        return orderDetailRepository.save(orderDetail);
    }

    // Elimina un dettaglio dell'ordine
    @Transactional
    public void deleteOrderDetail(Integer id) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with id: " + id));
        orderDetailRepository.delete(orderDetail);
    }
}